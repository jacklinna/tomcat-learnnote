package com.jack.server.core;


import com.jack.server.events.ContainerEvent;
import com.jack.server.events.ContainerListener;
import com.jack.server.events.ContainerListenerDef;
import com.jack.server.facades.HttpRequestFacade;
import com.jack.server.facades.HttpResponseFacade;
import com.jack.server.filters.ApplicationFilterConfig;
import com.jack.server.filters.FilterDef;
import com.jack.server.filters.FilterMap;
import com.jack.server.https.HttpConnector;
import com.jack.server.interfaces.*;
import com.jack.server.loaders.WebappClassLoader;
import com.jack.server.startup.BootStrap;
import com.jack.server.https.requests.HttpRequestImpl;
import com.jack.server.https.responses.HttpResponseImpl;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Desciption 管理Servlet
 * 再一次修改名字 ServletWrapper--> StandardWrapper
 */
public class StandardContext2 extends ContainerBase implements Context {
    //与此容器关联的Connector容器
    HttpConnector connector = null;
    //不需要loader
    //ClassLoader loader = null;
    //内部管理的Servlet类和实例
    Map<String,String> servletClsMap = new ConcurrentHashMap<>();
    //Map<String, Servlet> servletInstanceMap = new ConcurrentHashMap<>();
    //调整成 Wapper 类
    //依旧是保存Servlet实例，不过这次有了一层Wrapper包装
    Map<String, StandardWrapper> servletInstanceMap = new ConcurrentHashMap<>();

    //添加 Filter 配置
    private Map<String, ApplicationFilterConfig> filterConfigs = new ConcurrentHashMap<>();

    private Map<String, FilterDef> filterDefs = new ConcurrentHashMap<>();
    private FilterMap filters[] = new FilterMap[0];

    //添加 监听器
    private ArrayList<ContainerListener> listeners = new ArrayList<>();
    //带有一定默认参数的Listener
    private ArrayList<ContainerListenerDef> listenerDefs = new ArrayList<>();

    //添加一个启动事件锚点
    public void start(){
        fireContainerEvent("Container Started",this);
    }

    //添加监听
    public void addContainerListener(ContainerListener listener){
        synchronized (listeners){
            listeners.add(listener);
        }
    }
    //移除监听
    public void removeContainerListener(ContainerListener listener){
        synchronized (listeners){
            listeners.remove(listener);
        }
    }

    private void fireContainerEvent(String type, Object data) {
        //是否有监听器，没有的话，触发也就没有意义
        if(listeners.size() <1){
            return;
        }

        //具体是哪个 事件--这里是 容器事件
        ContainerEvent event = new ContainerEvent(this,type,data);
        ContainerListener list[] = new ContainerListener[0];

        synchronized (listeners){
            list = (ContainerListener[]) listeners.toArray(list);
        }

        //遍历所有 容器事件 监听器，并且触发事件
        for(int i=0;i<list.length;i++){
            ((ContainerListener) list[i]).containerEvent(event);
        }
    }

    //这个不要也可以，引入这个，就可以接收 Def类型参数，
    public void  addListenerListDef(ContainerListenerDef listenerDef){
        synchronized (listenerDefs){
            listenerDefs.add(listenerDef);
        }
    }
    //启动监听
    public boolean listenerStart(){
        System.out.println("Listener Start ......");

        boolean ok = true;

        synchronized (listeners){
            //清空之前的
            listeners.clear();

            Iterator<ContainerListenerDef> defs = listenerDefs.iterator();
            while(defs.hasNext()){
                ContainerListenerDef def = defs.next();
                ContainerListener listener = null;

                try{
                    //还是老一套获取Class
                    String listenerClass = def.getListenerClass();
                    Loader classLoader = null;

                    classLoader = this.getLoader();
                    ClassLoader old = Thread.currentThread().getContextClassLoader();
                    //创建 新实例
                    Class<?> clazz = classLoader.getClassLoader().loadClass(listenerClass);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }catch (Throwable e){
                    e.printStackTrace();
                    ok = false;
                }
            }
        }

        return ok;
    }


    public void  addFilterDef(FilterDef filterDef){
        filterDefs.put(filterDef.getFilterName(),filterDef);
    }

    public void addFilterMap(FilterMap filterMap){
        String filterName = filterMap.getFilterName();
        String servletName = filterMap.getServletName();
        String urlPattern = filterMap.getUrlPattern();

        if(findFilterDef(filterName) == null){
            throw new IllegalArgumentException("no exist filtername="+filterName);
        }

        if((servletName == null) && (urlPattern == null)){
            throw new IllegalArgumentException("no exist filter either two class="+servletName);
        }

        if((servletName != null) && (urlPattern != null)){
            throw new IllegalArgumentException("no exist filter either two class="+servletName);
        }

        //因为过滤器是从tomcat 2.3 才有的，
        //2.2之前
        if((urlPattern!=null) && !validateURLPattern(urlPattern)){
            throw new IllegalArgumentException("no exist urlPattern="+urlPattern);
        }
        synchronized (filterMap){
            FilterMap results[] = new FilterMap[filters.length +1];
            System.arraycopy(filters,0,results,0,filters.length);
            results[filters.length] = filterMap;
            filters = results;
        }
    }

    public boolean validateURLPattern(String urlPattern) {
        if(urlPattern == null){
            return false;
        }
        if(urlPattern.startsWith("*.")){
            if(urlPattern.indexOf('/') <0){
                return true;
            }else{
                return false;
            }
        }

        if(urlPattern.startsWith("/")){
            return true;
        }else{
            return false;
        }
    }

    public FilterDef findFilterDef(String filterName) {
        return ((FilterDef) filterDefs.get(filterName));
    }

    public FilterDef[] findFilterDefs() {
       synchronized (filterDefs){
           FilterDef results[] = new FilterDef[filterDefs.size()];
           return ((FilterDef[]) filterDefs.values().toArray(results));
       }
    }

    public FilterMap[] findFilterMaps(){
        return filters;
    }

    public void removeFilterDef(FilterDef filterDef){
        filterDefs.remove(filterDef.getFilterName());
    }

    public void removeFilterMap(FilterMap filterMap){
        synchronized (filters){
            //确保当前存在 filters
            int n= -1;
            //初始化味-1；<0
            for(int i=0;i<filters.length;i++){
                if(filters[i] == filterMap){
                    n=i;
                    //存在，重新赋值；>=0;
                    break;
                }
            }
            if(n<0){
                return;
            }
            //先删除指定的
            FilterMap results[] = new FilterMap[filters.length-1];

            System.arraycopy(filters,0,results,0,n);
            System.arraycopy(filters,n+1,results,n,(filters.length-1)-n);
            filters = results;
        }
    }

    public boolean filterStart(){
        System.out.println("Filter Start ......");
        //为每个Filter实例化，并且记录一个配置文件Config
        boolean ok= true;
        synchronized (filterConfigs){
            filterConfigs.clear();;

            Iterator<String> names = filterDefs.keySet().iterator();

            while(names.hasNext()){
                String name = names.next();
                ApplicationFilterConfig filterConfig = null;

                try{
                    filterConfig = new ApplicationFilterConfig(this,(FilterDef) filterDefs.get(name));
                    filterConfigs.put(name,filterConfig);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (ServletException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (Throwable e){
                    ok = false;
                }
            }
        }
        return ok;
    }

    public FilterConfig findFilterConfig(String name){
        return (filterConfigs.get(name));
    }


    public StandardContext2(){
        //这里处理 calss loader
        //这个COntext 容器也需要日志
        //新增 对于 构造的调用
        super();
        //这里需要设置 基础的 basic
        pipeline.setBasic(new StandardContextValve());
        //新增 对于 构造的调用

        log("Container created.--Context");
    }


    public String getInfo(){
        return "Jean Servley Context,version 1.0";
    }


    public HttpConnector getConnector() {
        return connector;
    }

    public void setConnector(HttpConnector connector) {
        this.connector = connector;
    }

    public String getName(){
        return null;
    }
    public void setName(String name){}


    @Override
    public Container[] findChildren() {
        return new Container[0];
    }


    public void invoke(Request request, Response response) throws IOException,ServletException{
        System.out.println("StandardContext invoke()");
        super.invoke(request,response);
    }
//    public void invoke(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
//        StandardWrapper servletWrapper = null;
//        String uri = ((HttpRequestImpl)request).getUri();
//        String servletName = uri.substring(uri.lastIndexOf("/") + 1);
//
//        String servletClassName = servletName;
//
//        //从容器中获取 servlet wrapper
//        servletWrapper = servletInstanceMap.get(servletName);
//        if(servletWrapper == null){
//            servletWrapper = new StandardWrapper(servletClassName,this);
//            this.servletClsMap.put(servletName,servletClassName);
//            this.servletInstanceMap.put(servletName,servletWrapper);
//        }
//
//        //将调用传递到下层容器也就是Wrapper容器中
//        try{
//            HttpServletRequest requestFacade = new HttpRequestFacade(request);
//            HttpServletResponse responseFacade = new HttpResponseFacade(response);
//
//            System.out.println("Call Wrapper service()");
//            servletWrapper.invoke(requestFacade,responseFacade);
//        }catch (Exception e) {
//            System.out.println(e.toString());
//        }catch (Throwable e) {
//            System.out.println(e.toString());
//        }
//
//
//    }
//
//    //invoke
//    //从map中找到相关的servlet，然后调用
//    public void invoke(HttpRequestImpl request, HttpResponseImpl response) throws ServletException {
//
//        //调用 对应的不同 Servlet 接口和方法
//        //如今改造 CalssLoader 不再放到 HttpConnector管理，而是ServletContaiiner
//        //并且提供getLoader,setLoader
//        //这里的实例化，都是放在map里面，不是立马重新实例化
//        //Servlet servlet = null;
//        StandardWrapper servletWrapper = null;
//
//
//        ClassLoader loader = getLoader();
//        String uri = request.getUri();
//        String servletName = uri.substring(uri.lastIndexOf("/") +1);
//        String servletClassName = servletName;
//
//        //servlet = servletInstanceMap.get(servletName);
//        servletWrapper = servletInstanceMap.get(servletName);
//        //如果容器里没有servlet，就先loader，创建实例
//        if(servletWrapper == null){
//            //只是获取到 ServletWrapper 的实例，调用 ServletWrapper 内的 invoke() 方法，进一步进行了解耦。
//            servletWrapper = new StandardWrapper(servletClassName,this);
//            //类容器
//            servletClsMap.put(servletName,servletClassName);
//            //实例容器
//            servletInstanceMap.put(servletName,servletWrapper);
//            //servlet.init(null);
//        }
//
//        //调用service
//        try{
//            HttpRequestFacade requestFacade = new HttpRequestFacade(request);
//            HttpResponseFacade responseFacade = new HttpResponseFacade(response);
//
//            System.out.println("Call Container servlet service()");
//            servletWrapper.invoke(requestFacade,responseFacade);
//        } catch (IOException e) {
//            //e.printStackTrace();
//            System.out.println(e.toString());
//        }
//    }

//    public void invoke(Request request, Response response) throws IOException,ServletException{
//        super.invoke(request,response);
//    }
    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public void setDisplayName(String displayName) {

    }

    @Override
    public String getDocBase() {
        return null;
    }

    @Override
    public void setDocBase(String docBase) {

    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public void setPath(String path) {

    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public int getSessionTimeout() {
        return 0;
    }

    @Override
    public void setSessionTimeout(int timeout) {

    }

    @Override
    public String getWrapperClass() {
        return null;
    }

    @Override
    public void setWrapperClass(String WrapperClass) {

    }

    @Override
    public Wrapper createWrapper() {
        return null;
    }



    @Override
    public String findServletMapping(String patter) {
        return null;
    }

    @Override
    public String[] findServletMappings() {
        return new String[0];
    }

    @Override
    public void reload() {

    }

    public Wrapper getWrapper(String name){
        StandardWrapper servletWrapper = servletInstanceMap.get(name);
        if(servletWrapper ==null){
            String servletClassName = name;
            //servletWrapper = new StandardWrapper(servletClassName,this);

            this.servletClsMap.put(name,servletClassName);
            this.servletInstanceMap.put(name,servletWrapper);

        }

        return servletWrapper;
    }
}
