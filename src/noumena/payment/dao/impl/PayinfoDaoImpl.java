package noumena.payment.dao.impl;

import java.util.List;

import noumena.payment.dao.PayinfoDao;
import noumena.payment.model.Orders;
import noumena.payment.model.Payinfo;
import noumena.payment.util.NoumenaHibernateDaoSupport;

public class PayinfoDaoImpl extends NoumenaHibernateDaoSupport implements PayinfoDao {

	@Override
	public Payinfo get(String orderId) {
		Payinfo payinfo = (Payinfo) getHibernateTemplate().get(Payinfo.class, orderId);
		return payinfo;
	}

	@Override
	public List<Payinfo> getPayinfoByTime(String beginTime, String endTime, String payTypeId) {
		String sql = "from Payinfo where create_time>='" + beginTime + "'";
		sql += " and create_time<='" + endTime + "'";
		sql += " and pay_status=1";
		sql += " and pay_type_id='" + payTypeId + "'";
		List<Payinfo> list = getHibernateTemplate().find(sql);
		return list;
	}

	@Override
	public void save(Payinfo payinfo) {
		getHibernateTemplate().save(payinfo);
	}

	@Override
	public void update(Payinfo payinfo) {
		getHibernateTemplate().update(payinfo);
	}

}
