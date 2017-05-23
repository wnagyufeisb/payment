package noumena.payment.now;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import noumena.payment.bean.OrdersBean;
import noumena.payment.model.Orders;
import noumena.payment.util.Constants;
import noumena.payment.util.DateUtil;
import noumena.payment.util.OSUtil;
import noumena.payment.vo.OrderIdVO;
import noumena.payment.vo.OrderStatusVO;

public class NowCharge {
	private static NowParams params = new NowParams();

	public static String verifySign(HttpServletRequest req) throws IOException {
		Map<String, String> dataMap = new HashMap<String, String>();
		Map<?, ?> requestParams = req.getParameterMap();
		for (Iterator<?> iter = requestParams.keySet().iterator(); iter
				.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i]
						: valueStr + values[i] + ",";
			}
			dataMap.put(name, valueStr);
		}
		dataMap.remove("signType");
		dataMap.remove("model");
		dataMap.remove("signature");
		String md5Key = params.getParams(dataMap.get("appId")).getAppkey();
		System.out.println("now md5Key"+md5Key);
		String signature = MD5Facade.getFormDataParamMD5(dataMap, md5Key,
				"UTF-8");
		return "mhtSignature=" + signature + "&mhtSignType=MD5";

	}

	public static String getTransactionId(Orders order) {
		order.setCurrency(Constants.CURRENCY_RMB);
		order.setUnit(Constants.CURRENCY_UNIT_FEN);
		OrdersBean bean = new OrdersBean();

		String cburl = order.getCallbackUrl();
		String payId;
		if (cburl == null || cburl.equals("")) {
			payId = bean.CreateOrder(order);
		} else {
			if (cburl.indexOf("?") == -1) {
				cburl += "?pt=" + Constants.PAY_TYPE_NOW;
			} else {
				cburl += "&pt=" + Constants.PAY_TYPE_NOW;
			}
			cburl += "&currency=" + Constants.CURRENCY_RMB;
			cburl += "&unit=" + Constants.CURRENCY_UNIT_FEN;
			payId = bean.CreateOrder(order, cburl);
		}
		order.setCallbackUrl(cburl);
		String date = DateUtil.formatDate(order.getCreateTime());
		OrderIdVO orderIdVO = new OrderIdVO(payId, date);
		JSONObject json = JSONObject.fromObject(orderIdVO);
		System.out.println("getTransactionId================ ->"
				+ json.toString());
		return json.toString();
	}
	
	
	public static String getPayStatus(String orderId){
		OrdersBean bean = new OrdersBean();
		Orders orders=bean.qureyOrder(orderId);
		if(orders==null){
			return "{\"status\":\"fail\"}";
		}
		if(orders.getKStatus().equals(Constants.CALLBACK_STSTUS_ERROR)){
			return "{\"status\":\"fail\"}";
		}
		if(orders.getKStatus().equals(Constants.CALLBACK_STSTUS_COMPLETE)){
			return "{\"status\":\"success\"}";
		}
		return "{\"status\":\"wait\"}";
	}

	public static String checkOrdersStatus(String payIds) {
		String[] orderIds = payIds.split(",");

		OrdersBean bean = new OrdersBean();
		List<Orders> orders = bean.qureyOrders(orderIds);
		List<OrderStatusVO> statusret = new ArrayList<OrderStatusVO>();
		for (int i = 0; i < orders.size(); i++) {
			Orders order = orders.get(i);
			int status = order.getKStatus();
			OrderStatusVO st = new OrderStatusVO();
			st.setPayId(order.getOrderId());
			if (status == Constants.K_STSTUS_DEFAULT
					|| status == Constants.K_CON_ERROR) {
				// 如果订单状态是初始订单或者是网络连接有问题状态，返回不知道
				Calendar cal1 = DateUtil.getCalendar(order.getCreateTime());
				Calendar cal2 = Calendar.getInstance();
				if ((cal2.getTimeInMillis() - cal1.getTimeInMillis()) >= Constants.ORDER_TIMEOUT) {
					st.setStatus(4);
				} else {
					st.setStatus(3);
				}
			} else if (status == Constants.K_STSTUS_SUCCESS) {
				// 如果订单已经成功，直接返回订单状态
				st.setStatus(1);
			} else {
				// 订单已经失败，直接返回订单状态
				st.setStatus(2);
			}
			statusret.add(st);
		}
		JSONArray arr = JSONArray.fromObject(statusret);

		return arr.toString();
	}

	public static String getCallbackFromNow(Map<String, String> m) {
		String ret = "success=N";
		System.out.println("xianzai cb ->" + m.toString());
		// 去除签名类型和签名值
		m.remove("signType");
		String signature = m.remove("signature");

		boolean isValidSignature = MD5Facade.validateFormDataParamMD5(m, params
				.getParams(m.get("appId")).getAppkey(), signature);

		OrdersBean bean = new OrdersBean();
		try {
			// 支付成功
			Orders order = bean.qureyOrder(m.get("mhtOrderNo"));
			System.out.println("xianzai order " + order.toString());
			if (isValidSignature) {
				if (order != null) {
					if (order.getKStatus() != Constants.K_STSTUS_SUCCESS) {
						bean.updateOrderAmountPayIdExinfo(m.get("mhtOrderNo"),
								m.get("nowPayOrderNo"), m.get("mhtOrderAmt"),
								"");
						bean.updateOrderKStatus(m.get("mhtOrderNo"),
								Constants.K_STSTUS_SUCCESS);
						System.out.println("xianzai order ("
								+ order.getOrderId() + ")  succeed");
						ret = "success=Y";
					} else {
						System.out.println("xianzai order ("
								+ order.getOrderId() + ") had been succeed");
					}
				}
			} else {
				ret = "success=N";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("xianzai cb ret->" + ret);

		String path = OSUtil.getRootPath() + "../../logs/xianzaicb/"
				+ DateUtil.getCurTimeStr().substring(0, 8);
		OSUtil.makeDirs(path);
		String filename = path + "/" + m.get("mhtOrderNo");

		OSUtil.saveFile(filename, m.toString());

		return ret;
	}

	public static void init() {
		params.initParams(NowParams.CHANNEL_ID, new NowParamsVO());
	}

	public static void main(String args[]) {
		// System.out.println(StringEncrypt.Encrypt("d08d0029d0284082b359e5ea03e19831b5924894508f4be29f087e78432cfaff20160511185948468E702016-05-11 19:00:58"));
		System.out
				.println(URLDecoder
						.decode("GET&%2Fv3%2Fr%2Fmpay%2Fget_balance_m&appid%3D1105285057%26openid%3DopnFdwIAljms1Xeh6N2MSmKVCHrg%26openkey%3DOezXcEiiBSKSxW0eoylIeF3fsmX53crxnEk9mfKu22SMAXrxvef6wB4wGA8vf4O3CG5_JN7OsuJ1gfMPJ8mkIIFTJKMfohyrFol3Yz5DqBBdraDGteypVDpxs79JfOj5J8BXwaocOAAtRAbPZOrMAg%26pf%3Dmyapp_m_wx-00000000-android-00000000-ysdk%26pfkey%3D8e89f54ce0dbf7cb015be3db01af26fd%26ts%3D1463128292%26zoneid%3D1"));
	}
}
