package org.eclipse.smarthome.binding.drehbinding.internal.REST;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Map.Entry;

import org.eclipse.smarthome.binding.drehbinding.internal.REST.implementation.RESTRequest;
import org.eclipse.smarthome.binding.drehbinding.internal.REST.implementation.RESTResponse;
import org.eclipse.smarthome.binding.drehbinding.internal.exception.UnexpectedResponseCodeException;
import org.eclipse.smarthome.binding.drehbinding.internal.exception.WrongRespondCodeException;

import com.google.gson.Gson;

public interface RESTService {

    public static RESTResponse makeRestCall(RESTRequest request) throws IOException {

        // Build connection
        HttpURLConnection connection = (HttpURLConnection) request.getUrl().openConnection();
        connection.setRequestMethod(request.getMethod());
        connection.setConnectTimeout(5000);

        if (request.getParams() != null) {
            for (Entry<String, String> param : request.getParams().entrySet()) {
                connection.setRequestProperty(param.getKey(), param.getValue());
            }
        }

        // Work with connection
        RESTResponse response = null;
        switch (connection.getResponseCode()) {
            case 200:
                StringBuilder responseBodyBuilder = null;
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                responseBodyBuilder = new StringBuilder();
                String currentLine;

                while ((currentLine = reader.readLine()) != null) {
                    responseBodyBuilder.append(currentLine);
                }

                Gson gson = new Gson();
                response = gson.fromJson(responseBodyBuilder.toString(), RESTResponse.class);
                if (response == null) {
                    /*
                     * Dieser Fall sollte eigentlich nicht auftreten, da es sich sonst um einen
                     * 204 handelt und nicht um einen 200. Dieser Fall ist nur drinnen, um falsch
                     * implementierte WebServices abzufangen.
                     */
                    throw new WrongRespondCodeException();
                } else {
                    response.setResponseCode(connection.getResponseCode());
                    return response;
                }

            case 204:
                response = new RESTResponse();
                response.setResponseCode(connection.getResponseCode());
                return response;

            default:
                throw new UnexpectedResponseCodeException();
        }
    }

    public static void GET() {
    }

    public static void PUT() {
    }

    public static void POST() {
    }

    public static void DELETE() {
    }

}