package com.jack.server.filters;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description 包括描述，名称等信息，定义了相关信息的方法
 * 这个类的信息，可以使用Config 得到对应的Filter,然后进行初始化工作。
 * 这个Config 应该可以存在多个，所以需要使用chain
 */
public class FilterDef {

    private String description = null;
    private String displayName = null;
    private String filterClass = null;
    private String filterName = null;
    private String largeIcon = null;
    private String smallIcon = null;
    private Map<String,String> parameters = new ConcurrentHashMap<>();

    public String toString(){
        StringBuffer stringBuffer = new StringBuffer("FilterDef[");
        stringBuffer.append("filterName=");
        stringBuffer.append(this.filterName);
        stringBuffer.append(",filterClass=");
        stringBuffer.append(this.filterClass);
        stringBuffer.append("]");

        return stringBuffer.toString();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setFilterClass(String filterClass) {
        this.filterClass = filterClass;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getLargeIcon() {
        return largeIcon;
    }

    public void setLargeIcon(String largeIcon) {
        this.largeIcon = largeIcon;
    }

    public String getSmallIcon() {
        return smallIcon;
    }

    public void setSmallIcon(String smallIcon) {
        this.smallIcon = smallIcon;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public String getFilterName() {
        return filterName;
    }

    public Map<String, String> getParameterMap() {
        return parameters;
    }

    public String getFilterClass() {
        return filterClass;
    }
}
