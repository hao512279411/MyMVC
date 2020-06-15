package controller;

import domain.Atm;
import mvc.Param;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public class AtmController {


    public String login(@Param("name") String name){
        System.out.println("login运行了");
        System.out.println("获取到了name="+name);
        return "test.jsp";
    }
    public void login02(HttpServletRequest request, HttpServletResponse response){
        System.out.println("login02运行了");
        System.out.println(request.getParameter("id"));
        response.getStatus();
    }
}
