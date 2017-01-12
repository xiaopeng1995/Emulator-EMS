/**
 * Created by xiaopeng on 2016/12/13.
 */



import io.j1st.data.entity.Registry;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


enum Shrubbery {GROUND, CRAWING, HANGING}

public class TestMethods {
    private static Logger log = LoggerFactory.getLogger(TestMethods.class);

    @Test
    public void testLogBack() {
        // TODO Auto-generated method stub
        // file(内存)----输入流---->【程序】----输出流---->file(内存)
        File file = new File("F:/temp", "product_agent.properties");
        try {
            file.createNewFile(); // 创建文件
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // 向文件写入内容(输出流)
        String str = "agentud=dssdfsdfsdfffff";
        byte bt[] ;
        bt = str.getBytes();
        try {
            FileOutputStream in = new FileOutputStream(file);
            try {
                in.write(bt, 0, bt.length);
                in.close();
                 boolean success=true;
                 System.out.println("写入文件成功");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            // 读取文件内容 (输入流)
            FileInputStream out = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(out);
            int ch = 0;
            while ((ch = isr.read()) != -1) {
                System.out.print((char) ch);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

}