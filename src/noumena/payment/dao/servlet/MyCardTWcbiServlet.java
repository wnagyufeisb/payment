package noumena.payment.dao.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import noumena.payment.mycardtw.MyCardTWCharge;
import noumena.payment.mycardtw.MyCardTWOrderDataVO;

public class MyCardTWcbiServlet extends HttpServlet
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The doGet method of the servlet. <br>
	 * 
	 * MyCard台湾InGame模式支付成功回调
	 * 
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doPost(request, response);
	}

	/**
	 * The doPost method of the servlet. <br>
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
//		BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream()));
//		String res = in.readLine();
//		System.out.println("mycartw ingame cb success->" + res);

		MyCardTWOrderDataVO vo = new MyCardTWOrderDataVO();
		vo.setFacId(request.getParameter("facId"));
		vo.setFacMemId(request.getParameter("facMemId"));
		vo.setFacTradeSeq(request.getParameter("facTradeSeq"));
		vo.setTradeSeq(request.getParameter("tradeSeq"));
		vo.setCardId(request.getParameter("CardId"));
		vo.setProjNo(request.getParameter("oProjNo"));
		vo.setCardKind(request.getParameter("CardKind"));
		vo.setCardPoint(request.getParameter("CardPoint"));
		vo.setReturnMsgNo(request.getParameter("ReturnMsgNo"));
		vo.setErrorMsgNo(request.getParameter("ErrorMsgNo"));
		vo.setErrorMsg(request.getParameter("ErrorMsg"));
		vo.setHash(request.getParameter("hash"));
		
//		facId=GFD00835&facMemId=1&facTradeSeq=600&tradeSeq=SW121225000001&CardId=MCARAT0000001256&oProjNo=A0000&CardKind=2&CardPoint=150&ReturnMsgNo=1&ErrorMsgNo=&ErrorMsg=&hash=7ec04c4375bf2693cdee03efc44641565544c2ab2fe7d111c20ca0ea72aa6e47&submit1=Click+to+continue+if+you+are+not+automatically+redirected.
		MyCardTWCharge.getCallbackFromMyCardTWIngame(vo);
		
		PrintWriter out = response.getWriter();
		out.println("SUCCESS");
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
