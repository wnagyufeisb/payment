﻿<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" pageEncoding="UTF-8"%>

<%
	String location = request.getHeader("location");
	if (location == null)
	{
		location = "tw";
	}
	String mainpage = "index" + location + ".jsp";
	response.sendRedirect(mainpage);
%>

