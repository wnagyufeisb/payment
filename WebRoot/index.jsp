﻿<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" pageEncoding="UTF-8"%>

<%@ page import="noumena.payment.bean.PayGameBean"%>
<%@ page import="noumena.payment.vo.PayGameVO"%>
<%@ page import="noumena.payment.vo.PayServerVO"%>
<%@ page import="noumena.payment.vo.WebConvertVO"%>
<%@ page import="noumena.payment.model.PayServer"%>
<%@ page import="java.util.List"%>

<%
	String ua = request.getHeader("User-Agent");
	if (ua != null)
	{
		String mua = ua.toLowerCase();
		System.out.println("ua-->" + mua);
		if (mua.indexOf("ipad") >= 0 || mua.indexOf("iphone") >= 0 || mua.indexOf("android") >= 0)
		{
	//mobile
	response.sendRedirect("phone.jsp");
		}
	}
	
	String defaultGameId = request.getParameter("gameid");
	String defaultitem = request.getParameter("money");
	String gameselectdisabled = "";
	if (defaultGameId == null || defaultGameId.equals(null))
	{
		defaultGameId = "4200000";
	}
	else
	{
		gameselectdisabled = "disabled";
	}
	String defaultServerId = request.getParameter("serverid");
	if (defaultServerId == null || defaultServerId.equals(null))
	{
		defaultServerId = "0";
	}
	String defaultUserId = request.getParameter("uid");
	if (defaultUserId == null || defaultUserId.equals(null))
	{
		defaultUserId = "";
	}
	
	String area = request.getParameter("area");
	if (area != null && area.equals("adr"))
	{
		defaultServerId = "821_4400000";
	}
	else if (area != null && area.equals("ch"))
	{
		defaultServerId = "831_4400000";
	}
	else if (area != null && area.equals("app"))
	{
		defaultServerId = "811_4400000";
	}
	
	PayGameBean bean = new PayGameBean();
	List<PayGameVO> list = bean.getPayGameVOs();
%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
		<meta http-equiv="expires" content="0">
			<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
			<title>空中网手机游戏充值中心</title>
			<link type="text/css" rel="stylesheet" href="style/reset.css" />
			<link type="text/css" rel="stylesheet" href="style/page.css" />

			<script>
function AddFavorite(sURL, sTitle) {
	sURL = encodeURI(sURL); 
	try{   
		window.external.addFavorite(sURL, sTitle);   
	}catch(e) {   
		try{   
			window.sidebar.addPanel(sTitle, sURL, "");   
		}catch (e) {   
			alert("加入收藏失败，请使用Ctrl+D进行添加,或手动在浏览器里进行设置.");
		}   
	}
}
</script>

			<script>
	function initpage () {
		initGame(null, <%=defaultGameId%>, "<%=defaultServerId%>");
		}
		
	window.onscroll = function (){
		var oHeight = 100;
		var scrollpos = document.body.scrollTop || document.documentElement.scrollTop;
		var oDiv = document.getElementById('gamebulletinboard');
		if(scrollpos>=oHeight){
			oDiv.style.position='fixed';
			oDiv.style.marginLeft='747px';
			oDiv.style.marginTop='0px';
		}else if(scrollpos<oHeight){
			oDiv.style.position='relative';
			oDiv.style.marginLeft='0px';
		}
}
</script>

			<script type="text/javascript">
	var PayGames = [];
	var PayGame = {};
	var payServer = {};
	var payConvert = {};
	var checkUrl;
	var req;
	var flag = false;
	var flag1 = false;
<%for (PayGameVO payGameVO : list) {%>
	PayGame = {};
	PayGame.gameid = "<%=payGameVO.getGameId()%>";
	PayGame.gamename = "<%=payGameVO.getGameName()%>";
	PayGame.website = "<%=payGameVO.getWebsite()%>";
	PayGame.servers = [];
	PayGame.converts = [];
	<%for (PayServerVO payServer : payGameVO.getServers()) {%>
		payServer = {};
		payServer.serverid = "<%=payServer.getServerId()%>";
		payServer.servername = "<%=payServer.getServerName()%>";
		payServer.checkurl = "<%=payServer.getCheckUrl()%>";
		PayGame.servers.push(payServer);
	<%}%>
	<%for (WebConvertVO webConvertVO : payGameVO.getConverts()) {%>
		payConvert = {};
		payConvert.codename = "<%=webConvertVO.getCodeName()%>";
		payConvert.money = "<%=webConvertVO.getMoney()%>";
		payConvert.unit = "<%=webConvertVO.getUint()%>";
		payConvert.point = "<%=webConvertVO.getPoint()%>";
		payConvert.pointname = "<%=webConvertVO.getPointName()%>
				";
				PayGame.converts.push(payConvert);
			<%}%>
				PayGames.push(PayGame);
			<%}%>
				function initGame(select, igameid, serverid) {
					var game = document.getElementById("game");
					var selectedid = 0;
					if (select == null) {
						for (var i = 0; i < PayGames.length; i++) {
							game.options.add(new Option(PayGames[i].gamename,
									PayGames[i].gameid));
							if (PayGames[i].gameid == igameid) {
								selectedid = i;
							}
						}
						createselect(selectedid, serverid);
						createconvert(selectedid);
						game.options[selectedid].selected = true;

					} else {
						for (var i = 0; i < game.options.length; i++) {
							if (game.options[i].selected) {
								createselect(i, serverid);
								createconvert(i);
								break;
							}
						}
					}
					//alert(select.value);
					initdiv();
					var username = document.getElementById("username");
					var server = document.getElementById("server");
					initCheckUrl(server);
					checkUserByServer(username);
					checkUser();
				}
				function createselect(num, serverid) {
					PayGames[num];
					var server = document.getElementById("server");
					server.gameNum = num;
					while (server.options.length > 0) {
						server.options.remove(0);
					}
					for (var i = 0; i < PayGames[num].servers.length; i++) {
						var option = new Option(
								PayGames[num].servers[i].servername,
								PayGames[num].servers[i].serverid);

						if (i == 0) {
							option.select = true;
							checkUrl = PayGames[num].servers[i].checkurl;
						}

						if (PayGames[num].servers[i].serverid == serverid) {
							option.selected = true;
						}
						server.options.add(option);
					}
				}
				function createconvert(num) {
					PayGames[num];
					var defaultitem = document.getElementById("defaultitem");
					var bankamount = document.getElementById("bankamount");
					while (bankamount.firstChild) {
						bankamount.removeChild(bankamount.firstChild);
					}
					var nextstepbtn = document.getElementById("nextstepbtn");
					while (nextstepbtn.firstChild) {
						nextstepbtn.removeChild(nextstepbtn.firstChild);
					}
					var huodongdesc = document.getElementById("huodongdesc");
					while (huodongdesc.firstChild) {
						huodongdesc.removeChild(huodongdesc.firstChild);
					}

					var tr;
					var td;
					var input;
					var s = "";
					s += "<div class=\"pleft\"><span class=\"icon1\"></span>充值金额：</div>";
					s += "<div class=\"pright\">";
					s += "<ul class=\"charge\">";

					var cc = "";
					for (var i = 0; i < PayGames[num].converts.length; i++) {
						var c = "";
						if (defaultitem.value == PayGames[num].converts[i].money) {
							c = "checked";
							cc = "checked";
						}
						if (cc == "" && i == PayGames[num].converts.length - 1) {
							c = "checked";
						}
						s += "<li><input class=\"radio\" " + c + " type=\"radio\" name=\"amount\" value=\"" + PayGames[num].converts[i].codename  + "#" + PayGames[num].converts[i].money  + "#" + PayGames[num].converts[i].unit + "#" + PayGames[num].converts[i].point + "\">";
						/*if (i == 0) {
							s += "<li><input class=\"radio\" checked=\"checked\" type=\"radio\" name=\"amount\" value=\"" + PayGames[num].converts[i].codename  + "#" + PayGames[num].converts[i].money  + "#" + PayGames[num].converts[i].unit + "#" + PayGames[num].converts[i].point + "\">";
						} else {
							s += "<li><input class=\"radio\" type=\"radio\" name=\"amount\" value=\"" + PayGames[num].converts[i].codename  + "#" + PayGames[num].converts[i].money  + "#" + PayGames[num].converts[i].unit + "#" + PayGames[num].converts[i].point + "\">";
						}*/

						var unit;
						if (PayGames[num].converts[i].unit == "CN") {
							unit = "￥";
						} else {
							unit = "$ ";
						}
						s += "<label>" + unit + PayGames[num].converts[i].money
								+ "=" + PayGames[num].converts[i].point
								+ PayGames[num].converts[i].pointname;
						s += "</label></li>";
					}
					s += "</ul></div>";
					bankamount.innerHTML = s;

					s = "";
					s += "<div style=\"padding-left:100px; line-height:1.6em; font-size:0.9em;\">";
					if (PayGames[num].gameid == 4200000) {
						s += "提示：<br/>1.每个账号有且只有一次充值双倍机会，若在游戏内已获得首充双倍奖励，则网页充值不能再获得奖励。<br/>2.只有指定档位的充值金额才享受首充双倍奖励";
					} else if (PayGames[num].gameid == 3900000) {
						s += "提示：<br/>1.新付费玩家首次充值返还100%元宝，即充即返，还可获赠超值首充礼包，<br/>每个账号在每个服务器各有且只有一次充值双倍机会，若在游戏内已获得首充双倍奖励，则网页充值不能再获得奖励。<br/>2.单笔充值68、128、328、648、1298、3998元可获得超级大礼（奖励紫装均无等级限制）<br/>详情请添加微信号:mjz-kongzhong查询<br/>注：游戏ID为游戏客户端内，设置-账号管理界面左上角的7位数字";
					} else if (PayGames[num].gameid == 4400000) {
						s += "提示：<br/>1.客服qq：2914658986，qq群：92886789<br/>2.网页充值无法充值元宝袋和超值大礼包";
					}
					s += "</div>";
					huodongdesc.innerHTML = s;

					s = "<a id=\"next\" href=\"javascript:;\" class=\"btnnext\" onclick=\"submitform()\">下一步</a>";
					s += "<a id=\"nextno\" href=\"javascript:;\" class=\"btnno\">下一步</a>";
					nextstepbtn.innerHTML = s;

					var imgname = PayGames[num].gameid;
					var website = PayGames[num].website;
					s = "<div class=\"ad\">";
					s += "<a href=\"" + website + "\" target=\"_blank\"><img width=\"219\" height=\"469\" src=\"images/" + imgname + ".jpg\"/></a>";
					s += "</div>";
					gamebulletinboard.innerHTML = s;
				}

				function submitform() {
					var form = document.getElementById("form");
					if (form.username.value == form.usernamer.value
							&& form.username.value != "") {
						form.submit();
					} else {
						alert("二次用户名不匹配或用户名为空");
					}
				}
				function checkUserByServer(user) {
					//alert(user.value + checkUrl);
					var useriddesc = document.getElementById("useriddesc");
					var usernamedui = document.getElementById("usernamedui");
					var usernamecuowu = document
							.getElementById("usernamecuowu");
					var usernameload = document.getElementById("usernameload");
					useriddesc.style.display = "none";
					usernamedui.style.display = "none";
					usernamecuowu.style.display = "none";
					usernameload.style.display = "";
					var form = document.getElementById("form");
					if (form.username.value == null
							|| form.username.value == "") {
						useriddesc.style.display = "";
						usernamedui.style.display = "none";
						usernamecuowu.style.display = "none";
						usernameload.style.display = "none";
						return;
					}
					var url = checkUrl;

					if (window.ActiveXObject) {
						req = new ActiveXObject("Microsoft.XMLHTTP");
					} else if (window.XMLHttpRequest) {
						req = new XMLHttpRequest();
					}
					var post = "&gameid=" + form.game.value;
					post += "&serverid=" + form.server.value;
					post += "&userid=" + form.username.value;
					req.onreadystatechange = display;

					req.open("POST", "checkuser.jsp", true);

					req.setRequestHeader("Content-Type",
							"application/x-www-form-urlencoded");

					req.send("url=" + url + post);

				}
				function checkUser() {
					var form = document.getElementById("form");
					var useridre = document.getElementById("useridre");
					var usernamerdui = document.getElementById("usernamerdui");
					var usernamercuowu = document
							.getElementById("usernamercuowu");
					if (form.usernamer.value == null
							|| form.usernamer.value == "") {
						var flag1 = false;
						usernamercuowu.style.display = "none";
						usernamerdui.style.display = "none";
						useridre.style.display = "";
						return;
					}

					if (form.username.value == form.usernamer.value) {
						var flag1 = true;
						usernamerdui.style.display = "";
						usernamercuowu.style.display = "none";
						useridre.style.display = "none";
					} else {
						var flag1 = false;
						usernamercuowu.style.display = "";
						usernamerdui.style.display = "none";
						useridre.style.display = "none";
					}
					if (flag1 && flag) {
						var next = document.getElementById("next");
						var nextno = document.getElementById("nextno");
						next.style.display = "";
						nextno.style.display = "none";
					} else {
						var next = document.getElementById("next");
						var nextno = document.getElementById("nextno");
						next.style.display = "none";
						nextno.style.display = "";
					}
				}
				function initCheckUrl(select) {
					var num = select.gameNum;
					for (var i = 0; i < select.options.length; i++) {
						if (select.options[i].selected) {
							for (var j = 0; j < PayGames[num].servers.length; j++) {
								if (select.options[i].value == PayGames[num].servers[i].serverid) {
									checkUrl = PayGames[num].servers[i].checkurl;
									break;
								}
							}
							break;
						}
					}
					initdiv();
					//alert(checkUrl);
				}
				function display() {
					var usernamedui = document.getElementById("usernamedui");
					var usernamecuowu = document
							.getElementById("usernamecuowu");
					if (req.readyState == 4 && req.status == 200) {
						var ruid, rsid, rret;
						var ss = req.responseText.split(",");
						if (ss.length > 1) {
							rret = ss[0];
							ruid = ss[1];
							rsid = ss[2];
							var retuid = document.getElementById("retuid");
							var retsid = document.getElementById("retsid");
							retuid.value = ruid;
							retsid.value = rsid;
						} else {
							rret = ss[0];
						}
						if (rret == 0) {
							usernamedui.style.display = "";
							usernamecuowu.style.display = "none";
							flag = true;
						} else {
							usernamedui.style.display = "none";
							usernamecuowu.style.display = "";
							flag = false;
						}
						var usernameload = document
								.getElementById("usernameload");
						usernameload.style.display = "none";
						var form = document.getElementById("form");
						if (form.username.value == form.usernamer.value) {
							var flag1 = true;
						} else {
							var flag1 = false;
						}
						if (flag1 && flag) {
							var next = document.getElementById("next");
							var nextno = document.getElementById("nextno");
							next.style.display = "";
							nextno.style.display = "none";
						} else {
							var next = document.getElementById("next");
							var nextno = document.getElementById("nextno");
							next.style.display = "none";
							nextno.style.display = "";
						}
					}
				}
				function initdiv() {
					var useriddesc = document.getElementById("useriddesc");
					var useridre = document.getElementById("useridre");
					var usernamedui = document.getElementById("usernamedui");
					var usernamecuowu = document
							.getElementById("usernamecuowu");
					var usernamerdui = document.getElementById("usernamerdui");
					var usernamercuowu = document
							.getElementById("usernamercuowu");
					var usernameload = document.getElementById("usernameload");
					var next = document.getElementById("next");
					if (next != null)
						next.style.display = "none";
					useriddesc.style.display = "";
					useridre.style.display = "";
					usernamedui.style.display = "none";
					usernamecuowu.style.display = "none";
					usernamerdui.style.display = "none";
					usernamercuowu.style.display = "none";
					usernameload.style.display = "none";
				}
			</script>
</head>

<body onload="initpage()">
	<div class="container">
		<div class="top">
			<a style="float:right; margin:10px 60px 0px 0px;"
				href="javascript:AddFavorite('http://p.ko.cn/pay', '空中网充值')"><img
				src="images/shoucang.jpg" /></a>
			<div class="logo">
				<a href="http://www.kongzhong.com" target="_blank"></a>
			</div>
			<h2>空中网手机游戏充值中心</h2>
		</div>

		<div class="form clearfix">
			<div class="left">
				<form id="form" action="webpay" method="post">
					<div class="title1">
						手游充值流程
						<div class="select game">
							<div class="selected">
								当前充值游戏：<select id="game" name="game" onchange="initGame(this)"
									class="select"></select>
							</div>
						</div>
					</div>
					<div class="chargetips">
						<img src="images/chargebg.jpg" />
						<h3>充值信息</h3>
					</div>
					<table class="info" cellpadding="0" cellspacing="0">
						<tr>
							<td width="120"><span class="icon1"></span>服务器：</td>
							<td width="180">
								<div class="select fwq">
									<div class="selected">
										<select id="server" name="server" class="select"
											onchange="initCheckUrl(this)"></select>
									</div>
								</div>
							</td>
							<td width="300"></td>
						</tr>
						<tr>
							<td><span class="icon1"></span>游戏帐号：</td>
							<td><input name="username" id="username" type="text"
								value="<%=defaultUserId%>" onblur="checkUserByServer(this)" /></td>
							<td class="" colspan="3">
								<div id="useriddesc" style="display:;">请输入您的游戏ID</div>
								<div id="usernamedui" style="display:none;">
									<span class="icon3"></span><span class="warning">游戏帐号存在</span>
								</div>
								<div id="usernamecuowu" style="display:none;">
									<span class="icon2"></span><span class="warning">游戏帐号不存在</span>
								</div>
								<div id="usernameload" style="display:none;">
									<span class="icon2"></span><span class="warning">游戏帐号验证中</span>
								</div>
							</td>
						</tr>
						<tr>
							<td><span class="icon1"></span>确认帐号：</td>
							<td><input name="usernamer" id="usernamer" type="text"
								value="<%=defaultUserId%>" onblur="checkUser()" /></td>
							<td class="" colspan="3">
								<div id="useridre" style="display:;">再次输入游戏ID</div>
								<div id="usernamerdui" style="display:none;">
									<span class="icon3"></span>
								</div>
								<div id="usernamercuowu" style="display:none;">
									<span class="icon2"></span>
								</div>
							</td>
						</tr>
					</table>
					<div id="bankamount" class="wraper"></div>
					<div id="huodongdesc" class="wraper"></div>
					<div id="nextstepbtn" class="wraper"></div>
					<div class="tips">
						<h3 class="title2">充值提示</h3>
						<p class="red">1 、三国群英OL,王国创世录,合金要塞,银河创世录,苍穹启示录,游戏ID通过游戏内设置查看。</p>
						<p class="red">2 、三国群英OL的玩家请在正常登陆游戏的情况下再进行充值。</p>
						<p class="red">3 、魔界勇士请使用自己昵称ID。</p>
						<p>4 、由于银行只支持 IE 浏览器，建议使用 IE6.0 和 IE6.0 以上浏览器。</p>
						<p>5 、网银支付页面会在新的一页打开，如未能弹出，请点击 ie 的菜单 " 工具 "—"internet 选项 "—"
							安全 "— 将安全中的各项设置恢复到默认级别。</p>
						<p>6 、银行卡充值可能存在 24 小时网络延迟，请在扣费成功后，耐心等待，不要反复操作，否则将无法退还您的费用。</p>
						<p>7 、如在充值时遇到问题，请您联系客服。</p>
					</div>
					<input type="hidden" id="retuid" name="retuid" value=""> <input
						type="hidden" id="retsid" name="retsid" value=""> <input
							type="hidden" id="defaultitem" name="defaultitem"
							value="<%=defaultitem%>">
				</form>
			</div>
			<div id="gamebulletinboard" class="ad"></div>
		</div>

		<!--底部信息-->
		<script src="http://stc.kongzhong.com/js/foot.js"
			type="text/javascript"></script>
	</div>
</body>
</html>
