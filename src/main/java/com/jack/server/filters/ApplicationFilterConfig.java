package com.jack.server.filters;

import com.jack.server.interfaces.Context;
import com.jack.server.interfaces.Loader;
import com.jack.server.loaders.WebappClassLoader;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.*;

public class ApplicationFilterConfig implements FilterConfig {
    private Context context = null;
    private Filter filter = null;
    private FilterDef filterDef= null;


    public ApplicationFilterConfig(Context context, FilterDef filterDef) throws ClassCastException,ClassNotFoundException
    ,IllegalAccessException,InstantiationException, ServletException {
        super();
        this.context = context;
        setFilterDef(filterDef);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("ApplicationFilterConfig[");
        sb.append("name=");
        sb.append(filterDef.getFilterName());
        sb.append(", filterClass=");
        sb.append(filterDef.getFilterClass());
        sb.append("]");
        return (sb.toString());
    }


    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Filter getFilter() throws ClassCastException,ClassNotFoundException
            ,IllegalAccessException,InstantiationException, ServletException{
        if(this.filter !=null){
            return this.filter;
        }

        String filterClass = filterDef.getFilterClass();
        //获取 Filter 实现类
        Loader classLoader = null;
        classLoader = context.getLoader();

        ClassLoader old = Thread.currentThread().getContextClassLoader();

        Class clazz = classLoader.getClassLoader().loadClass(filterClass);
        this.filter = (Filter) clazz.newInstance();
        filter.init(this);

        return this.filter;
    }

    public void release(){
        if(this.filter != null){
            filter.destroy();
        }
        this.filter = null;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public FilterDef getFilterDef() {
        return filterDef;
    }

    public void setFilterDef(FilterDef filterDef) throws ClassCastException,ClassNotFoundException
            ,IllegalAccessException,InstantiationException, ServletException{
        this.filterDef = filterDef;

        if(filterDef == null){
            //释放之前的所有 过滤器实例
            if(this.filter != null){
                this.filter.destroy();
            }
            this.filter = null;
        }else{
            Filter filter = getFilter();
        }
    }

    @Override
    public String getFilterName() {
        return filterDef.getFilterName();
    }

    @Override
    public ServletContext getServletContext() {
        return this.context.getServletContext();
    }

    @Override
    public String getInitParameter(String name) {
        Map<String,String> map = filterDef.getParameterMap();
        if(map == null){
            return null;
        }else {
            return map.get(name);
        }
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        Map<String,String> map = filterDef.getParameterMap();
        if(map == null){
            return Collections.enumeration(new ArrayList<String>());
        }else {
            return Collections.enumeration(map.keySet());
        }
    }
}
