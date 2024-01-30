package com.jack.server.valves;

import com.jack.server.interfaces.*;

import javax.servlet.ServletException;
import java.io.IOException;

public abstract class ValveBase implements Valve {
    //需要当前的容器，每一层都是容器
    protected Container container = null;
    protected int debug = 0;

    protected  static String info = "com.jack.server.valves.ValveBase/1.0";

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

}
