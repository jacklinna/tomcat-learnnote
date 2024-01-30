package com.jack.server;

import com.jack.server.https.HttpConnector;
import com.jack.server.interfaces.Request;
import org.apache.commons.lang3.text.StrSubstitutor;

import com.jack.server.interfaces.Request;
import com.jack.server.interfaces.Response;

import javax.servlet.ServletException;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ServletProcessor {
    //此处更新有三点：
    //使用 PrintWriter 接口替换了原来的 OutputStream。
    // 在加载 Servlet 之前设置 characterEncoding 为 UTF-8，再获取 Writer。
    // Writer 中设置了 autoflush，因此不再需要像原来一样手动设置 output.flush。
//    private static String OKMessage =
//            "HTTP/1.1 ${statusCode} ${statusName}\r\n"
//            +"Content-Type:${Content-Type}\r\n"
//            +"Server:minit\r\n"
//            +"Date:${ZonedDateTime}\r\n"
//            +"\r\n";

    private HttpConnector connector;

    public ServletProcessor(HttpConnector connector) {
        this.connector = connector;
    }

    //如今新的process，不再需要Socket接收
    public void process(Request request, Response response) throws ServletException, IOException {
        System.out.println("ServletProcessor process");
        this.connector.getContainer().invoke(request,response);
    }

}
