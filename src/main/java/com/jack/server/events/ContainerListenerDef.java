package com.jack.server.events;

import com.jack.server.interfaces.Container;

import java.util.EventObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 与FilterDef 类似，只是进行一些属性定义。
 */
public class ContainerListenerDef{

    private String description = null;
    public String diaplayName = null;
    public String listenerClass = null;
    public String listenerName = null;
    public Map<String,String> parameters = new ConcurrentHashMap<>();

    public Map<String,String> getParameters(){
        return parameters;
    }

    public void addInitParameters(String name,String value){
        parameters.put(name,value);
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDiaplayName() {
        return diaplayName;
    }

    public void setDiaplayName(String diaplayName) {
        this.diaplayName = diaplayName;
    }

    public String getListenerClass() {
        return listenerClass;
    }

    public void setListenerClass(String listenerClass) {
        this.listenerClass = listenerClass;
    }

    public String getListenerName() {
        return listenerName;
    }

    public void setListenerName(String listenerName) {
        this.listenerName = listenerName;
    }

    public String toString(){
        StringBuffer stringBuffer = new StringBuffer("ListenerDef[");
        stringBuffer.append("listenerName=");
        stringBuffer.append(listenerName);
        stringBuffer.append(",listenerClass=");
        stringBuffer.append(listenerClass);
        stringBuffer.append("]");

        return stringBuffer.toString();
    }
}
