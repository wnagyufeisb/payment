package noumena.payment.card19;

import java.util.Vector;

public class Card19Params
{
	public static String MERCHANT_ID		= "220271";
	public static String MERCHANT_KEY		= "123456789";//"w6fzp7r875h3ktb2qgb2rg9afqxypg4f2hnqpw2j4ncu804hbyy7q3k0vpypl82cvrk7kz3ezc9e8e883h6shhv47f48e4w0psaok60odub5nu99jhincvlyvm58356y";
	public static String MERCHANT_DES_KEY	= "12345678";//"w6fzp7r8";

	private Vector<Card19ParamApp> apps = new Vector<Card19ParamApp>();

	public Vector<Card19ParamApp> getApps()
	{
		return apps;
	}
	
	public void addApp(Card19ParamApp app)
	{
		this.getApps().add(app);
	}
	
	public void addApp(String appname, String appid, String appkey)
	{
		Card19ParamApp app = new Card19ParamApp();
		app.setAppname(appname);
		app.setAppid(appid);
		app.setAppkey(appkey);
		this.getApps().add(app);
	}
	
	public String getAppKeyById(String appid)
	{
		for (Card19ParamApp app : this.getApps())
		{
			if (app.getAppid().equals(appid))
			{
				return app.getAppkey();
			}
		}
		return null;
	}
}
