package com.jack.server.events;

import com.jack.server.interfaces.Container;
import com.jack.server.interfaces.Wrapper;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.EventObject;

public class InstanceEvent extends EventObject {

    private static final String BEFORE_INIT_EVENT = "beforeInit";
    private static final String AFTER_INIT_EVENT = "afterInit";

    private static final String BEFORE_SERVICE_EVENT = "beforeService";
    private static final String AFTER_SERVICE_EVENT = "afterService";

    private static final String BEFORE_DESTROY_EVENT = "beforeDestroy";
    private static final String AFTER_DESTROY_EVENT = "afterDestroy";

    private static final String BEFORE_DISPATCH_EVENT = "beforeDispatch";
    private static final String AFTER_DISPATCH_EVENT = "afterDispatch";

    private static final String BEFORE_FILTER_EVENT = "beforeFilter";
    private static final String AFTER_FILTER_EVENT = "afterFilter";

    //以上只是一些常量
    //下面是内部字段
    private Throwable e = null;
    private Filter filter = null;
    private ServletRequest request = null;
    private ServletResponse response = null;

    private Servlet servlet = null;
    private String type = null;
    private Wrapper wrapper = null;

    public Throwable getE() {
        return e;
    }

    public Filter getFilter() {
        return filter;
    }

    public ServletRequest getRequest() {
        return request;
    }

    public ServletResponse getResponse() {
        return response;
    }

    public Servlet getServlet() {
        return servlet;
    }

    public Wrapper getWrapper() {
        return wrapper;
    }


    //继承EventObject 必须调用 super
    public InstanceEvent(Wrapper wrapper,Filter filter,String type){
        super(wrapper);
        this.wrapper = wrapper;
        this.filter = filter;
        this.servlet = null;

        this.type = type;
    }

    public InstanceEvent(Wrapper wrapper,Filter filter,String type,Throwable e){
        super(wrapper);
        this.wrapper = wrapper;
        this.filter = filter;
        this.servlet = null;

        this.type = type;
        this.e = e;
    }

    public InstanceEvent(Wrapper wrapper,Filter filter,String type,ServletRequest request,ServletResponse response){
        super(wrapper);
        this.wrapper = wrapper;
        this.filter = filter;
        this.servlet = null;

        this.type = type;
        this.request = request;
        this.response = response;
    }

    public InstanceEvent(Wrapper wrapper,Filter filter,String type,ServletRequest request
            ,ServletResponse response,Throwable e){
        super(wrapper);
        this.wrapper = wrapper;
        this.filter = filter;
        this.servlet = null;

        this.type = type;
        this.request = request;
        this.response = response;

        this.e = e;
    }


}
