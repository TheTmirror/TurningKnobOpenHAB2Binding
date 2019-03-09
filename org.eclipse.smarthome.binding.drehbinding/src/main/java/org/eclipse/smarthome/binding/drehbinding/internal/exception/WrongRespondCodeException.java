package org.eclipse.smarthome.binding.drehbinding.internal.exception;

public class WrongRespondCodeException extends RuntimeException {

    private static final String errorMessage = "The response code returned by the server doesn't"
            + "match the response";

    public WrongRespondCodeException() {
        super(errorMessage);
    }

}