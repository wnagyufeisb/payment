package noumena.payment.dao.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import noumena.payment.qihu.QihuCBOrderVO;
import noumena.payment.qihu.QihuCharge;
import noumena.payment.util.DateUtil;
import noumena.payment.util.OSUtil;

public class QihucbServlet extends HttpServlet {

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
	 * model: 1 - 客户端请求支付 2 - 客户端请求验证订单 11 - 小米请求Token服务成功状态回调
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
		// 接受360Pay返回的参数
		QihuCBOrderVO cbvo = new QihuCBOrderVO();
		StringBuffer buffer = new StringBuffer();
		StringBuffer signstr = new StringBuffer();

		String amount = request.getParameter("amount"); // 总价,单位：分
		if (amount != null && !amount.equals("")) {
			signstr.append(amount);
		}
		buffer.append("&");
		buffer.append("amount=");
		buffer.append(amount);
		cbvo.setAmount(amount);

		String app_ext1 = request.getParameter("app_ext1"); // 内部订单号
		if (app_ext1 != null && !app_ext1.equals("")) {
			signstr.append("#");
			signstr.append(app_ext1);
		}
		buffer.append("&");
		buffer.append("app_ext1=");
		buffer.append(app_ext1);
		cbvo.setApp_ext1(app_ext1);

		String app_ext2 = request.getParameter("app_ext2"); // 应用扩展信息2
		if (app_ext2 != null && !app_ext2.equals("")) {
			signstr.append("#");
			signstr.append(app_ext2);
		}
		buffer.append("&");
		buffer.append("app_ext2=");
		buffer.append(app_ext2);
		cbvo.setApp_ext2(app_ext2);

		String app_key = request.getParameter("app_key"); // 应用App key（加入签名）
		if (app_key != null && !app_key.equals("")) {
			signstr.append("#");
			signstr.append(app_key);
		}
		buffer.append("app_key=");
		buffer.append(app_key);
		cbvo.setApp_key(app_key);

		String app_order_id = request.getParameter("app_order_id"); // 应用订单号
		if (app_order_id != null && !app_order_id.equals("")) {
			signstr.append("#");
			signstr.append(app_order_id);
		}
		buffer.append("&");
		buffer.append("app_order_id=");
		buffer.append(app_order_id);
		cbvo.setApp_order_id(app_order_id);

		String app_uid = request.getParameter("app_uid"); // 用户在应用内的用户id
		if (app_uid != null && !app_uid.equals("")) {
			signstr.append("#");
			signstr.append(app_uid);
		}
		buffer.append("&");
		buffer.append("app_uid=");
		buffer.append(app_uid);
		cbvo.setApp_uid(app_uid);

		String gateway_flag = request.getParameter("gateway_flag"); // 如果支付返回成功，返回success，如果支付过程断路，返回空，如果支付失败，返回fail
		if (gateway_flag != null && !gateway_flag.equals("")) {
			signstr.append("#");
			signstr.append(gateway_flag);
		}
		buffer.append("&");
		buffer.append("gateway_flag=");
		buffer.append(gateway_flag);
		cbvo.setGateway_flag(gateway_flag);

		String order_id = request.getParameter("order_id"); // 应用平台接口生成的订单号
		if (order_id != null && !order_id.equals("")) {
			signstr.append("#");
			signstr.append(order_id);
		}
		buffer.append("&");
		buffer.append("order_id=");
		buffer.append(order_id);
		cbvo.setOrder_id(order_id);

		String product_id = request.getParameter("product_id"); // 支付产品id
		if (product_id != null && !product_id.equals("")) {
			signstr.append("#");
			signstr.append(product_id);
		}
		buffer.append("&");
		buffer.append("product_id=");
		buffer.append(product_id);
		cbvo.setProduct_id(product_id);

		String sign_type = request.getParameter("sign_type"); // 签名算法
		if (sign_type != null && !sign_type.equals("")) {
			signstr.append("#");
			signstr.append(sign_type);
		}
		buffer.append("&");
		buffer.append("sign_type=");
		buffer.append(sign_type);
		cbvo.setSign_type(sign_type);

		String user_id = request.getParameter("user_id"); // 360账号id
		if (user_id != null && !user_id.equals("")) {
			signstr.append("#");
			signstr.append(user_id);
		}
		buffer.append("&");
		buffer.append("user_id=");
		buffer.append(user_id);
		cbvo.setUser_id(user_id);

		String sign_return = request.getParameter("sign_return"); // 应用回传给订单核实接口的参数
		/*
		 * buffer.append("&"); buffer.append("sign_return=");
		 * buffer.append(sign_return);
		 */
		cbvo.setSign_return(sign_return);

		String sign = request.getParameter("sign"); // 提取的签名
		/*
		 * buffer.append("&"); buffer.append("sign="); buffer.append(sign);
		 */
		cbvo.setSign(sign);

		System.out.println("360 cb ->" + buffer.toString());
		System.out.println("360 cb sign ->" + signstr.toString());

		String resString = QihuCharge.getOrderCBFrom360(cbvo,
				signstr.toString());

		String path = OSUtil.getRootPath() + "../../logs/qihucb/"
				+ DateUtil.getCurTimeStr().substring(0, 8);
		OSUtil.makeDirs(path);
		String filename = path + "/" + cbvo.getApp_order_id();
		OSUtil.saveFile(filename, buffer.toString());

		PrintWriter out = response.getWriter();
		out.println(resString);
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
