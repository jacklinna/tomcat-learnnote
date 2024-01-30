package com.jack.server;

import java.io.IOException;


//这个是自定义的 Servlet 接口，
// 为了符合规范，就需要使用 java.servlet.Servlet
public interface JeanServlet {
    public void service(Request req, Response res) throws IOException;
}
