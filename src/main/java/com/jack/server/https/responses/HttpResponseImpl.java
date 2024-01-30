package com.jack.server.https.responses;

import com.jack.server.constants.DefaultHeaders;
import com.jack.server.https.requests.HttpRequestImpl;
import com.jack.server.interfaces.Connector;
import com.jack.server.interfaces.Context;
import com.jack.server.interfaces.Request;
import com.jack.server.interfaces.Response;
import com.jack.server.tools.CookieTools;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @Descripton HttpResponse 改为 HttpResponseImpl
 */
public class HttpResponseImpl implements HttpServletResponse, Response {

    //private Map<String,String> statusMessage = Collections.singletonMap();
    //定义的状态常量都来自 HttpServletResponse 里的定义。


    Map<String,String> headers = new ConcurrentHashMap<>();

    //默认返回 OK
    String message = getStatusMessage(HttpServletResponse.SC_OK);
    int status = HttpServletResponse.SC_OK;

    HttpRequestImpl request;
    OutputStream outputStream;
    PrintWriter writer;

    String contentType = null;
    int contentLength = -1;
    String charset = null;
    String protocol = "HTTP/1.1";

    //编码
    String charcterEncoding = "UTF-8";

    //保存一下 Cookie
    ArrayList<Cookie> cookies = new ArrayList<>();
    public HttpResponseImpl() {
    }

    public HttpResponseImpl(OutputStream output) {
        this.outputStream = output;
    }

    public void setRequest(HttpRequestImpl request) {
        this.request = request;
    }

    @Override
    public Connector getConnector() {
        return null;
    }

    @Override
    public void setConnector(Connector connector) {

    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public void setContext(Context context) {

    }

    @Override
    public Request getRequest() {
        return null;
    }

    @Override
    public void setRequest(Request request) {

    }

    @Override
    public ServletResponse getResponse() {
        return null;
    }

    @Override
    public OutputStream getStream() {
        return null;
    }

    public void setStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void setError() {

    }

    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public ServletOutputStream createOutputStream() throws IOException {
        return null;
    }

    public void finishResponse(){
        try{
            this.getWriter().flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    @Override
    public int getContentCount() {
        return 0;
    }

    @Override
    public String getContentType(String type) {
        return null;
    }

    @Override
    public PrintWriter getReporter() {
        return null;
    }

    @Override
    public void recycle() {

    }

    protected String getStatusMessage(int status){
        switch (status){
            case SC_OK:
                return ("OK");
            case SC_ACCEPTED:
                return ("Accepted");
            case SC_BAD_GATEWAY:
                return ("Bad GateWay");
            case SC_BAD_REQUEST:
                return ("Bad Request");
            case SC_CONTINUE:
                return ("Continue");
            case SC_FORBIDDEN:
                return ("Forbidden");
            case SC_INTERNAL_SERVER_ERROR:
                return ("Internal Server Error");
            case SC_METHOD_NOT_ALLOWED:
                return ("Method Not Allowed");
            case SC_NOT_FOUND:
                return ("Uri not Found");
            case SC_NOT_IMPLEMENTED:
                return ("Not Implemented");
            case SC_REQUEST_URI_TOO_LONG:
                return ("Request URI Too Long");
            case SC_SERVICE_UNAVAILABLE:
                return ("Service Unavaiable");
            case SC_UNAUTHORIZED:
                return ("Unauthorized");
            default:
                return ("HTTP Response Status:" + status);
        }
    }

    public void sendHeaders() throws IOException{
        //Session  也是响应头一部分
        PrintWriter writer = getWriter();

        //输出状态行
        writer.print(this.getProtocol());
        writer.print(" ");
        writer.print(status);

        if(message != null){
            writer.print(" ");
            writer.print(message);
        }

        writer.print("\r\n");
        if(getContentType() != null){
            writer.print("Cotent-Type: "+getContentType()+"\r\n");
        }
        if(getContentLength() >= 0){
            writer.print("Cotent-Type: "+getContentLength()+"\r\n");
        }

        //输出头信息

        Iterator<String> names = headers.keySet().iterator();
        while(names.hasNext()){
            String name = names.next();
            String value = headers.get(name);

            writer.print(name);
            writer.print(": ");
            writer.print(value);
            writer.print("\r\n");
        }
        //在空行之前 添加更多信息，包括session
        HttpSession session = this.request.getSession(false);
        if(session != null){
            Cookie cookie = new Cookie(DefaultHeaders.JSESSIONID_NAME,session.getId());
            cookie.setMaxAge(-1);
            //不会过期
            addCookie(cookie);
        }
        //同步一下cookie，保持一致，其他暂时不处理
        synchronized (cookies){
            Iterator<Cookie> items = cookies.iterator();

            while(items.hasNext()){
                Cookie cookie = (Cookie) items.next();

                writer.print(CookieTools.getCookieHeaderName(cookie));
                writer.print(":");

                StringBuffer sbValue = new StringBuffer();
                CookieTools.getCookieHeaderValue(cookie,sbValue);

                writer.print(sbValue.toString());
                writer.print("\r\n");
            }
        }
        //输出 空行
        writer.print("\r\n");
        writer.flush();

    }

    public int getContentLength() {
        return contentLength;
    }

    private String getProtocol() {
        return protocol;
    }

    @Override
    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    @Override
    public boolean containsHeader(String s) {
        return false;
    }

    @Override
    public String encodeURL(String s) {
        return null;
    }

    @Override
    public String encodeRedirectURL(String s) {
        return null;
    }

    @Override
    public String encodeUrl(String s) {
        return null;
    }

    @Override
    public String encodeRedirectUrl(String s) {
        return null;
    }

    @Override
    public void sendError(int i, String s) throws IOException {

    }

    @Override
    public void sendError(int i) throws IOException {

    }

    @Override
    public void sendRedirect(String s) throws IOException {

    }

    @Override
    public void setDateHeader(String s, long l) {

    }

    @Override
    public void addDateHeader(String s, long l) {

    }

    @Override
    public void setHeader(String name, String value) {
        headers.put(name,value);

        if(name.toLowerCase() == DefaultHeaders.CONTENT_LENGTH_NAME){
            setContentLength(Integer.parseInt(value));
        }

        if(name.toLowerCase() == DefaultHeaders.CONTENT_TYPE_NAME){
            setContentType(value);
        }

    }

    @Override
    public void addHeader(String name, String value) {
        headers.put(name,value);

        if(name.toLowerCase() == DefaultHeaders.CONTENT_LENGTH_NAME){
            setContentLength(Integer.parseInt(value));
        }

        if(name.toLowerCase() == DefaultHeaders.CONTENT_TYPE_NAME){
            setContentType(value);
        }
    }

    @Override
    public void setIntHeader(String s, int i) {

    }

    @Override
    public void addIntHeader(String s, int i) {

    }

    @Override
    public void setStatus(int i) {

    }

    @Override
    public void setStatus(int i, String s) {

    }

    @Override
    public int getStatus() {
        return 0;
    }

    @Override
    public String getHeader(String s) {
        return headers.get(s);
    }

    @Override
    public Collection<String> getHeaders(String s) {
        return headers.keySet();
    }

    @Override
    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }

    @Override
    public String getCharacterEncoding() {
        return charcterEncoding;
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
        writer = new PrintWriter(new OutputStreamWriter(outputStream,getCharacterEncoding()),true);
        return writer;
    }

    @Override
    public void setCharacterEncoding(String s) {
        this.charcterEncoding = s;
    }

    @Override
    public void setContentLength(int i) {
        this.contentLength = i;
    }

    @Override
    public void setContentLengthLong(long l) {

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
    public void sendAcknowledgement() throws IOException {

    }

    @Override
    public String getInfo() {
        return null;
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

    public OutputStream getoutput() {

        return this.outputStream;
    }

}
