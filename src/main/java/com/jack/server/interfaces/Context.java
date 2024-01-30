package com.jack.server.interfaces;

import javax.servlet.ServletContext;

public interface Context extends Container{
    public static final String RELOAD_EVENT = "reload";

    public String getDisplayName();
    public void setDisplayName(String displayName);

    public String getDocBase();
    public void setDocBase(String docBase);

    public String getPath();
    public void setPath(String path);

    public ServletContext getServletContext();

    public int getSessionTimeout();
    public void setSessionTimeout(int timeout);

    public String getWrapperClass();
    public void setWrapperClass(String WrapperClass);

    public Wrapper createWrapper();
    public String findServletMapping(String patter);
    public String[] findServletMappings();

    public void reload();
}
