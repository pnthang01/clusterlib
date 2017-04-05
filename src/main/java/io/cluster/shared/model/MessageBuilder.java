/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.shared.model;

import io.cluster.util.MethodUtil;
import java.util.Map;

/**
 *
 * @author thangpham
 */
public class MessageBuilder {

    private Map<String, String> message;

    public MessageBuilder(Map<String, String> message) {
        this.message = message;
    }

    public MessageBuilder put(String key, String value) {
        message.put(key, value);
        return this;
    }
    
    public Map<String, String> getMessageAsMap() {
        return message;
    }
    
    public String getMessageAsJson() {
        return MethodUtil.toJson(message);
    }
}
