package com.jack.server.https;

import com.jack.server.ServletProcessor;
import com.jack.server.StaticResourceProcessor;
import com.jack.server.https.requests.HttpRequestImpl;
import com.jack.server.https.responses.HttpResponseImpl;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class HttpProcessor implements Runnable {
    //拆分：process 方法具体实现和原本并没有差异，只是新增 Socket 参数传入。
    // 现在有了这个专门的机构来分工，调用 Servlet 或者是静态资源。
    Socket socket = null;
    boolean available = false;
    HttpConnector connector;
    //一个小变动在于新增了 serverPort、keepAlive 与 http11 三个域，而且为了更好的安全性，都用 private 关键字修饰
    //处理可持续连接
    int serverPort = 0;
    private boolean keepAlive = false;
    private boolean http11 = true;


    public HttpProcessor(HttpConnector connector){
        this.connector = connector;
    }


    public void process(Socket socket){
        //新增线程等待处理逻辑,可以不需要了
//        try{
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        InputStream inputStream = null;
        OutputStream outputStream = null;


        try{
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            //增加 Keep-alive处理
            keepAlive = true;
            //分块 持续处理，这里没有实现分块
            while(keepAlive){
                //create Request obj and parse
                HttpRequestImpl request = new HttpRequestImpl(inputStream);
                request.parse(socket);

                //在HttpConnector 接收Socket之后，为Processor分配Socket，
                //所以 现在HttpProcessor中处理Session

                //这个方法内会判断有没有 Session，如果没有就创建。
                // 目前还存在一个问题，
                // 当 Request 请求每次都是新创建的，那么 Session 一定是空的，
                // 所以在 getSession 方法内我们会进一步判断在 URL 中是否存在 jsessionid，
                // 如果解析后的结果中有 jsessionid，
                // 我们会用这个 jsessionid 从 HttpConnector 类的全局 Map 里查找相应的 Session
                if(request.getSessionId() ==null || ("").equals(request.getSessionId())){
                    request.getSession(true);
                }

                HttpResponseImpl response = new HttpResponseImpl(outputStream);
                response.setRequest(request);

                //response.sendStaticResource();
                request.setResponse(response);

                try{
                    response.sendHeaders();
                }catch(IOException e){
                    e.printStackTrace();
                }

                //check request rule
                if(request.getUri().startsWith("/servlet")){
                    ServletProcessor servletProcessor = new ServletProcessor(this.connector);
                    servletProcessor.process(request,response);
                }else{
                    StaticResourceProcessor processor = new StaticResourceProcessor();
                    processor.process(request,response);
                }

                //处理
                //这里修改了时序，这里就不处理 header
                finishResponse(response);
                System.out.println("response header connection---" + response.getHeader("Connection"));
                //这是http 协议约定的消息头
                //同事请求段也要处理这个，发送过来
//                if("close".equals(response.getHeader("Connection"))){
//                    keepAlive = false;
//                }
                //直接关闭
                keepAlive = false;
            }

            socket.close();
            socket = null;
        } catch (IOException | ServletException e) {
            e.printStackTrace();
        }
    }

    private void finishResponse(HttpResponseImpl response) {
        response.finishResponse();
    }

    @Override
    public void run() {
        //这里 对应 Connector ，当COnnector的标识为false，
        //这里Processor就等待，知道Connector把标识改为 true,Procesasor,就跳出循环，获取SOcket，
        //然后通知其他 Processor，这个Socket它处理了
        //这就是 线程互锁机制，保证同步协调。
        while(true){
            //等待线程
            Socket socket = await();
            if(socket == null){
                continue;
            }
            process(socket);

            //回收processors
            connector.recycle(this);
        }
    }

    synchronized void assign(Socket socket){
        //等待 COnnecotor 的新 Socket
        while(available){
            try{
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //获取到新的Socket，通知其他线程
        this.socket = socket;
        available = true;
        notifyAll();
    }
    //这个方法 是Connector调用
    private synchronized Socket await() {
        //等待 Connector 提供新的Socket
        while(!available){
            try{
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //通知 COnnector
        //assign 是通知Processor 其他线程
        Socket socket = this.socket;
        available = true;
        notifyAll();
        return socket;
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }
}
