package noumena.payment.tencent;

public class TencentOrderVO
{
	private String openid;
	private String openkey;
	private String pay_token;
	private String pf;
	private String pfkey;
	private String appid;
	private String ts;
	private String zoneid;
	private String type;
	public String getOpenid() {
		return openid;
	}
	public void setOpenid(String openid) {
		this.openid = openid;
	}
	public String getOpenkey() {
		return openkey;
	}
	public void setOpenkey(String openkey) {
		this.openkey = openkey;
	}
	public String getPay_token() {
		return pay_token;
	}
	public void setPay_token(String payToken) {
		pay_token = payToken;
	}
	public String getPf() {
		return pf;
	}
	public void setPf(String pf) {
		this.pf = pf;
	}
	public String getPfkey() {
		return pfkey;
	}
	public void setPfkey(String pfkey) {
		this.pfkey = pfkey;
	}
	public String getAppid() {
		return appid;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}
	public String getTs() {
		return ts;
	}
	public void setTs(String ts) {
		this.ts = ts;
	}
	public String getZoneid() {
		return zoneid;
	}
	public void setZoneid(String zoneid) {
		this.zoneid = zoneid;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
