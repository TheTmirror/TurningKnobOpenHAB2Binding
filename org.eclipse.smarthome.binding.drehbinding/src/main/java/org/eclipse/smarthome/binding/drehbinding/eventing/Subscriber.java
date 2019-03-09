package org.eclipse.smarthome.binding.drehbinding.eventing;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

public interface Subscriber {

    public String getIdentifier();

    public void onFullSuccessfulSubscription(@NonNull String topic);

    public void onFullSuccessfullUnsubscription(@NonNull String topic);

    public void onSubcriptionEvent(@NonNull String topic, @NonNull Map<String, String> values);

    /**
     * The device didn't receivce the subscription but the subscription
     * manager registered it only localy
     *
     * @param topic
     */
    public void onPartialSucessfulSubscription(@NonNull String topic);

    /**
     * The device didn't receivce the unsubscription but the subscription
     * manager removed it only localy
     *
     * @param topic
     */
    public void onPartialSucessfulUnsubscription(@NonNull String topic);

}
