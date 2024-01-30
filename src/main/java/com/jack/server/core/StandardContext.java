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
import com.jack.server.logger.FileLogger;
import com.jack.server.startup.BootStrap;
import com.jack.server.https.requests.HttpRequestImpl;
import com.jack.server.https.responses.HttpResponseImpl;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.lang.model.util.ElementFilter;
import javax.servlet.*;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StandardContext extends ContainerBase implements Context{
    HttpConnector connector = null;
    Map<String,String> servletClsMap = new ConcurrentHashMap<>(); //servletName - ServletClassName
    Map<String,StandardWrapper> servletInstanceMap = new ConcurrentHashMap<>();//servletName - servletWrapper

    private Map<String,ApplicationFilterConfig> filterConfigs = new ConcurrentHashMap<>();
    private Map<String,FilterDef> filterDefs = new ConcurrentHashMap<>();
    private FilterMap filterMaps[] = new FilterMap[0];

    private ArrayList<ContainerListenerDef> listenerDefs = new ArrayList<>();
    private ArrayList<ServletContextListener> listeners = new ArrayList<>();

    //保存 cointext 配置信息
    private Map<String,String> initParametersMap = new ConcurrentHashMap<>();
    private Map<String,Map<String,String>> servletInitParametersMap = new ConcurrentHashMap<>();

    public StandardContext() {
        super();
        pipeline.setBasic(new StandardContextValve());

        log("Container created.");
    }

    public void start(){
        fireContainerEvent("Container Started",this);

        Logger logger = new FileLogger();
        setLogger(logger);

        //扫描对应的配置文件
        String file = System.getProperty("jean.base") + File.separator +this.docbase
                +File.separator + "WEB-INF" +File.separator + "web.xml";

        SAXReader reader = new SAXReader();
        Document document;

        try{
            document = reader.read(file);
            Element root = document.getRootElement();
            //解析 context-param 入口context配置
            List<Element> contextParams = root.elements("context-param");
            for(Element contextParam:contextParams){
                Element element = contextParam.element("param-name");
                String paramName = element.getText();
                Element paramValueElement = contextParam.element("param-value");
                String paramValue = paramValueElement.getText();

                initParametersMap.put(paramName,paramValue);
            }
            //TODO
            //servletContext = new StandardServletContext(this.docbase,initParametersMap);
            //先处理 listener
            List<Element> listeners = root.elements("listener");
            for(Element listener:listeners){
                Element listenerClass = listener.element("listener-class");
                String listenerClassName = listenerClass.getText();
                System.out.println("listenerClassName:"+listenerClassName);

                //加载
                ContainerListenerDef listenerDef =  new ContainerListenerDef();
                listenerDef.setListenerName(listenerClassName);
                listenerDef.setListenerClass(listenerClassName);
                addListenerDef(listenerDef);
            }
            //启动监听
            listenerStart();

            //过滤器
            List<Element> filters = root.elements("filter");
            for(Element filter:filters){
                Element filterName = filter.element("filter-name");
                String filterNameStr = filterName.getText();

                Element filterClass = filter.element("filter-class");
                String filterClassStr = filterClass.getText();
                System.out.println("filter "+filterNameStr + filterClassStr);

                //加载filters
                FilterDef filterDef = new FilterDef();
                filterDef.setFilterName(filterNameStr);
                filterDef.setFilterClass(filterClassStr);
                addFilterDef(filterDef);
            }

            //filter 映射
            List<Element> filtermaps = root.elements("filter-mapping");
            for(Element filtermap:filtermaps){
                Element filterName = filtermap.element("filter-name");
                String filterNameStr = filterName.getText();

                Element urlpattern = filtermap.element("url-pattern");
                String urlPatternStr = urlpattern.getText();

                System.out.println("filter mapping :"+filterNameStr+urlPatternStr);

                FilterMap filterMap = new FilterMap();
                filterMap.setFilterName(filterNameStr);
                filterMap.setUrlPattern(urlPatternStr);

                addFilterMap(filterMap);
            }
            filterStart();

            //处理Servlet
            List<Element> servlets = root.elements("servlet");
            for(Element servlet:servlets){
                Element servletName = servlet.element("servlet-name");
                String servletNameStr = servletName.getText();

                Element servletClass = servlet.element("servlet-class");
                String servletClassStr = servletClass.getText();

                //处理初始化参数
                Element servletInitParamElement = servlet.element("init-param");
                Element servletInitParamNameElement = servletInitParamElement.element("param-name");
                String servletInitParamName = servletInitParamNameElement.getText();
                Element servletInitParamValueElement = servletInitParamElement.element("param-value");
                String servletInitParamValue = servletInitParamValueElement.getText();
                Map<String, String> servletInitParamMap = new ConcurrentHashMap<>();
                servletInitParamMap.put(servletInitParamName, servletInitParamValue);
                servletInitParametersMap.put(servletClassStr, servletInitParamMap);

                Element loadOnStartup = servlet.element("load-on-startup");
                String loadOnStartupStr = null;

                if(loadOnStartup !=null){
                    loadOnStartupStr = loadOnStartup.getText();
                }
                System.out.println("servlet "+servletNameStr + servletClassStr);
                this.servletClsMap.put(servletNameStr,servletClassStr);
                if(loadOnStartupStr != null){
                    getWrapper(servletNameStr);
                }
            }

            //解析 servlet-mapping
            List<Element> servletMappings = root.elements("servlet-mapping");
            for(Element servletMapping:servletMappings){
                Element servletName = servletMapping.element("servlet-name");
                String servletNameStr = servletName.getText();

                Element ServletClass = servletMapping.element("url-pattern");
                String urlPatternStr = ServletClass.getText();

            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        System.out.println("Context Started....");

//        FilterDef filterDef = new FilterDef();
//        filterDef.setFilterName("TestFilter");
//        filterDef.setFilterClass("test.TestFilter");
//
//        addFilterDef(filterDef);
//
//        FilterMap filterMap = new FilterMap();
//        filterMap.setFilterName("TestFilter");
//        filterMap.setUrlPattern("/*");
//
//        addFilterMap(filterMap);
//
//        filterStart();
//
//        ContainerListenerDef listenerDef = new ContainerListenerDef();
//        listenerDef.setListenerName("TestListener");
//        listenerDef.setListenerClass("test.TestListener");
//
//        addListenerDef(listenerDef);
//
//        listenerStart();
    }

    public void addContainerListener(ServletContextListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    public void removeContainerListener(ServletContextListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    public void fireContainerEvent(String type, Object data) {
        if (listeners.size() < 1)
            return;
        ContainerEvent event = new ContainerEvent(this, type, data);
        ServletContextListener list[] = new ServletContextListener[0];
        synchronized (listeners) {
            list = (ServletContextListener[]) listeners.toArray(list);
        }
        for (int i = 0; i < list.length; i++)
            //TODO
            ((ContainerListener) list[i]).containerEvent(event);

    }
    public void addListenerDef(ContainerListenerDef listenererDef) {
        synchronized (listenerDefs) {
            listenerDefs.add(listenererDef);
        }
    }

    public String getInfo() {
        return "Minit Servlet Context, vesion 0.1";
    }

    public HttpConnector getConnector() {
        return connector;
    }
    public void setConnector(HttpConnector connector) {
        this.connector = connector;
    }

    public void invoke(Request request, Response response)
            throws IOException, ServletException {
        System.out.println("StandardContext invoke()");

        super.invoke(request, response);
    }

    public Map<String, Map<String, String>> getServletInitParametersMap() {
        return servletInitParametersMap;
    }

    public void setServletInitParametersMap(Map<String, Map<String, String>> servletInitParametersMap) {
        this.servletInitParametersMap = servletInitParametersMap;
    }

    @Override
    public String getDisplayName() {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public void setDisplayName(String displayName) {
        // TODO Auto-generated method stub

    }


    @Override
    public String getDocBase() {
        // TODO Auto-generated method stub
        return this.docbase;
    }
    @Override
    public void setDocBase(String docBase) {
        // TODO Auto-generated method stub
        this.docbase = docBase;
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
    public void setWrapperClass(String wrapperClass) {
    }
    @Override
    public Wrapper createWrapper() {
        return null;
    }
    public Wrapper getWrapper(String name){
        StandardWrapper servletWrapper = servletInstanceMap.get(name);
        if ( servletWrapper == null) {
            String servletClassName = name;
            servletWrapper = new StandardWrapper(servletClassName,this);
            this.servletClsMap.put(name, servletClassName);
            this.servletInstanceMap.put(name, servletWrapper);
        }
        return servletWrapper;
    }
    @Override
    public String findServletMapping(String pattern) {
        return null;
    }
    @Override
    public String[] findServletMappings() {
        return null;
    }
    @Override
    public void reload() {
    }

    public void addFilterDef(FilterDef filterDef) {
        filterDefs.put(filterDef.getFilterName(), filterDef);
    }

    public void addFilterMap(FilterMap filterMap) {
        // Validate the proposed filter mapping
        String filterName = filterMap.getFilterName();
        String servletName = filterMap.getServletName();
        String urlPattern = filterMap.getUrlPattern();
        if (findFilterDef(filterName) == null)
            throw new IllegalArgumentException("standardContext.filterMap.name"+filterName);
        if ((servletName == null) && (urlPattern == null))
            throw new IllegalArgumentException("standardContext.filterMap.either");
        if ((servletName != null) && (urlPattern != null))
            throw new IllegalArgumentException("standardContext.filterMap.either");
        // Because filter-pattern is new in 2.3, no need to adjust
        // for 2.2 backwards compatibility
        if ((urlPattern != null) && !validateURLPattern(urlPattern))
            throw new IllegalArgumentException("standardContext.filterMap.pattern"+urlPattern);

        // Add this filter mapping to our registered set
        synchronized (filterMaps) {
            FilterMap results[] =new FilterMap[filterMaps.length + 1];
            System.arraycopy(filterMaps, 0, results, 0, filterMaps.length);
            results[filterMaps.length] = filterMap;
            filterMaps = results;
        }
    }

    public FilterDef findFilterDef(String filterName) {
        return ((FilterDef) filterDefs.get(filterName));
    }
    public FilterDef[] findFilterDefs() {
        synchronized (filterDefs) {
            FilterDef results[] = new FilterDef[filterDefs.size()];
            return ((FilterDef[]) filterDefs.values().toArray(results));
        }
    }
    public FilterMap[] findFilterMaps() {
        return (filterMaps);
    }
    public void removeFilterDef(FilterDef filterDef) {
        filterDefs.remove(filterDef.getFilterName());
    }


    public void removeFilterMap(FilterMap filterMap) {
        synchronized (filterMaps) {
            // Make sure this filter mapping is currently present
            int n = -1;
            for (int i = 0; i < filterMaps.length; i++) {
                if (filterMaps[i] == filterMap) {
                    n = i;
                    break;
                }
            }
            if (n < 0)
                return;

            // Remove the specified filter mapping
            FilterMap results[] = new FilterMap[filterMaps.length - 1];
            System.arraycopy(filterMaps, 0, results, 0, n);
            System.arraycopy(filterMaps, n + 1, results, n,
                    (filterMaps.length - 1) - n);
            filterMaps = results;

        }
    }

    public boolean filterStart() {
        System.out.println("Filter Start..........");
        // Instantiate and record a FilterConfig for each defined filter
        boolean ok = true;
        synchronized (filterConfigs) {
            filterConfigs.clear();
            Iterator<String> names = filterDefs.keySet().iterator();
            while (names.hasNext()) {
                String name = names.next();
                ApplicationFilterConfig filterConfig = null;
                try {
                    filterConfig = new ApplicationFilterConfig
                            (this, (FilterDef) filterDefs.get(name));
                    filterConfigs.put(name, filterConfig);
                } catch (Throwable t) {
                    ok = false;
                }
            }
        }

        return (ok);

    }
    public FilterConfig findFilterConfig(String name) {
        return (filterConfigs.get(name));
    }
    private boolean validateURLPattern(String urlPattern) {
        if (urlPattern == null)
            return (false);
        if (urlPattern.startsWith("*.")) {
            if (urlPattern.indexOf('/') < 0)
                return (true);
            else
                return (false);
        }
        if (urlPattern.startsWith("/"))
            return (true);
        else
            return (false);
    }

    public boolean listenerStart() {
        System.out.println("Listener Start..........");
        boolean ok = true;
        synchronized (listeners) {
            listeners.clear();
            Iterator<ContainerListenerDef> defs = listenerDefs.iterator();
            while (defs.hasNext()) {
                ContainerListenerDef def = defs.next();
                ServletContextListener listener = null;
                try {
                    // Identify the class loader we will be using
                    String listenerClass = def.getListenerClass();
                    Loader classLoader = null;
                    classLoader = this.getLoader();

                    ClassLoader old =
                            Thread.currentThread().getContextClassLoader();

                    // Instantiate a new instance of this filter and return it
                    Class<?> clazz = classLoader.getClassLoader().loadClass(listenerClass);
                    listener = (ServletContextListener) clazz.newInstance();

                    addContainerListener(listener);
                } catch (Throwable t) {
                    t.printStackTrace();
                    ok = false;
                }
            }
        }

        return (ok);
    }
}
