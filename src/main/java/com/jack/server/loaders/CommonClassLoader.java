package com.jack.server.loaders;

import java.net.URL;
import java.net.URLClassLoader;

public class CommonClassLoader extends URLClassLoader {
    protected boolean delegate = false;
    private ClassLoader parent = null;
    private ClassLoader system = null;

    public CommonClassLoader() {
        super(new URL[0]);
        this.parent = getParent();
        system = getSystemClassLoader();
    }
    public CommonClassLoader(URL[] urls) {
        super(urls);
        this.parent = getParent();
        system = getSystemClassLoader();
    }

    public CommonClassLoader(ClassLoader parent) {
        super(new URL[0],parent);
        this.parent = parent;
        system = getSystemClassLoader();
    }

    public CommonClassLoader(URL[] urls,ClassLoader parent) {
        super(urls,parent);
        this.parent = parent;
        system = getSystemClassLoader();
    }

    //是否使用双亲委托
    public boolean getDelegate(){
        return delegate;
    }

    public void setDelegate(boolean delegate){
        this.delegate = delegate;
    }

    //这个类加载器 查找 不同的类
    public Class findClass(String name) throws ClassNotFoundException {
        Class clazz = null;

        try{
            clazz = super.findClass(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(clazz ==null){
            throw new ClassNotFoundException(name);
        }

        return clazz;
    }

    //查找以后，是加载...
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name,false);
    }

    //这个是Tomcat 是自己的，所以使用双亲委托是可以的，因为lib目录 下的类是整个Tomcat使用的，只有一份，所以可以这样加载

    //这个CommondLoader 是全局通用的，也就是 lib目录下加载类
    public Class<?> loadClass(String name,boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = null;

        // 先使用系统加载器
        //主要是防止覆盖

        try{
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

        //系统加载器无法处理
        //如果坚持采用双亲委托，则委托
        if(delegateLoad) {
            ClassLoader loader = parent;

            if (loader == null) {
                loader = system;
            }
            try {
                clazz = loader.loadClass(name);
                if (clazz != null) {
                    if (resolve) {
                        resolveClass(clazz);
                    }
                    return clazz;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

            //使用本地自定义
            try{
                clazz = findClass(name);
                if(clazz != null){
                    if(resolve){
                        resolveClass(clazz);
                    }
                    return clazz;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            //依旧无法加载，只能向上委托
            if(!delegateLoad){
                ClassLoader loader = parent;
                if(loader == null){
                    loader = system;
                }else{
                    try {
                        clazz = loader.loadClass(name);
                        if (clazz != null) {
                            if (resolve) {
                                resolveClass(clazz);
                            }
                            return clazz;
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            //依然没有找到
        throw new ClassNotFoundException(name);
    }

    private void log(String msg){
        System.out.println("WebappClassLoader :"+msg);
    }

    private void log(String msg,Throwable e){
        System.out.println("WebappClassLoader :"+msg);
        e.printStackTrace(System.out);
    }
}
