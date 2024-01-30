package com.jack.server.logger;

/**
 * @Description 标准输出日志类
 */
public class SystemOutLogger extends LoggerBase{
    protected static final String info = "com.jack.server.logger.SystemOutLogger/1.0";

    @Override
    public void log(String msg) {
        System.out.println(msg);
    }
}
