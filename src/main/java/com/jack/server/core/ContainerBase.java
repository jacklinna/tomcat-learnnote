package com.jack.server.core;


import com.jack.server.interfaces.*;
import com.jack.server.loaders.WebappClassLoader;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ContainerBase implements Container,Pipeline {
    //子容器
    protected Map<String,Container> children = new ConcurrentHashMap<>();


    protected String name = null;

    //父容器
    protected Container parent = null;

    //增加 日志
    protected Logger logger = null;

    //增加职责链条
    protected Pipeline pipeline = new StandardPipeline(this);

    //类加载器
    // protected ClassLoader loader = null;
    //增加新的 Loader WebApp
    //替换为 接口 Loader
    protected Loader loader = null;
    //path
    protected String path;
    protected String docbase;

    public Pipeline getPipeline(){
        return (this.pipeline);
    }

    public void invoke(Request request, Response response) throws IOException, ServletException{
        System.out.println("ContainerBase invoke()");
        pipeline.invoke(request,response);
    }

    public synchronized void addValve(Valve valve){
        pipeline.addValve(valve);
    }
    public Valve getBasic(){
        return pipeline.getBasic();
    }

    public Valve[] getValves(){
        return pipeline.getValves();
    }

    public synchronized void removeValve(Valve valve){
        pipeline.removeValve(valve);
    }

    public void setBasic(Valve valve){
        pipeline.setBasic(valve);
    }
    public Logger getLogger(){
        if(logger != null){
            return logger;
        }
        if(parent !=null){
            return parent.getLogger();
        }
        return null;
    }

    public synchronized void setLogger(Logger logger){
        Logger old = this.logger;
        if(old == logger){
            return;
        }
        this.logger = logger;
    }

    //实现具体的日志，将抽象方法具体
    protected void log(String msg){
        Logger logger = getLogger();
        if(logger != null){
            logger.log(logName() +": "+msg);
        }else{
            System.out.println(logName() +": "+msg);
        }
    }
    protected void log(String msg,Throwable throwable){
        Logger logger = getLogger();
        if(logger != null){
            logger.log(logName() +": "+msg,throwable);
        }else{
            System.out.println(logName() +": "+msg+": "+throwable);
            throwable.printStackTrace(System.out);
        }
    }

    protected String logName(){
        String className = this.getClass().getName();

        int period = className.lastIndexOf(".");
        if(period >0){
            className = className.substring(period+1);
        }

        return (className +"["+getName()+"]");
    }

    public Map<String, Container> getChildren() {
        return children;
    }

    public void setChildren(Map<String, Container> children) {
        this.children = children;
    }

    @Override
    public Loader getLoader() {
        if (loader !=null){
            return loader;
        }
        if(parent != null){
            return parent.getLoader();
        }
        return null;
    }

    public synchronized void setLoader(Loader loader) {
        loader.setPath(path);
        loader.setDocbase(docbase);
        loader.setContainer(this);

        Loader old = this.loader;
        if(old == loader){
            return;
        }
        this.loader = loader;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Container getParent() {
        return parent;
    }

    public void setParent(Container parent) {
        Container old = this.parent;
        if(old == parent){
            return;
        }
        this.parent = parent;
    }

    public abstract String getInfo();
    public void addChild(Container child){
        addChildInternal(child);
    }

    private void addChildInternal(Container child){
        synchronized (children){
            if(children.get(child.getName()) !=null){
                throw new IllegalArgumentException("child "+child.getName()+" has exist,not unique;");

            }else{
                child.setParent(this);
                children.put(child.getName(),child);
            }
        }
    }

    public Container findChild(String name){
        if(name ==null){
            return null;
        }
        synchronized (children){
            return ((Container)children.get(name));
        }
    }

    //这里之前写的不对
    public Container[] findChildren(){
//        if(name ==null){
//            return null;
//        }
        synchronized (children){
            Container[] results = new Container[children.size()];
            return ((Container[]) children.values().toArray(results));
        }
    }

    public void removeChild(Container child){

        synchronized (children){
            if(children.get(child.getName()) == null){
                return;
            }else{
                children.remove(child.getName());
            }
        }
        child.setParent(null);
    }

}
