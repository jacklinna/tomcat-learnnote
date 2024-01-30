package com.jack.server.interfaces;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

public interface Wrapper {
    //Servlet包装
    public int getLoadOnStartup();
    public void setLoadOnStartup();

    public String getServletClass();
    public void setServletClass(String servletClass);

    public void addInitParameter(String name,String value);

    public Servlet allocate() throws ServletException;

    public String findInitParameter(String name);
    public String[] findInitParameters();
    public void removeInitParameter(String name);

    public void load() throws ServletException;
}
