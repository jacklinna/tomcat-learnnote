package com.jack.server;

import com.jack.server.https.requests.HttpRequestImpl;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import java.io.*;
import java.util.Locale;
//@Remove
public class Response implements ServletResponse {
    //更新01：这之前我们用 byte[] 数组类型作为 output 的输出，
    // 这对业务程序员来说是不太便利的，
    // 因此我们现在支持往输出流里写入 String 字符串数据，
    // 于是就需要用到 PrintWriter 类。
    // 可以看到这里调用了 getCharacterEncoding() 方法，一
    // 般常用的是 UTF-8，所以在调用 getWriter() 之前，
    // 一定要先调用 setCharacterEncoding() 设置字符集。

    private static final int BUFFER_SIZE = 2048;
    HttpRequestImpl request;
    OutputStream output;

    //实现 JAva ServletResponse 需要的属性
    String contentType = null;
    long contentLength = -1;
    String charset = null;
    String characterEncoding = null;

    PrintWriter writer;

    public Response(OutputStream output) {
        this.output = output;
    }

    public void setRequest(HttpRequestImpl request) {
        this.request = request;
    }

    public OutputStream getoutput() {
        return output;
    }

    @Override
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        //我们目前设置了一个值为 true。这个值的含义为 autoflush，
        // 当为 true 时，println、printf 等方法会自动刷新输出流的缓冲。
        writer = new PrintWriter(new OutputStreamWriter(output,getCharacterEncoding()),true);
        return writer;
    }

    @Override
    public void setCharacterEncoding(String s) {
        this.characterEncoding = s;
    }

    @Override
    public void setContentLength(int i) {
        this.contentLength = i;
    }

    @Override
    public void setContentLengthLong(long l) {
        this.contentLength = l;
    }

    @Override
    public void setContentType(String s) {
        this.contentType = s;
    }

    @Override
    public void setBufferSize(int i) {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() throws IOException {

    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setLocale(Locale locale) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }
}
