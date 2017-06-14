package io.j1st.utils.http;

/**
 * Created by xiaopeng on 2017/1/5.
 */
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


public class JSModifiedJava {

    public void sayHello(String name){
        System.out.println("*************Hello***************"+name);
    }

//    public static void main(String[] args) {
//
//        //创建 List对象，并添加3个原用户
//        List<String> us = new ArrayList<String>();
//        us.add("xiaopeng 1");
//        us.add("xiaopeng 2");
//
//        //获得JS引擎
//        ScriptEngineManager manager = new ScriptEngineManager();
//        ScriptEngine engine = manager.getEngineByExtension("js");
//        String script = "";
//        //把List对象加入JS引擎
//        engine.put("usList", us);
//
//        {//用JS输出java对象的数据
//            System.out.println("用JS输出java对象的数据");
//            script = "var index; " + "var usname = usList.toArray(); "
//                    + "for (index in usname) { "
//                    + "print('原用户='+usname[index]); " + "}";
//
//            //执行脚本
//            try {
//                engine.eval(script);// 通过引擎调用getScript()
//            } catch (ScriptException e) {
//                e.printStackTrace();
//            }
//        }
//
//        {//JS可以对对象进行修改在,java代码可以得到修改后的
//            System.out.println("JS可以对对象进行修改在,java代码可以得到修改后的");
//            script = "usList.add(\"xiaopeng 3\"); "
//                    + "usList.add(\"xiaopeng 4\"); ";
//
//            //执行脚本
//            try {
//                engine.eval(script);// 通过引擎调用getScript()
//            } catch (ScriptException e) {
//                e.printStackTrace();
//            }
//
//            //java代码显示被JS修改后所有用户
//            for (String usname : us) {
//                System.out.println("所有用户 = " + usname);
//            }
//        }
//
//
//        //将要操作的对象加入引擎
//        engine.put("obj", new JSModifiedJava()) ;
//
//        {//在JS中甚至可以调用对象的方法
//            System.out.println("在JS中甚至可以调用对象的方法");
//            script = "obj.sayHello('xiaopeng')" ;
//
//            //执行脚本
//            try {
//                engine.eval(script);// 通过引擎调用getScript()
//            } catch (ScriptException e) {
//                e.printStackTrace();
//            }
//
//        }
//
//
//
////        //根据JavaScript名获取一个脚本引擎实例
////        ScriptEngine engine2 = manager.getEngineByName("JavaScript");
////        try {
////            engine2.eval("print('Hello xiaopeng ...')");
////        } catch (ScriptException e) {
////            e.printStackTrace();
////        }
//
//    }

}
