package com.jack.server.logger;

/**
 * @Description 标准错误类
 */
public class SystemErrLogger extends LoggerBase {
    public static final String info = "com.jack.server.logger.SystemErrLogger/1.0";

    @Override
    public void log(String msg) {
        //默认实现就是打印一下子....
        System.err.println(msg);
    }
}
