package test;

import com.jack.server.JeanServlet;
import com.jack.server.Request;
import com.jack.server.Response;

import java.io.IOException;

public class JeanHelloServlet implements JeanServlet {

    @Override
    public void service(Request req, Response res) throws IOException {
        String doc = "<!DOCTYPE html> \n" +                "<html>\n"
                +                "<head><meta charset=\"utf-8\"><title>Test</title></head>\n"
                +                "<body bgcolor=\"#f0f0f0\">\n"
                +                "<h1 align=\"center\">"
                + "Hello World 你好"
                + "</h1>\n";
        res.getoutput().write(doc.getBytes("utf-8"));
    }
}
