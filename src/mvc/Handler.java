package mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * 用来获取配置问阿金
 */
public class Handler {

    //单列模式
    private static Handler Handler;

    private Handler(String path) {
        this.setPath(path);

    }

    ;

    /**
     * @说明: 单列 创建对象
     */
    static Handler newInstance(String path) {
        if (Handler == null) {
            Handler = new Handler(path);
        }
        return Handler;
    }


    /**
     * @说明:存放类的全限定类名
     */
    private Map<String, String> classNameMap;
    /**
     * @说明:存放实体对象,实现单列
     */
    private Map<String, Object> objectMap;
    /**
     * @说明:用来存储对象里的所有方法
     */
    private Map<Object, Map<String, Method>> methodMap = new HashMap();


    /**
     * 找到并加载配置文件
     *
     * @param path
     * @throws IOException
     */
    void setPath(String path) {

        if (classNameMap == null) {
            classNameMap = new HashMap();
        }
        Properties pro = new Properties();
        try {
            pro.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Enumeration en = pro.propertyNames();
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            String value = pro.getProperty(key);
            classNameMap.put(key, value);
        }
    }

    /**
     * @说明:扫描包
     * scanPackage 包的路径
     */

    void scanAnnotation(String[] scanPackages){

        for (String scanPackage: scanPackages ) {





        }

    }







    /**
     * 通过key  获取value
     *
     * @param key
     * @return
     */
    String getValue(String key) {
        //查看路径是否已上传
        if (classNameMap == null) {
            throw new RuntimeException("没有传入配置文件");
        }
        return classNameMap.get(key);
    }

    /**
     * @说明:根据简单类名，创建对象实体
     */
    <T> T getBean(String key) {
        if (objectMap == null) {
            objectMap = new HashMap();
        }
        T obj = (T) objectMap.get(key);
        try {
            if (obj == null) {
                //创建真实类
                obj = (T) Class.forName(this.getValue(key)).newInstance();
                objectMap.put(key, obj);
                //将真实类的所有方法存入方法集合
                Class clazz = obj.getClass();
                Method[] methods = clazz.getDeclaredMethods();
                Map map = new HashMap();
                methodMap.put(obj, map);
                for (int i = 0; i < methods.length; i++) {
                    map.put(methods[i].getName(), methods[i]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }


    /**
     * @说明: 通过对象和方法名, 获取方法
     */
    Method getMethod(Object obj, String methodName) {
        System.out.println(methodName);
        Method method = methodMap.get(obj).get(methodName);
        if (method == null) {
            throw new RuntimeException("没有获取到方法");
        }
        return method;
    }


    //小弟1  注解类型的参数 获取和转换
    private Object paramAnnSplit( Param param,HttpServletRequest request,Class parameterType){
        //获取key
        String key = param.value();
        //获取value
        String value = request.getParameter(key);


        //判断参数的类型并且转型
        if (parameterType == String.class ){
            return  value;
        }else if(parameterType == int.class || parameterType == Integer.class){
            return new Integer(value);
        }else if(parameterType == long.class || parameterType == Long.class){
            return new Long(value);
        }else {
            throw new RuntimeException("还不支持此类型参数转换");
        }
    }


    /**
     * @说明: 将方法传递过来, 获取方法需要的参数类型和个数
     */
    Object[] methodParams( Method method,HttpServletRequest request,HttpServletResponse response) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        //获取所有参数
        Parameter[] parameters = method.getParameters();
        Object[] resultsValue = new Object[parameters.length];
        if (parameters.length<1 || parameters==null){   //方法不需要参数
            return null;
        }

        for (int i = 0 ; i < parameters.length ; i++) {
            //判断这个参数上有没有哦注解
            Param param = parameters[i].getAnnotation(Param.class);
            //获取参数的类型
            Class parameterType = parameters[i].getType();
            if (param != null){ //有注解
                //将注解里的参数名获取 并且将需要的值存入 resultsValue
                resultsValue[i]=this.paramAnnSplit(param,request,parameterType);
            }else { //证明是 其他类型 map 或者对象


                if (parameterType == HttpServletRequest.class){
                    resultsValue[i]=request;
                    continue;
                }
                if (parameterType == HttpServletResponse.class){
                    resultsValue[i]=response;
                    continue;
                }
                if (parameterType == Map.class || parameterType == List.class){
                    throw new RuntimeException("不支持的参数类型");
                }

                //创建参数需要的对象
                Object obj = parameterType.newInstance();
                if (obj instanceof Map){//是个MAP集合
                    resultsValue[i] = obj;
                    Enumeration<String> en = request.getParameterNames();
                    while (en.hasMoreElements()){
                        String key = en.nextElement();
                        String value = request.getParameter(key);
                        ((Map) obj).put(key,value);
                    }
                }else { //是个对象类型
                    //获取所有属性
                    Field[] fields = parameterType.getDeclaredFields();
                    //循环
                    for (int j = 0; j <fields.length ; j++) {
                        //获取当前属性类型
                        Class fieldType = fields[j].getType();
                        //获取属性的Name
                        String fieldName = fields[j].getName();
                        //获取属性对应的setName
                        String tou = fieldName.toUpperCase().substring(0,1);
                        String wei = fieldName.substring(1);
                        String methodName = "set"+tou+wei;
                        System.out.println("获取到set方法名:"+fieldName);
                        //获取当前属性对应的set方法
                        Method setMethod = parameterType.getMethod(methodName, fieldType);
                        //将获取到的参数 转换成 属性对应的类型
                        String requestParameter = request.getParameter(fieldName);
                        Object methodParam = fieldType.getConstructor(String.class).newInstance(requestParameter);//获取构造方法
                        //执行set方法
                        setMethod.invoke(obj,methodParam);
                    }
                    resultsValue[i]=obj;
                }
            }
        }
        return resultsValue;
    }


}
