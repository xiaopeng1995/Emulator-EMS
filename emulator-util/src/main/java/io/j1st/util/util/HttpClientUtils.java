package io.j1st.util.util;


import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Administrator on 2016/4/21.
 */
public class HttpClientUtils {


    private static final Logger log = LoggerFactory.getLogger(HttpClientUtils.class);


    /**
     * @param url
     * @param token
     * @return
     */
    public static String sendGet(String url, String token) {
        log.debug("正准备发送http get请求，请求地址为：{}， token为：{}", url, token);
        String result = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        if (!StringUtils.isBlank(token)) {
            httpGet.setHeader("Authorization", "Bearer " + token);
        }
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httpGet);
            result = EntityUtils.toString(response.getEntity());
        } catch (ClientProtocolException e) {
            log.error("发送http get请求时出错，错误信息为：{}", e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            log.error("发送http get请求时出错，错误信息为：{}", e.getMessage());
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    if (response != null)
                        response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }


    /**
     * 执行一个HTTP POST请求，返回请求响应的HTML
     *
     * @param url  请求的URL地址
     * @param json 请求的查询参数,可以为null
     * @param  contentType 数据类型
     * @return 返回请求响应的HTML
     */
    public static String sendPost(String url, String token,String contentType, String json) {
        log.debug("正准备发送http post请求，请求地址为：{}， token为：{} 参数为：{}", url, token, json);
        if(StringUtils.isBlank(contentType)){
            contentType = "application/json";
        }
        String result = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
//        if(headers !=null && headers.length > 0) {
//            httpPost.setHeaders(headers);
//        }
        if (!StringUtils.isBlank(token)) {
            httpPost.setHeader("Authorization", "Bearer " + token);
        }
        CloseableHttpResponse response = null;
        //建立的http连接，仍旧被response保持着，允许我们从网络socket中获取返回的数据
        //为了释放资源，我们必须手动消耗掉response或者取消连接（使用CloseableHttpResponse类的close方法）
        //拼接参数
        StringEntity entity = new StringEntity(json, "utf-8");//解决中文乱码问题
        entity.setContentEncoding("UTF-8");
        entity.setContentType(contentType);
        httpPost.setEntity(entity);
        try {
            httpPost.setHeader("Content-Type", contentType);
            response = httpclient.execute(httpPost);
            result = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            log.error("发送http post请求时出错，错误信息为：{}", e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (response != null)
                    response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * @param url   请求url
     * @param token 用户token
     * @param json  发送的json数据
     *  @param  contentType  数据类型
     * @return result请求结果
     */
    public static String sendDownStreamPost(String url, String token,String contentType, String json) {
        log.debug("正准备发送http post请求，请求地址为：{}", url);
        if(StringUtils.isBlank(contentType)){
            contentType = "text/plain";
        }
        String result = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        if (!StringUtils.isBlank(token)) {
            httpPost.setHeader("Authorization", "Bearer " + token);
        }
        CloseableHttpResponse response = null;
        StringEntity entity = new StringEntity(json, "utf-8");   //解决中文乱码问题
        entity.setContentEncoding("UTF-8");
        entity.setContentType(contentType);
        httpPost.setEntity(entity);
        try {
            httpPost.setHeader("Content-Type", contentType);
            response = httpclient.execute(httpPost);
            result = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            log.error("发送http post请求时出错，错误信息为：{}", e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (response != null)
                    response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 执行一个HTTP POST请求，返回请求响应的HTML
     *
     * @param url  请求的URL地址
     * @param data 请求的查询参数,可以为null
     * @param  contentType  数据类型
     * @return 返回请求响应的HTML
     */
    public static String sendPost(String url, String token, String contentType ,byte[] data) {
        log.debug("正准备发送http post请求，请求地址为：{}， token为：{}", url, token);
        if(StringUtils.isBlank(contentType)){
            contentType = "application/json";
        }
        String result = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
//        if(headers !=null && headers.length > 0) {砌筑
//            httpPost.setHeaders(headers);
//        }
        if (!StringUtils.isBlank(token)) {
            httpPost.setHeader("Authorization", "Bearer " + token);
        }
        CloseableHttpResponse response = null;
        //建立的http连接，仍旧被response保持着，允许我们从网络socket中获取返回的数据
        //为了释放资源，我们必须手动消耗掉response或者取消连接（使用CloseableHttpResponse类的close方法）
        //拼接参数
        ByteArrayEntity entity = new ByteArrayEntity(data);
        httpPost.setEntity(entity);
        entity.setContentType(contentType);
        try {
            response = httpclient.execute(httpPost);
            result = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            log.error("发送http post请求时出错，错误信息为：{}", e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (response != null)
                    response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
