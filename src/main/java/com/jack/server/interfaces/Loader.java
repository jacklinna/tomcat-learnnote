package com.jack.server.interfaces;

public interface Loader {
    //容器相关
    public Container getContainer();
    public void setContainer(Container container);

    //获取应用根目录
    public String getPath();
    public void setPath(String path);

    public String getDocbase();
    public void setDocbase(String docbase);

    //获取 自定义类加载器
    public ClassLoader getClassLoader();

    public String getInfo();

    //添加在类的 第三方库
    public void addRepositories();

    //加载器的基本行为
    public void start();
    public void stop();
}
