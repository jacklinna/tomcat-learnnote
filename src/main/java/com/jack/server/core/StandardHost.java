package com.jack.server.core;


import com.jack.server.events.ContainerEvent;
import com.jack.server.events.ContainerListener;
import com.jack.server.events.ContainerListenerDef;
import com.jack.server.https.HttpConnector;
import com.jack.server.interfaces.*;
import com.jack.server.loaders.WebappClassLoader;
import com.jack.server.loaders.WebappLoader;
import com.jack.server.logger.FileLogger;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 这里 包含一个 Connector和下一级别的context
 * listenrer 也是这里启动
 * 自然它本身就是一个容器，所以 也应该有自己的filter，Valve
 * 承接了 原来启动类 Bootstrap 的很多功能
 * 过滤器启动 filterStart
 * 监听器启动 listenerStart
 */
public class StandardHost extends ContainerBase{

    HttpConnector connector = null;
    //host 中保存 多个Context
    //这里面需要 根据启动类的调整，重新调整一下 获取COntext的方法
    Map<String,StandardContext> contextMap = new ConcurrentHashMap<>();

    //针对 host 监听
    private ArrayList<ContainerListenerDef> listenerDefs = new ArrayList<>();
    private ArrayList<ContainerListener> listeners = new ArrayList<>();

    public StandardHost(){
        super();
        pipeline.setBasic(new StandardContextValve());
        log("Host created");
    }

    @Override
    public String getInfo() {
        return "Jean Host，version 1.0";
    }

    public HttpConnector getConnector() {
        return connector;
    }

    public void setConnector(HttpConnector connector) {
        this.connector = connector;
    }

    public void invoke(Request request, Response response) throws IOException, ServletException {
        System.out.println("StandardHost invoke()");
        super.invoke(request,response);
    }

    //匹配不同的应用
    public StandardContext getContext(String name){
        StandardContext context = contextMap.get(name);
        if(context ==null){
            System.out.println("loading context:"+name);
            context = new StandardContext();
            //根据路径的某个名字 找项目根目录
            context.setDocBase(name);
            context.setConnector(connector);

            //TODO 增加了新的接口
            //传入进去 parent loader
            Loader loader = new WebappLoader(name,this.loader.getClassLoader());
            //TODO:
            context.setLoader(loader);
            loader.start();
            //唯一区别
            //因如此，遍历 /webapps 目录后会启动所有的 Context，
            // 这样也就实现了在 Host 调用前预装载 Context。
            context.start();
            this.contextMap.put(name,context);
        }
        return  context;
    }

    //host 启动
    public void start(){
        fireContainerEvent("Host Started",this);
        Logger logger = new FileLogger();
        setLogger(logger);
        ContainerListenerDef listenerDef = new ContainerListenerDef();

        listenerDef.setListenerClass("test.TestListener");
        listenerDef.setListenerName("TestListener");

        addListenerDef(listenerDef);
        listenerStart();

        //提前获取一下 有哪些COntext
        //就是在 webapps里面遍历目录，每一个文件夹就是一个COntext
        File classPath = new File(System.getProperty("jean.base"));
        String dirs[] = classPath.list();
        for(int i=0;i<dirs.length;i++){
            getContext(dirs[i]);
        }
    }

    private void addContainerListener(ContainerListener listener) {
        synchronized (listeners){
            listeners.add(listener);
        }
    }
    private void removeContainerListener(ContainerListener listener) {
        synchronized (listeners){
            listeners.remove(listener);
        }
    }

    private void fireContainerEvent(String type, Object data) {
        if(listeners.size() <1){
            return;
        }

        ContainerEvent event = new ContainerEvent(this,type,data);
        ContainerListener list[] = new ContainerListener[0];
        synchronized (listeners){
            list = (ContainerListener[]) listeners.toArray(list);
        }
        for (int i= 0;i<list.length;i++){
            ((ContainerListener) list[i]).containerEvent(event);
        }

    }
        public void addListenerDef(ContainerListenerDef listenerDef){
            synchronized (listenerDef){
                listenerDefs.add(listenerDef);
            }
        }

        public boolean listenerStart(){
            System.out.println("listener Start...");
            boolean ok = true;

            synchronized (listeners){
                listeners.clear();
                Iterator<ContainerListenerDef> defs = listenerDefs.iterator();

                while(defs.hasNext()){
                    ContainerListenerDef def = defs.next();
                    ContainerListener listener = null;

                    try{
                        //定义 需要使用的 loader
                        String listenerClass = def.getListenerClass();
                        Loader classLoader = null;

                        //host 对应 ，loader 就是 listener 的 loader
                        classLoader = this.getLoader();
                        ClassLoader old = Thread.currentThread().getContextClassLoader();

                        //获取 filter listener
                        Class<?> clazz = classLoader.getClassLoader().loadClass(listenerClass);
                        listener = (ContainerListener) clazz.newInstance();

                        addContainerListener(listener);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            return ok;
        }
}
