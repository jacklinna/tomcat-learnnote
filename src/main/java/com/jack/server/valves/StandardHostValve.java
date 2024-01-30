package com.jack.server.valves;

import com.jack.server.core.StandardContext;
import com.jack.server.core.StandardHost;
import com.jack.server.https.requests.HttpRequestImpl;
import com.jack.server.interfaces.Request;
import com.jack.server.interfaces.Response;
import com.jack.server.interfaces.ValveContext;

import javax.servlet.ServletException;
import java.io.IOException;

public class StandardHostValve extends ValveBase {
    @Override
    public void invoke(Request request, Response response, ValveContext context) throws IOException, ServletException {
        System.out.println("StandardHost Valve invoke()");

        String docbase = ((HttpRequestImpl)request).getDocbase();
        System.out.println("StandardHost valve invoke getdocbase:"+docbase);

        StandardHost host = (StandardHost)getContainer();
        StandardContext servletContext = host.getContext(docbase);

        servletContext.invoke(request,response);



    }
}
