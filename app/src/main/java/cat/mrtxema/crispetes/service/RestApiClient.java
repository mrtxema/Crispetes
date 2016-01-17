package cat.mrtxema.crispetes.service;

import org.androidannotations.annotations.EBean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EBean
public class RestApiClient {

    private RestApiResponse callUrl(String url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
        try {
            urlConnection.setRequestProperty("content-type", "application/json");
            return new RestApiResponse(
                    readStream(new BufferedInputStream(urlConnection.getInputStream())),
                    urlConnection.getResponseCode());
        } finally {
                urlConnection.disconnect();
        }
    }

    private String readStream(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copyStream(in, out);
        return out.toString();
    }

    private void copyStream(InputStream in, OutputStream out) throws IOException {
        int result;
        while ((result = in.read()) != -1) {
            out.write(result);
        }
    }

    JSONObject callUrlAsJsonObject(String url) throws RestApiException, IOException, JSONException {
        RestApiResponse response = callUrl(url);
        if (response.getStatus() == HttpURLConnection.HTTP_BAD_REQUEST) {
            throw new RestApiException("Error calling url: " + url, new JSONObject(response.getContent()));
        }
        return new JSONObject(response.getContent());
    }

    JSONArray callUrlAsJsonArray(String url) throws RestApiException, IOException, JSONException {
        RestApiResponse response = callUrl(url);
        if (response.getStatus() == HttpURLConnection.HTTP_BAD_REQUEST) {
            throw new RestApiException("Error calling url: " + url, new JSONObject(response.getContent()));
        }
        return new JSONArray(response.getContent());
    }

    List<String> asStringList(JSONArray array) throws JSONException {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            result.add(array.getString(i));
        }
        return result;
    }

    String buildQueryString(Map<String, String> parameters) {
        if (parameters.isEmpty()) {
            return "";
        } else {
            StringBuffer result = new StringBuffer();
            for (Map.Entry<String,String> entry : parameters.entrySet()) {
                result.append(result.length() == 0 ? "?" : "&");
                result.append(entry.getKey());
                result.append("=");
                result.append(entry.getValue());
            }
            return result.toString();
        }
    }

    private static class RestApiResponse {
        private final String content;
        private final int status;

        public RestApiResponse(String content, int status) {
            this.content = content;
            this.status = status;
        }

        public String getContent() {
            return content;
        }

        public int getStatus() {
            return status;
        }
    }
}
