package org.eclipse.smarthome.binding.drehbinding.internal.REST.implementation;

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

import org.eclipse.smarthome.binding.drehbinding.internal.REST.RESTIOParticipant;
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
public class SubscriptionService {

    private final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

    private static SubscriptionService instance;

    private static final String POOL_NAME = "DrehbindingSubscriptionPool";
    ExecutorService schedular;

    private final Map<String, List<RESTIOParticipant>> subscriptions;

    private boolean shutdown = false;
    private int callbackPort = -1;

    private final Lock shutdownLock;
    private final Lock callbackPortLock;

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

    private SubscriptionService() {
        callbackPortLock = new ReentrantLock();
        shutdownLock = new ReentrantLock();

        schedular = ThreadPoolManager.getPool(POOL_NAME);

        subscriptions = new HashMap<>();

        schedular.execute(callbackListener);
    }

    public static synchronized SubscriptionService getInstance() {
        if (SubscriptionService.instance == null) {
            SubscriptionService.instance = new SubscriptionService();
        }

        return SubscriptionService.instance;
    }

    public void addSubscription(RESTIOParticipant participant, String topic) {
        List<RESTIOParticipant> subscriber;

        synchronized (subscriptions) {
            if (!subscriptions.containsKey(topic)) {
                subscriptions.put(topic, new LinkedList<RESTIOParticipant>());
            }

            subscriber = subscriptions.get(topic);
        }

        synchronized (subscriber) {
            if (!subscriber.contains(participant)) {
                subscriber.add(participant);
            }
        }
    }

    public void shutdown() {
        shutdownLock.lock();
        this.shutdown = true;
        shutdownLock.unlock();
    }

    private void notifyAllSubscriber(String topic, Map<String, String> values) {
        List<RESTIOParticipant> subscriber;

        synchronized (subscriptions) {
            subscriber = subscriptions.get(topic);
        }

        synchronized (subscriber) {
            for (RESTIOParticipant participant : subscriber) {
                participant.onSubcriptionEvent(topic, values);
            }
        }
    }

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

}