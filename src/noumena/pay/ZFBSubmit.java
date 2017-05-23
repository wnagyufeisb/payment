package noumena.pay;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;

import net.sf.json.JSONObject;
import noumena.payment.alipay.AlipayParams;
import noumena.payment.alipay.RSA;


public class ZFBSubmit extends HttpServlet {
	
	//支付宝提供给商户的服务接入网关URL(新)
    private static final String ALIPAY_GATEWAY_NEW = "https://mapi.alipay.com/gateway.do?";
    
    // 合作身份者ID，签约账号，以2088开头由16位纯数字组成的字符串，查看地址：https://b.alipay.com/order/pidAndKey.htm
 	public static String partner = "2088701535333835";
 	
 	// 收款支付宝账号，以2088开头由16位纯数字组成的字符串，一般情况下收款账号就是签约账号
 	public static String seller_id = partner;

 	// MD5密钥，安全检验码，由数字和字母组成的32位字符串，查看地址：https://b.alipay.com/order/pidAndKey.htm
     public static String key = "u05026qpvr4ymxxhc9fzm0npj533a2dj";
 	

 	// 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
 	//public static String notify_url = "http://p.ko.cn/pay/alipaycb";
      public static String notify_url = "http://paystage.ko.cn:6001/paymentsystem/alipaycb";
 	// 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
 	//public static String return_url = "http://p.ko.cn/pay/zfbhtmlcb";
 	public static String return_url = "http://paystage.ko.cn:6001/paymentsystem/zfbhtmlcb";

 	// 签名方式
 	public static String sign_type = "MD5";
    
 	// 字符编码格式 目前支持utf-8
 	public static String input_charset = "utf-8";
 		
 	// 支付类型 ，无需修改
 	public static String payment_type = "1";
 		
 	// 调用的接口名，无需修改
 	public static String service = "alipay.wap.create.direct.pay.by.user";
    
    
	/**
     * 生成签名结果
     * @param sPara 要签名的数组
     * @return 签名结果字符串
     */
	public static String buildRequestMysign(Map<String, String> sPara) {
    	String prestr = createLinkString(sPara); //把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
        String mysign = "";
        mysign= RSA.sign(prestr,
				AlipayParams.KONG_PRIVATE_KEY_PKCS8, "UTF-8");
     //mysign = MD5.sign(prestr, key, input_charset);
        return mysign;
    }
	
    /**
     * 生成要请求给支付宝的参数数组
     * @param sParaTemp 请求前的参数数组
     * @return 要请求的参数数组
     */
    private static Map<String, String> buildRequestPara(Map<String, String> sParaTemp) {
        //除去数组中的空值和签名参数
        Map<String, String> sPara = paraFilter(sParaTemp);
        //生成签名结果
        String mysign = buildRequestMysign(sPara);

        //签名结果与签名方式加入请求提交参数组中
        sPara.put("sign", mysign);
        sPara.put("sign_type", AlipayParams.ALIPAY_SIGN_TYPE);

        return sPara;
    }
    
    /** 
     * 除去数组中的空值和签名参数
     * @param sArray 签名参数组
     * @return 去掉空值与签名参数后的新签名参数组
     */
    public static Map<String, String> paraFilter(Map<String, String> sArray) {

        Map<String, String> result = new HashMap<String, String>();

        if (sArray == null || sArray.size() <= 0) {
            return result;
        }

        for (String key : sArray.keySet()) {
            String value = sArray.get(key);
            if (value == null || value.equals("") || key.equalsIgnoreCase("sign")
                || key.equalsIgnoreCase("sign_type")) {
                continue;
            }
            result.put(key, value);
        }

        return result;
    }
    
    /** 
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     * @param params 需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
    public static String createLinkString(Map<String, String> params) {

        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        String prestr = "";

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);

            if (i == keys.size() - 1) {//拼接时，不包括最后一个&字符
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }

        return prestr;
    }
    
    /**
     * 建立请求，以表单HTML形式构造（默认）
     * @param sParaTemp 请求参数数组
     * @param strMethod 提交方式。两个值可选：post、get
     * @param strButtonName 确认按钮显示文字
     * @return 提交表单HTML文本
     */
    public static String buildRequest(Map<String, String> sParaTemp, String strMethod, String strButtonName) {
        //待请求参数数组
        Map<String, String> sPara = buildRequestPara(sParaTemp);
        List<String> keys = new ArrayList<String>(sPara.keySet());

        StringBuffer sbHtml = new StringBuffer();

        sbHtml.append("<form id=\"submit\" name=\"submit\" action=\"" + ALIPAY_GATEWAY_NEW
                      + "_input_charset=" + input_charset + "\" method=\"" + strMethod
                      + "\">");

        for (int i = 0; i < keys.size(); i++) {
            String name = (String) keys.get(i);
            String value = (String) sPara.get(name);

            sbHtml.append("<input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\"/>");
        }

        //submit按钮控件请不要含有name属性
        sbHtml.append("<input type=\"submit\" value=\"" + strButtonName + "\" style=\"display:none;\"></form>");
        sbHtml.append("<script>document.forms['submit'].submit();</script>");
        
        return sbHtml.toString();
    }
    
    public static String getOrderId(String uid,String pkgid,String itemid,String price,String cburl,String channel) throws UnsupportedEncodingException{
		String orderid="";
		//String urlstr = "http://p.ko.cn/pay/alipay";
		System.out.print(cburl+"wewewewewewe+++++++++++++++++++++++");
		String urlstr = "http://paystage.ko.cn:6001/paymentsystem/alipay";
		urlstr += "?model=1";
		urlstr += "&uid="+uid;//角色ID
		urlstr += "&pkgid="+pkgid;//包名
		urlstr += "&itemid="+itemid;//商品ID
		urlstr += "&price="+price;//价格
		urlstr += "&cburl="+URLEncoder.encode(cburl,"UTF-8");//回调地址
		urlstr += "&channel="+channel;//渠道号
		try {
			URL url = new URL(urlstr);
		
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			//connection.setReadTimeout(5000);
			connection.setDoOutput(true);
			OutputStreamWriter outs = new OutputStreamWriter(connection.getOutputStream());
			outs.flush();
			outs.close();
			BufferedReader in = new BufferedReader
			(
				new InputStreamReader(connection.getInputStream())
			);
		String res = "", line = null;
		while ((line = in.readLine()) != null)
		{
			res += line;
		}
		connection.disconnect();
		JSONObject json = JSONObject.fromObject(res);
		orderid = json.getString("payId");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return orderid;
	}

}
