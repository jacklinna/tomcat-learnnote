package com.jack.server.interfaces;

import javax.servlet.http.HttpSession;

public interface Session {
    public static final String SESSION_CREATED_EVENT = "createSession";
    public static final String SESSION_DESTROYED_EVENT = "destroySession";

    //设置session创建时间
    public long getCreationTime();
    public void setCreationTime(long time);

    //设置Session-id
    public String getId();
    public void setId(String id);

    //获取最后的时间
    public long getLastAccessedTime();

    //设置过期时间
    public int getMaxInactiveInterval();
    public void setMaxInactiveInterval(int interval);

    //设置是否新建标识
    public void setNew(boolean isNew);

    public HttpSession getSession();
    public void setValid(boolean isValid);
    public boolean isValid();

    public void access();
    public void expire();
    public void recycle();

    public String getInfo();
}
