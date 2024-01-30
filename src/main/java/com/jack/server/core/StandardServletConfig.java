package com.jack.server.core;


import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class StandardServletConfig implements ServletConfig{

    private Map<String,String> servletInitParameterMap = new ConcurrentHashMap<>();
    private ServletContext servletContext;
    private String servletName;

    public StandardServletConfig(String servletName,ServletContext servletContext
            ,Map<String,String> servletInitParameterMap){
        this.servletInitParameterMap = servletInitParameterMap;
        this.servletContext = servletContext;
        this.servletName = servletName;
    }
    @Override
    public String getServletName() {
        return servletName;
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public String getInitParameter(String s) {
        return servletInitParameterMap.get(s);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return null;
    }
}
