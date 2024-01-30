package com.jack.server.https;

import com.jack.server.StaticResourceProcessor;
import com.jack.server.facades.HttpResponseFacade;
import com.jack.server.https.requests.HttpRequestImpl;
import com.jack.server.https.responses.HttpResponseImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class JeanHttpServer {
    public static final String WEB_ROOT  = System.getProperty("user.dir") + File.separator + "webroot";
    public static final String JAVA_ROOT  = System.getProperty("user.dir") + File.separator
            + "src"+ File.separator + "main"+ File.separator + "java";
    public static final String TARGET_ROOT  = System.getProperty("user.dir") + File.separator
            + "target"+ File.separator + "classes";
    private Object input;

    public static void main(String[] args) {
        JeanHttpServer server = new JeanHttpServer();
        System.out.println("JeanTomcat is On.");
        server.await();
    }

    private void await() {
        ServerSocket serverSocket = null;

        int port = 9090;

        try{
            serverSocket = new ServerSocket(port,1, InetAddress.getByName("127.0.0.1"));

        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        while(true){
            Socket socket = null;
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try{
                socket = serverSocket.accept();
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();

                //解析 Request
                HttpRequestImpl request = new HttpRequestImpl(inputStream);
                request.parse(socket);

                //构建 Response
                HttpResponseImpl response2 = new HttpResponseImpl(outputStream);
                HttpResponseFacade response = new HttpResponseFacade(response2);

                //处理数据
                if(request.getUri().startsWith("/servlet/")){
                    //ServletProcessor processor = new ServletProcessor();
                    //processor.process(request,response2);
                }else{
                    StaticResourceProcessor processor = new StaticResourceProcessor();
                    processor.process(request,response2);
                }
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
