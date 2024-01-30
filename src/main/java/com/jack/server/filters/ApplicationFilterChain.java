package com.jack.server.filters;

import com.jack.server.facades.HttpRequestFacade;
import com.jack.server.facades.HttpResponseFacade;
import com.jack.server.https.requests.HttpRequestImpl;
import com.jack.server.https.responses.HttpResponseImpl;
import com.jack.server.interfaces.Context;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class ApplicationFilterChain implements FilterChain {

    public ApplicationFilterChain(){
        super();
    }

    private ArrayList<ApplicationFilterConfig> filters = new ArrayList<>();
    private Iterator<ApplicationFilterConfig> iterator = null;

    private Servlet servlet = null;



    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        System.out.println("Filter Chain doFilter()");
        internalDoFilter(request,response);
    }

    private void internalDoFilter(ServletRequest request,ServletResponse response){
        if(this.iterator == null){
            this.iterator = filters.iterator();
        }

        if(this.iterator.hasNext()){
            //获取到 一个Filter
            ApplicationFilterConfig filterConfig = (ApplicationFilterConfig) iterator.next();

            Filter filter = null;
            try{
                //执行filter doFilter
                //这个也是职责链的模式
                filter = filterConfig.getFilter();
                System.out.println("Filter doFilter()");

                //根据规范，filter 需要在执行完自己的逻辑后，再次调用 FilterChain的doFilter。
                //这样就会回到 internalDoFilter方法，实现循环下一个
                filter.doFilter(request,response,this);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (ServletException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        try{
            HttpServletRequest requestFacade = new HttpRequestFacade((HttpRequestImpl) request);
            HttpServletResponse responseFacade = new HttpResponseFacade((HttpResponseImpl) response);

            //执行完Filter, 执行Servlet，这也是Filter Chain字段完成，service()成为CHain之后的一个环节
            //所以 Processor,Container 不需要显示 调用service（）

            //这样就需要在每一层都加入Filter
            //只需要在StandardContext这一层保存，重新启动在Bootstrap 加上，调用的程序在StandardWrapperValve；

            servlet.service(requestFacade,responseFacade);
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addFilter(ApplicationFilterConfig filterConfig){
        this.filters.add(filterConfig);
    }

    public void release(){
        this.filters.clear();
        this.iterator = iterator;
        this.servlet = null;
    }

    public void setServlet(Servlet servlet){
        this.servlet = servlet;
    }

}
