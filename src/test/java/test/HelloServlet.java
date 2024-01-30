package test;

import com.jack.server.JeanServlet;
import com.jack.server.Request;
import com.jack.server.Response;

import javax.servlet.*;
import java.io.IOException;

public class HelloServlet implements Servlet {
//目前实现的是  javax.servlet.Servlet，新增 characterEncoding 设置，
// 最后用 Writer 输出自定义的 HTML 文本。
    @Override
    public void service(ServletRequest req, ServletResponse res) throws IOException {
        res.setCharacterEncoding("UTF-8");
        String doc = "<!DOCTYPE html> \n"
                +"<html>\n"
                + "<head><meta charset=\"utf-8\"><title>Test</title></head>\n"
                + "<body bgcolor=\"#f0f0f0\">\n"
                + "<h1 align=\"center\">"
                + "Hello World 你好"
                + "</h1>\n";
        //应该没有输出到 output
        //res.getWriter().write(doc);
        //直接打印出 所有字符，没有解析
        res.getWriter().println(doc);
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {

    }
}
