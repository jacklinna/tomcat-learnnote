package com.jack.server.valves;

import com.jack.server.https.responses.HttpResponseImpl;
import com.jack.server.interfaces.*;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

/**
 * @Description 针对log和valve结合
 */
public class AccessLogValve extends ValveBase {
    //Valve 引入log的参数
    protected  static final String COMMON_ALIA = "common";
    protected  static final String COMMON_PATTERN = "%h %l %u %t \"%r\" %s %b";
    protected  static final String COMBINED_ALIAS = "combined";
    protected  static final String COMBINED_PATTERN = "%h %l %u %t \"%r\" %s %b \"%{Referer}i\" \"%{User-Agent}i\"";


    private String dateStamp="";
    private String directory="logs";
    private Boolean common = false;
    private Boolean combined = false;
    private String pattern = null;
    private String prefix="access_log.";
    private String suffix="";
    private PrintWriter writer= null;

    private DateTimeFormatter dateFormatter= null;
    private DateTimeFormatter dayFormatter= null;
    private DateTimeFormatter monthFormatter= null;
    private DateTimeFormatter yearFormatter= null;
    private DateTimeFormatter timeFormatter= null;

    private String timeZone = null;
    private LocalDate currentDate= null;

    private String space="";
    private long rotationLastChecked = 0L;

    protected static final String info="com.jack.server.valves.AccessLogValve/1.0";
    protected static final String  months[]={"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

    public static String getCommonAlia() {
        return COMMON_ALIA;
    }

    public static String getCommonPattern() {
        return COMMON_PATTERN;
    }

    public static String getCombinedAlias() {
        return COMBINED_ALIAS;
    }

    public static String getCombinedPattern() {
        return COMBINED_PATTERN;
    }

    public String getDateStamp() {
        return dateStamp;
    }

    public void setDateStamp(String dateStamp) {
        this.dateStamp = dateStamp;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public Boolean getCommon() {
        return common;
    }

    public void setCommon(Boolean common) {
        this.common = common;
    }

    public Boolean getCombined() {
        return combined;
    }

    public void setCombined(Boolean combined) {
        this.combined = combined;
    }

    public String getPattern() {
        return pattern;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }

    public DateTimeFormatter getDateFormatter() {
        return dateFormatter;
    }

    public void setDateFormatter(DateTimeFormatter dateFormatter) {
        this.dateFormatter = dateFormatter;
    }

    public DateTimeFormatter getDayFormatter() {
        return dayFormatter;
    }

    public void setDayFormatter(DateTimeFormatter dayFormatter) {
        this.dayFormatter = dayFormatter;
    }

    public DateTimeFormatter getMonthFormatter() {
        return monthFormatter;
    }

    public void setMonthFormatter(DateTimeFormatter monthFormatter) {
        this.monthFormatter = monthFormatter;
    }

    public DateTimeFormatter getYearFormatter() {
        return yearFormatter;
    }

    public void setYearFormatter(DateTimeFormatter yearFormatter) {
        this.yearFormatter = yearFormatter;
    }

    public DateTimeFormatter getTimeFormatter() {
        return timeFormatter;
    }

    public void setTimeFormatter(DateTimeFormatter timeFormatter) {
        this.timeFormatter = timeFormatter;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate currentDate) {
        this.currentDate = currentDate;
    }

    public String getSpace() {
        return space;
    }

    public void setSpace(String space) {
        this.space = space;
    }

    public long getRotationLastChecked() {
        return rotationLastChecked;
    }

    public void setRotationLastChecked(long rotationLastChecked) {
        this.rotationLastChecked = rotationLastChecked;
    }

    public static String[] getMonths() {
        return months;
    }

    public AccessLogValve(){
        super();
        setPattern("common");
    }

    private void setPattern(String common) {
    }

    @Override
    public String getInfo() {
        return info;
    }

    public int getDebug(){
        return this.debug;
    }

    public void setDebug(int debug) {
        this.debug = debug;
    }

    @Override
    public Container getContainer() {
        return this.container;
    }

    @Override
    public void setContainer(Container container) {
        this.container = container;
    }
    //  核心方法
    @Override
    public void invoke(Request request, Response response, ValveContext context) throws IOException, ServletException {
        //1.调用context中的invokeNext,实现职责链调用
        context.invokeNext(request,response);

        //这是当前valve 的逻辑
        LocalDate date = getDate();
        StringBuffer result = new StringBuffer();

        //是否属于公共日志
        if(common || combined){
            //省略
        }else {
            //按照模式处理
            boolean replace = false;
            for(int i=0;i<pattern.length();i++){
                char ch = pattern.charAt(i);
                if(replace){
                    result.append(replace(ch,date,request,response));
                    replace = false;
                }else if(ch =='%'){
                    replace = true;
                }else{
                    result.append(ch);
                }
            }
        }
        log(request.toString(),date);

    }

    private synchronized void close(){
        if(writer == null){
            return;
        }
        writer.flush();
        writer.close();
        writer=null;
        dateStamp="";
    }

    private void log(String msg, LocalDate date) {
        long systime = System.currentTimeMillis();

        if((systime - rotationLastChecked) > 1000){
            //新的时间
            currentDate = LocalDate.now();
            rotationLastChecked = systime;

            String tsDate = dateFormatter.format(currentDate);

            if(!dateStamp.equals(tsDate)){
                synchronized (this){
                    if(!dateStamp.equals(tsDate)){
                        close();
                        dateStamp = tsDate;
                        open();
                    }
                }
            }
        }

        if(writer !=null){
            writer.println(msg);
        }
    }

    private synchronized void open() {
       File dir = new File(directory);
       if(!dir.isAbsolute()){
           dir = new File(System.getProperty("Jean.base"),directory);
       }
       dir.mkdirs();

       //打开log文件
        try{
            String pathname = dir.getAbsolutePath() + File.separator + prefix + dateStamp +suffix;
            writer = new PrintWriter(new FileWriter(pathname,true));
        } catch (IOException e) {
            e.printStackTrace();
            writer = null;
        }
    }

    private String replace(char pattern, LocalDate date, Request request, Response response) {
        String value = null;

        ServletRequest req = request.getRequest();
        HttpServletRequest hreq =null;

        if(req instanceof HttpServletRequest){
            hreq = (HttpServletRequest)req;
        }

        ServletResponse res = response.getResponse();
        HttpServletResponse hres = null;

        if(res instanceof HttpServletResponse){
            hres = (HttpServletResponse)res;
        }

        switch (pattern){
            case 'a':
                value = req.getRemoteAddr();
                break;
            case 'A':
                value = "127.0.0.1";
                break;
            case 'b':
                int length = response.getContentCount();
                if(length <= 0){
                    value = "-";
                }else{
                    value = ""+length;
                }
                break;
            case 'B':
                //value = ""+res.getContentLength();
                break;
            case 'h':
                value = req.getRemoteHost();
                break;
            case 'H':
                value = req.getProtocol();
                break;
            case 'l':
                value = "-";
                break;
            case 'm':
                if(hreq != null){
                    value = hreq.getMethod();
                }else{
                    value = "";
                }
                break;
            case 'p':
                value = ""+req.getServerPort();
                break;
            case 'q':
                String query = null;
                if(hreq != null){
                    query = hreq.getQueryString();
                }
                if(query != null){
                    value = "?"+ query;
                }else{
                    value = "";
                }
                break;
            case 'r':
                StringBuffer stringBuffer = new StringBuffer();
                if(hreq != null){
                    stringBuffer.append(hreq.getMethod());
                    stringBuffer.append(space);
                    stringBuffer.append(hreq.getRequestURI());
                    if(hreq.getQueryString() !=null){
                        stringBuffer.append("?");
                        stringBuffer.append(hreq.getQueryString());
                    }
                    stringBuffer.append(space);
                    stringBuffer.append(hreq.getProtocol());
                }else{
                    stringBuffer.append("- -");
                    stringBuffer.append(req.getProtocol());
                }
                value = stringBuffer.toString();
                break;
            case 'S':
                if(hreq != null){
                    if(hreq.getSession(false) != null){
                        value = hreq.getSession(false).getId();
                    }else{
                        value = "-";
                    }
                }else{
                    value = "-";
                }
                break;
            case 's':
                if(hreq != null){
                    value = ""+((HttpResponseImpl) response).getStatus();
                }else{
                    value = "-";
                }
                break;
            case 't':
                StringBuffer stringBuffer2 = new StringBuffer("[");
                stringBuffer2.append(dayFormatter.format(date));
                stringBuffer2.append("/");
                stringBuffer2.append(lookup(monthFormatter.format(date)));
                stringBuffer2.append("/");
                stringBuffer2.append(yearFormatter.format(date));
                stringBuffer2.append(":");
                stringBuffer2.append(timeFormatter.format(date));
                stringBuffer2.append(" ");
                stringBuffer2.append(timeZone);
                stringBuffer2.append(']');
                break;
            case 'u':
                if(hreq != null){
                    value =hreq.getRemoteUser();
                }
                if(value ==null){
                    value="-";
                }
                break;
            case 'U':
                if(hreq != null){
                    value =hreq.getRequestURI();
                }else{
                    value="-";
                }
                break;
            case 'v':
                value = req.getServerName();
                break;
            default:
                value = "???"+pattern + "???";
                break;
        }
        if(value == null){
            return ("");
        }else{
            return value;
        }
    }

    private String lookup(String month) {
        // 分配不同的月份
        int index;
        try{
            index = Integer.parseInt(month) -1;
        }catch (Throwable t){
            index = 0;
        }
        return months[index];
    }

    private LocalDate getDate() {
        long systime = System.currentTimeMillis();
        if((systime - currentDate.getLong(ChronoField.MILLI_OF_SECOND)) > 1000){
            currentDate = LocalDate.now();
        }
        return currentDate;
    }
}
