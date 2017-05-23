package noumena.payment.jusdk;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import noumena.payment.bean.OrdersBean;
import noumena.payment.model.Orders;
import noumena.payment.userverify.util.StringEncrypt;
import noumena.payment.util.Constants;
import noumena.payment.util.DateUtil;
import noumena.payment.util.OSUtil;
import noumena.payment.vo.OrderIdVO;
import noumena.payment.vo.OrderStatusVO;


public class JusdkCharge
{
	private static JusdkParams params = new JusdkParams();
	
	public static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static String getTransactionId(Orders order)
	{
		order.setCurrency(Constants.CURRENCY_RMB);
		order.setUnit(Constants.CURRENCY_UNIT_YUAN);
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
				cburl += "?pt=" + Constants.PAY_TYPE_JUSDK;
			}
			else
			{
				cburl += "&pt=" + Constants.PAY_TYPE_JUSDK;
			}
			cburl += "&currency=" + Constants.CURRENCY_RMB;
			cburl += "&unit=" + Constants.CURRENCY_UNIT_YUAN;
			payId = bean.CreateOrder(order, cburl);
		}
		order.setCallbackUrl(cburl);
		String date = DateUtil.formatDate(order.getCreateTime());
		OrderIdVO orderIdVO = new OrderIdVO(payId, date);
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
	
	public static String getCallbackFromJusdk(Map<String,String> m)
	{
		String ret = "success";
		System.out.println("jusdk cb ->" + m.toString());
		String orderid = m.get("dealseq");
		String signData = Util.getSignData(m);
			OrdersBean bean = new OrdersBean();
			try {
				Orders order = bean.qureyOrder(orderid);
					if (order != null) {
						 if(!RSASignature.doCheck(signData, m.get("sign"), params.getParams(order.getSign()).getAppkey(), "utf-8")){
						       	//RSA验签失败，数据不可信
							 ret = "fail";
						       }else{
						       	//"RSA验签成功，数据可信
						           RSAEncrypt rsaEncrypt= new RSAEncrypt();
						           
						           //加载公钥   
						           try {
						               rsaEncrypt.loadPublicKey(params.getParams(order.getSign()).getAppkey()); 
						               //加载公钥成功
						           } catch (Exception e) {  
						           	//加载公钥失败
						           }  
						           
						         	//公钥解密通告加密数据
						           byte[] dcDataStr = Base64.decode(m.get("notify_data"));
						           byte[] plainData = rsaEncrypt.decrypt(rsaEncrypt.getPublicKey(), dcDataStr);  
						           //获取到加密通告信息
						           String notifyData = new String(plainData, "UTF-8");
						           //开发商业务逻辑处理
						           String[] notifyDatas = notifyData.split("&");
						           
						        	   if (order.getKStatus() != Constants.K_STSTUS_SUCCESS) {
											bean.updateOrderAmountPayIdExinfo(orderid, m.get("orderid"), notifyDatas[1].split("=")[1], "");
											if("0".equals(notifyDatas[2].split("=")[1])){
												bean.updateOrderKStatus(orderid,Constants.K_STSTUS_SUCCESS);
											}else{
												bean.updateOrderKStatus(orderid,Constants.K_STSTUS_ERROR);
											}
										} else {
											System.out.println("jusdk order ("+ order.getOrderId() + ") had been succeed");
										}
						     }
							
					}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			System.out.println("jusdk cb ret->" + ret);

			String path = OSUtil.getRootPath() + "../../logs/jusdkcb/"
					+ DateUtil.getCurTimeStr().substring(0, 8);
			OSUtil.makeDirs(path);
			String filename = path + "/" + orderid;

			OSUtil.saveFile(filename, m.toString());
		
		return ret;
	}
	
	public static void init()
	{
		params.initParams(JusdkParams.CHANNEL_ID, new JusdkParamsVO());
	}
	
	public static void main(String args[]){
		//System.out.println(StringEncrypt.Encrypt("d08d0029d0284082b359e5ea03e19831b5924894508f4be29f087e78432cfaff20160511185948468E702016-05-11 19:00:58"));
		System.out.println(URLDecoder.decode("GET&%2Fv3%2Fr%2Fmpay%2Fget_balance_m&appid%3D1105285057%26openid%3DopnFdwIAljms1Xeh6N2MSmKVCHrg%26openkey%3DOezXcEiiBSKSxW0eoylIeF3fsmX53crxnEk9mfKu22SMAXrxvef6wB4wGA8vf4O3CG5_JN7OsuJ1gfMPJ8mkIIFTJKMfohyrFol3Yz5DqBBdraDGteypVDpxs79JfOj5J8BXwaocOAAtRAbPZOrMAg%26pf%3Dmyapp_m_wx-00000000-android-00000000-ysdk%26pfkey%3D8e89f54ce0dbf7cb015be3db01af26fd%26ts%3D1463128292%26zoneid%3D1"));
	}
}
