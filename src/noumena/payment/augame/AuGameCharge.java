package noumena.payment.augame;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import noumena.payment.bean.OrdersBean;
import noumena.payment.model.Orders;
import noumena.payment.util.Constants;
import noumena.payment.util.DateUtil;
import noumena.payment.util.OSUtil;
import noumena.payment.vo.OrderIdVO;
import noumena.payment.vo.OrderStatusVO;

import com.kddi.market.alml.util.ALMLConstants;
import com.kddi.market.alml.util.Base64;

public class AuGameCharge {
	private static AuGameParams params = new AuGameParams();

	public static String getTransactionId(Orders order) {
		 order.setCurrency(Constants.CURRENCY_DIVERSE);
		order.setUnit(Constants.CURRENCY_UNIT_FEN);
		OrdersBean bean = new OrdersBean();
		String cburl = order.getCallbackUrl();
		String payId;
		if (cburl == null || cburl.equals("")) {
			payId = bean.CreateOrder(order);
		} else {
			if (cburl.indexOf("?") == -1) {
				cburl += "?pt=" + Constants.PAY_TYPE_AUGAME;
			} else {
				cburl += "&pt=" + Constants.PAY_TYPE_AUGAME;
			}
		    cburl += "&currency=" + Constants.CURRENCY_DIVERSE;
			cburl += "&unit=" + Constants.CURRENCY_UNIT_FEN;
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

	public static String getAuGameCallbackFrom(String resultCode,
			String receipt, String signature, String orderId, String amount,
			String otherid) throws Exception {
		System.out.println(orderId+"param "+resultCode+"/"+receipt+"/"+signature+"/"+amount+"/"+otherid);
		String ret = "{\"result\":\"false\",\"msg\":\"fail\"}";
		OrderStatusVO st = new OrderStatusVO();
		OrdersBean bean = new OrdersBean();
		Orders ordervo = bean.qureyOrder(orderId);
		// 订单是否存在
		if (ordervo == null) {
			System.out.println(orderId + "getAuGameCallbackFrom=====("
					+ DateUtil.getCurTimeStr()
					+ ")=====channel(AuGame )->orde is not exit");
			st.setStatus(2);
			return JSONObject.fromObject(st).toString();
		}
		// 判断订单成功状态
		if (ordervo.getKStatus() == Constants.K_STSTUS_SUCCESS) {
			System.out.println(orderId + "getAuGameCallbackFrom=====("
					+ DateUtil.getCurTimeStr()
					+ ")=====channel(AuGame )-> order is already  succerd  ");
			st.setStatus(1);
			return JSONObject.fromObject(st).toString();
		}
		System.out.println( orderId+"appkey "+params.getParams(ordervo.getAppId()));
		// 是否验证成功
		boolean is = onIssueReceiptResult(Integer.parseInt(resultCode),
				receipt, signature, params.getParams(ordervo.getAppId())
						.getAppkey());
		try {
			if (!is) {
				System.out.println(orderId + "getAuGameCallbackFrom=====("
						+ DateUtil.getCurTimeStr()
						+ ")=====channel(AuGame )->Receipt  not pass");
				st.setStatus(2);
				return JSONObject.fromObject(st).toString();
			}

			System.out.println(orderId + "getAuGameCallbackFrom=====("
					+ DateUtil.getCurTimeStr()
					+ ")=====channel(AuGame )-> Receipt  success  ");
			// 价值是否一样
			if (Float.parseFloat(amount) == ordervo.getAmount()) {
				bean.updateOrderAmountPayIdExinfo(orderId, otherid, amount, "");
				bean.updateOrderKStatus(orderId, Constants.K_STSTUS_SUCCESS);

				st.setPayId(orderId);
				st.setStatus(1);
				ret = JSONObject.fromObject(st).toString();
			} else {
				System.out.println(orderId + "getAuGameCallbackFrom=====("
						+ DateUtil.getCurTimeStr()
						+ ")=====channel(AuGame)->money is duff order"
						+ ordervo.getAmount() + "pay amount" + amount);
				st.setStatus(2);
				return JSONObject.fromObject(st).toString();
			}
		} catch (Exception e) {
			System.out.println(orderId + "getAuGameCallbackFrom=====("
					+ DateUtil.getCurTimeStr() + ")=====error  ");
			e.printStackTrace();
			st.setStatus(2);
			return JSONObject.fromObject(st).toString();
		}

		System.out.println("getAuGameCallbackFrom cb->" + params.toString());

		String path = OSUtil.getRootPath() + "../../logs/oppocb/"
				+ DateUtil.getCurTimeStr().substring(0, 8);
		OSUtil.makeDirs(path);
		String filename = path + "/" + orderId;

		OSUtil.saveFile(filename, params.toString());

		return ret;
	}

	public static boolean onIssueReceiptResult(int resultCode, String receipt,
			String signature, String key) throws Exception {

		PublicKey mPublicKey = null;
		// 对公开键进行解密
		byte[] decodePublicKey = Base64.decode(key, Base64.NO_WRAP);
		try {
			// RSA 加密
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			// 生成公开键
			EncodedKeySpec keySpec = new X509EncodedKeySpec(decodePublicKey);
			// 生成最终的公开键
			mPublicKey = keyFactory.generatePublic(keySpec);
		} catch (NoSuchAlgorithmException e) {
			return false;
		} catch (InvalidKeySpecException e) {
			// TODO
			return false;
		}
		if (ALMLConstants.ALML_SUCCESS == resultCode && null != signature) {
			// 領収書情報のデコード
			byte[] base64decode = Base64.decode(signature, Base64.NO_WRAP);
			// 復号オブジェクト
			Cipher cipher = null;
			try {
				// RSA 復号を指定
				cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			} catch (NoSuchAlgorithmException e) {
				// TODO
				return false;
			} catch (NoSuchPaddingException e) {
				// TODO
				return false;
			}
			// 復号オブジェクトの初期化
			try {
				// 復号モードと公開鍵を設定
				cipher.init(Cipher.DECRYPT_MODE, mPublicKey);
			} catch (InvalidKeyException e) {
				// TODO
				return false;
			}
			// 復号されたハッシュ値情報格納領域
			byte[] decode = null;
			// 復号の実施
			try {
				decode = cipher.doFinal(base64decode);
			} catch (IllegalBlockSizeException e) {
				// TODO
				return false;
			} catch (BadPaddingException e) {
				// TODO
				return false;
			}
			// 復号されたハッシュ値を文字列に変換
			String decodeHash = new String(decode);
			// MD5 ハッシュ用インスタンスを生成
			MessageDigest digest = null;
			try {
				digest = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				// TODO
				return false;
			}
			// ハッシュ値(バイト配列)更新
			digest.update(receipt.getBytes());
			// バイト配列を文字列に変換
			byte[] hashedReceiptB = digest.digest();
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < hashedReceiptB.length; i++) {
				if ((0xff & hashedReceiptB[i]) < 0x10) {
					// < 16（ <=F）の場合、 1 桁になるので、頭に "0"を追加
					// HEX
					hexString.append("0"
							+ Integer.toHexString((0xFF & hashedReceiptB[i])));
				} else {
					// HEX
					hexString.append(Integer
							.toHexString(0xFF & hashedReceiptB[i]));
				}
			} // 領収書のハッシュ値
			String hashedReceipt = hexString.toString();
			System.out.println("getAuGameCallbackFrom recipet=====("
					+ DateUtil.getCurTimeStr() + ")=====hashedReceipt"
					+ hashedReceipt + " decodeHash" + decodeHash);
			if (hashedReceipt.equals(decodeHash)) {
				// 一致
				return true;
			} else {
				// 不一致
				return false;
			}
		}
		return false;
	}

	public static void init() {
		params.initParams(AuGameParams.CHANNEL_ID, new AuGameParamsVO());
	}

	public static void main(String args[]) {// 0c8865930fc94d8d61b1bdcc8312a65d
		String res = "";
		try {
			URL url = new URL(
					"http://game.jinggle.net:8080/payment/kongzhong/notice?orderid=J201617370802000001&goodid=SHIPHUNTER_DIAMOND_01&goodname=花费6购买60钻石&serverid=200&userid=62733206723035136&pt=5049&currency=RMB&unit=100&status=1&payId=2016062217371432508Z&flowid=2016062221001004650271412320&payrealprice=6.00");
			System.out.println(url);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setInstanceFollowRedirects(false);
			connection.setUseCaches(false);
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			connection.setRequestProperty("charset", "utf-8");

			connection.connect();

			DataOutputStream wr = new DataOutputStream(
					connection.getOutputStream());
			wr.flush();
			wr.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String line = null;
			while ((line = in.readLine()) != null) {
				res += line;
			}
			connection.disconnect();

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(res);
	}

}
