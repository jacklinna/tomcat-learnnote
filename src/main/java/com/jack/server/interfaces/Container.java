package com.jack.server.interfaces;

import com.jack.server.loaders.WebappClassLoader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Description 这个接口含有ClassLoader,child,parent的操作方法，还有invoke
 * 应该有一个 基于这个实现的抽象基础类
 */
public interface Container {
    public static final String ADD_CHILD_EVENT = "addChild";
    public static final String REMOVE_CHILD_EVENT = "removeChild";

    //引入日志
    public Logger getLogger();
    public void setLogger(Logger logger);

    public String getInfo();
    //替换原来的ClassLoader
    //如今新增了 loader接口，替换一下
    public Loader getLoader();
    public void setLoader(Loader loader);

    public String getName();
    public void setName(String name);

    public Container getParent();
    public void setParent(Container parent);

    public void addChild(Container child);
    public Container findChild(String name);
    public Container[] findChildren();
    public void removeChild(Container child);

    public void invoke(Request request, Response response) throws IOException, ServletException;


}
