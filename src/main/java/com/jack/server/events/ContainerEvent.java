package com.jack.server.events;

import com.jack.server.interfaces.Container;

import java.util.EventObject;

public class ContainerEvent extends EventObject {

    private Container container = null;
    private Object data = null;
    private String type = null;

    public ContainerEvent(Container container,String type,Object data){
        super(container);
        this.container = container;
        this.type = type;
        this.data = data;
    }

    public Container getContainer() {
        return container;
    }

    public Object getData() {
        return data;
    }

    public String getType() {
        return type;
    }

    public String toString(){
        return ("ContainerEvent['"+getContainer()+","+getType()+","+getData()+"']");
    }
}
