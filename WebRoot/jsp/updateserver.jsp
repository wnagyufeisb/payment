<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ page language="java" pageEncoding="UTF-8"%>

<%@ page import="noumena.payment.bean.PayServerBean" %>
<%@ page import="noumena.payment.model.PayServer" %>
<%@ page import="noumena.payment.util.DateUtil" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>

<%
request.setCharacterEncoding("utf-8");
response.setCharacterEncoding("utf-8");

String serverid = request.getParameter("serverid");
String servername = request.getParameter("servername");
String serverurl = request.getParameter("serverurl");
String url = request.getParameter("url");
String paynotify = request.getParameter("paynotify");
PayServerBean bean = new PayServerBean();
PayServer vo = bean.get(serverid);

vo.setServerName(servername);
vo.setCallbackUrl(serverurl);
vo.setCheckUrl(url);
vo.setPayNotify(paynotify);
vo.setCreateTime(DateUtil.getCurrentTime());

bean.update(vo);

response.sendRedirect("game.jsp?gameid=" + vo.getGameId());
%>
