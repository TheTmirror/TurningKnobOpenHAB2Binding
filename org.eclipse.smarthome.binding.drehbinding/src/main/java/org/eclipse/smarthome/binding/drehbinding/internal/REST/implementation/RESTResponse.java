package org.eclipse.smarthome.binding.drehbinding.internal.REST.implementation;

import java.util.Map;

public class RESTResponse {

    private int responseCode;

    private Map<String, Object> parameter;

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public Map<String, Object> getParameter() {
        return parameter;
    }

    public void setParameter(Map<String, Object> parameter) {
        this.parameter = parameter;
    }

}