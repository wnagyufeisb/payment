package noumena.payment.caishen;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import noumena.payment.bean.OrdersBean;
import noumena.payment.lenovo.util.Base64;
import noumena.payment.model.Orders;
import noumena.payment.util.Constants;
import noumena.payment.util.DateUtil;
import noumena.payment.util.OSUtil;
import noumena.payment.util.StringEncrypt;
import noumena.payment.util.TrustAnyTrustManager;
import noumena.payment.vo.OrderIdVO;
import noumena.payment.vo.OrderStatusVO;

public class CaishenCharge
{
	private static CaishenParams params = new CaishenParams();
	
	public static String getTransactionId(Orders order)
	{
		OrdersBean bean = new OrdersBean();
		String cburl = order.getCallbackUrl();
		String payId;
		if (cburl == null || cburl.equals(""))
		{
			payId = bean.CreateOrder(order);
		}
		else
		{
			if (cburl.indexOf("?") == -1)
			{
				cburl += "?pt=" + Constants.PAY_TYPE_CAISHEN;
			}
			else
			{
				cburl += "&pt=" + Constants.PAY_TYPE_CAISHEN;
			}
			payId = bean.CreateOrder(order, cburl);
		}
		order.setCallbackUrl(cburl);
		String date = DateUtil.formatDate(order.getCreateTime());
		OrderIdVO orderIdVO = new OrderIdVO(payId, date);
		String msg = StringEncrypt.Encrypt(order.getOrderId()+"_"+order.getItemId()+"_"+order.getAmount().intValue()+"_"+order.getUId()+"_"+order.getSign()+"_"+order.getAppId()+"_"+params.getParams(order.getAppId()).getSecretkey());
		System.out.println("msg:"+msg);
		orderIdVO.setMsg(msg);
		JSONObject json = JSONObject.fromObject(orderIdVO);
		return json.toString();
	}
	
	public static String checkOrdersStatus(String payIds)
	{
		String[] orderIds = payIds.split(",");

		OrdersBean bean = new OrdersBean();
		List<Orders> orders = bean.qureyOrders(orderIds);
		List<OrderStatusVO> statusret = new ArrayList<OrderStatusVO>();
		for (int i = 0 ; i < orders.size() ; i++)
		{
			Orders order = orders.get(i);
			int status = order.getKStatus();
			OrderStatusVO st = new OrderStatusVO();
			st.setPayId(order.getOrderId());
			if (status == Constants.K_STSTUS_DEFAULT || status == Constants.K_CON_ERROR)
			{
				//如果订单状态是初始订单或者是网络连接有问题状态，返回不知道
				Calendar cal1 = DateUtil.getCalendar(order.getCreateTime());
				Calendar cal2 = Calendar.getInstance();
				if ((cal2.getTimeInMillis() - cal1.getTimeInMillis()) >= Constants.ORDER_TIMEOUT)
				{
					st.setStatus(4);
				}
				else
				{
					st.setStatus(3);
				}
			}
			else if (status == Constants.K_STSTUS_SUCCESS)
			{
				//如果订单已经成功，直接返回订单状态
				st.setStatus(1);
			}
			else
			{
				//订单已经失败，直接返回订单状态
				st.setStatus(2);
			}
			statusret.add(st);
		}
		JSONArray arr = JSONArray.fromObject(statusret);
		
		return arr.toString();
	}
	
	public static String getCallbackFromCaishen(Map<String,String> caishenparams)
	{
		String ret = "0001";
		System.out.println("caishen cb ->" + caishenparams.toString());
		CaishenOrderVO covo = new CaishenOrderVO();
		covo.setGame_id(caishenparams.get("game_id"));
		covo.setGame_uid(caishenparams.get("game_uid"));
		covo.setOrder_id(caishenparams.get("order_id"));
		covo.setPrice(caishenparams.get("price"));
		covo.setProduct_id(caishenparams.get("product_id"));
		covo.setSign(caishenparams.get("sign"));
		covo.setU_id(caishenparams.get("u_id"));
		covo.setXcs_order(caishenparams.get("xcs_order"));
		
		String sign = StringEncrypt.Encrypt(covo.getOrder_id()+"_"+covo.getProduct_id()+"_"+covo.getPrice()+"_"+covo.getGame_uid()+"_"+covo.getU_id()+"_"+covo.getXcs_order()+"_"+covo.getGame_id()+"_"+params.getParams(caishenparams.get("id")).getSecretkey());
		System.out.println("caishen cb sign:"+sign);
		if(sign.equals(covo.getSign())){
			OrdersBean bean = new OrdersBean();
			try {
				// 支付成功
				Orders order = bean.qureyOrder(covo.getOrder_id());
				if (order != null) {
					if(covo.getU_id().equals(order.getSign())){
						if (order.getKStatus() != Constants.K_STSTUS_SUCCESS) {
							bean.updateOrderAmountPayIdExinfo(covo.getOrder_id(), covo.getXcs_order(), covo.getPrice(), "");
							bean.updateOrderKStatus(covo.getOrder_id(),Constants.K_STSTUS_SUCCESS);
						} else {
							System.out.println("caishen order ("+ order.getOrderId() + ") had been succeed");
						}
						ret = "0000";
					}else{
						ret = "0002";
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			System.out.println("caishen cb ret->" + ret);

			String path = OSUtil.getRootPath() + "../../logs/caishencb/"
					+ DateUtil.getCurTimeStr().substring(0, 8);
			OSUtil.makeDirs(path);
			String filename = path + "/" + covo.getOrder_id();

			OSUtil.saveFile(filename, caishenparams.toString());
		}
		return ret;
	}
	public static boolean validateSign(CaishenOrderVO covo,String sign){
		String res = "";
		StringBuffer sb = new StringBuffer();
		sb.append("order_id=").append(covo.getOrder_id()).append("&");
		sb.append("product_id=").append(covo.getProduct_id()).append("&");
		sb.append("price=").append(covo.getPrice()).append("&");
		sb.append("game_uid=").append(covo.getGame_uid()).append("&");
		sb.append("u_id=").append(covo.getU_id()).append("&");
		sb.append("xcs_order=").append(covo.getXcs_order()).append("&");
		sb.append("game_id=").append(covo.getGame_id()).append("&");
		sb.append("sign=").append(sign);
		try {
		URL	url = new URL("http://pay.s.the9.com/Callbacks/checkOrder?"+sb.toString());
		System.out.println(url);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setInstanceFollowRedirects(false);
		connection.setUseCaches(false);
		connection.setRequestProperty("Connection", "Keep-Alive");
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
		connection.setRequestProperty("charset", "utf-8");
		
		connection.connect();
		
		DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
		wr.flush();
		wr.close();
		
		BufferedReader in = new BufferedReader
			(
				new InputStreamReader(connection.getInputStream())
			);
		String line = null;
		while ((line = in.readLine()) != null)
		{
			res += line;
		}
		connection.disconnect();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("res:"+res);
		if("0000".equals(res)){
			return true;
		}else{
			return false;
		}
		
	}
	public static void init()
	{
		params.initParams(CaishenParams.CHANNEL_ID, new CaishenParamsVO());
	}
	
	public static void main(String args[]){
		String res="";
		HttpsURLConnection urlCon = null;  
        try {  
            urlCon = (HttpsURLConnection) (new URL("https://admin.jpush.cn/v1/app")).openConnection();  
            urlCon.setDoInput(true);  
            urlCon.setDoOutput(true);  
            urlCon.setRequestMethod("POST");
            urlCon.setRequestProperty("base64_auth_string", "ZjQ1MjYzNTRlNzk4ZDQyMzVhNjFlMzk4OjVjYmY5NGM5MmY2NDI1NGJkZDhkMTc3NQ==");
            urlCon.setRequestProperty("Content-type","application/json");  
            urlCon.setUseCaches(false);
            
            urlCon.getOutputStream().write(("{\"app_name\": \""+"战舰大海战"+"\", \"android_package\": \""+"com.kongzhong.d61cn.mi"+"\", \"group_name\": \""+"xiaomi"+"\"}").getBytes());  
            urlCon.getOutputStream().flush();  
            urlCon.getOutputStream().close();  
            BufferedReader in = new BufferedReader(new InputStreamReader(  
                    urlCon.getInputStream()));  
            String line;  
            while ((line = in.readLine()) != null) {  
               res+=line; 
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        System.out.println(res);  
	}

}
