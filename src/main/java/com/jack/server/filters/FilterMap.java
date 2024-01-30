package com.jack.server.filters;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description 包括描述，名称等信息，定义了相关信息的方法
 * 这个类的信息，可以使用Config 得到对应的Filter,然后进行初始化工作。
 * 这个Config 应该可以存在多个，所以需要使用chain
 */
public final class FilterMap {


    private String filterName = null;
    private String servletName = null;
    private String urlPattern = null;

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getServletName() {
        return servletName;
    }

    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public String toString(){
        StringBuffer stringBuffer = new StringBuffer("FilterMap[");
        stringBuffer.append("filterName=");
        stringBuffer.append(this.filterName);
        if(servletName != null){
            stringBuffer.append(",servletName=");
            stringBuffer.append(this.servletName);
        }
        if(urlPattern != null){
            stringBuffer.append(",urlPattern=");
            stringBuffer.append(this.urlPattern);
        }
        stringBuffer.append("]");

        return stringBuffer.toString();
    }


}
