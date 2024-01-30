package com.jack.server.logger;

import com.jack.server.interfaces.Logger;

import javax.servlet.ServletException;
import java.io.CharArrayWriter;
import java.io.PrintWriter;

//一个基本的抽象实现类
public abstract class LoggerBase implements Logger {
    protected int debug = 0;
    protected static final String info="com.jack.server.logger.LoggerBase";

    protected int verbosity = ERROR;

    public int getDebug() {
        return debug;
    }

    public void setDebug(int debug) {
        this.debug = debug;
    }

    public String getInfo(){
        return info;
    }

    @Override
    public int getVerbosity() {
        return verbosity;
    }

    @Override
    public void setVerbosity(int verbosity) {
        this.verbosity = verbosity;
    }

    public void setVerbosityLevel(String verbosity){
        switch (verbosity.toUpperCase()){
            case "FATAL":
                this.verbosity = FATAL;
                break;
            case "ERROR":
                this.verbosity = ERROR;
                break;
            case "WARNING":
                this.verbosity = WARNING;
                break;
            case "INFORMATION":
                this.verbosity = INFORMATION;
                break;
            case "DEBUG":
                this.verbosity = DEBUG;
                break;
            default:
                this.verbosity = DEBUG;
                break;
        }
    }

    public abstract void log(String msg);
    //由具体实现进一步重写，不进行默认处理;加一个 abstract

    public void log(Exception e,String msg){
        log(msg,e);
    }
    //核心方法
    public void log(String msg,Throwable throwable){
        CharArrayWriter buf = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(buf);

        writer.println(msg);
        throwable.printStackTrace(writer);

        Throwable rootCause = null;
        if(rootCause instanceof ServletException){
            rootCause = ((ServletException)throwable).getRootCause();

        }
        if(rootCause != null){
            writer.println("------- Root Cause ---------");
            rootCause.printStackTrace(writer);
        }

        log(buf.toString());

    }

    public void log(String msg,int verbosity){
        if(this.verbosity >= verbosity){
            //这个还是交给具体实现，此处没有默认处理
            log(msg);
        }
    }

    public void log(String msg,Throwable throwable,int verbosity){
        if(this.verbosity >= verbosity){
            log(msg,throwable);
        }
    }
}
