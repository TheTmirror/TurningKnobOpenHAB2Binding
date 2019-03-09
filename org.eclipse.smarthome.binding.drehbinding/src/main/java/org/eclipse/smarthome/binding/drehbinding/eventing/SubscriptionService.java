package org.eclipse.smarthome.binding.drehbinding.eventing;

public interface SubscriptionService {

    int getCallbackPort();

    void subscribe(Subscriber subscriber, String topic, long bootid);

    void onSuccessfulRemoteSubscription(Subscriber subscriber, String topic);

    void onFailedRemoteSubscription(Subscriber subscriber, String topic);

    void unsubscribe(Subscriber subscriber, String topic, long bootid);

    void onSuccessfulRemoteSubscriptionRemoval(Subscriber subscriber, String topic);

    void onFailedRemoteSubscriptionRemoval(Subscriber subscriber, String topic);

    void removeSubscription(Subscriber subscriber, String topic);

    boolean doesSubscriptionExists(Subscriber subscriber, String topic);

}
