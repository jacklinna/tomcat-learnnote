package com.jack.server;

import java.io.OutputStream;

public class JeanResponse {
    private static final int BUFFER_SIZE = 2048;
    Request request;
    OutputStream output;

    public JeanResponse(OutputStream output) {
        this.output = output;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public OutputStream getoutput() {
        return output;
    }
}
