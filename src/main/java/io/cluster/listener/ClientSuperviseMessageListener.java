/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.listener;

import io.cluster.bean.ResponseNetBean;
import io.cluster.core.IMessageListener;
import io.cluster.dao.derby.HibernateSessionManager;
import io.cluster.util.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
public class ClientSuperviseMessageListener extends IMessageListener<ResponseNetBean> {

    private static final Logger LOGGER = LogManager.getLogger(HibernateSessionManager.class.getName());
    
    @Override
    public void onChannel(ResponseNetBean bean) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onMessage(ResponseNetBean response) {
        String result = null;
        try {
            String messageStr = response.getMessageAsString();
            if (StringUtil.isNullOrEmpty(response.getMessageAsString())) {
                LOGGER.error("Server response is empty or a null, cannot process.");
                result = null;
            }
//            Map<String, String> message = MethodUtil.fromJsonToMap(messageStr);
//            instructionValues.clear();
//            instructionValues.putAll(message);
//            hasInstruction.set(Boolean.TRUE);
//            instructionCondition.signalAll();
            LOGGER.info("Receive message from server: " + messageStr);
            result = null;
        } catch (Exception ex) {
            LOGGER.info("Problem occured when receiving message, error: ", ex);
        }
    }

}
