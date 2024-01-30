package test;


import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class Test2Servlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    //测试
    static int count = 0;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("Enter Another 222 doGet()");
        System.out.println("Parameter name:"+ request.getParameter("name"));

        Test2Servlet.count++;
        System.out.println(":::::::::call count::::::::::::::"+ Test2Servlet.count);
        if(Test2Servlet.count >2){
            response.addHeader("Connection","close");
        }

        HttpSession session = request.getSession(true);
        String user = "";
        if(session!=null){
             user= (String) session.getAttribute("user");
            System.out.println("get user from session:"+user);

            if(user ==null ||("").equals(user)){
                session.setAttribute("user","Jean");
            }
        }


        response.setCharacterEncoding("UTF-8");
        String doc = "<!DOCTYPE html> \n" +                "<html>\n"
                +                "<head><meta charset=\"utf-8\"><title>Test</title></head>\n"
                +                "<body bgcolor=\"#f0f0f0\">\n"
                +                "<h1 align=\"center\">"
                + "Hello World 你好"
                + "</h1>\n";
        response.getWriter().println(doc);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("Enter doPost()");
        System.out.println("Parameter name:"+ request.getParameter("name"));

        HttpSession session = request.getSession(true);
        String user = (String) session.getAttribute("user");
        System.out.println("get user from session:"+user);

        if(user ==null ||("").equals(user)){
            session.setAttribute("user","Jean");

        }

        response.setCharacterEncoding("UTF-8");
        String doc = "<!DOCTYPE html> \n" +                "<html>\n"
                +                "<head><meta charset=\"utf-8\"><title>Test</title></head>\n"
                +                "<body bgcolor=\"#f0f0f0\">\n"
                +                "<h1 align=\"center\">"
                + "Hello World 你好"
                + "</h1>\n";
        response.getWriter().println(doc);
    }
}
