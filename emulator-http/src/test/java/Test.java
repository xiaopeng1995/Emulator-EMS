/**
 * Created by xiaopeng on 2017/4/10.
 */
public class Test {
    public static void main(String[] args) throws  Exception{
        String msg="\\u4f60\\u4eec\\u90fd\\u662f\\u50bb\\u903c";

                System.out.println("使用GBK解码..." + new String(msg.getBytes(),"GBK"));
                System.out.println("使用ISO8859-1解码..." + new String(msg.getBytes(),"ISO8859-1"));
                System.out.println("使用UTF8解码..." + new String(msg.getBytes(),"UTF-8"));
        byte[] b ={55, 98, 99};
        System.out.println(new String(b));
    }
}
