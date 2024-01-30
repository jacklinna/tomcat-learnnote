package test;


import com.jack.server.events.ContainerEvent;
import com.jack.server.events.ContainerListener;
import com.jack.server.events.ContainerListenerDef;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class TestListener implements ContainerListener {


    @Override
    public void containerEvent(ContainerEvent event) {
        System.out.println(event);
    }
}
