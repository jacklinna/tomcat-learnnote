package com.jack.server.events;

import com.jack.server.interfaces.Session;
import com.jack.server.interfaces.Wrapper;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.EventObject;

public class SessionEvent extends EventObject {


    //以上只是一些常量
    //下面是内部字段
    private Object data = null;
    private Session session = null;
    private String type = null;




    //继承EventObject 必须调用 super
    public SessionEvent(Session session, String type, Object data){
        super(session);
        this.session = session;
        this.data = data;

        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public Session getSession() {
        return session;
    }

    public String getType() {
        return type;
    }
}
