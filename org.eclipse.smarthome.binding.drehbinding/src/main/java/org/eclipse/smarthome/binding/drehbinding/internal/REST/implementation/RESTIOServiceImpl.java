package org.eclipse.smarthome.binding.drehbinding.internal.REST.implementation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.binding.drehbinding.eventing.Subscriber;
import org.eclipse.smarthome.binding.drehbinding.eventing.SubscriptionServiceImpl;
import org.eclipse.smarthome.binding.drehbinding.internal.REST.RESTIOService;
import org.eclipse.smarthome.binding.drehbinding.internal.REST.RESTService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Is ThreadSafe
 *
 * @author Tristan
 *
 */
/*
 * Singleton pattern ist sinnvoll, da Service
 */
public class RESTIOServiceImpl implements RESTIOService {

    private static RESTIOServiceImpl instance;

    private static final String GET = "GET";
    private static final String PUT = "PUT";
    private static final String POST = "POST";
    private static final String DELETE = "DELETE";

    private final Logger logger = LoggerFactory.getLogger(RESTIOServiceImpl.class);

    private RESTIOServiceImpl() {

    }

    public synchronized static RESTIOServiceImpl getInstance() {
        if (instance == null) {
            instance = new RESTIOServiceImpl();
        }

        return instance;
    }

    @Override
    public RESTResponse callService(String serviceIdentifier) throws IOException {
        return callService(serviceIdentifier, null);
    }

    @Override
    public RESTResponse callService(String serviceIdentifier, Map<String, String> params) throws IOException {
        // url muss iwie aus den Discovery Configs gewonnen werden
        String urlString = "http://localhost:9090/webapi/functions/" + serviceIdentifier;
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        RESTRequest request = new RESTRequest(GET, url, params);

        return RESTService.makeRestCall(request);
    }

    @Override
    public void addSubscription(Subscriber subscriber, String topic, long bootid) {
        String urlString = "http://192.168.2.109:5000/subscribe";
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Map<String, String> params = new HashMap<>();
        params.put("identifier", subscriber.getIdentifier());
        params.put("topic", topic);
        params.put("callbackPort", "" + SubscriptionServiceImpl.getInstance().getCallbackPort());
        params.put("bootid", "" + bootid);
        RESTRequest request = new RESTRequest(POST, url, params);

        RESTResponse response;
        try {
            response = RESTService.makeRestCall(request);
        } catch (IOException e) {
            logger.error("Eine Exception trat auf!");
            SubscriptionServiceImpl.getInstance().onFailedRemoteSubscription(subscriber, topic);
            return;
        }

        if (response.getResponseCode() == 200 || response.getResponseCode() == 204) {
            SubscriptionServiceImpl.getInstance().onSuccessfulRemoteSubscription(subscriber, topic);
        } else {
            SubscriptionServiceImpl.getInstance().onFailedRemoteSubscription(subscriber, topic);
        }
    }

    @Override
    public void removeSubscription(Subscriber subscriber, String topic, long bootid) {
        // url muss iwie aus den Discovery Configs gewonnen werden
        String urlString = "http://192.168.2.109:5000/unsubscribe";
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Map<String, String> params = new HashMap<>();
        params.put("identifier", subscriber.getIdentifier());
        params.put("topic", topic);
        params.put("bootid", "" + bootid);
        RESTRequest request = new RESTRequest(DELETE, url, params);

        RESTResponse response;
        try {
            response = RESTService.makeRestCall(request);
        } catch (IOException e) {
            SubscriptionServiceImpl.getInstance().onFailedRemoteSubscriptionRemoval(subscriber, topic);
            return;
        }

        if (response.getResponseCode() == 200 || response.getResponseCode() == 204) {
            SubscriptionServiceImpl.getInstance().onSuccessfulRemoteSubscriptionRemoval(subscriber, topic);
        } else {
            SubscriptionServiceImpl.getInstance().onFailedRemoteSubscriptionRemoval(subscriber, topic);
        }
    }

}