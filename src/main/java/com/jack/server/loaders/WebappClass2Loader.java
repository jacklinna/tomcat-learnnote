package com.jack.server.loaders;

import java.net.URL;
import java.net.URLClassLoader;

public class WebappClass2Loader extends URLClassLoader{
    protected boolean delegate = false;
    //调用类 传进来的loader.parent 就是 Common
    private ClassLoader parent = null;
    //java 内置的那些ClassLoader
    private ClassLoader system = null;

//    ClassLoader classLoader;
//    // 这个ClassLoader 就是一个URLClassLoader，工作目录由docBase决定
//
//    // 根据java管理类的机制，这些不同的classloader,之间是相互隔离的，所以这些
//    //context 代表的应用之间也就天然相互隔离。
//    //java机制：不同的classloader 记载的类在JVM之中，就是两个不同的类，
//    //
//    // 因为JVM中表示一个类的唯一标识就是 classloader + 类名。
//
//    //关于整个服务器的目录，我们可以使用系统变量控制；
//    // System.setProperty("minit.base",WEB_ROOT);
//
//    String path;
//    String docbase;
//    Container container;

    public WebappClass2Loader(){
        super(new URL[0]);
        this.parent = getParent();
        system = getSystemClassLoader();
    }

    public WebappClass2Loader(URL[] urls) {
        super(urls);
        this.parent = getParent();
        system = getSystemClassLoader();
    }

    public WebappClass2Loader(ClassLoader parent) {
        super(new URL[0],parent);
        this.parent = parent;
        system = getSystemClassLoader();
    }

    public WebappClass2Loader(URL[] urls, ClassLoader parent) {
        super(urls,parent);
        this.parent = parent;
        system = getSystemClassLoader();
    }

    public boolean getDelegate(){
        return this.delegate;
    }

    public void setDelegate(boolean delegate){
        this.delegate = delegate;
    }

    public Class<?> findClass(String name) throws ClassNotFoundException {
        Class clazz = null;

        try{
            clazz = super.findClass(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(clazz == null){
            throw new ClassNotFoundException(name);
        }
        return clazz;
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        return loadClass(name,false);
    }

    //核心方法
    //这个加载顺序，已经打乱 Java 标准的类加载机制
    public Class<?> loadClass(String name,boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = null;

        try{
            //系统加载器
            //首先使用，防止 覆盖 java 自身类
            clazz = system.loadClass(name);
            if(clazz != null){
                if(resolve){
                    resolveClass(clazz);
                }
                return clazz;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        boolean delegateLoad = delegate;

        //基本的加载套路都是类似的
        if(delegateLoad){
            ClassLoader loader = parent;
            if(loader == null){
                loader = system;
            }

            try{
                clazz = loader.loadClass(name);
                if(clazz != null){
                    if(resolve){
                        resolveClass(clazz);
                    }
                    return clazz;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        //本地去加载
        clazz = findClass(name);
        if(clazz != null){
            if(resolve){
                resolveClass(clazz);
            }
            return clazz;
        }

        //本地没有，主动委托双亲
        if(!delegateLoad){
            //上面没经过双亲

            ClassLoader loader = parent;

            if(loader ==null){
                loader = system;
            }
            try{
                clazz = loader.loadClass(name);
                if(clazz != null){
                    if(resolve){
                        resolveClass(clazz);
                    }
                    return clazz;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        throw  new ClassNotFoundException(name);

    }
    public String getInfo(){
        return "A simple loader";
    }

    public void addRepository(String repository){

    }

    public String[] findRepositories(){
        return null;
    }

    public void stop(){}

    private void log(String msg){
        System.out.println("WebappClassLoader :"+msg);
    }

    private void log(String msg,Throwable e){
        System.out.println("WebappClassLoader :"+msg);
        e.printStackTrace(System.out);
    }
}
