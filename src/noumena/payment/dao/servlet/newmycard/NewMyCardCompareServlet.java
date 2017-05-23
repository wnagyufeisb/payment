package noumena.payment.dao.servlet.newmycard;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import noumena.payment.newmycard.NewMyCardTWCharge;
import noumena.payment.util.DateUtil;

public class NewMyCardCompareServlet extends HttpServlet {
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
		String startDateTime = request.getParameter("StartDateTime"); // 订单状态
		String endDateTime = request.getParameter("EndDateTime"); // 返回信息
		String myCardTradeNo = request.getParameter("MyCardTradeNo"); // 服务号
		String gameId = request.getParameter("gameid"); // 服务号
		String ip = request.getRemoteAddr();
		System.out.println("NewMyCardCompareServlet  param-========"
				+ DateUtil.getCurTimeStr() + "==================>StartDateTime"
				+ startDateTime + " EndDateTime" + endDateTime + " MyCardTradeNo"
				+ myCardTradeNo );
		String ret = NewMyCardTWCharge.compareCb(ip, startDateTime, endDateTime,myCardTradeNo,gameId);
		System.out.println("NewMyCardCompareServlet  result-========"
				+ DateUtil.getCurTimeStr() + "==================>" + ret);
		response.setCharacterEncoding("utf-8");
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
