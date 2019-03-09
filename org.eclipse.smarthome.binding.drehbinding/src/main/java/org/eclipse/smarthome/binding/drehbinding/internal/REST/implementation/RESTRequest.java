package org.eclipse.smarthome.binding.drehbinding.internal.REST.implementation;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class RESTRequest {

    private String method;
    private URL url;
    private Map<String, String> params;

    public RESTRequest() {
        this(null);
    }

    public RESTRequest(String method) {
        this(method, null);
    }

    public RESTRequest(String method, URL url) {
        this(method, url, new HashMap<>());
    }

    public RESTRequest(String method, URL url, Map<String, String> params) {
        this.method = method;
        this.url = url;
        this.params = params;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return method + " " + url;
    }

}