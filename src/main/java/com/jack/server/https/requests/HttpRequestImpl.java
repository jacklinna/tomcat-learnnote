package com.jack.server.https.requests;

import com.jack.server.constants.DefaultHeaders;
import com.jack.server.interfaces.*;
import com.jack.server.sessions.StandardSessionFacade;
import com.jack.server.https.HttpConnector;
import com.jack.server.https.responses.HttpResponseImpl;
import com.jack.server.https.streams.SocketInputStream;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Descripton HttpRequest 改为 HttpRequestImpl
 */
public class HttpRequestImpl implements HttpServletRequest, Request {

    private SocketInputStream sis;
    HttpRequestLine requestLine = new HttpRequestLine();
    InetAddress inetAddress;
    int port;

    private boolean paused=false;


    private String uri;
    private String queryString;
    //接收处理 post参数
    //value是字符串数组  存在多个值与之相对，options，checkbox
    //目前只考虑文本类型，其实可以支持 文本，二进制，压缩包等，都是通过 Content-Type 定义指定。
    //常见的 Content-Type 有：application/json,applicaition/xml;
    //这样就支持 json xml
    //post 也可以混合，使用 multipart/form-data,一部分二进制，一部分文本


    //protected Map<String,String[]> parametersPost = new ConcurrentHashMap<>();

    private InputStream inputStream;

    //添加不同应用的目录
    private String docbase;

    protected HashMap<String,String> headers = new HashMap<>();
    protected Map<String,String[]> parameters = new ConcurrentHashMap<>();

    //cookie session
    Cookie[] cookies;
    HttpSession session;
    String sessionid;
    StandardSessionFacade sessionFacade;
    //
    private HttpResponseImpl response;

    public HttpRequestImpl(){}

    public HttpRequestImpl(InputStream inputStream){
        this.inputStream = inputStream;
        //没有这个会出现异常：thread "Thread-3" java.lang.NullPointerException
        this.sis = new SocketInputStream(this.inputStream,2048);
        //另一个异常：thread "Thread-3" java.lang.ArrayIndexOutOfBoundsException: 512
    }

    public String getDocbase() {
        return docbase;
    }

    public void setDocbase(String docbase) {
        this.docbase = docbase;
    }

    public void  parse(Socket socket) throws IOException {
        try{
            parseConnection(socket);
            // 这里将整个地址作为 URI,其实 URI 应该只是问号前面的
            this.sis.readRequestLine(requestLine);
            //需要将 问好后面的 具体参数分离出
            parseRequestLine();
            parseHeaders();
        }catch (ServletException e){
            e.printStackTrace();
        }
        this.uri = new String(requestLine.uri,0,requestLine.uriEnd);
    }

    //处理请求行
    private void parseRequestLine() {
        //这是简单处理 GET的
        int question = requestLine.indexOf("?");
        if(question >=0){
            queryString = new String(requestLine.uri,question +1,requestLine.uriEnd -(question +1));
            uri = new String(requestLine.uri,0,question);

            //加入对于sessionid的处理逻辑
            String tmp = ";"+DefaultHeaders.JSESSIONID_NAME+"=";
            int semicolon = uri.indexOf(tmp);
            if(semicolon>=0){
                sessionid = uri.substring(semicolon + tmp.length());
                uri = uri.substring(0,semicolon);
            }
            int contextslash = uri.indexOf("/",1);
            if(contextslash != -1){
                this.docbase = uri.substring(1,contextslash);
                uri = uri.substring(contextslash);
            }
        }else{
            //没有问号后面的参数
            queryString = null;
            uri = new String(requestLine.uri,0,requestLine.uriEnd);

            //加入对于sessionid的处理逻辑
            String tmp = ";"+DefaultHeaders.JSESSIONID_NAME+"=";
            int semicolon = uri.indexOf(tmp);
            if(semicolon>=0){
                sessionid = uri.substring(semicolon + tmp.length());
                uri = uri.substring(0,semicolon);
            }
            int contextslash = uri.indexOf("/",1);
            if(contextslash != -1){
                this.docbase = uri.substring(1,contextslash);
                uri = uri.substring(contextslash);
            }
        }
    }

    private void parseParameters(){
       //设置字符集
        String encoding = getCharacterEncoding();
        if(encoding == null){
            //每一个字节 直接作为一个 Unicode 字符，
            // 比如 0xD6,0xD0 这两个字节
            // 通过 IOS-8859-1 转化为 字符串时候，将直接得到两个 Unicode 字符
            // 如果将 Unicode 字符串 按照 ISO-8859-1 寻找对应关系，只能找到 0-255 范围内
            encoding = "ISO-8859-1";
        }
        //获取字符串
        String qString = getQueryString();

        if(qString != null){
            byte[] bytes = new byte[qString.length()];

            try{
                bytes = qString.getBytes(encoding);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        //获取 Content-Type
        String contentType = getContentType();
        if(contentType == null){
            contentType="";
        }
        int semicolon = contentType.indexOf("；");
        if(semicolon >=0){
            contentType = contentType.substring(0,semicolon).trim();
        }else{
            contentType = contentType.trim();
        }

        //针对 post方法，从body解析参数

        if("POST".equals(getMethod()) && (getContentLength() >0)
        && ("application/x-www-form-urlencoded".equals(contentType))){
            try{
                int  max = getContentLength();
                int len = 0;

                byte buf[] = new byte[getContentLength()];

                ServletInputStream is = getInputStream();

                while(len < max){
                    int next = is.read(buf,len,max-len);

                    if(next <0){
                        break;
                    }
                    len += next;
                }

                is.close();
                if(len < max){
                    throw new RuntimeException("Content length mismatch");
                }
                parseParameters(this.parameters,buf,encoding);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void parseConnection(Socket socket) {
        inetAddress = socket.getInetAddress();
        port = socket.getPort();
    }

    private void parseHeaders() throws IOException,ServletException{
        while(true){
            HttpHeader httpHeader = new HttpHeader();
            sis.readHeader(httpHeader);

            if(httpHeader.nameEnd == 0){
                if(httpHeader.valueEnd ==0){
                    return;
                }else{
                    throw new ServletException("httpProcessor.parseHeaders.colon");

                }
            }
            String name = new String(httpHeader.name,0,httpHeader.nameEnd);
            String value= new String(httpHeader.value,0,httpHeader.valueEnd);
            //key使用小写
            name = name.toLowerCase();

            //set request headers

            if(name.equals(DefaultHeaders.ACCEPT_LANGUAGE_NAME)){
                headers.put(name,value);
            }else if(name.equals(DefaultHeaders.CONTENT_LENGTH_NAME)){
                headers.put(name,value);
            }else if(name.equals(DefaultHeaders.CONTENT_TYPE_NAME)){
                headers.put(name,value);
            }else if(name.equals(DefaultHeaders.HOST_NAME)){
                headers.put(name,value);
            }else if(name.equals(DefaultHeaders.CONNECTION_NAME)){
                headers.put(name,value);
            }else if(name.equals(DefaultHeaders.TRANSFER_ENCODING_NAME)){
                headers.put(name,value);
            }else if(name.equals(DefaultHeaders.COOKIE_NAME)){
                headers.put(name,value);
                //加上处理cookie，session 
                Cookie[] cookies = parseCookieHeader(value);
            }else if(name.equals(DefaultHeaders.CONNECTION_NAME)){
                headers.put(name,value);
                //加上处理connection消息头
                if("colse".equals(value)){
                    response.setHeader("Connection","close");
                }

            }else{
                headers.put(name,value);
            }

        }

    }

    //解析格式如下的字符串：
    //Cookie头：key1=value1;key2=value2
    private Cookie[] parseCookieHeader(String header) {
        if((header == null) ||(header.length() <1)){
            return new Cookie[0];
        }
        ArrayList<Cookie> cookies = new ArrayList<>();
        while(header.length()>0){
            int semicolon = header.indexOf("；");
            if(semicolon <0){
                semicolon = header.length();
            }
            if(semicolon ==0){
                break;
            }

            String token = header.substring(0,semicolon);
            if(semicolon < header.length()){
                header = header.substring(semicolon+1);

            }else{
                header="";
            }

            try{
                //获取到 key1=value1
                int equals = token.indexOf("=");
                if(equals>0){
                    String name = token.substring(0,equals).trim();
                    String value = token.substring(equals+1).trim();
                    cookies.add(new Cookie(name,value));
                }
            }catch (Throwable e){

            }

        }
        return ((Cookie[]) cookies.toArray(new Cookie[cookies.size()]));
    }

    //十六进制  到数字转换
    private byte convertHexDigit(byte  b){
        if((b >='0') && (b<='9')){
            return (byte)(b-'0');
        }
        if((b >='a') && (b<='f')){
            return (byte)(b-'a' + 10);
        }
        if((b >='A') && (b<='F')){
            return (byte)(b-'A' + 10);
        }
        return 0;
    }

    // 参数解析 多态
    // 是将字节流 读入到 buf[] 通过逐个字节 进行解析，处理
    public void parseParameters(Map<String,String[]> map,byte[] data,String encoding) throws UnsupportedEncodingException {
        if(paused){
            return;
        }

        if(data !=null && data.length >0){
            int pos = 0;
            int ix = 0;
            int ox = 0;

            String key = null;
            String value = null;

            //解析参数串，处理特殊字符
            while(ix < data.length){
                byte c = data[ix++];
                switch((char) c){
                    case '&':
                        //两个参数之间的间隔符
                        value = new String(data,0,ox,encoding);
                        if(key !=null){
                            putMapEntry(map,key,value);
                            key = null;
                        }
                        ox = 0;
                        break;
                    case '=':
                        //这是 键值对 间隔符
                        key = new String(data,0,ox,encoding);
                        ox = 0;
                        break;
                    case '+':
                        //特殊字符，空格
                        data[ox++] = (byte)' ';
                        break;
                    case '%':
                        //处理%表示的ASCII字符
                        data[ox++] = (byte) ((convertHexDigit(data[ix++]) << 4) + convertHexDigit(data[ix++]));
                        break;
                    default:
                        data[ix++] =c;
                }
            }
            if(key != null){
                value = new String(data,0,ox,encoding);
                putMapEntry(map,key,value);
            }

        }
        paused = true;
    }

    private void putMapEntry(Map<String, String[]> map, String name, String value) {
        String[] newValues = null;
        String[] oldvalues= (String[]) map.get(name);

        if(oldvalues == null){
            newValues = new String[1];
            newValues[0] = value;
        }else{
            newValues = new String[oldvalues.length +1];
            System.arraycopy(oldvalues,0,newValues,0,oldvalues.length);
            newValues[oldvalues.length] = value;
        }
        map.put(name,newValues);
    }

    public String getUri(){
        return uri;
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
    public ServletRequest getRequest() {
        return null;
    }

    @Override
    public Response getResponse() {
        return null;
    }

    @Override
    public void setResponse(Response reponse) {

    }

    @Override
    public Socket getSocket() {
        return null;
    }

    @Override
    public void setSocket(Socket socket) {

    }

    @Override
    public InputStream getSteam() {
        return null;
    }

    public void setStream(InputStream inputStream){
        this.inputStream = inputStream;
        this.sis = new SocketInputStream(this.inputStream,2048);
    }

    @Override
    public Wrapper getWrapper() {
        return null;
    }

    @Override
    public void setWrapper() {

    }

    @Override
    public ServletInputStream createInputStream() throws IOException {
        return null;
    }

    @Override
    public void finishRequest() throws IOException {

    }

    @Override
    public void recycle() {

    }

    @Override
    public void setContentLength(int length) {

    }

    @Override
    public void setContentType(String type) {

    }

    @Override
    public void setProtocol(String protocol) {

    }

    @Override
    public void setRemoteAddr(String remote) {

    }

    @Override
    public void setScheme(String scheme) {

    }

    @Override
    public void setServerPort(int port) {

    }

    @Override
    public String getInfo() {
        return null;
    }

    @Override
    public Object getAttribute(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {

    }

    @Override
    public int getContentLength() {
        return 0;
    }

    @Override
    public long getContentLengthLong() {
        return 0;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public String getParameter(String name) {
        parseParameters();
        String values[] = (String[]) parameters.get(name);

        if(values != null){
            return (values[0]);
        }else{
            return null;
        }
    }

    @Override
    public Enumeration<String> getParameterNames() {
        parseParameters();
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        parseParameters();

        String values[] = (String[]) parameters.get(name);

        if(values != null){
            return values;
        }else{
            return null;
        }
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        parseParameters();
        return (parameters);
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public String getServerName() {
        return null;
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getRemoteHost() {
        return null;
    }

    @Override
    public void setAttribute(String s, Object o) {

    }

    @Override
    public void removeAttribute(String s) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }

    @Override
    public String getRealPath(String s) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        return this.cookies;
    }

    @Override
    public long getDateHeader(String s) {
        return 0;
    }

    @Override
    public String getHeader(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getHeaders(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return null;
    }

    @Override
    public int getIntHeader(String s) {
        return 0;
    }

    @Override
    public String getMethod() {
        //这里实现了method，客户端就可以只需要重载doGet,doPost，内部匹配 靠这个完成。
        return new String(this.requestLine.method,0,this.requestLine.methodEnd);
    }

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getContextPath() {
        return null;
    }

    @Override
    public String getQueryString() {
        return null;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String s) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public String getRequestURI() {
        return null;
    }

    @Override
    public StringBuffer getRequestURL() {
        return null;
    }

    @Override
    public String getServletPath() {
        return null;
    }

    @Override
    public HttpSession getSession(boolean b) {
        return this.sessionFacade;
    }

    public String getSessionId() {
        return this.sessionid;
    }

    //如果有sesion 就直接返回。没有新建
    @Override
    public HttpSession getSession() {
        if(sessionFacade != null){
            return sessionFacade;
        }
        if(sessionid !=null){
            session = HttpConnector.sessions.get(sessionid);
            if(session !=null){
                sessionFacade = new StandardSessionFacade(session);
                return sessionFacade;
            }else{
                session = HttpConnector.createSession();
                sessionFacade = new StandardSessionFacade(session);
                return sessionFacade;
            }
        }else{
            session = HttpConnector.createSession();
            sessionFacade = new StandardSessionFacade(session);
            sessionid = session.getId();
            return sessionFacade;
        }
    }

    @Override
    public String changeSessionId() {
        return this.sessionid;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String s, String s1) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String s) throws IOException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> aClass) throws IOException, ServletException {
        return null;
    }

    public void setResponse(HttpResponseImpl response) {
        this.response = response;
    }
}
