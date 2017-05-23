package noumena.payment.userverify;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;


import net.sf.json.JSONObject;
import noumena.payment.userverify.util.TrustAnyTrustManager;
import noumena.payment.userverify.util.Util;

public class CGoogleVerify
{
	public static String verify(int model, ChannelInfoVO vo)
	{
		String ret = "";
		switch (model)
		{
		case 0:
			//正常参数验证，返回合法id
			ret = getIdFrom(vo);
			break;
		case 1:
			//正常参数验证，返回json格式状态
			ret = getIdFrom(vo);
			break;
		case 2:
			//json参数验证，返回合法id
			String info = vo.getExinfo();
			JSONObject json = JSONObject.fromObject(info);
			vo.setUid(json.getString("uid"));
			vo.setToken(json.getString("token"));
			ret = getIdFrom(vo);
			break;
		}
		return ret;
	}
	
	private static String getIdFrom(ChannelInfoVO vo)
	{
		String id = "";
		try{
			String token = vo.getToken();
			
			String urlstr = "https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=%s";
			urlstr = String.format(urlstr,token);
			//System.out.println(urlstr);
			ChannelVerify.GenerateLog("google get user info url ->" + urlstr);
			
			SSLContext context = SSLContext.getInstance("SSL");
			context.init(null, new TrustManager[]{ new TrustAnyTrustManager() }, new java.security.SecureRandom());   
			HttpsURLConnection connection = (HttpsURLConnection)new URL(urlstr).openConnection();
			connection.setSSLSocketFactory(context.getSocketFactory());
			connection.setRequestMethod("GET");
			connection.setHostnameVerifier
			(
				new HostnameVerifier()
				{
					@Override
					public boolean verify(String arg0, SSLSession arg1)
					{
						return true; //不验证
						//return false; //验证
					}
				}
			);

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String res = "", line = null;
			while ((line = in.readLine()) != null)
			{
				res += line;
			}
			
			connection.disconnect();
			ChannelVerify.GenerateLog("google get user info ret ->" + res);
			//System.out.println(res);
			JSONObject json = JSONObject.fromObject(res);
			String aud = Util.getGoogleKey("");
			//System.out.println(aud);
			//System.out.println(json.getString("aud"));
			//System.out.println(json.getString("sub"));
			if(vo.getUid().equals(json.getString("sub")) && aud.equals(json.getString("aud"))){
				id = vo.getUid();
			}else{
				ChannelVerify.GenerateLog("google sub OR aud error ->" + vo.getUid()+"|||"+vo.getExinfo());
			}
		}catch(Exception e){
			e.printStackTrace();
			return "";
		}
		return id;
	}
	
	/*public static void main(String args[]){
		String loginfo = "{\"type\":\"google\",\"uid\":\"\",\"token\":\"XXX\"}";
		ChannelInfoVO vo = new ChannelInfoVO();
		vo.setType("google");
		vo.setExinfo(loginfo);
		CGoogleVerify.verify(2, vo);
		
	}*/
}
