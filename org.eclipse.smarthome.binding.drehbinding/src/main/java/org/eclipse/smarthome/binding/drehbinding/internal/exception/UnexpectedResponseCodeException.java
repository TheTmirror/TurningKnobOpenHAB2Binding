package org.eclipse.smarthome.binding.drehbinding.internal.exception;

public class UnexpectedResponseCodeException extends RuntimeException {

    private static final String errorMessage = "Not prepared to handle the response code";

    public UnexpectedResponseCodeException() {
        super(errorMessage);
    }

}
