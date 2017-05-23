package noumena.payment.dao.servlet.newmycard;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import noumena.payment.model.Orders;
import noumena.payment.newmycard.NewMyCardTWCharge;
import noumena.payment.util.Constants;
import noumena.payment.util.DateUtil;

public class NewMyCardServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The doGet method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * The doPost method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to
	 * post.
	 * 
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=UTF-8");
		String stype = request.getParameter("model"); // 请求的类型：1-得到交易id；2-查询订单状态
		// String gameid = request.getParameter("appid"); // tstore的appid
		String productid = request.getParameter("productid"); // tstore的付费id
		String imei = request.getParameter("imei"); // imei
		String accountid = request.getParameter("uid"); // uid
		String payprice = request.getParameter("price"); // 钱
		String cburl = request.getParameter("cburl"); // 回调游戏的url
		String channel = request.getParameter("channel"); // 渠道号
		String device_type = request.getParameter("device_type"); // 设备类型，iPhone/ANDK
		String device_id = request.getParameter("device_id"); // 设备id
		String gversion = request.getParameter("gversion"); // 游戏版本
		String osversion = request.getParameter("osversion"); // 操作系统版本
		// String payTypeId = request.getParameter("payTypeId"); // 不要
		String payIds = request.getParameter("payIds"); // 待查询的所有订单号，以“,”分隔
		String subId = request.getParameter("subid"); // sub id
		// String ip = request.getRemoteAddr();

		String pkgid = request.getParameter("pkgid"); // tstore的付费id
		String productName = request.getParameter("productName"); // tstore的付费id
		if(productName!=null){
		productName=productName.replace("%", "").replace("超值", "");
		}
		String trainType = request.getParameter("trainType"); // 交易类型
		String authCode = request.getParameter("authCode"); // 交易类型
		String payId = request.getParameter("payId"); // 订单号
		String cuurrency = request.getParameter("cuurrency");// 、币种
		String facServiceId = request.getParameter("facServiceId");// 廠商服務代碼
		String serverId = request.getParameter("serverId");// 伺服器代號
		String itemCode = request.getParameter("itemid");// 品項代碼
		String paymentType = request.getParameter("paymentType");// 支付类型
		if (payprice == null || payprice.equals("")) {
			payprice = "0.00";
		} else {
			payprice = new DecimalFormat("0.00").format(new Float(payprice));
		}
		Orders vo = new Orders();
		vo.setImei(imei);
		vo.setUId(accountid);
		vo.setProductId(productid);
		vo.setItemId(itemCode);
		vo.setGversion(gversion);
		vo.setOsversion(osversion);
		vo.setDeviceId(device_id);
		vo.setDeviceType(device_type);
		vo.setChannel(channel);
		vo.setAppId(pkgid);
		vo.setAmount(Float.valueOf(payprice));
		vo.setCreateTime(DateUtil.getCurrentTime());
		vo.setPayType(getPayTypeIdById(paymentType));
		vo.setCallbackUrl(cburl);
		vo.setSubId(subId);

		String ret = "";
		// 获取订单号，并请求到交易授权码
		if (stype == null) {
			System.out
					.println("newmycardtw  model is error===========================================");
		} else if (stype.equals("1")) {
			System.out.println("mycardtw app id=================>"+vo.toString()
					+ vo.getAppId());
			System.out.println("mycardtw product id===================>"
					+ vo.getProductId() + productName);
			System.out.println("mycardtw sub id========================>"
					+ vo.getSubId());

			ret = NewMyCardTWCharge.getTransactionId(vo, productName,
					trainType, cuurrency, facServiceId, serverId, paymentType);
			// 单纯的检验交易状态
		} else if (stype.equals("2")) {
			System.out.println("NewMyCardTWCharge reciepOrders   =====("
					+ DateUtil.getCurTimeStr() + ")=========");
			ret = NewMyCardTWCharge.checkOrdersInterance(payId, authCode);
		}
		// 从支付中心数据库中验证订单状态
		else if (stype.equals("3")) {
			System.out.println("mycardtw check order ids->" + payIds);
			ret = NewMyCardTWCharge.checkOrdersStatus(payIds);
		} else {
			System.out
					.println("newmycardtw  model is error===========================================");
		}

		System.out.println("mycardtw order id ret->" + ret);
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println(ret);
		out.flush();
		out.close();
	}

	private String getPayTypeIdById(String id) {
		if (id == null || id.equals("INGAME")) {
			return Constants.PAY_TYPE_MYCARD_TW_BILLING;
		} else if (id.equals("1")) {
			return Constants.PAY_TYPE_MYCARD_TW_BILLING;
		} else if (id.equals("2")) {
			return Constants.PAY_TYPE_MYCARD_TW_INGAME_TW;
		} else if (id.equals("3")) {
			return Constants.PAY_TYPE_MYCARD_TW_INGAME_HK;
		} else if (id.equals("4")) {
			return Constants.PAY_TYPE_MYCARD_TW_POINT;
		}
		return Constants.PAY_TYPE_MYCARD_TW_BILLING;
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
