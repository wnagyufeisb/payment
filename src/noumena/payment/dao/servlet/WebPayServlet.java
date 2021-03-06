package noumena.payment.dao.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import noumena.payment.bean.CallBackGameServBean;
import noumena.payment.bean.OrdersBean;
import noumena.payment.bean.PayGameBean;
import noumena.payment.bean.PayServerBean;
import noumena.payment.model.Orders;
import noumena.payment.model.PayGame;
import noumena.payment.model.PayServer;
import noumena.payment.util.Constants;
import noumena.payment.util.StringEncrypt;

public class WebPayServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor of the object.
	 */
	public WebPayServlet() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		return;
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("WebPayServlet======================>");
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		String retuid = request.getParameter("retuid");			//搞怪需要用验证用户返回的uid和sid当作参数传到支付通知地址中
		String retsid = request.getParameter("retsid");			//搞怪需要用验证用户返回的uid和sid当作参数传到支付通知地址中
		String game = request.getParameter("game");
		String server = request.getParameter("server");
		String username = request.getParameter("username");
		String usernamer = request.getParameter("usernamer");
		String info = request.getParameter("amount");
		String[] infos = info.split("#");
		float amount = 0;
		int gem = 0;
		if (game == null || server == null || username == null || !username.equals(usernamer) || info == null || infos.length < 4) {
			return;
		}
		try{
			amount = Float.parseFloat(infos[1]);
			gem = Integer.parseInt(infos[3]);
		}catch (Exception e) {
			return;
		}
		PayServerBean payServerBean = new PayServerBean();
		PayGameBean payGameBean = new PayGameBean();
		PayServer payServer = payServerBean.get(server);
		PayGame payGame = payGameBean.getGame(game);
		if (payServer == null || payGame == null) {
			return;
		}
		try {
			JSONArray jsonary = JSONArray.fromObject(payGame.getInfo());
			for (int i = 0; i < jsonary.size(); i++)
			{
				JSONObject json = jsonary.getJSONObject(i);
				if (json.getString("codeName").equals(infos[0])) 
				{
					float money = Float.parseFloat(json.getString("money"));
					if (amount < money)
					{
						return;
					}
					else
					{
						break;
					}
				}
			}
		} catch (Exception e) {
			return;
		}
		
		Date date = new Date();
		SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat df2 = new SimpleDateFormat("yyyyMMddHHmmss");
		String unit = infos[2];
		String codeName = infos[0];
		String cburl = payServer.getCallbackUrl();
		String paynotify = payServer.getPayNotify();
		server = payServer.getServerId().split("_")[0];
		String Parameter = "";
		Parameter += "userid=" + URLEncoder.encode(username, "utf-8");
		Parameter += "&gameid=" + game;
		Parameter += "&serverid=" + server;
		Parameter += "&itmeid=" + codeName;
		Parameter += "&itemid=" + codeName;
		Parameter += "&amount=" + (int)amount;
		Parameter += "&itemprice=" + gem;
		if (retuid != null && !retuid.equals(""))
		{
			Parameter += "&uid=" + retuid;
			Parameter += "&sid=" + retsid;
		}
		String cbsign = game+server+username+codeName+gem+(int)amount;
		cbsign = StringEncrypt.Encrypt(cbsign + Constants.P_KEY);
		if (cburl != null && !cburl.equals("")) {
			if (cburl.indexOf("?") == -1) {
				cburl += "?" + Parameter;
			} else {
				cburl += "&" + Parameter;
			}
			cburl += "&sign=" + cbsign;
		}
		
		
		Orders vo = new Orders();
		
		vo.setImei("");
		vo.setUId(username);
		vo.setChannel(Constants.getGameIdByAppId(game) + "A0ABE00A0000000");
		vo.setAppId(game);
		vo.setAmount(amount);
		vo.setCreateTime(df1.format(date));
		vo.setPayType(Constants.PAY_TYPE_DACHENG_WEB);
		vo.setItemId(codeName);
		vo.setItemPrice(String.valueOf(gem));
		vo.setItemNum(1);
		vo.setDeviceId(unit);
		vo.setExInfo("");
		vo.setCurrency(Constants.CURRENCY_RMB);
		vo.setUnit(Constants.CURRENCY_UNIT_YUAN);
		
		String payid;
		OrdersBean bean = new OrdersBean();
		if (cburl == null || cburl.equals("")) {
			payid = bean.CreateOrder(vo);
		} else {
			if (cburl.indexOf("?") == -1) {
				cburl += "?pt=" + Constants.PAY_TYPE_DACHENG_WEB;
			} else {
				cburl += "&pt=" + Constants.PAY_TYPE_DACHENG_WEB;
			}
			cburl += "&currency=" + Constants.CURRENCY_RMB;
			cburl += "&unit=" + Constants.CURRENCY_UNIT_YUAN;
			payid = bean.CreateOrder(vo, cburl);
		}
		
		String r = payid+game+server+username+codeName+gem+(int)amount;
	System.out.println(r);
		String gamesign = StringEncrypt.Encrypt(r + Constants.N_KEY);
		if (paynotify != null && !paynotify.equals("")) {
			if (paynotify.indexOf("?") == -1) {
				paynotify += "?" + Parameter;
			} else {
				paynotify += "&" + Parameter;
			}
			paynotify += "&payId=" + payid;
			paynotify += "&sign=" + gamesign;
			paynotify += "&status=888";
		}
		
		String payprice = new DecimalFormat("0.00").format(amount);
		CallBackGameServBean cbean = new CallBackGameServBean();
		try {
//			System.out.println(paynotify);
			String res = cbean.doGet(paynotify);
//			System.out.println(res);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		String url = Constants.PAY_WEB_URL;
		String tmp = "";
		tmp += payid;
		tmp += game;
		tmp += payprice;
		tmp += df2.format(date);
		String sign = StringEncrypt.Encrypt(tmp + Constants.WEB_KEY);
//		System.out.println("sign=" + tmp + Constants.WEB_KEY);
//		System.out.println("sign=" + sign);
		if (url.indexOf("?") < 0) {
			url += "?gameid=" + game;
			url += "&areaid=1";
			url += "&serverid=" + server;
			url += "&accountid=" + URLEncoder.encode(username, "utf-8");
			url += "&payprice=" + payprice;
			url += "&payId=" + payid;
			url += "&paytime=" + df2.format(date);
			url += "&gamename=" + URLEncoder.encode(payGame.getGameName(), "utf-8");
			url += "&areaname=" + URLEncoder.encode("中国", "utf-8");
			url += "&servername=" + URLEncoder.encode(payServer.getServerName(), "utf-8");
			url += "&sign=" + sign;
		} else {
			url += "&gameid=" + game;
			url += "&areaid=1";
			url += "&serverid=" + server;
			url += "&accountid=" + URLEncoder.encode(username, "utf-8");
			url += "&payprice=" + payprice;
			url += "&payId=" + payid;
			url += "&paytime=" + df2.format(date);
			url += "&gamename=" + URLEncoder.encode(payGame.getGameName(), "utf-8");
			url += "&areaname=" + URLEncoder.encode("中国区", "utf-8");
			url += "&servername=" + URLEncoder.encode(payServer.getServerName(), "utf-8");
			url += "&sign=" + sign;
			
		}
	System.out.println("WebPayServlet======================>"+url);
		response.sendRedirect(url);
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
