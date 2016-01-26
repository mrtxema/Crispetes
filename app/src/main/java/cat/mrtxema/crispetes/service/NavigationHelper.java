package cat.mrtxema.crispetes.service;

import org.androidannotations.annotations.EBean;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@EBean
public class NavigationHelper {
    private static final String UTF8_CHARSET = "utf-8";

    public String get(String url) throws NavigationException {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(url).openConnection();
            return readStream(new BufferedInputStream(urlConnection.getInputStream()));
        } catch (IOException e) {
            throw new NavigationException("Error in GET connection to " + url, e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    public String post(String url, String postData) throws NavigationException {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("charset", UTF8_CHARSET);
            urlConnection.getOutputStream().write(postData.getBytes(UTF8_CHARSET));
            return readStream(new BufferedInputStream(urlConnection.getInputStream()));
        } catch (IOException e) {
            throw new NavigationException("Error in POST connection to " + url, e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private String readStream(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copyStream(in, out);
        return out.toString(UTF8_CHARSET);
    }

    private void copyStream(InputStream in, OutputStream out) throws IOException {
        int result;
        while ((result = in.read()) != -1) {
            out.write(result);
        }
    }
}
