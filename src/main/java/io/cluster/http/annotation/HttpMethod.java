/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.http.annotation;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author thangpham
 */
public enum HttpMethod {

    @SerializedName("GET")
    GET("GET"),
    @SerializedName("POST")
    POST("POST"),
    @SerializedName("DELETE")
    DELETE("DELETE"),
    @SerializedName("HEAD")
    HEAD("HEAD"),
    @SerializedName("OPTIONS")
    OPTIONS("OPTIONS"),
    @SerializedName("PUT")
    PUT("PUT"),
    @SerializedName("PATCH")
    PATCH("PATCH"),
    @SerializedName("TRACE")
    TRACE("TRACE"),
    @SerializedName("CONNECT")
    CONNECT("CONNECT");

    private String value;

    private HttpMethod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return this.value;
    }
}
