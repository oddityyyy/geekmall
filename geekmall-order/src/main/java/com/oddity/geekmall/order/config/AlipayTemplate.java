package com.oddity.geekmall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.oddity.geekmall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private String app_id = "9021000130640983";

    //商户私钥，您的PKCS8格式RSA2私钥
    private String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC3FjrPdTQEYjqut7P1QWH77Sc/btNGME69BE86WQC+8WEuEKyaNT8ApEsX/7Z+rz9jRIBOC4wsJDXc9PrH+ShTzq+hSBuXLFWXcTgNnqNQf3mA/d42Nr7qevGbMoVIVV5p3fkTP5vZ+I5yMyhCjvbcMMi25P3yjsPK24nb/2m/hdPJohdG1rRdBpgj00F4YJuAMgNv3/AX4vKyI/0Zc3nqzJ67AhIQBr1JtbN9LypMIXib52cDRFy04G3y+dR2OlMm3iWCjNKRFBBWF7XtDS5L1zmLOwVb8Zl+HSWrsZo9qE0KK/cWBZoAD56ZpoeSSMOeNIJAGRnQqf6P4H66G8dzAgMBAAECggEAPHZ354kyK3xxGsAKCyWTgUoZWwZzeUt8xUlGDOPVrjkyua7CAY8yBjk7n2pQT1f6nALESIHhwbYXn6e7pgyvd7XbJG0Wx37ZqVC3jS/liTD/5ExydP7xX6hIX9N4VqHysw2eMvAfvQUrraUdV5W1CpSFxHitMMxa9oQ369JWjuvJAtxPyBRfvCDR1eZ7mm5ffl2S+tXoH9zqopa8TwhKEx+HkGZc4VC9Cc+scpMWqAUT8Qvm9auUDscWAbTUDK91tSkfivgZYM0lTE6Z/i/KKMPtB+DkuI1CJjgpVZtMKECHF+fcotlu3nA+H8mqJxVYK0G69Oli+VWTbOY2oAwCIQKBgQDlKYCpMIy38XVRiXQF3/dYHe3qBsiyYjbTegRMnl2nYTv23fiBP5AjBmuZsl7tYMwso05lRHulQ7/2rT4L5cTEvSJL1iWo/Bq1jZYuDLKyTPx+3PAi3oyl3KVApr6aCExBxCPePnhGkuBxAjcKpxN0vqPWpasWwUl2MXHiYh/84wKBgQDMh1iyM58Rt47K0rfhRoMk+fOOZkDafx6mR9nePEUWSPaqre0UboF6/n/belDC99nGNAxwbYFiHsC35JPpwqD8NTTPXrH78xLnD8LIsq/bYl1sLb5PYDzhhBVJjQO6lJ3aRo13cr0UgF5UtRdwUxoovlo43HB/S8ogdqMWMiAgMQKBgQC1kLV815eWofvW93XytanOqh/3fRqEo2ZXFeTUI+GGHXcaOHKNTSRRa+PS6c1mfHwondceSy1AGra8pjRzzr+hNU04EtrR0bUDxcOIhs0Kkg64ISsuJXAAdhqEZ9i6R/rLXLEihBJdsD+dqiicCxZSOD2tcqNkqw4eVDysJhy8CwKBgDwfWTphJFRv8gC7OqR1Nt3qv+bPNidEkPzuSCQk8WYgoqn/oJ6N8W/3Whxpc04sz46NBE8UUaFHSv2DqttfhDU/aru+qXFXvenAX1Dh/gHug2nmdHge4SalziAl3L+/Kjvc238OcniRV6ls5mv1cc4iTqFCY6t32v36AeznZTwxAoGAfzoltmX5509/SD8WBIq9ArlLMkM+EuG6FadFOgUee52sRejYWK6lD8opMYKVL789HlYsJCKDtc8okia6lt/iqm5eu3QZ56ZsTchQhhrzFvWc8IZZJcEEMkdUKQLRAnkH+dlf7VfQLCBm+Dt4LWlArGTZXYEvT3z/Z4zsBxHfVk4=";

    //支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAs+tjFzUvW1Smuy2WyK3jFjQ/KLyH3xtOadUwS5rRWTtmYV6T7nIPOmF4gfnssWBqWESjOxs8ytfG9DbnhHm/zi9umV2dc6wPs/XHKfq0w24ORY1R+zoG1zIVeGe5nOB70lozddMlb4s8lbd/l0g2+aYCojF/s61WGs4P5QqsnnwBwP//hHDrwWvtrqx+2gHtdRN8ZcaFP9y9/BoDMw8eNObxWMhAIp56bXvYAZQKc0SgdIu20fj3r3LmSsHqJIvpp7K57kTDlmAd3/qTRe318ltlxBbIC5DCUTOsqS5PcDvEfdTlJ+uzEOxZbp/Z2/pCWrIQfR2gIWCAzkLjrhAbfQIDAQAB";

    //服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private String notify_url = "https://tomcat-top-marten.ngrok-free.app/payed/notify";

    //页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private String return_url = "http://member.geekmall.com/memberOrder.html";

    //签名方式
    private String sign_type = "RSA2";

    //字符编码格式
    private String charset = "utf-8";

    //超时时间
    private String timeout = "1m";

    //支付宝网关； https://openapi.alipaydev.com/gateway.do
    private String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    public String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\"" + out_trade_no + "\","
                + "\"total_amount\":\"" + total_amount + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"body\":\"" + body + "\","
                + "\"timeout_express\":\"" + timeout + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应：" + result);

        return result;
    }
}
