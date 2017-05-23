package noumena.payment.dao.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import noumena.payment.alipay.AlipayCharge;
import noumena.payment.model.Orders;
import noumena.payment.util.Constants;
import noumena.payment.util.DateUtil;

/**
 * @author LiangJun
 * 支付宝钱包
 *
 */
public class AlipayServlet extends HttpServlet
{

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
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doPost(request, response);
	}

	/**
	 * The doPost method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to
	 * post.
	 * 
	 * model:
	 * 		1 - 客户端请求支付
	 * 		2 - 客户端请求验证订单
	 * 		11 - 小米请求Token服务成功状态回调
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
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		request.setCharacterEncoding("utf-8");
		
		//必需的参数
		String stype = request.getParameter("model");			//请求的类型：1-得到交易id；2-查询订单状态
		String uid = request.getParameter("uid");				//
		String pkgid = request.getParameter("pkgid");			//
		String itemid = request.getParameter("itemid");			//
		String payprice = request.getParameter("price");		//
		String cburl = request.getParameter("cburl");			//单机游戏可以不需要回调地址
		
		//选填的参数
		String imei = request.getParameter("imei");				//
		String channel = request.getParameter("channel");		//
		String device_type = request.getParameter("device_type");	//
		String device_id = request.getParameter("device_id");	//
		String gversion = request.getParameter("gversion");		//
		String osversion = request.getParameter("osversion");	//

		//支付方的参数
		String subject = request.getParameter("name");			//产品名称
		String body = request.getParameter("desc");				//产品描述
		String authcode = request.getParameter("alipay_auth_code");			//授权码
		String refreshtoken = request.getParameter("alipay_refresh_token");	//刷新token
		String appid = request.getParameter("alipay_app_id");				//appid
		String alipayid = request.getParameter("alipay_user_id");			//支付宝id

		//验证订单用参数
		String payIds = request.getParameter("payIds");			//待查询的所有订单号，以“,”分隔

		String ret = "";
		if (stype.equals("1"))
		{
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
			if (payprice == null || payprice.equals(""))
			{
				payprice = "0.00";
			}
			else
			{
				payprice = new DecimalFormat("0.00").format(new Float(payprice));
			}
			vo.setAmount(Float.valueOf(payprice));
			vo.setCreateTime(DateUtil.getCurrentTime());
			vo.setExInfo(subject);
			vo.setPayType(Constants.PAY_TYPE_ALIPAY);
			vo.setCallbackUrl(cburl);
			System.out.print(cburl+"=============================wewewewewewe");
			ret = AlipayCharge.getTransactionId(vo, body, authcode, refreshtoken, appid, alipayid);
		}
		else
		{
			ret = AlipayCharge.checkOrdersStatus(payIds);
		}

		response.setCharacterEncoding("utf-8");
		System.out.println("alipay order id->" + ret);
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println(ret);
		out.flush();
		out.close();
	}

	/**
	 * {"msg":"_input_charset=\"UTF-8\"
	 * &body=\"\"
	 * &notify_url=\"http%3A%2F%2Fp.ko.cn%2Fpay%2Falipaycb\"
	 * &out_trade_no=\"201605231641382913CU\"
	 * &partner=\"2088701535333835\"
	 * &payment_type=\"1\"
	 * &seller_id=\"2088701535333835\"
	 * &service=\"mobile.securitypay.pay\"
	 * &subject=\"?荤.\"&total_fee=\"1.0\"
	 * &sign=\"R9q3rBCbKw0EAEuEKTKldioNfRk7AxtG3oZNycLUw159FFX6ggbsRqRkUZ0%2BSW%2FZosJt18OoxG6p2e9077g8QnSKlDN86z2veAUP7Sx2z%2FgrHDiFY9TDOL%2Fml9xlb46YogVwchu3XOCYZjINh1hhOxgc92MeqaqozJ4szbBzY%3D\"
	 * &sign_type=\"RSA\"","payId":"201605231641382913CU","time":"20160523164138","token":""}
	 */
	/**
	 * Initialization of the servlet. <br>
	 * 
	 * @throws ServletException
	 *             if an error occure
	 */
	public void init() throws ServletException
	{
		// Put your code here
	}

}
