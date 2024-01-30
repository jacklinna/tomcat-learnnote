package com.jack.server;

import java.io.*;

public class Responses {
    private static final int BUFFER_SIZE = 1024;
    Request request;
    OutputStream outputStream;

    public Responses(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void setResponse(Request request) {
        this.request = request;
    }

    public void sendStaticResource() throws IOException {
        byte[] bytes = new byte[BUFFER_SIZE];

        FileInputStream fis = null;

        try{
            File file = new File(HttpServer.WEB_ROOT,request.getUri());

            if(file.exists()){
                fis = new FileInputStream(file);
                int ch = fis.read(bytes,0,BUFFER_SIZE);
                while(ch != -1){
                    outputStream.write(bytes,0,ch);
                    ch = fis.read(bytes,0,BUFFER_SIZE);
                }
                outputStream.flush();
            }else{
                String errorMessage = "HTTP/1.1 404 File Not Found\r\n"
                        + "Content-Type:text/html\r\n"
                        +"Content-Length:23\r\n"
                        +"\r\n"
                        +"<h1>File not Found By MiniServer</h1>";
                outputStream.write(errorMessage.getBytes());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e){
            System.out.println(e.toString());
        }finally {
            if(fis !=null){
                fis.close();
            }
        }
    }
}
