/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.http.core;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

/**
 *
 * @author thangpham
 */
public abstract class AbstractScheduler {

    protected AbstractScheduler() {
        this.getClass().getMethods();
    }

    public Object controlService(Map<String, List<String>> params, String uri) {
        System.out.println(this.getClass().getSimpleName());
        Annotation[] annotations = this.getClass().getAnnotations();
        System.out.println(annotations.length);
        for (int i = 0; i < annotations.length; ++i) {
            System.out.println(annotations[i].toString());
        }
        return null;
    }
}
