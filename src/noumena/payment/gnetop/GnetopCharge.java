package noumena.payment.gnetop;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

public class GnetopCharge {

	private static GnetopParams params = new GnetopParams();
	private static boolean testmode = false;
	private static String appid = "gnetop";

	public static boolean isTestmode() {
		return testmode;
	}

	public static void setTestmode(boolean testmode) {
		GnetopCharge.testmode = testmode;
	}

	public static void init() {
		params.initParams(GnetopParams.CHANNEL_ID, new GnetopVO());
	}

	/***
	 * 获取订单号
	 * 
	 * @param order
	 * @return
	 */
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
				cburl += "?pt=" + Constants.PAY_TYPE_GNOTOP;
			} else {
				cburl += "&pt=" + Constants.PAY_TYPE_GNOTOP;
			}
			cburl += "&currency=" + Constants.CURRENCY_RMB;
			cburl += "&unit=" + Constants.CURRENCY_UNIT_FEN;

			payId = bean.CreateOrder(order, cburl);
		}
		order.setCallbackUrl(cburl);
		String date = DateUtil.formatDate(order.getCreateTime());
		OrderIdVO orderIdVO = new OrderIdVO(payId, date);

		JSONObject json = JSONObject.fromObject(orderIdVO);
		return json.toString();
	}

	/***
	 * 检验订单状态
	 * 
	 * @param payIds
	 * @return
	 */
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

	/***
	 * 回调
	 * 
	 * @param vo
	 * @return
	 */
	public static String getCallbackFromGnetop(GnetopOrderVO vo,
			String secondValues) {
		System.out.println("=====(" + DateUtil.getCurTimeStr()
				+ ")=====channel(Gnetop cb params)->" + vo.toString());

		String ret = "{\"result\":\"%d\",\"msg\":\"%s\",\"data\":\"%f\"}";
		String orderid = vo.getOrderId();
		try {
			OrdersBean bean = new OrdersBean();
			Orders order = bean.qureyOrder(orderid);
			if (order == null) {
				ret ="{\"result\":\"error\",\"msg\":\"order is not exit\",\"data\":\"\"}";
				return ret;
			}
			// 得到加密字符串
			String sign = "POST" + secondValues
					+ params.getParams(appid).getSecretkey() + vo.getTime();
			sign = StringEncrypt.Encrypt(sign);
			if (sign.equals(order.getSign())) {
				ret ="{\"result\":\"error\",\"msg\":\"sign is error\",\"data\":\"\"}";
				return ret;
			}

			// 服务器签名验证成功
			if (order.getKStatus() != Constants.K_STSTUS_SUCCESS) {
				bean.updateOrderAmountPayIdExinfo(orderid, orderid,
						vo.getAmount(), vo.getUserId());
				bean.updateOrderKStatus(orderid, Constants.K_STSTUS_SUCCESS);
			} else {
				System.out.println("=====(" + DateUtil.getCurTimeStr()
						+ ")=====channel(Gnetop cb) order ("
						+ order.getOrderId() + ") had been succeed");
			}

			ret ="{\"result\":\"success\",\"msg\":\"success\",\"data\":\"\"}";
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Gnetop cb ret->" + ret);

		String path = OSUtil.getRootPath() + "../../logs/gnetopcb/"
				+ DateUtil.getCurTimeStr().substring(0, 8);
		OSUtil.makeDirs(path);
		String filename = path + "/" + orderid;

		OSUtil.saveFile(filename, vo.toString());

		return ret;
	}
}
