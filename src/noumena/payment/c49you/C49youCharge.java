package noumena.payment.c49you;

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

public class C49youCharge
{
	private static C49youParams params = new C49youParams();
	
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
				cburl += "?pt=" + Constants.PAY_TYPE_SIJIUYOU;
			}
			else
			{
				cburl += "&pt=" + Constants.PAY_TYPE_SIJIUYOU;
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
	
	public static String getCallbackFrom49you(Map<String,String> c49youparams)
	{
		String ret = "fail";
		System.out.println("49you cb ->" + c49youparams.toString());
		
		C49youOrderVO covo = new C49youOrderVO();
		covo.setOrderId(c49youparams.get("orderId"));
		covo.setUid(c49youparams.get("uid"));
		covo.setAmount(c49youparams.get("amount"));
		covo.setServerId(c49youparams.get("serverId"));
		covo.setExtraInfo(c49youparams.get("extraInfo"));
		
		OrdersBean bean = new OrdersBean();
		Orders order = bean.qureyOrder(covo.getExtraInfo());
		String miwen = "";
		miwen+= covo.getOrderId()+covo.getUid()+covo.getServerId()+covo.getAmount()+covo.getExtraInfo()+params.getParams(order.getSign()).getAppkey();
		String sign = StringEncrypt.Encrypt(miwen);
		System.out.println("49youmiwen:"+miwen+"     sign:"+sign+"     49yousign:"+c49youparams.get("sign"));
			try {
				if(sign.equals(c49youparams.get("sign"))){
					// 支付成功
					if (order != null) {
						if(order.getAmount().equals(Float.valueOf(covo.getAmount()))){
							if (order.getKStatus() != Constants.K_STSTUS_SUCCESS) {
								bean.updateOrderAmountPayIdExinfo(covo.getExtraInfo(), covo.getOrderId(), covo.getAmount(), "");
								bean.updateOrderKStatus(covo.getExtraInfo(),Constants.K_STSTUS_SUCCESS);
							} else {
								System.out.println("49you order ("+ order.getOrderId() + ") had been succeed");
							}
							ret = "success";
						}else{
							System.out.println(covo.getExtraInfo()+"：jine_is_diff");
						}
					}
				}else{
					System.out.println("49you order ("+ covo.getExtraInfo() + ") sign is error");
					ret = "sign";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			System.out.println("49you cb ret->" + ret);

			String path = OSUtil.getRootPath() + "../../logs/c49youcb/"
					+ DateUtil.getCurTimeStr().substring(0, 8);
			OSUtil.makeDirs(path);
			String filename = path + "/" + covo.getExtraInfo();

			OSUtil.saveFile(filename, c49youparams.toString());
		return ret;
	}
	
	
	public static void init()
	{
		params.initParams(C49youParams.CHANNEL_ID, new C49youParamsVO());
	}
	
	public static void main(String args[]){//0c8865930fc94d8d61b1bdcc8312a65d
		
	}

}
