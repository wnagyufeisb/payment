package noumena.payment.alipay;

public class AlipayOrderDataVO
{
	private String body;
	private String buyer_email;
	private String buyer_id;
	private String discount;
	private String gmt_close;
	private String gmt_create;
	private String gmt_payment;
	private String is_total_fee_adjust;
	private String notify_id;
	private String notify_time;
	private String notify_type;
	private String out_trade_no;
	private String payment_type;
	private String price;
	private String quantity;
	private String seller_email;
	private String seller_id;
	private String subject;
	private String total_fee;
	private String trade_no;
	private String trade_status;
	private String use_coupon;
	
	private String sign_type;
	private String sign;
	
	public String getGmt_close()
	{
		return gmt_close;
	}
	public void setGmt_close(String gmt_close)
	{
		this.gmt_close = gmt_close;
	}
	public String getNotify_time()
	{
		return notify_time;
	}
	public void setNotify_time(String notify_time)
	{
		this.notify_time = notify_time;
	}
	public String getNotify_type()
	{
		return notify_type;
	}
	public void setNotify_type(String notify_type)
	{
		this.notify_type = notify_type;
	}
	public String getNotify_id()
	{
		return notify_id;
	}
	public void setNotify_id(String notify_id)
	{
		this.notify_id = notify_id;
	}
	public String getSign_type()
	{
		return sign_type;
	}
	public void setSign_type(String sign_type)
	{
		this.sign_type = sign_type;
	}
	public String getSign()
	{
		return sign;
	}
	public void setSign(String sign)
	{
		this.sign = sign;
	}
	public String getOut_trade_no()
	{
		return out_trade_no;
	}
	public void setOut_trade_no(String out_trade_no)
	{
		this.out_trade_no = out_trade_no;
	}
	public String getSubject()
	{
		return subject;
	}
	public void setSubject(String subject)
	{
		this.subject = subject;
	}
	public String getPayment_type()
	{
		return payment_type;
	}
	public void setPayment_type(String payment_type)
	{
		this.payment_type = payment_type;
	}
	public String getTrade_no()
	{
		return trade_no;
	}
	public void setTrade_no(String trade_no)
	{
		this.trade_no = trade_no;
	}
	public String getTrade_status()
	{
		return trade_status;
	}
	public void setTrade_status(String trade_status)
	{
		this.trade_status = trade_status;
	}
	public String getSeller_id()
	{
		return seller_id;
	}
	public void setSeller_id(String seller_id)
	{
		this.seller_id = seller_id;
	}
	public String getSeller_email()
	{
		return seller_email;
	}
	public void setSeller_email(String seller_email)
	{
		this.seller_email = seller_email;
	}
	public String getBuyer_id()
	{
		return buyer_id;
	}
	public void setBuyer_id(String buyer_id)
	{
		this.buyer_id = buyer_id;
	}
	public String getBuyer_email()
	{
		return buyer_email;
	}
	public void setBuyer_email(String buyer_email)
	{
		this.buyer_email = buyer_email;
	}
	public String getTotal_fee()
	{
		return total_fee;
	}
	public void setTotal_fee(String total_fee)
	{
		this.total_fee = total_fee;
	}
	public String getQuantity()
	{
		return quantity;
	}
	public void setQuantity(String quantity)
	{
		this.quantity = quantity;
	}
	public String getPrice()
	{
		return price;
	}
	public void setPrice(String price)
	{
		this.price = price;
	}
	public String getBody()
	{
		return body;
	}
	public void setBody(String body)
	{
		this.body = body;
	}
	public String getGmt_create()
	{
		return gmt_create;
	}
	public void setGmt_create(String gmt_create)
	{
		this.gmt_create = gmt_create;
	}
	public String getGmt_payment()
	{
		return gmt_payment;
	}
	public void setGmt_payment(String gmt_payment)
	{
		this.gmt_payment = gmt_payment;
	}
	public String getIs_total_fee_adjust()
	{
		return is_total_fee_adjust;
	}
	public void setIs_total_fee_adjust(String is_total_fee_adjust)
	{
		this.is_total_fee_adjust = is_total_fee_adjust;
	}
	public String getUse_coupon()
	{
		return use_coupon;
	}
	public void setUse_coupon(String use_coupon)
	{
		this.use_coupon = use_coupon;
	}
	public String getDiscount()
	{
		return discount;
	}
	public void setDiscount(String discount)
	{
		this.discount = discount;
	}
}
