package mvc;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;


public class LoginController extends HttpServlet {
    private Handler handler;

    public void init() throws ServletException {
        handler = Handler.newInstance("test.properties");
        //设置配置文件路径


    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {

            //1.解析字符串获取
            String requestURI = request.getRequestURI();
            String uri = requestURI.substring(requestURI.lastIndexOf("/") + 1);
            //获取  类名简写
            String className = uri.substring(0, uri.indexOf("."));
            //获取请求的 方法名 简写
            String methodName= request.getParameter("method");

            //2.通过 类名简写  找到真实的类 (并且对象的所有方法 存入method集合)
            Object obj = handler.getBean(className);

            //3.找到要执行的方法
            Method method = handler.getMethod(obj,methodName);
            //4.找到方法 需要的参数
            Object[] objects = handler.methodParams(method,request,response);
            //5.执行方法
            String results = (String)method.invoke(obj,objects);
            System.out.println("获取到方法的返回值："+results);
            //方法的返回值 转发或者重定向
//            if (results != null) {
//                request.getRequestDispatcher(results).forward(request, response);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}