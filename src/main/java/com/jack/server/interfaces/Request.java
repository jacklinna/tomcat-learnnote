package com.jack.server.interfaces;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public interface Request {
    //需要连接器
    public Connector getConnector();
    public void setConnector(Connector connector);

    //需要应用容器Context,管理着Wrapper
    public Context getContext();
    public void setContext(Context context);

    //获取具体的request实现
    public ServletRequest getRequest();
    public Response getResponse();
    public void setResponse(Response reponse);

    //获取Socket
    public Socket getSocket();
    public void setSocket(Socket socket);

    //获取输入流
    public InputStream getSteam();
    public void setStream(InputStream stream);

    //获取下一级的Wrapper容器
    public Wrapper getWrapper();
    public void setWrapper();

    public ServletInputStream createInputStream() throws IOException;

    public void finishRequest() throws IOException;

    public void recycle();
    //设置请求头基本内容
    public void setContentLength(int length);
    public void setContentType(String type);
    public void setProtocol(String protocol);
    public void setRemoteAddr(String remote);
    public void setScheme(String scheme);
    public void setServerPort(int port);

    public String getInfo();
}
