package noumena.payment.dao.servlet.lezhuo;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import noumena.payment.getorders.OrderUtil;
import noumena.payment.model.Orders;
import noumena.payment.util.Constants;
import noumena.payment.util.DateUtil;
import noumena.payment.vo.OrderStatusVO;

public class LeZhuoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	request.setCharacterEncoding("utf-8");
		
		//必需的参数
		String stype = request.getParameter("model");			//请求的类型：1-得到交易id；2-查询订单状态
		String uid = request.getParameter("uid");				//
		String pkgid = request.getParameter("pkgid");			//
		String itemid = request.getParameter("itemid");			//
		String payprice = request.getParameter("price");		//一定是人民币元
		String cburl = request.getParameter("cburl");			//单机游戏可以不需要回调地址
		
		//选填的参数
		String imei = request.getParameter("imei");				//
		String channel = request.getParameter("channel");		//
		String device_type = request.getParameter("device_type");	//
		String device_id = request.getParameter("device_id");	//
		String gversion = request.getParameter("gversion");		//
		String osversion = request.getParameter("osversion");	//

		//支付方的参数
		
		//验证订单用参数
		String payIds = request.getParameter("payIds");			//待查询的所有订单号，以“,”分隔

		String ret = "";
		if (stype == null) {
			stype = "";
		}
		if (stype.equals("1")) {
			Orders vo = new Orders();
			vo.setImei(imei);
			vo.setUId(uid);
			vo.setItemId(itemid);
			vo.setGversion(gversion);
			vo.setOsversion(osversion);
			vo.setDeviceId(device_id);
			vo.setDeviceType(device_type);
			vo.setChannel(channel);
			vo.setAppId(pkgid);
			// vo.setSign(u_id);
			if (payprice == null) {
				payprice = "0";
			}
			vo.setAmount(Float.valueOf(payprice));
			vo.setCreateTime(DateUtil.getCurrentTime());
			vo.setPayType(Constants.PAY_TYPE_LeZuo);
			vo.setCallbackUrl(cburl);
			vo.setCurrency(Constants.CURRENCY_RMB);
			vo.setUnit(Constants.CURRENCY_UNIT_YUAN);
			ret = OrderUtil.getTransactionId(vo, Constants.PAY_TYPE_LeZuo+"", "");
		} else if (stype.equals("2")) {
			OrderStatusVO otherst =new OrderStatusVO();
			otherst.setStatus(2);
			ret = OrderUtil.checkOrdersStatus(payIds, otherst);
		} else {
			System.out
					.println("lezhuo model invalid------------------------------------------------->"
							+ stype);
			ret = "invalid";
		}

		System.out.println("lezhuo order id->(" + uid + ")" + ret);
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println(ret);
		out.flush();
		out.close();
	}

	/**
	 * Initialization of the servlet. <br>
	 * 
	 * @throws ServletException
	 *             if an error occure
	 */
	public void init() throws ServletException {
		// Put your code here
	}
}
