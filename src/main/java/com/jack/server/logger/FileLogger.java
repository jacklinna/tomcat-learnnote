package com.jack.server.logger;

import com.jack.server.constants.LogConstants;
import com.jack.server.tools.StringManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;

public class FileLogger extends LoggerBase {

    //输出到文档的 日志类型

    //时间
    private String date = "";
    //目录
    private String directory = "logs";
    //基本类信息
    protected static final String info = "com.jack.server.logger.FileLogger";
    // 文件前缀
    private String prefix = "jean.";
    // StringManager 包
    private StringManager sm = StringManager.getManager(LogConstants.Package);
    //是否从头开始
    private Boolean started = false;
    //文件后缀
    private String suffix = ".log";
    //时间戳
    private Boolean timestamp = true;
    //输出类
    private PrintWriter writer = null;

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        String old = this.directory;
        this.directory = directory;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        String old = this.prefix;
        this.prefix = prefix;
    }


    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        String old = this.suffix;
        this.suffix = suffix;
    }

    public Boolean getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Boolean timestamp) {
        Boolean old = this.timestamp;
        this.timestamp = timestamp;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }

    @Override
    public void log(String msg) {
        //使用当前时间构建日志
        Timestamp ts = new Timestamp(System.currentTimeMillis());

        //这样截取日期格式不是很合理
        String tsString = ts.toString().substring(0,19);
        String tsDate = ts.toString().substring(0,10);

        //日期发生变化，产生新文件
        if(!date.equals(tsDate)){
            synchronized (this){
                if(!date.equals(tsDate)){
                    close();
                    date = tsDate;
                    open();
                }
            }
        }

        //记录
        if(writer != null){
            if(timestamp){
                writer.println(tsString + " "+msg);
            }else{
                writer.println(msg);
            }
        }

    }

    private void open() {
        File dir = new File(directory);
        //如果是相对路径
        if(!dir.isAbsolute()){
            dir = new File(System.getProperty("catalina.base"),directory);
        }
        dir.mkdirs();
        //写入文件
        try{
            String pathName = dir.getAbsolutePath() + File.separator + prefix +date
                    + suffix;
            writer = new PrintWriter(new FileWriter(pathName,true),true);
        } catch (IOException e) {
            e.printStackTrace();
            writer = null;

        }
    }
    private void close() {
        if(writer == null){
            return;
        }else{
            writer.flush();
            writer.close();
            writer = null;
            date="";
        }
    }
}
