package com.jack.server.interfaces;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public interface Response {
    //也需要COnnector
    public Connector getConnector();
    public void setConnector(Connector connector);

    //也需要COntext
    public Context getContext();
    public void setContext(Context context);

    //需要request
    public Request getRequest();
    public void setRequest(Request request);

    public ServletResponse getResponse();

    //获取输出
    public OutputStream getStream();
    public void setStream(OutputStream stream);

    //设置返回头信息
    public void setError();
    public boolean isError();

    public ServletOutputStream createOutputStream() throws IOException;

    public void finishResponse() throws IOException;


    //设置请求头基本内容
    //先获取请求头中的基本信息
    public int getContentLength();
    public int getContentCount();
    public String getContentType(String type);
    public PrintWriter getReporter();

    public void recycle();
    public void resetBuffer();
    public void sendAcknowledgement() throws IOException;

    public String getInfo();
}
