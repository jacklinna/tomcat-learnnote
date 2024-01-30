package com.jack.server.interfaces;

public interface Logger {
    //定义不同的日志级别
    //重载了几个log方法，多态
    public static final int FATAL = Integer.MIN_VALUE;
    public static final int ERROR = 1;
    public static final int WARNING = 3;
    public static final int INFORMATION = 5;
    public static final int DEBUG = 7;

    public String getInfo();

    public int getVerbosity();
    public void setVerbosity(int verbosity);

    public void log(String message);
    public void log(String message,Throwable throwable);
    public void log(String message,int verbosity);
    public void log(String message,Throwable throwable,int verbosity);
    public void log(Exception e,String message);
}
