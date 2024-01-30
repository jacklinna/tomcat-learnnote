package com.jack.server.core;

import com.jack.server.interfaces.*;

import javax.servlet.ServletException;
import java.io.IOException;
import java.security.ProtectionDomain;

public class StandardPipeline implements Pipeline {
    
    public StandardPipeline(){
        this(null);
    }
    public StandardPipeline(Container container){
        super();
        setContainer(container);
    }
    //这个basic valve 需要单独保存
    protected Valve basic = null;
    protected Container container = null;
    protected int debug = 0;
    protected String info="com.jack.server.core.StandardPipeline/1.0";

    protected Valve valves[] = new Valve[0];
    //一组valve，可以逐个调用
    //对于Pipeline的调用，也变成了启动 内部类的invokeNext

    // StandardContext[null]: Container created.
    // 这里忘记赋值了
    //TODO
    private void setContainer(Container container) {
        this.container = container;
    }

    @Override
    public Valve getBasic() {
        return this.basic;
    }

    @Override
    public void setBasic(Valve valve) {
        Valve old = this.basic;

        if(old == valve){
            return;
        }
        if(valve == null){
            return;
        }
        valve.setContainer(container);
        this.basic = valve;
    }

    @Override
    public void addValve(Valve valve) {
        //不同的pipe 有自己的valve
        synchronized (valves){
            Valve results[] = new Valve[valves.length +1];
            System.arraycopy(valves,0,results,0,valves.length);
            valve.setContainer(container);

            results[valves.length] = valve;
            valves = results;
        }
    }

    @Override
    public Valve[] getValves() {
        //保持数据最新，同步
        //basic 是最后一个 valve，特殊
        if(basic ==null){
            return (valves);
        }
        synchronized (valves){
            Valve results[] = new Valve[valves.length +1];
            System.arraycopy(valves,0,results,0,valves.length);

            results[valves.length] = basic;
            return (results);
        }
    }

    @Override
    public void removeValve(Valve valve) {
        synchronized (valves){
            int j=-1;
            for(int i=0;i<valves.length;i++){
                if(valve == valves[i]){
                    j=1;
                    break;
                }

            }

            if(j<0){
                return;
            }

            valve.setContainer(null);
            Valve results[] = new Valve[valves.length-1];

            int n=0;
            for(int i=0;i<valves.length;i++){
                if(i ==j){
                    continue;
                }
                results[n++] = valves[i];

            }
            valves = results;
        }
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        System.out.println("StandardPipeline invoke()");

        //继续调用context invoke，实现职责链
        (new StandardPipelineValueContext()).invokeNext(request,response);
    }

    protected void log(String message){
        Logger logger = null;

        if(container !=null){
            logger = container.getLogger();
        }

        if(logger !=null){
            logger.log("StandardPipeline["+container.getName()+"]:"+message);
        }else{
            System.out.println("StandardPipeline["+container.getName()+"]:"+message);
        }
    }

    protected void log(String message,Throwable e){
        Logger logger = null;

        if(container !=null){
            logger = container.getLogger();
        }

        if(logger !=null){
            logger.log("StandardPipeline["+container.getName()+"]:"+message,e);
        }else{
            System.out.println("StandardPipeline["+container.getName()+"]:"+message);
            e.printStackTrace();
        }
    }
    protected class StandardPipelineValueContext implements ValveContext {
        //是为了记录Valve编号
        protected int stage = 0;

        @Override
        public String getInfo() {
            return info;
        }

        @Override
        public void invokeNext(Request request, Response response) throws IOException, ServletException {
            System.out.println("StandardPipelineValueContext invokeNext");
            int subscript = stage;
            stage +=1;

            //根据编号来依次调用；
            //最后一个basic
            //所以 valve.invoke本质就是调用 ValveContext 的 invokeNext
            //一个个传递下去，所有链路就调用完毕

            //因为Tomcat 起点是connector,COntainer,所以pipeline需要加入其中
            if(subscript <valves.length){
                valves[subscript].invoke(request,response,this);

            }else if((subscript == valves.length) && (basic != null)){
                basic.invoke(request,response,this);
            }else{
                throw new ServletException("standardPipeline.noValve");
            }
        }
    }
}
