<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
String ua = request.getHeader("User-Agent");
response.sendRedirect("/phoneResult.jsp");
if (ua != null)
{
	String mua = ua.toLowerCase();
	System.out.println("ua-->" + mua);
	if (mua.indexOf("ipad") >= 0 || mua.indexOf("iphone") >= 0 || mua.indexOf("android") >= 0)
	{
		//mobile
		response.sendRedirect("/phoneResult.jsp");
	}
}
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>My JSP 'result.jsp' starting page</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->

  </head>
  
  <body>
    支付结果：<%=request.getSession().getAttribute("result") %>
    订单号：<%=request.getSession().getAttribute("orderid") %>
  </body>
</html>
