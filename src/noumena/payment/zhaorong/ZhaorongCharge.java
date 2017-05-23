package noumena.payment.zhaorong;

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

public class ZhaorongCharge
{
	private static ZhaorongParams params = new ZhaorongParams();
	private static boolean testmode = false;
	
	public static boolean isTestmode()
	{
		return testmode;
	}
	public static void setTestmode(boolean testmode)
	{
		ZhaorongCharge.testmode = testmode;
	}
	
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
				cburl += "?pt=" + Constants.PAY_TYPE_ZHAORONG;
			}
			else
			{
				cburl += "&pt=" + Constants.PAY_TYPE_ZHAORONG;
			}
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

	public static String getCallbackFromZhaorong(String orderid, String status, String money, String oporderid, String userid, String sign)
	{
		String ret = "SUCCESS";
		
		String minwen = "";
		minwen += "orderid=" + orderid;
		minwen += "&status=" + status;
		minwen += "&oporderid=" + oporderid;
		minwen += "&userid=" + userid;
		minwen += "&key=" + ZhaorongParams.KEY;
		String miwen = StringEncrypt.Encrypt(minwen);
		
		System.out.println("zhaorong minwen->" + minwen);
		System.out.println("zhaorong miwen->" + miwen);
		System.out.println("zhaorong sign->" + sign);
		
		if (miwen.equals(sign))
		{
			try
			{
				OrdersBean bean = new OrdersBean();
				Orders order = bean.qureyOrder(orderid);
				if (order != null)
				{
					if (order.getKStatus() != Constants.K_STSTUS_SUCCESS)
					{
						bean.updateOrderAmountPayIdExinfo(orderid, oporderid, money, userid);
						
						if (status.equals("0"))
						{
							//支付成功
							bean.updateOrderKStatus(orderid, Constants.K_STSTUS_SUCCESS);
						}
						else
						{
							//支付失败
							bean.updateOrderKStatus(orderid, Constants.K_STSTUS_ERROR);
						}
					}
					else
					{
						System.out.println("zhaorong order (" + order.getOrderId() + ") had been succeed");
					}
				}
				
				String path = OSUtil.getRootPath() + "../../logs/zhaorongcb/" + DateUtil.getCurTimeStr().substring(0, 8);
				OSUtil.makeDirs(path);
				String filename = path + "/" + orderid;
				
				OSUtil.saveFile(filename, minwen);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				ret = "FAILURE";
			}
		}
		else
		{
			ret = "FAILURE";
		}
		
		return ret;
	}
	
	public static void init()
	{
		params.addApp("gaoguai", "2201151", "4Lz7cuDLkSGUa0FhdbLBb74O");	//alT96LfBmgTAGndfGkkR9MHgiStVcEKT
	}
}
