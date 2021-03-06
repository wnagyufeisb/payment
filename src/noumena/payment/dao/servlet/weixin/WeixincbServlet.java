package noumena.payment.dao.servlet.weixin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;
import noumena.payment.weixin.WeixinCharge;
import noumena.payment.weixin.WeixinOrderVO;

public class WeixincbServlet extends HttpServlet
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
		response.setContentType("text/html;charset=UTF-8");

/**
 * <xml>
  <appid><![CDATA[wx2421b1c4370ec43b]]></appid>
  <attach><![CDATA[支付测试]]></attach>
  <bank_type><![CDATA[CFT]]></bank_type>
  <fee_type><![CDATA[CNY]]></fee_type>
  <is_subscribe><![CDATA[Y]]></is_subscribe>
  <mch_id><![CDATA[10000100]]></mch_id>
  <nonce_str><![CDATA[5d2b6c2a8db53831f7eda20af46e531c]]></nonce_str>
  <openid><![CDATA[oUpF8uMEb4qRXf22hE3X68TekukE]]></openid>
  <out_trade_no><![CDATA[1409811653]]></out_trade_no>
  <result_code><![CDATA[SUCCESS]]></result_code>
  <return_code><![CDATA[SUCCESS]]></return_code>
  <sign><![CDATA[B552ED6B279343CB493C5DD0D78AB241]]></sign>
  <sub_mch_id><![CDATA[10000100]]></sub_mch_id>
  <time_end><![CDATA[20140903131540]]></time_end>
  <total_fee>1</total_fee>
  <trade_type><![CDATA[JSAPI]]></trade_type>
  <transaction_id><![CDATA[1004400740201409030005092168]]></transaction_id>
</xml>

private String appid;
	private String mch_id;
	private String nonce_str;
	private String sign;
	private String result_code;
	private String trade_type;
	private String total_fee;
	private String fee_type;
	private String cash_fee;
	private String transaction_id;
	private String out_trade_no;
 */
		BufferedReader reader = request.getReader();
		String resultXml = "";
		String tmp = null;
		while((tmp = reader.readLine())!=null) {
			resultXml+=tmp;
		}
		System.out.println(resultXml);
		XMLSerializer xmlSerializer = new XMLSerializer();
		JSONObject jsonObj = JSONObject.fromObject(xmlSerializer.read(resultXml).toString());
		WeixinOrderVO w = new WeixinOrderVO();
		w.setAppid(jsonObj.getString("appid"));
		w.setMch_id(jsonObj.getString("mch_id"));
		w.setNonce_str(jsonObj.getString("nonce_str"));
		w.setSign(jsonObj.getString("sign"));
		w.setResult_code(jsonObj.getString("result_code"));
		w.setTrade_type(jsonObj.getString("trade_type"));
		w.setTotal_fee(jsonObj.getString("total_fee"));
		w.setFee_type(jsonObj.getString("fee_type"));
		w.setCash_fee(jsonObj.getString("cash_fee"));
		w.setTransaction_id(jsonObj.getString("transaction_id"));
		w.setOut_trade_no(jsonObj.getString("out_trade_no"));
		String ret = WeixinCharge.getCallbackFromWeixin(w);

		response.setContentType("text/html;charset=UTF-8");
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
	public void init() throws ServletException
	{
		// Put your code here
	}

}
