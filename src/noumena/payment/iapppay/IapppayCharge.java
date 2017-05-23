package noumena.payment.iapppay;

import java.util.Map;

import net.sf.json.JSONObject;
import noumena.payment.bean.OrdersBean;
import noumena.payment.huawei.HuaweiParams;
import noumena.payment.huawei.HuaweiParamsVO;
import noumena.payment.model.Orders;
import noumena.payment.util.Constants;

public class IapppayCharge {
	private static String PLATP_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCU52nbWmi8jQB4etHNKkOWCarx6fm5aQw5d1PDaw46Dj8QOkBWfy4h9YVeA/Kv9su3h2pj+3fC0EblAjICXby0D7De7IZi2IPZVvkD5yxY3mgn+bZpPCsi45y6qGJdAIbHgSA0VtUKekBpxBhBivH1LVJGhD8/DqtoU/JK1ZIx1QIDAQAB";
	private static IappayParams params = new IappayParams();
	public static String getCallbackFromIapppay(
			Map<String, String> iapppaynparams) {
		try {
			String transdata = iapppaynparams.get("transdata");
			System.out.println("getCallbackFromIapppay==================="
					+ transdata);
			String sign = iapppaynparams.get("sign"); // 对transdata的签名数据
			String signtype = iapppaynparams.get("signtype"); // 签名算法类型
			JSONObject jsonObject = JSONObject.fromObject(transdata);
			String money = jsonObject.getString("money"); // 交易金额
			String cporderid = jsonObject.getString("cporderid"); // 商户订单号
			String transtime = jsonObject.getString("transtime"); // 交易完成时间
			String transid = jsonObject.getString("transid"); // 交易流水号 计费支付平台的交易流水号
			String appid = jsonObject.getString("appid"); // 游戏id	
		
		
		/*
		 * String transtype = jsonObject.getString("transtype"); // 交易类型 String
		 * cporderid = jsonObject.getString("cporderid"); // 商户订单号 String
		 * transid = jsonObject.getString("transid"); // 交易流水号 计费支付平台的交易流水号
		 * String appuserid = jsonObject.getString("appuserid"); // 用户在商户应用的唯一标识
		 * // 用户在商户应用的唯一标识 String appid = jsonObject.getString("appid"); // 游戏id
		 * 平台为商户应用分配的唯一代码 String waresid = jsonObject.getString("waresid"); //
		 * 商品编码 // 平台为应用内需计费商品分配的编码 String feetype =
		 * jsonObject.getString("feetype"); // 计费方式 String money =
		 * jsonObject.getString("money"); // 交易金额 //
		 * 本次交易的金额（请务必严格校验商品金额与交易的金额是否一致） String currency =
		 * jsonObject.getString("currency"); // 货币类型 货币类型以及单位：RMB // – //
		 * 人民币（单位：元） String result = jsonObject.getString("result"); // 交易结果 //
		 * 交易结果：0–交易成功1–交易失败 String transtime =
		 * jsonObject.getString("transtime"); // 交易完成时间 // 交易时间格式：yyyy-mm-dd //
		 * hh24:mi:ss String cpprivate = jsonObject.getString("cpprivate"); //
		 * 商户私有信息 商户私有信息 String paytype = jsonObject.getString("paytype"); //
		 * 支付方式
		 */if (signtype == null) {
			System.out
					.println(cporderid
							+ "getCallbackFromIapppay ===================signtype is empty");
			return "fail";
		} else {
			if (SignHelper.verify(transdata, sign, params.getParams(appid).getAppkey())) {
				System.out
						.println(cporderid
								+ "getCallbackFromIapppay ===================verify ok");
				OrdersBean bean = new OrdersBean();
				Orders order = bean.qureyOrder(cporderid);
				if (order == null) {
					System.out
							.println(cporderid
									+ "getCallbackFromIapppay ===================order is not exit");
					return "fail";
				}
				if (order.getAmount() != Float.valueOf(money)) {
					System.out
							.println(cporderid
									+ "getCallbackFromIapppay ===================money is different order amount"
									+ order.getAmount() + "pay money" + money);
					return "fail";
				}
				order.setMoney(money);
				order.setCompleteTime(transtime);
				order.setPayId(transid);
				bean.updateOrder(cporderid, order);
				// 更新订单状态
				bean.updateOrderKStatus(cporderid, Constants.K_STSTUS_SUCCESS);
				System.out
						.println(cporderid
								+ "getCallbackFromIapppay ===================order success");
				return "SUCCESS";
			} else {
				System.out
						.println(cporderid
								+ "getCallbackFromIapppay==================verify fail");
				return "fail";
			}

		}
		} catch (Exception e) {
			return "fail";
		}
	}
	public static void init()
	{
		params.initParams(IappayParams.CHANNEL_ID, new IappayParamsVO());
//		params.addApp("M3","10215916","9vqsxiuyirsjvm0vt9pfzmqk20r2gmes"); //正义红师
//		params.addApp("M5","10215522","am5kwtyfusr1e5jirbnqyih49lpjfr1m"); //真!吞食天地
	}
}
