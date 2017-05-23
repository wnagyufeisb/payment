package noumena.payment.bean;

import noumena.payment.dao.SendGameDao;
import noumena.payment.model.SendGameBean;
import noumena.payment.util.SpringContextUtil;

public class SendGame {
	public  SendGameBean getChannel(String gameId) {
		SendGameDao dao = (SendGameDao) SpringContextUtil
				.getBean("SendGameDao");
		return dao.getInfoByGame(gameId);
	}
}
