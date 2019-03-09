package org.eclipse.smarthome.binding.drehbinding.eventing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.smarthome.binding.drehbinding.internal.REST.implementation.RESTIOServiceImpl;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Is ThreadSafe
 *
 * @author Tristan
 *
 */
/*
 * Vom SubscriptionService sollte es ebenfalls nur eine Instanz geben, da es sich
 * hierbei um einen Background Task handelt.
 */
public class SubscriptionServiceImpl implements SubscriptionService {

    private final Logger logger = LoggerFactory.getLogger(SubscriptionServiceImpl.class);

    private static SubscriptionServiceImpl instance;

    private static final String POOL_NAME = "DrehbindingSubscriptionPool";
    ExecutorService schedular;

    private boolean shutdown = false;
    private int callbackPort = -1;

    private final Lock shutdownLock;
    private final Lock callbackPortLock;

    private final Map<String, List<Subscriber>> subscriptions;

    Runnable callbackListener = new Runnable() {

        @Override
        public void run() {
            ServerSocket callbackSocket = null;
            Socket connection = null;
            try {
                callbackSocket = new ServerSocket(0);
                callbackPortLock.lock();
                callbackPort = callbackSocket.getLocalPort();
                callbackPortLock.unlock();

                shutdownLock.lock();
                while (!shutdown) {
                    shutdownLock.unlock();
                    connection = callbackSocket.accept();
                    BufferedReader inFromClient = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    String clientSentence = inFromClient.readLine();
                    logger.trace("Received: " + clientSentence);
                    connection.close();

                    /*
                     * Es kann vorkommen, dass bei einem Fehler auf der Deviceseite das Device trotzdem eine Null
                     * Nachricht verschickt. Diese kann nicht verarbeitet werden und wird daher ignoriert.
                     */
                    if (clientSentence == null) {
                        logger.warn("Received callback message was null - therefor ignoring it");
                        shutdownLock.lock();
                        continue;
                    }

                    String topic = decipherTopic(clientSentence);
                    Map<String, String> values = decipherValues(clientSentence);
                    notifyAllSubscriber(topic, values);

                    // GGf delay timer = overflood protection
                    shutdownLock.lock();
                }
                shutdownLock.unlock();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (connection != null && !connection.isClosed()) {
                    try {
                        connection.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (callbackSocket != null && !callbackSocket.isClosed()) {
                    try {
                        callbackSocket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    private SubscriptionServiceImpl() {
        callbackPortLock = new ReentrantLock();
        shutdownLock = new ReentrantLock();

        schedular = ThreadPoolManager.getPool(POOL_NAME);
        subscriptions = new HashMap<>();

        schedular.execute(callbackListener);
    }

    public static synchronized SubscriptionServiceImpl getInstance() {
        if (SubscriptionServiceImpl.instance == null) {
            SubscriptionServiceImpl.instance = new SubscriptionServiceImpl();
        }

        return SubscriptionServiceImpl.instance;
    }

    public void shutdown() {
        shutdownLock.lock();
        this.shutdown = true;
        shutdownLock.unlock();
    }

    private void notifyAllSubscriber(String topic, Map<String, String> values) {
        List<Subscriber> allSubscriber;

        synchronized (subscriptions) {
            allSubscriber = subscriptions.get(topic);
        }

        synchronized (allSubscriber) {
            for (Subscriber subscriber : allSubscriber) {
                subscriber.onSubcriptionEvent(topic, values);
            }
        }
    }

    @Override
    public int getCallbackPort() {
        int callbackPort = -1;
        callbackPortLock.lock();
        callbackPort = this.callbackPort;
        callbackPortLock.unlock();
        return callbackPort;
    }

    private String decipherTopic(String crypticText) {
        return crypticText.substring("topic:".length(), crypticText.indexOf(";"));
    }

    private Map<String, String> decipherValues(String crypticText) {
        Map<String, String> values = new HashMap<>();
        crypticText = crypticText.substring(crypticText.indexOf(";") + 1, crypticText.length());
        logger.trace("CrypticText without topic:{}", crypticText);

        while (crypticText.length() > 0) {
            logger.trace("Index: " + crypticText.indexOf(":"));
            String param = crypticText.substring(0, crypticText.indexOf(":"));
            String value = crypticText.substring(crypticText.indexOf(":") + 1, crypticText.indexOf(";"));

            values.put(param, value);

            crypticText = crypticText.substring(crypticText.indexOf(";") + 1, crypticText.length());
        }

        return values;
    }

    /*
     * Muss es im den Subscriber im Service registrieren, aber auch
     * nen RESTCall nach außen absetzen
     */
    @Override
    public void subscribe(Subscriber subscriber, String topic, long bootid) {
        addSubscription(subscriber, topic);
        addRemoteSubscription(subscriber, topic, bootid);
    }

    /**
     * Add subscriber to internel map of all subscriptions
     *
     * @param subscriber
     * @param topic
     */
    private void addSubscription(Subscriber subscriber, String topic) {
        List<Subscriber> allSubscriber;

        synchronized (subscriptions) {
            if (!subscriptions.containsKey(topic)) {
                subscriptions.put(topic, new LinkedList<Subscriber>());
            }

            allSubscriber = subscriptions.get(topic);
        }

        synchronized (allSubscriber) {
            if (!allSubscriber.contains(subscriber)) {
                allSubscriber.add(subscriber);
            }
        }
    }

    /**
     * Add as a subscriber at devices
     *
     * @param subscriber
     * @param topic
     * @param bootid
     */
    private void addRemoteSubscription(Subscriber subscriber, String topic, long bootid) {
        RESTIOServiceImpl.getInstance().addSubscription(subscriber, topic, bootid);
    }

    @Override
    public void onSuccessfulRemoteSubscription(Subscriber subscriber, String topic) {
        subscriber.onFullSuccessfulSubscription(topic);
    }

    @Override
    public void onFailedRemoteSubscription(Subscriber subscriber, String topic) {
        subscriber.onPartialSucessfulSubscription(topic);
    }

    @Override
    public void unsubscribe(Subscriber subscriber, String topic, long bootid) {
        removeSubscription(subscriber, topic);
        removeRemoteSubscription(subscriber, topic, bootid);
    }

    @Override
    public void removeSubscription(Subscriber subscriber, String topic) {
        // TODO: internal removal
        List<Subscriber> allSubscriber;

        synchronized (subscriptions) {
            if (!subscriptions.containsKey(topic)) {
                return;
            }

            allSubscriber = subscriptions.get(topic);
        }

        synchronized (allSubscriber) {
            if (allSubscriber.contains(subscriber)) {
                allSubscriber.remove(subscriber);
            }
        }
    }

    private void removeRemoteSubscription(Subscriber subscriber, String topic, long bootid) {
        RESTIOServiceImpl.getInstance().removeSubscription(subscriber, topic, bootid);
    }

    @Override
    public void onSuccessfulRemoteSubscriptionRemoval(Subscriber subscriber, String topic) {
        subscriber.onFullSuccessfullUnsubscription(topic);
    }

    @Override
    public void onFailedRemoteSubscriptionRemoval(Subscriber subscriber, String topic) {
        subscriber.onPartialSucessfulUnsubscription(topic);
    }

    @Override
    public boolean doesSubscriptionExists(Subscriber subscriber, String topic) {
        List<Subscriber> allSubscriber;

        synchronized (subscriptions) {
            if (!subscriptions.containsKey(topic)) {
                return false;
            } else {
                allSubscriber = subscriptions.get(topic);
            }
        }

        synchronized (allSubscriber) {
            if (allSubscriber != null) {
                return allSubscriber.contains(subscriber);
            }
        }

        return false;
    }
}