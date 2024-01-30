package com.jack.server.core;

import com.jack.server.core.StandardContext;
import com.jack.server.interfaces.*;
import com.jack.server.core.ContainerBase;
import com.jack.server.loaders.WebappClassLoader;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Description 修改名字 ServletWrapper --> StandardWrapper
 */
public class StandardWrapper extends ContainerBase implements Wrapper {

    //Wrapper包装的就是Servlet，需要有这个实例
    private Servlet instance = null;

    private String servletClass;
    //原来使用的loader,name ,parent 都直接使用ContainerBase

    //其中，ServletContext 是 Wrapper 的 parent
    private StandardContext standardContext;

    public StandardWrapper(String servletClass, StandardContext parent){

        //添加 职责链
        super();
        pipeline.setBasic(new StandardContextValve());
        this.parent = parent;
        this.servletClass = servletClass;

        try{
            loadServlet();
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }

    //这是核心方法
    //主要是通一个ClassLoader 加载并且实例化 Servlet,然后调用 init 初始化，这和COntainer中是一样的处理。
    //所以Container 可以直接调用这个类
    public Servlet loadServlet() throws ServletException {
        if(instance != null){
            return instance;
        }

        Servlet servlet = null;
        String actualClass = servletClass;
        if(actualClass == null){
            throw new ServletException("servlet class has not been specified");
        }
        Loader classLoader = getLoader();
        Class classClass = null;

        try{
            if(classLoader != null){
                classClass = classLoader.getClassLoader().loadClass(actualClass);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try{
            servlet = (Servlet) classClass.newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    //加入 初始化参数
        //servlet.init(null);
        try{
            servlet.init(new StandardServletConfig(servletClass
            ,standardContext.getServletContext()
            ,standardContext.getServletInitParametersMap().get(servletClass)));

        }catch (Throwable e){
            throw new ServletException("Failed initialize servlet.");
        }

        instance = servlet;
        return servlet;

    }

    public Loader getLoader() {
        if(loader != null){return loader;}
        return parent.getLoader();
    }

    @Override
    public int getLoadOnStartup() {
        return 0;
    }

    @Override
    public void setLoadOnStartup() {

    }

    public String getServletClass(){
        return servletClass;
    }

    public void setServletClass(String servletClass){
        this.servletClass = servletClass;
    }

    @Override
    public void addInitParameter(String name, String value) {

    }

    @Override
    public Servlet allocate() throws ServletException {
        return null;
    }

    @Override
    public String findInitParameter(String name) {
        return null;
    }

    @Override
    public String[] findInitParameters() {
        return new String[0];
    }

    @Override
    public void removeInitParameter(String name) {

    }

    @Override
    public void load() throws ServletException {

    }
    @Override
    public Container[] findChildren() {
        return null;
    }
    @Override
    public Container findChild(String name) {
        return null;
    }
    @Override
    public void addChild(Container child) { }
    @Override
    public void removeChild(Container child) { }


    @Override
    public String getInfo() {
        return "Jean Servlet Wrapper Version 1.0";
    }

    public Servlet getServlet(){
        return this.instance;
    }

    public void setParent(StandardContext container){
        this.parent = container;
    }

    @Override
    public void invoke(Request request, Response response) throws ServletException, IOException {
        System.out.println("StandardWrapper invoke()");

        super.invoke(request,response);
    }


}
