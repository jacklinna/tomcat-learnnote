package com.jack.server.impl;

import com.jack.server.JeanServlet;
import com.jack.server.Request;
import com.jack.server.Response;

import java.io.IOException;
import java.io.OutputStream;

public class StringJean implements JeanServlet {
    @Override
    public void service(Request req, Response res) throws IOException {
        OutputStream outputStream = null;

        outputStream = res.getoutput();
        outputStream.write("hello String!".getBytes());
    }
}
