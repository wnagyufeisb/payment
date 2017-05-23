package noumena.payment.pptv;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import noumena.payment.bean.OrdersBean;
import noumena.payment.model.Orders;
import noumena.payment.util.Constants;
import noumena.payment.util.DateUtil;
import noumena.payment.util.OSUtil;
import noumena.payment.util.StringEncrypt;
import noumena.payment.vo.OrderIdVO;
import noumena.payment.vo.OrderStatusVO;

public class PPTVCharge {
	private static PPTVParams params = new PPTVParams();
	private static boolean testmode = false;

	public static boolean isTestmode() {
		return testmode;
	}

	public static void setTestmode(boolean testmode) {
		PPTVCharge.testmode = testmode;
	}

	public static String getTransactionId(Orders order) {
		order.setCurrency(Constants.CURRENCY_RMB);
		order.setUnit(Constants.CURRENCY_UNIT_YUAN);

		OrdersBean bean = new OrdersBean();
		String cburl = order.getCallbackUrl();
		String payId;
		if (cburl == null || cburl.equals("")) {
			payId = bean.CreateOrder(order);
		} else {
			if (cburl.indexOf("?") == -1) {
				cburl += "?pt=" + Constants.PAY_TYPE_PPTV;
			} else {
				cburl += "&pt=" + Constants.PAY_TYPE_PPTV;
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

	public static String checkOrdersStatus(String payIds) {
		String[] orderIds = payIds.split(",");

		OrdersBean bean = new OrdersBean();
		List<Orders> orders = bean.qureyOrders(orderIds);
		List<OrderStatusVO> statusret = new ArrayList<OrderStatusVO>();
		for (int i = 0; i < orders.size(); i++) {
			try {
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
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		JSONArray arr = JSONArray.fromObject(statusret);

		return arr.toString();
	}

	public static String getCallbackFromPPTV(Map<String, Object> pptvparams) {
		String ret = "{\"code\":\"1\",\"message\":\"\"}";
		String orderid = "";
		String sporderid = "";
		String orderAmount = "";
		String sign = "";

		try {
			orderid = (String) pptvparams.get("extra");
			sporderid = (String) pptvparams.get("oid");
			orderAmount = (String) pptvparams.get("amount");
			sign = (String) pptvparams.get("sign");

			OrdersBean bean = new OrdersBean();
			Orders order = bean.qureyOrder(orderid);
			if (order != null) {

				String minwen = "";
				String miwen = "";
				minwen += pptvparams.get("sid");
				minwen += pptvparams.get("username");
				minwen += pptvparams.get("roid");
				minwen += pptvparams.get("oid");
				minwen += pptvparams.get("amount");
				minwen += pptvparams.get("time");
				minwen += params.getParams(order.getSign()).getAppkey();
				miwen = StringEncrypt.Encrypt(minwen);

				if (sign.equals(miwen)) {
					// 支付成功
					if (order.getKStatus() != Constants.K_STSTUS_SUCCESS) {
						bean.updateOrderAmountPayIdExinfo(orderid, sporderid,
								orderAmount,
								(String) pptvparams.get("username"));
						bean.updateOrderKStatus(orderid,
								Constants.K_STSTUS_SUCCESS);
					} else {
						System.out.println("pptv order (" + order.getOrderId()
								+ ") had been succeed");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("pptv cb->" + pptvparams.toString());

		String path = OSUtil.getRootPath() + "../../logs/pptvcb/"
				+ DateUtil.getCurTimeStr().substring(0, 8);
		OSUtil.makeDirs(path);
		String filename = path + "/" + orderid;

		OSUtil.saveFile(filename, pptvparams.toString());

		return ret;
	}
    /***
     * 新的支付回调
     * @param pptvparams
     * @return
     */
	public static String getNewCallbackFromPPTV(Map<String, String> pptvparams) {
		System.out
				.println("getNewCallbackFromPPTv==================order param"
						+ pptvparams.toString());
		String amount = pptvparams.get("amount");
		String ext = pptvparams.get("ext");
		String gold = pptvparams.get("gold");
		String outTradeNo = pptvparams.get("out_trade_no");
		String platform = pptvparams.get("platform");
		String sid = pptvparams.get("sid");
		String sign = pptvparams.get("sign");
		String subPlatform = pptvparams.get("sub_platform");
		String time = pptvparams.get("time");
		String tradeNo = pptvparams.get("trade_no");
		String userId = pptvparams.get("user_id");
		OrdersBean bean = new OrdersBean();
		Orders order = bean.qureyOrder(outTradeNo);
		if (order == null) {
			System.out
					.println(outTradeNo
							+ "getNewCallbackFromPPTv==================order is not exist");
			return "{\"code\":\"1009\",\"message\":\"未知查询结果\"}";
		}
		String perSign = "amount=" + amount + "&ext="+ext+"&gold=" + gold
				+ "&out_trade_no=" + outTradeNo +"&platform=" + platform+ "&sid=" + sid+ "&sub_platform=" + subPlatform  + "&time="
				+ time + "&trade_no=" + tradeNo + "&user_id=" + userId
				+ params.getParams(order.getSign()).getAppkey();
		System.out.println(outTradeNo
				+ "getNewCallbackFromPPTv==================perSign" + perSign);
		String endsign = StringEncrypt.Encrypt(perSign);
		System.out.println(outTradeNo
				+ "getNewCallbackFromPPTv==================endsign" + endsign);
		if (!sign.equals(endsign)) {
			System.out
					.println(outTradeNo
							+ "getNewCallbackFromPPTv==================sign is different"
							+ endsign + " " + sign);
			return "{\"code\":\"1001\",\"message\":\"签名错误\"}";
		}
		if (!order.getAmount().equals(Float.parseFloat(amount))) {
			System.out.println(outTradeNo
					+ "getNewCallbackFromPPTv==================amount is error"
					+ order.getAmount() + " " + amount);
			return "{\"code\":\"1006\",\"message\":\"充值金额错误\"}";
		}
		if (!order.getKStatus().equals(Constants.K_STSTUS_DEFAULT)) {
			System.out
					.println(outTradeNo
							+ "getNewCallbackFromPPTv==================order already use");
			return "{\"code\":\"1005\",\"message\":\"订单号重复\"}";
		}
		bean.updateOrderAmountPayIdExinfo(outTradeNo, tradeNo, amount, gold
				+ "/" + platform + "/" + subPlatform + "/" + ext);
		bean.updateOrderKStatus(outTradeNo, Constants.K_STSTUS_SUCCESS);
		return "{\"code\":\"1\",\"message\":\"充值成功\"}";
	}

	public static OrderIdVO getTransactionIdVO(Orders order) {
		order.setCurrency(Constants.CURRENCY_RMB);
		order.setUnit(Constants.CURRENCY_UNIT_YUAN);

		OrdersBean bean = new OrdersBean();
		String cburl = order.getCallbackUrl();
		String payId;
		if (cburl == null || cburl.equals("")) {
			payId = bean.CreateOrder(order);
		} else {
			if (cburl.indexOf("?") == -1) {
				cburl += "?pt=" + order.getPayType();
			} else {
				cburl += "&pt=" + order.getPayType();
			}
			cburl += "&currency=" + Constants.CURRENCY_RMB;
			cburl += "&unit=" + Constants.CURRENCY_UNIT_YUAN;

			payId = bean.CreateOrder(order, cburl);
		}
		order.setCallbackUrl(cburl);
		String date = DateUtil.formatDate(order.getCreateTime());
		return new OrderIdVO(payId, date);
	}

	public static void init() {
		params.initParams(PPTVParams.CHANNEL_ID, new PPTVParamsVO());

		// params.addApp("gaoguai", "ggsg", "0b2fcee79a0e983367c04307647c90ff");
		// //53f478bafd98c51aa200560a
	}
}
