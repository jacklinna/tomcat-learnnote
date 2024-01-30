package com.jack.server.impl;

import com.jack.server.HttpServer;
import com.jack.server.Request;
import com.jack.server.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class String implements Servlet {
    @Override
    public void service(Request req, Response res) throws IOException {
        OutputStream outputStream = null;

        outputStream = res.getoutput();
        outputStream.write("hello String!".getBytes());
    }
}
