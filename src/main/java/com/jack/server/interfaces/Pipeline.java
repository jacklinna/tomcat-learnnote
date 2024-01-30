package com.jack.server.interfaces;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @Description 表示的是Container中的Valve链条,其中
 * 一个Valve其中包含从first到basic一连串的过程，
 * 其中basic是个特殊的，
 * Pipeline启动 Valve链条的调用
 */
public interface Pipeline {
    public Valve getBasic();
    public void setBasic(Valve valve);

    public void addValve(Valve valve);
    public Valve[] getValves();

    public void removeValve(Valve valve);

    public void invoke(Request request, Response response) throws
            IOException, ServletException;
}
