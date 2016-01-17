package cat.mrtxema.crispetes.service;

import org.json.JSONObject;

public class RestApiException extends Exception {
    private final JSONObject error;

    public RestApiException(String message, JSONObject error) {
        super(message);
        this.error = error;
    }

    public JSONObject getError() {
        return error;
    }
}
