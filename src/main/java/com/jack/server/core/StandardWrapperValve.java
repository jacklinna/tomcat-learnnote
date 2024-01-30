package com.jack.server.core;

import com.jack.server.facades.HttpRequestFacade;
import com.jack.server.facades.HttpResponseFacade;
import com.jack.server.filters.ApplicationFilterChain;
import com.jack.server.filters.ApplicationFilterConfig;
import com.jack.server.filters.FilterDef;
import com.jack.server.filters.FilterMap;
import com.jack.server.https.requests.HttpRequestImpl;
import com.jack.server.https.responses.HttpResponseImpl;
import com.jack.server.interfaces.*;
import com.jack.server.valves.ValveBase;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Description 修改名字 ServletWrapper --> StandardWrapper
 */
public class StandardWrapperValve extends ValveBase{
    //添加 filter
    private FilterDef filterDef = null;

    @Override
    public void invoke(Request request, Response response, ValveContext context) throws IOException, ServletException {
        System.out.println("StandardWrapperValve invoke()");
        // 创建 filter Chain 在调用filter 然后调用 servlet



        HttpServletRequest requestFacade = new HttpRequestFacade((HttpRequestImpl) request);
        HttpServletResponse responseFacade = new HttpResponseFacade((HttpResponseImpl) response);

        Servlet instance = ((StandardWrapper)getContainer()).getServlet();

        //添加filter chain
        ApplicationFilterChain filterChain = createFilterChain(request,instance);

        //chain 里面会自动调用service ，所以不需要 再显式调用
//        if(instance != null){
//            instance.service(requestFacade,responseFacade);
//        }
        if((instance != null) && (filterChain != null)){
            filterChain.doFilter((ServletRequest)request,(ServletResponse)response);
        }

        filterChain.release();
    }

    //根据context 信息 挑选出 符合 模式的filter 创建filterChain
    private ApplicationFilterChain createFilterChain(Request request, Servlet servlet) {
        System.out.println("createFilterChain()");

        if(servlet == null){
            return null;
        }
        ApplicationFilterChain filterChain = new ApplicationFilterChain();
        filterChain.setServlet(servlet);

        StandardWrapper wrapper = (StandardWrapper) getContainer();
        StandardContext context = (StandardContext) wrapper.getParent();

        //从Context 拿到filter信息
        FilterMap filters[] = context.findFilterMaps();
        if((filters == null) || (filters.length == 0)){
            return filterChain;
        }
        //要匹配 路径
        String requestPath = null;
        if(request instanceof  HttpServletRequest){
            String contextPath = "";
            String requestURI = ((HttpRequestImpl) request).getUri();

            if(requestURI.length() >= contextPath.length()){
                requestPath = requestURI.substring(contextPath.length());
            }
        }
        //要匹配 servlet 名字
        String servletName = wrapper.getName();
        //寻找
        int n = 0;
        for(int i=0;i < filters.length;i++){
            if(!matchFiltersURL(filters[i],requestPath)){
                continue;
            }
            ApplicationFilterConfig filterConfig = (ApplicationFilterConfig) context.findFilterConfig(
                    filters[i].getFilterName());
            if(filterConfig == null){
                continue;
            }
            filterChain.addFilter(filterConfig);
            n++;
        }
        //遍历 filters ，找到，放入 chain
        for(int i=0;i<filters.length;i++){
            if(!matchFiltersServlet(filters[i],servletName)){
                continue;
            }
            ApplicationFilterConfig filterConfig = (ApplicationFilterConfig)
                    context.findFilterConfig(filters[i].getFilterName());
            if(filterConfig == null){
                continue;
            }
            filterChain.addFilter(filterConfig);
            n++;
        }

        return filterChain;
    }

    private boolean matchFiltersServlet(FilterMap filter, String servletName) {
        if(servletName == null){
            return false;
        }else {
            return (servletName.equals(filter.getServletName()));
        }
    }

    //字符串 匹配 filter 过滤路径
    private boolean matchFiltersURL(FilterMap filter, String requestPath) {
        if(requestPath == null){
            return false;
        }
        String testPath = filter.getUrlPattern();

        if(testPath == null){
            return false;
        }

        if(testPath.equals(requestPath)){
            return true;
        }

        if(("/*").equals(testPath)){
            return true;
        }
        if(testPath.endsWith("/*")){
            //这是通配符
            String comparePath = requestPath;
            while(true){
                //截取，
                if(testPath.equals(comparePath +"/*")){
                    return true;
                }
                int slash = comparePath.lastIndexOf('/');
                if(slash <0){
                    break;
                }
                comparePath = comparePath.substring(0,slash);
            }

            return false;
        }

        if(testPath.startsWith("*.")){
            int slash = requestPath.lastIndexOf('/');
            int period = requestPath.lastIndexOf('.');
            //先寻找 /和.
            if((slash >= 0) && (period > slash)){
                return (testPath.equals("*." + requestPath.substring(period +1)));
            }
        }
        return false;
    }

}
