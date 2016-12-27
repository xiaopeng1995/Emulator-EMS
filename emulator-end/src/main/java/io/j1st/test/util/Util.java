package io.j1st.test.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

//import com.fasterxml.uuid.Generators;
//import com.fasterxml.uuid.impl.NameBasedGenerator;
//import com.google.gson.Gson;
//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.MultiFormatWriter;
//import com.google.zxing.client.j2se.MatrixToImageWriter;
//import com.google.zxing.common.BitMatrix;

public class Util {

    public static String IOPN = null;

    // 保存总功率值文件
    public static String proFileName = "top.properties";

    // 默认TCP连接地址配置KEY
    public static String pro_host_key = "tcpDefaultHost";

    /**
     * 测试主函数
     * 
     * @param args
     */
    public static void main(String[] args) {
        // byte[] temp = DataParse.encodeRemainingLength(21);
        // //System.out.println(temp.length);
        // byte[] in = "{\"a\":\"aaa\",\"b\":\"bbb\"}".getBytes();
        // try {
        // int x = DataParse.decodeRemainingLength(DataParse.byteMerger(temp,
        // in));
        // System.out.println(x);
        // int len = DataParse.encodeRemainingLength(x).length;
        // System.out.println(len);
        // Map data = DataParse.decode(DataParse.byteMerger(temp, in));
        // System.out.println(data.get("b"));
        // } catch (Exception e) {
        // e.printStackTrace();
        // }

        // public String getIntCode(){

        // String code = "C3, A7, 00, 00, 20, 40, 20, 00, 00, 20, 00, 05, 0F";
        // List<Integer> list = new ArrayList<Integer>();
        // for(String s : code.split(",")) {
        // list.add(Integer.parseInt(s.trim(), 16));
        // }
        // String s = JSON.toJSONString(list);
        //
        //
        // String ts =
        // "{\"iopn\":\"0F01D0DF9A5883ED\", \"pv\": \"v1.0\",
        // \"inc\":\"cfc\",\"fnc\": { \"itid\":1 ,\"icode\":"+s+"}}";
        //
        // Map data = JSON.parseObject(ts);
        // Map fnc = (Map)data.get("fnc");
        // System.out.println(fnc);
        // System.out.println(fnc.get("icode"));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
        Date d = new Date(1449648060025l);
        System.out.println(df.format(d));

        // char[] arr = new char[6];
        // addToCharArr(arr ,'A');
        // addToCharArr(arr ,'b');
        // addToCharArr(arr ,'c');
        // addToCharArr(arr ,'d');
        //
        // for(char c : arr)
        // System.out.println(c);
        // System.out.println(getIopn());

        // getNetCode();

        // System.out.println(System.currentTimeMillis());

    }

    /**
     * 测试从JSON中取数据
     */
    public static void getDataByJson() {
        String json = "{  \"iopn\": \"xxxxxxv1.0.12000003\",    \"pv\": \"v1.0.1\",    \"token\": \"e52f1ds55e2f22a15e3s68e1f3w81s15w97\",    \"inc\": \"ini\",    \"qos\": \"0\",    \"bif\": {        \" dsn\": \"my plug-in\",        \" fmv \": \"v1.0.2\"    },    \"fnc\": {        \"pwr\": \"on\",        \"swh\": \"on\",        \"vlt\": \"220V\",        \"cut\": \"10A\",        \"tc\": \"0\",        \"ts\": \"2014-04-20 10:25:21\"    }}";
        System.out.println(json.getBytes().length + " - " + json.length());
        //Map<String, Object> map = (Map<String, Object>) new Gson().fromJson(json, Map.class);
//        Map<String, Object> fun = (Map<String, Object>) map.get("fnc");
//        System.out.println(map.get("iopn"));
//        System.out.println(map.get("fnc"));
//        System.out.println(fun.get("ts"));
    }

    /**
     * //解析出IP地址
     * 
     * @param host
     * @return
     */
    public static String getHostIpAddr(String host) {
        host = host.replace("/", "");
        String[] info = host.split(":");
        if (info.length != 2) {
            return null;
        }
        return info[0];
    }

    /**
     * 取当前系统时间
     * 
     * @return
     */
    public static String getCurrentDataString() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
        return df.format(new Date());
    }

    /**
     * 解析出端口号
     * 
     * @param host
     * @return
     */
    public static Integer getHostPort(String host) {
        // 解析出IP和端口号
        String[] info = host.split(":");
        if (info.length != 2) {
            return null;
        }
        try {
            Integer port = Integer.parseInt(info[1]);
            return port;
        } catch (NumberFormatException fe) {
            return null;
        }
    }

    public static String getUdpAddress() {
        String addr = getLocalAddress();
        if (addr != null && !"".equals(addr)) {
            addr = addr.substring(0, addr.lastIndexOf(".") + 1) + "255";
        }

        return addr;
    }

    /**
     * 获取本机IP地址
     * 
     * @return
     */
    public static String getLocalAddress() {
        String ip = null;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return ip;
    }
//
//    /**
//     * 设备IOPN
//     *
//     * @return
//     */
//    public static String getIopn() {
////        if (IOPN != null) {
////            return IOPN;
////        }
////        String URL = "http://pi.ichint.com/uuid?mac=";
////        // String mac =
////        // formatMacAddr(Util.getMACAddress(Util.getLocalAddress())) ;
////        String mac = getLocalMac();
////        NameBasedGenerator gen = Generators.nameBasedGenerator(NameBasedGenerator.NAMESPACE_URL);
////        mac = mac.replaceAll("-", "");
////        UUID uuid = gen.generate(URL + mac);
////        long l = ByteBuffer.wrap(uuid.toString().getBytes()).getLong();
////        String iopn = Long.toString(l, Character.MAX_RADIX);
//        return iopn;
//    }

//    /**
//     * 根据指定的操作返回JSON模拟数据
//     *
//     * @param inc
//     * @return
//     */
//    public static String getTestJson(String inc) {
//        String json = "{\"iopn\":\"" + getIopn() + "\",\"pv\":\"v1.0\",\"inc\":\"" + inc + "\",\"qos\":\"1\",\"ts\":\""
//            + Util.getCurrentTime() + "\"}";
//        return json;
//    }

//    /**
//     * 根据指定的操作返回JSON模拟数据
//     *
//     * @param data
//     * @return
//     */
//    public static String getCfcJson(Map<String, String> data) {
//        // 去除掉空的值
//        for (String key : data.keySet()) {
//            if (data.get(key) == null || "".equals(data.get(key).trim())) {
//                data.remove(key);
//            }
//        }
//
//        String fnc = new Gson().toJson(data);
//        String json = "{\"iopn\":\"" + getIopn() + "\",\"pv\":\"v1.0\",\"inc\":\"cfc\",\"qos\":\"1\",\"fnc\": " + fnc + "}";
//        return json;
//    }

    /**
     * 向指定的字符串数组中追加原素
     * 
     * @param arr
     * @return
     */
    public static String[] addToStringArr(String[] arr, String s) {
        for (int i = 0; i < arr.length; i++) {
            // 判断是否是一个数字
            Integer val = 0;
            try {
                val = Integer.parseInt(arr[i]);
                arr[i] = s;
                break;
            } catch (Exception e) {
                continue;
            }
        }
        return arr;
    }

    /**
     * 将十六进制转为整型数组
     * 
     * @param src
     * @return
     */
    public static List<Integer> hexStrToIntArr(String src) {
        String[] hexArr = src.split(" ");
        List<Integer> dataArr = new ArrayList<Integer>();
        for (int i = 0; i < hexArr.length; i++) {
            if (!"".equals(hexArr[i])) {
                dataArr.add(Integer.valueOf(hexArr[i], 16));
            }
        }
        return dataArr;
    }

    /**
     * 取点击的值
     * 
     * @param arr
     * @return
     */
    public static String getTouValues(String[] arr) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < arr.length; i++) {
            // 判断是否是一个数字
            Integer val = 0;
            try {
                val = Integer.parseInt(arr[i]);
            } catch (Exception e) {
                buf.append(arr[i]);
            }
        }
        return buf.toString();
    }

    /**
     * 随机产生DeviceName
     * 
     * @return
     */
    public static String getRandomDeviceName() {
        String[] arr = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H",
            "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
        StringBuffer str = new StringBuffer();
        Random ran = new Random();
        str.append(arr[ran.nextInt(arr.length - 1)]);
        str.append(arr[ran.nextInt(arr.length - 1)]);
        str.append(arr[ran.nextInt(arr.length - 1)]);

        str.append("-emu");
        return str.toString();
    }

    /**
     * 取当前时间（精确到秒）
     * 
     * @return
     */
    public static long getCurrentTime() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 设置日志
     * 
     * @param logs
     * @param rows
     * @return
     */
    public static String setTxtLogByRows(String logs, int rows) {
        StringBuffer buf = new StringBuffer();
        String[] logArr = logs.split("\r\n");
        if (logArr.length <= rows) {
            return logs;
        }
        for (int i = 0; i < rows; i++) {
            if (i != 0) {
                buf.append("\r\n");
            }
            buf.append(logArr[i]);
        }
        return buf.toString();
    }

//    // pong JSON 字符串
//    public static String PONG_JSON = "{\"iopn\":\"" + getIopn() + "\",\"pv\":\"v1.0\",\"inc\":\"pong\",\"qos\":\"0\",\"time\":\""
//        + Util.getCurrentTime() + "\"}";
//
    /**
     *
     * 
     * @return
     */
    public static String getLocalMac() {
        // TODO Auto-generated method stub
        StringBuffer sb = new StringBuffer("");
        // 获取网卡，获取地址
        InetAddress ia;
        try {
            ia = InetAddress.getLocalHost();
            byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();

            for (int i = 0; i < mac.length; i++) {
                if (i != 0) {
                    sb.append("-");
                }
                // 字节转换为整数
                int temp = mac[i] & 0xff;
                String str = Integer.toHexString(temp);
                if (str.length() == 1) {
                    sb.append("0" + str);
                } else {
                    sb.append(str);
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }

        return sb.toString().toUpperCase();
    }

    public static String getNetCode() {
        BufferedReader bufferedReader = null;
        Process process = null;
        StringBuffer sb = new StringBuffer();
        try {
            String buffer;
            process = Runtime.getRuntime().exec("ipconfig");
            // 得到结果
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((buffer = bufferedReader.readLine()) != null) {
                sb.append(buffer + "\n");
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString();
        } catch (IOException e) {
            System.out.println("execute failed.");
            return null;
        } finally {
            // 清理
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                    bufferedReader = null;
                }

                if (process != null) {
                    process.destroy();
                    process = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 编码(生成二维码)
     * 
     * @param str
     *            二维码的内容
     */
//    public static byte[] encode(String str, String path, int width, int height) {
//        try {
//            BitMatrix byteMatrix;
//            byteMatrix = new MultiFormatWriter().encode(new String(str.getBytes("UTF-8"), "ISO-8859-1"), BarcodeFormat.QR_CODE,
//                width, height);
//            File file = new File(path);
//
//            MatrixToImageWriter.writeToFile(byteMatrix, "png", file);
//            return getBytes(path);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    /**
     * 获得指定文件的byte数组
     * 
     * @param filePath
     * @return
     */
    public static byte[] getBytes(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    /**
     * 删除文件
     * 
     * @param filename
     */
    public static void delFile(String filename) {
        File f = new File(filename);
        if (f.exists()) {
            f.delete();
        }
    }

    public static String getGadgetId(String id) {
        return "{\"Gadget_Model\": \"Pi EVO\",\"Manufacturer\": \"Zeninfor\",\"Gadget_ID\":\"" + id
            + "\",\"Template_ID\": \"just-app\"}";
    }

    /**
     * 随机产生大于0，小于10的随机数
     * 
     * @return
     */
    public static Integer getRandomPower() {
        Random ran = new Random();
        Integer pow = ran.nextInt(10);
        // if (pow < 1) {
        // return getRandomPower();
        // }
        return pow;
    }

    /**
     * 加入对象属性覆盖的方法 ：(源对象属性不为空时覆盖目标对象)
     * 
     * @param from
     *            源对象
     * @param to
     *            目标对象
     * @return
     */
    public static Object overBean(Object from, Object to) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(to.getClass());
            PropertyDescriptor[] ps = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor p : ps) {
                Method getMethod = p.getReadMethod();
                Method setMethod = p.getWriteMethod();
                if (getMethod != null && setMethod != null) {
                    try {
                        Object result = getMethod.invoke(from);
                        if (result != null)
                            setMethod.invoke(to, result);
                    } catch (Exception e) {
                        // 如果from没有此属性的get方法，跳过
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return to;
    }

}