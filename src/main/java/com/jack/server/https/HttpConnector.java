package com.jack.server.https;

import com.jack.server.core.StandardContext;
import com.jack.server.core.StandardHost;
import com.jack.server.interfaces.Logger;
import com.jack.server.sessions.StandardSession;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class HttpConnector implements Runnable{
//它实现了 Runnable 接口，把它看作一个线程，支持并发处理，提高整个服务器的吞吐量。
// 而 Socket 的关闭，最后也统一交给 Connector 处理。

    //更新2：对Processor进行多线程处理
    //第一步实现不是多线程，而只是 多路复用
    //，到现在为止，HttpProcessor 并没有做到多线程，也没有实现 NIO，只是在池中放置了多个对象，做到了多路复用。
    // 目前整体架构还是阻塞式运行的，Socket 每次用之后也关闭丢弃了。
    //第二步，就是 使用多个线程，然后COnnector，Processor分别是不同线程来运行

    //工作流程，就是
    int  minProcessors = 3;
    int  maxProcessors = 10;
    int curProcessors = 0;

    //端口
    int port = 9080;
    //B
    Boolean available = true;
    Socket socket = null;

    Deque<HttpProcessor> processors = new ArrayDeque<>();

    //这里也需要加入session
    public static Map<String,HttpSession> sessions = new ConcurrentHashMap<>();


    //将类加载转移到Connector
    public static URLClassLoader loader = null;
    public static StandardSession createSession() {
        StandardSession session = new StandardSession();
        session.setValid(true);
        session.setCreationTime(System.currentTimeMillis());

        String sessionId = generateSessionId();
        session.setId(sessionId);
        sessions.put(sessionId,session);

        return session;
    }

    //如今改造，是需要将新加的Container融合进
    //与connector 相关联的 container
    StandardHost container = null;
    //保存线程名称
    private String threadName = null;


    //就是随机生成byte，而后转为字符串即可
    private static synchronized String generateSessionId() {
        Random random = new Random();

        long seed = System.currentTimeMillis();

        random.setSeed(seed);

        byte bytes[] = new byte[16];

        random.nextBytes(bytes);

        StringBuffer result = new StringBuffer();

        for(int i=0;i<bytes.length;i++){
            byte b1 = (byte) ((bytes[i] & 0xf0) >> 4);
            byte b2 = (byte) (bytes[i] & 0xf0);

            if(b1<10){
                result.append((char) ('0'+b1));
            }else{
                result.append((char) ('A'+b1-10));
            }
            if(b2<10){
                result.append((char) ('0'+b2));
            }else{
                result.append((char) ('A'+b2-10));
            }
        }
        return result.toString();
     }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try{
            serverSocket = new ServerSocket(port,1, InetAddress.getByName("127.0.0.1"));

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }


        // 初始化 处理器Processors对象，放入池
        for(int i=0;i<minProcessors;i++){
            HttpProcessor httpProcessor = new HttpProcessor(this);
            //这里需要 对多线程 进行改造，就是 线程互锁，去调用 Processor内的方法
            httpProcessor.start();
            processors.push(httpProcessor);
        }
        curProcessors = minProcessors;
        //获取到 服务器后
        while(true){
            Socket socket = null;
            try{
                socket = serverSocket.accept();

                //tips:引入池，把对象初始化好之后，需要用的时候再拿出来使用，不需要使用的时候就再放回池里，不用再构造新的对象。
                HttpProcessor processor = createProcessor();
                if(processor == null){
                    socket.close();
                    continue;
                }
                //这里不再是直接使用Processor处理事情，而是要同步processor
                processor.assign(socket);
                //使用完processor，就放回池
                //processors.push(processor);
                //处理完，关闭 socket

                //socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    // 从池中获取一个processors
    // 如果数量不足，新建一个
    private HttpProcessor createProcessor() {
        synchronized (processors){
            if(processors.size() >0){
                return processors.pop();
            }

            if(curProcessors < maxProcessors){
                return newProcessor();
            }else{
                return null;
            }
        }
    }
    //这里是 新建Processor对象
//    private HttpProcessor newProcessor() {
//        //这里需要 对多线程 进行改造，就是 线程互锁，去调用 Processor内的方法
//        //HttpProcessor initProcessor = new HttpProcessor();
//        HttpProcessor initProcessor = new HttpProcessor(this);
//        initProcessor.start();
//        processors.push(initProcessor);
//        curProcessors++;
//        return processors.pop();
//    }
    //书写回收方法，就是放回池
    void recycle(HttpProcessor processor){
        processors.push(processor);
    }

    // 与Processor 线程对应
    // 将一个Socket交给 Processor
    //基本逻辑就是 标志位为true,COnnector就等待，直到某个线程设置为false，
    // Connector 就不再等待，把接收到的Socket 交给 Processor,然后重新把标识归为 true
    //Connector 通知其他线程，这个Processor 我处理了
    synchronized void assign(Socket socket){
        while(available){
            try{
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //收到一个新的 Socket
        this.socket = socket;
        available = true;
        //这就是同步的一个机制
        //通知其他线程
        notifyAll();
    }

    private synchronized Socket await(){
        //等待 Connector 提供一个 客户端请求的 Sockct
        while(!available){
            try{
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //获取这个新的Socket

        Socket socket = this.socket;
        available = false;
        notifyAll();
        return socket;
    }

    public void start(){
        threadName = "HttpConnector["+port+"]";
        log("httpConnector.Starting "+threadName);
        Thread thread = new Thread(this);
        thread.start();
    }

    //创建新的处理器 Processor
    private HttpProcessor newProcessor(){
        HttpProcessor initProcessor = new HttpProcessor(this);
        initProcessor.start();
        processors.push(initProcessor);
        curProcessors++;
        log("newProcessor");
        return ((HttpProcessor) processors.pop());
    }

    private void log(String msg) {
        //自己容器的Logger
        Logger logger = container.getLogger();
        String localName =  threadName;
        if(localName ==null){
            localName ="HttpConnector";
        }
        if(logger != null){
            logger.log(localName +" "+msg);
        }else{
            //没有logger 就 System
            System.out.println(localName +" "+msg);
        }
    }
    private void log(String msg,Throwable throwable) {
        //自己容器的Logger
        Logger logger = container.getLogger();
        String localName =  threadName;
        if(localName ==null){
            localName ="HttpConnector";
        }
        if(logger != null){
            logger.log(localName +" "+msg,throwable);
        }else{
            //没有logger 就 System
            System.out.println(localName +" "+msg);
            throwable.printStackTrace(System.out);
        }
    }
    public StandardHost getContainer() {
        return container;
    }

    public void setContainer(StandardHost container) {
        this.container = container;
    }
}
