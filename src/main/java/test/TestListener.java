package test;


import com.jack.server.events.ContainerEvent;
import com.jack.server.events.ContainerListener;

public class TestListener implements ContainerListener {


    @Override
    public void containerEvent(ContainerEvent event) {
        System.out.println(event);
    }
}
