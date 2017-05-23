package noumena.payment.c185sy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import noumena.pay.util.MD5;
import noumena.payment.bean.OrdersBean;
import noumena.payment.model.Orders;
import noumena.payment.util.Constants;
import noumena.payment.util.DateUtil;
import noumena.payment.vo.OrderStatusVO;

public class C185syCharge {

	private static C185syParams params = new C185syParams();

	public static void init() {
		params.initParams(C185syParams.CHANNEL_ID, new C185syKVO());
	}

	private static String secretkey = "88187b6da5f1dc6592d2cab456a11992";
	public static SimpleDateFormat format = new SimpleDateFormat(
			"yyyyMMddHHmmss");

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

	public static String getCallbackFromSougou(Map<String, String> m) {
		try {
			System.out.println("getCallback185sy  cb ================>"
					+ m.toString());
			String username = m.get("username");
			String amount = m.get("amount");
			String rmb = m.get("rmb");
			String serverid = m.get("serverid");
			String channel = m.get("channel");
			String gameid = m.get("gameid");
			String product_id = m.get("product_id");
			String orderno = m.get("orderno");
			String sign = m.get("sign");
			String type3 = m.get("type3");
			String type4 = m.get("type4");
			String type5 = m.get("type5");
			// 验证参数
			if ("".equals(username) || username == null || "".equals(amount)
					|| amount == null || "".equals(rmb) || rmb == null
					|| "".equals(serverid) || serverid == null
					|| "".equals(product_id) || product_id == null
					|| "".equals(orderno) || orderno == null) {
				System.out
						.println("getCallback185sy=========result======>sign error");
				return "{\"errcode\":1,\"msg\":\"param error\"}";

			}
			String perSign = username + amount + rmb + serverid + orderno
					+ product_id + secretkey;
			System.out.println(type3
					+ "getCallback185sy===============>perSign" + perSign);
			String endsign = MD5.md5(perSign, "UTF-8");
			System.out.println(type3
					+ "getCallback185sy===============>endsign" + endsign);
			// 判断签名
			if (!sign.equals(endsign)) {
				System.out.println(type3
						+ "getCallback185sy=========result======>sign error");
				return "{\"errcode\":2,\"msg\":\"sign error\"}";
			}
			OrdersBean bean = new OrdersBean();
			Orders order = bean.qureyOrder(type3);
			// 订单不存在
			if (order == null) {
				System.out
						.println(type3
								+ "getCallback185sy=========result======>order is not exit");
				return "{\"errcode\":1,\"msg\":\"order is not exit\"}";
			}
			// 如果订单已经成功
			if (order.getKStatus() == Constants.K_STSTUS_SUCCESS) {
				System.out
						.println(type3
								+ "getCallback185sy=========result======>order is already success");
				return "{\"errcode\":4,\"msg\":\"order is already succeed\"}";
			}
			// 判断价格
			if (!(order.getAmount().equals(Float.valueOf(rmb)))) {
				System.out
						.println(type3
								+ "getCallback185sy=========result======>order amount is error"
								+ order.getAmount() + " " + Float.valueOf(rmb));
				return "{\"errcode\":1,\"msg\":\"rmb is error\"}";
			}

			bean.updateOrderAmountPayIdExinfo(type3, orderno, rmb, "");
			bean.updateOrderKStatus(type3, Constants.K_STSTUS_SUCCESS);
			System.out.println(type3
					+ "getCallback185sy=========result======>order is success");
			return "{\"errcode\":0,\"msg\":\"order is success\"}";
		} catch (Exception e) {
			System.out.println("getCallback185sy=========result======>error");
			e.printStackTrace();
			return "{\"errcode\":6,\"msg\":\"error\"}";
		}
	}

	public static String getCallbackFromNewC185(String mparam) {
		System.out.println("getCallbackFromNewC185  cb ================>"
				+ mparam);
		try {
			JSONObject objectPayNotifyUrl = JSONObject.fromObject(mparam);
			String data = objectPayNotifyUrl.getString("data");
			String state = objectPayNotifyUrl.getString("state");
			if (!"1".equals(state)) {
				return "FAIL";
			}
			JSONObject jsonObjectData = JSONObject.fromObject(data);
			String productID = jsonObjectData.getString("productID");// 商品ID
			String orderID = jsonObjectData.getString("orderID");// 订单号
			String userID = jsonObjectData.getString("userID");// 用户ID
			String channelID = jsonObjectData.getString("channelID");// 渠道ID
			String gameID = jsonObjectData.getString("gameID");// 游戏ID
			String serverID = jsonObjectData.getString("serverID");// 游戏服务器ID
			String money = jsonObjectData.getString("money");// 充值金额，单位分
			String currency = jsonObjectData.getString("currency");// 货币类型，默认RMB
			String extension = jsonObjectData.getString("extension");// 获取订单号服务器传过来的自定义参数，原样返回
			// String signType = jsonObjectData.getString("signType");//
			// 签名类型，目前支持md5，该字段不参与签名
			String sign = jsonObjectData.getString("sign");// 签名值(32位小写)。
			String appKey = params.getParams(gameID).getAppkey();
			StringBuilder sb = new StringBuilder();
			sb.append("channelID=").append(channelID).append("&")
					.append("currency=").append(currency).append("&")
					.append("extension=").append(extension).append("&")
					.append("gameID=").append(gameID).append("&")
					.append("money=").append(money).append("&")
					.append("orderID=").append(orderID).append("&")
					.append("productID=").append(productID).append("&")
					.append("serverID=").append(serverID).append("&")
					.append("userID=").append(userID).append("&")
					.append(appKey);
			System.out.println(extension
					+ "getCallbackFromNewC185===============>perSign"
					+ sb.toString());
			String signEndString = MD5.md5(sb.toString(), "UTF-8");
			System.out.println(extension
					+ "getCallbackFromNewC185===============>signEndString"
					+ signEndString);
			// 判断签名
			if (!sign.equals(signEndString)) {
				System.out
						.println(extension
								+ "getCallbackFromNewC185=========result======>sign error");
				return "FAIL";
			}

			OrdersBean bean = new OrdersBean();
			Orders order = bean.qureyOrder(extension);
			// 订单不存在
			if (order == null) {
				System.out
						.println(orderID
								+ "getCallbackFromNewC185=========result======>order is not exit");
				return "FAIL";
			}
			// 如果订单已经成功
			if (order.getKStatus() == Constants.K_STSTUS_SUCCESS) {
				System.out
						.println(extension
								+ "getCallbackFromNewC185=========result======>order is already success");
				return "SUCCESS";
			}
			Float amountZhuan=order.getAmount() * Float.parseFloat(order.getUnit());
			// 判断价格
			if (!(amountZhuan.equals(Float.valueOf(money)))) {
				System.out
						.println(orderID
								+ "getCallbackFromNewC185=========result======>order amount is error"
								+ order.getAmount() + " "+amountZhuan+" "
								+ Float.valueOf(money));
				return "FAIL";
			}

			bean.updateOrderAmountPayIdExinfo(extension, orderID, order.getAmount()+"",
					productID + " " + userID + " " + serverID);
			bean.updateOrderKStatus(extension, Constants.K_STSTUS_SUCCESS);
			System.out
					.println(extension
							+ "getCallbackFromNewC185=========result======>order is success");
			return "SUCCESS";

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "FAIL";
		}

	}

	/*
	 * public static void main(String args[]){
	 * System.out.println(MD5.getMessageDigest
	 * ("123123123123123123".getBytes())); }
	 */
}
