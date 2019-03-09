package org.eclipse.smarthome.binding.drehbinding.deprecated;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

public interface RESTIOParticipant {

    /**
     * Called after successful subscription to the desired subscriptionservice
     */
    public void onSuccessfulSubscription();

    /**
     * Called after failed subscription to the desired subscriptionservice
     */
    public void onFailedSubscription();

    public void onSuccessfulUnsubscription();

    public void onFailedUnsubscription();

    /**
     * Called when a event happend which refers to the subscription
     *
     * @param Identifier of the service that was subscriped to
     * @param values
     */
    public void onSubcriptionEvent(@NonNull String topic, @NonNull Map<String, String> values);

    public String getIdentifier();

}
