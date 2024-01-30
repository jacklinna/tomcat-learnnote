package com.jack.server.startup;

import com.jack.server.core.StandardContext;
import com.jack.server.core.StandardHost;
import com.jack.server.events.ContainerListenerDef;
import com.jack.server.filters.FilterDef;
import com.jack.server.filters.FilterMap;
import com.jack.server.https.HttpConnector;
import com.jack.server.interfaces.Loader;
import com.jack.server.interfaces.Logger;
import com.jack.server.loaders.CommonLoader;
import com.jack.server.loaders.WebappClassLoader;
import com.jack.server.logger.FileLogger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.print.Doc;
import javax.sound.sampled.Port;
import javax.swing.*;
import java.io.File;


/**
 * @Descripton 在Tomcat中，HttpServer 改名为 BootStrap
 */
public class BootStrap {
    public static final String JEAN_HOME  = System.getProperty("user.dir");
    public static  String WEB_ROOT  = System.getProperty("user.dir");

    public static final String JAVA_ROOT  = System.getProperty("user.dir") + File.separator
            + "src"+ File.separator + "main"+ File.separator + "java";
    public static final String TARGET_ROOT  = System.getProperty("user.dir") + File.separator
            + "target"+ File.separator + "classes";

    public static int port = 8090;

    //添加 日志
    private static int debug = 0;

    public static void main(String[] args) {
        System.out.println("JeanTomcat is On.");
        if(debug >=1){
            log(".... Startup .....");
        }


        //扫描 配置文件
        String file = JEAN_HOME + File.separator + "conf" +File.separator
                +"server.xml";
       //SAXR
        SAXReader reader = new SAXReader();

        Document document;

        try{
            document = reader.read(file);

            Element root = document.getRootElement();
            Element connectorElement = root.element("Connector");

            Attribute portAttribute = connectorElement.attribute("port");
            port =  Integer.parseInt(portAttribute.getText());

            Element hostElement = root.element("Host");
            Attribute appbaseAttribute = hostElement.attribute("appBase");

            WEB_ROOT = WEB_ROOT+File.separator+appbaseAttribute.getText();


        } catch (DocumentException e) {
            e.printStackTrace();
        }


        //设置一个系统属性
        System.setProperty("jean.home",JEAN_HOME);
        System.setProperty("jean.base",WEB_ROOT);

//        HttpConnector connector = new HttpConnector();
//        connector.start();
        HttpConnector connector = new HttpConnector();
        //这里由StandardContext 改为 StandardHost
        //Host 代表总容器
        //启动的时候，启动COnnector ,host
        //host 就是加载 Tomcat自身需要的类

        //所以 request发送invoke ，就是从host开始
        StandardHost container = new StandardHost();

        //很多逻辑 全都放在 Host
        //WebappClassLoader loader = new WebappClassLoader();
        //启动后，优先加载 Tomcat自身的 需要的类
        Loader loader = new CommonLoader();

        container.setLoader(loader);
        loader.start();
        connector.setContainer(container);
        container.setConnector(connector);

        //容器启动
        container.start();
        //连接器启动
        connector.start();

    }

    private static void log(String msg) {
        System.out.println("Bootstrap:");
        System.out.println(msg);
    }
    private static void log(String msg,Throwable throwable) {
        //System.out.println("Bootstrap:");
        log(msg);
        throwable.printStackTrace(System.out);
    }

}
