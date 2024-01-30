package com.jack.server.loaders;

import com.jack.server.interfaces.Container;
import com.jack.server.interfaces.Loader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;

public class WebappLoader implements Loader {
    private ClassLoader classLoader;
    private ClassLoader parent;
    private String path;
    private String docbase;
    private Container container;

    public WebappLoader(String docbase){
        this.docbase = docbase;
    }
    public WebappLoader(String docbase,ClassLoader parent){

        this.docbase = docbase;
        this.parent = parent;
    }
    @Override
    public Container getContainer() {
        return container;
    }

    @Override
    public void setContainer(Container container) {
        this.container = container;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getDocbase() {
        return docbase;
    }

    @Override
    public void setDocbase(String docbase) {
        this.docbase = docbase;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public String getInfo() {
        return "a simple loader";
    }

    @Override
    public void addRepositories() {

    }

    @Override
    public void start() {
        System.out.println("Startting Webapp loader,docbase :"+docbase);

        try{
            //创建一个URL ClassLoader
            // 加载目录就是 jean.base 规定的根目录，加上应用目录
            //最后是 WEB-INF/classes 目录，
            //这样就实现了 一个应用有自己的 类加载器

            URL[] urls = new URL[1];
            URLStreamHandler streamHandler = null;

            //之前刚定义了 base，这里有需要 home
            File classPath = new File(System.getProperty("jean.base"));
            String repository = (new URL("file",null,classPath.getCanonicalPath() + File.separator)).toString();

            //应用 目录
            if(docbase != null && !docbase.equals("")){
                repository = repository + docbase + File.separator;
            }

            //Tomcat 默认就是从自己的lib 加载自身使用的第三方类
            repository = repository + "WEB-INF" + File.separator+"classes"+ File.separator;

            urls[0] = new URL(null,repository,streamHandler);

            System.out.println("WebApp loader Repository:"+repository);
            classLoader = new WebappClassLoader(urls,parent);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {

    }
}
