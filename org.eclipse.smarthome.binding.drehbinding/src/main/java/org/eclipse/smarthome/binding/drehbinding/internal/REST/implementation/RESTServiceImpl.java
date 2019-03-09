package org.eclipse.smarthome.binding.drehbinding.internal.REST.implementation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Map.Entry;

import org.eclipse.smarthome.binding.drehbinding.internal.REST.RESTService;
import org.eclipse.smarthome.binding.drehbinding.internal.exception.UnexpectedResponseCodeException;
import org.eclipse.smarthome.binding.drehbinding.internal.exception.WrongRespondCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Is ThreadSafe
 *
 * @author Tristan
 *
 */
/*
 * Wird als Singleton eingerichtet. Der Service is threadsafe
 * und daher sollte immer auch die selbe Instanz verwendet werden,
 * da es ja auch immer der selbe Service ist.
 *
 * TODO: Quatsche dies mit Peter durch (ist mehr ein Pattern)
 */
public class RESTServiceImpl implements RESTService {

    private final Logger logger = LoggerFactory.getLogger(RESTServiceImpl.class);

    private static RESTServiceImpl instance;

    private RESTServiceImpl() {

    }

    public static RESTServiceImpl getInstance() {
        if (instance == null) {
            instance = new RESTServiceImpl();
        }

        return instance;
    }

    /**
     *
     * @param request
     * @return RESTResponse which contains the response code of the REST call and all obtained
     *         custom return values.
     * @throws IOException
     */
    /*
     * TODO: Ggf anstatt null zu returnen eine Exception werfen.
     */
    @Override
    public RESTResponse makeRestCall(RESTRequest request) throws IOException {

        HttpURLConnection connection = buildConnection(request);

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

    private HttpURLConnection buildConnection(RESTRequest request) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) request.getUrl().openConnection();
        connection.setRequestMethod(request.getMethod());
        connection.setConnectTimeout(5000);

        if (request.getParams() != null) {
            for (Entry<String, String> param : request.getParams().entrySet()) {
                connection.setRequestProperty(param.getKey(), param.getValue());
            }
        }

        return connection;
    }

    @Override
    public void GET() {
        // TODO Auto-generated method stub

    }

    @Override
    public void PUT() {
        // TODO Auto-generated method stub

    }

    @Override
    public void POST() {
        // TODO Auto-generated method stub

    }

    @Override
    public void DELETE() {
        // TODO Auto-generated method stub

    }

}