package com.jack.server.sessions;

import com.jack.server.events.SessionEvent;
import com.jack.server.events.SessionListener;
import com.jack.server.interfaces.Session;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Descripton Session改为StandardSession
 */
public class StandardSession implements HttpSession, Session {
    //逻辑与ContainerEvent一致
    //增加监听
    private transient ArrayList<SessionListener> listeners = new ArrayList<>();

    public void addSessionListener(SessionListener listener){
        synchronized (listeners){
            listeners.add(listener);
        }
    }
    public void removeSessionListener(SessionListener listener){
        synchronized (listeners){
            listeners.remove(listener);
        }
    }

    public void fireSessionEvent(String type,Object data){
        if(listeners.size()<1){
            return;
        }

        SessionEvent event = new SessionEvent(this,type,data);

        //虽然是 静态，但是可以直接使用list 赋值给数组；
        SessionListener list[] = new SessionListener[0];

        synchronized (listeners){
            list = (SessionListener[]) listeners.toArray(list);
        }
        for(int i=0;i<list.length;i++){
            ((SessionListener) list[i]).sessionEvent(event);
        }
    }

    //id
    private String sessionid;
    //创建时间 long
    private long creationTime;
    //是否有效
    private boolean valid;
    //属性
    private Map<String,Object> attributes = new ConcurrentHashMap<>();

    @Override
    public long getCreationTime() {
        return this.creationTime;
    }

    @Override
    public String getId() {
        return this.sessionid;
    }

    @Override
    public long getLastAccessedTime() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public void setMaxInactiveInterval(int i) {

    }

    @Override
    public void setNew(boolean isNew) {

    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public int getMaxInactiveInterval() {
        return 0;
    }

    @Override
    public HttpSessionContext getSessionContext() {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    @Override
    public Object getValue(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(this.attributes.keySet());
    }

    @Override
    public String[] getValueNames() {
        return null;
    }

    @Override
    public void setAttribute(String name, Object value) {
        this.attributes.put(name,value);
    }

    @Override
    public void putValue(String s, Object o) {

    }

    @Override
    public void removeAttribute(String s) {
        this.attributes.remove(s);
    }

    @Override
    public void removeValue(String s) {

    }

    @Override
    public void invalidate() {
        this.valid = false;
    }

    @Override
    public boolean isNew() {
        return false;
    }

    public void setValid(boolean b){
        this.valid = b;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public void access() {

    }

    @Override
    public void expire() {

    }

    @Override
    public void recycle() {

    }

    @Override
    public String getInfo() {
        return null;
    }

    public void setCreationTime(long currentTimeMillis){
        this.creationTime = currentTimeMillis;
    }

    public void setId(String sessionid){
        this.sessionid = sessionid;
    }
}
