package cn.itrip.trade.wx;

import cn.itrip.trade.config.WXPayConfig;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class WXPayRequest {

    private WXPayConfig config;

    public WXPayRequest(WXPayConfig config){
        this.config = config;
    }
    /***
     * 统一下单接口
     *
     * @param param
     * @return
     * @throws Exception
     */
    public Map<String, String> unifiedorder(Map<String, String> param) throws Exception {
        param.put("appid",config.getAppID());
        param.put("mch_id",config.getMchID());
        param.put("key",config.getKey());
        param.put("notify_url",config.getNotifyUrl());
        param.put("fee_type", "CNY");
        param.put("trade_type", "NATIVE");
        param.put("product_id", "4512");
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("sign_type","HMAC-SHA256");
        String key = param.get("key");
        param.remove("key");
        //2.转成xml请求微信支付平台,生成签名
        String xml = WXPayUtil.generateSignedXml(param, key, WXPayConstants.SignType.HMACSHA256);
        //3.请求微信支付平台，获取预支付交易链接
        String resultXml = requestByXml("https://api.mch.weixin.qq.com/pay/unifiedorder", xml);
        //resultXml转换成map，解析，然后返回个控制器
        return WXPayUtil.xmlToMap(resultXml);
    }

    /***
     * 请求数据
     *
     * @param url
     * @param data
     * @return
     * @throws Exception
     */
    public String requestByXml(String url, String data) throws Exception {
        //HTTP连接管理类，用这个类发起连接
        BasicHttpClientConnectionManager connManager = new BasicHttpClientConnectionManager(
                RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.getSocketFactory()).register("https", SSLConnectionSocketFactory.getSocketFactory()).build(),
                null,
                null,
                null
        );
        //获取连接，开始连接
        HttpClient httpClient = HttpClientBuilder.create().setConnectionManager(connManager).build();
        HttpPost httpPost = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(1000).setConnectTimeout(10000).build();
        httpPost.setConfig(requestConfig);
        StringEntity postEntity = new StringEntity(data, "UTF-8");
        httpPost.addHeader("Content-Type", "text/xml");
        httpPost.addHeader("User-Agent", "wxpay sdk java v1.0 " + config.getMchID());
        httpPost.setEntity(postEntity);
        HttpResponse httpResponse = httpClient.execute(httpPost);
        HttpEntity httpEntity = httpResponse.getEntity();
        return EntityUtils.toString(httpEntity, "UTF-8");
    }

    public boolean isResponseSignatureValid(Map<String, String> param) throws Exception {
        return WXPayUtil.isSignatureValid(param, this.config.getKey(), WXPayConstants.SignType.HMACSHA256);
    }
}
