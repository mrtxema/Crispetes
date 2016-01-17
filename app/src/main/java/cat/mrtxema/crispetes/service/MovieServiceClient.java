package cat.mrtxema.crispetes.service;

import android.content.Context;

import cat.mrtxema.crispetes.model.Credentials;
import cat.mrtxema.crispetes.store.DatabaseManager;
import cat.mrtxema.crispetes.store.StoreException;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

@EBean(scope = EBean.Scope.Singleton)
public class MovieServiceClient {
    private static final String SERVER = "http://tvshowsapi.herokuapp.com";
    private static final String BASE_PATH = "/movies/v1";
    private static final int SESSION_EXPIRED_ERROR = 6;
    private static final long TOKEN_EXPIRATION_TIME = 1740000;

    @Bean RestApiClient restApiClient;
    @Bean DatabaseManager database;
    private final ExpirableCache<String,String> tokens = new ExpirableCache<>(TOKEN_EXPIRATION_TIME);
    private Map<String, Store> stores;

    private JSONObject callApiUrl(String store, String urlPath) throws JSONException, MovieServiceException {
        try {
            return restApiClient.callUrlAsJsonObject(SERVER + BASE_PATH + urlPath);
        } catch (RestApiException e) {
            JSONObject error = e.getError();
            if ((store != null) && (error.getInt("code") == SESSION_EXPIRED_ERROR)) {
                tokens.remove(store);
            }
            throw new MovieServiceException(error.getString("message"));
        } catch (IOException e) {
            throw new MovieServiceException("Can't connect to endpoint: " + urlPath, e);
        }
    }

    private JSONArray callApiUrlAsArray(String urlPath) throws JSONException, MovieServiceException {
        try {
            return restApiClient.callUrlAsJsonArray(SERVER + BASE_PATH + urlPath);
        } catch (RestApiException e) {
            throw new MovieServiceException(e.getError().getString("message"));
        } catch (IOException e) {
            throw new MovieServiceException("Can't connect to endpoint: " + urlPath, e);
        }
    }

    public List<Store> getAllStores() throws MovieServiceException {
        return new ArrayList<>(getStoreMap().values());
    }

    private synchronized Map<String,Store> getStoreMap() throws MovieServiceException {
        if (stores == null) {
            final String url = "";
            final Map<String, Store> result = new LinkedHashMap<>();
            try {
                JSONArray response = callApiUrlAsArray(url);
                for (int i = 0; i < response.length(); i++) {
                    JSONObject obj = response.getJSONObject(i);
                    String code = obj.getString("code");
                    result.put(code, new Store(code, restApiClient.asStringList(obj.getJSONArray("loginParameters"))));
                }
            } catch (JSONException e) {
                throw new MovieServiceException("Can't parse response from url: " + url, e);
            }
            stores = result;
        }
        return stores;
    }

    private String getToken(Context context, String store) throws MovieServiceException {
        String token = tokens.get(store);
        if (token == null) {
            token = login(store, getLoginCredentials(context, store));
            tokens.put(store, token);
        }
        return token;
    }

    private Map<String, String> getLoginCredentials(Context context, String storeCode) throws MovieServiceException {
        Store store = getStoreMap().get(storeCode);
        if (store == null) {
            throw new MovieServiceException("Unknown store: " + storeCode);
        }
        List<String> loginParameters = store.getLoginParameters();
        if (loginParameters.isEmpty()) {
            return Collections.emptyMap();
        } else {
            Credentials credentials;
            try {
                credentials = database.getCredentials(context, storeCode);
                if (credentials == null || !credentials.containsParameters(loginParameters)) {
                    throw new MovieServiceException("Missing credentials for store " + storeCode);
                }
            } catch (StoreException e) {
                throw new MovieServiceException("Can't retrieve credentials for store " + storeCode, e);
            }
            return credentials.getParameters();
        }
    }

    private String login(String store, Map<String,String> parameters) throws MovieServiceException {
        final String url = String.format("/%s/login%s", store, restApiClient.buildQueryString(parameters));
        try {
            return callApiUrl(store, url).getString("token");
        } catch(JSONException e) {
            throw new MovieServiceException("Can\'t parse response from url: " + url, e);
        }
    }

    public List<Movie> searchMovies(Context context, String store, String searchString) throws MovieServiceException {
        String url = null;
        final List<Movie> result = new ArrayList<>();
        try {
            url = String.format("/%s/search?token=%s&q=%s", store, getToken(context, store), URLEncoder.encode(searchString, "utf-8"));
            JSONArray movies = callApiUrl(store, url).getJSONArray("movies");
            for (int i = 0; i < movies.length(); i++) {
                JSONObject obj = movies.getJSONObject(i);
                result.add(new Movie(obj.getString("id"), obj.getString("name")));
            }
        } catch (UnsupportedEncodingException e) {
            throw new MovieServiceException("Can't build url", e);
        } catch (JSONException e) {
            throw new MovieServiceException("Can't parse response from url: " + url, e);
        }
        return result;
    }

    public List<Link> getMovieLinks(Context context, String store, String movie) throws MovieServiceException {
        final String url = String.format("/%s/movie/%s?token=%s", store, movie, getToken(context, store));
        final List<Link> result = new ArrayList<>();
        try {
            JSONArray links = callApiUrl(store, url).getJSONArray("links");
            for (int i = 0; i < links.length(); i++) {
                JSONObject obj = links.getJSONObject(i);
                JSONObject languageObj = obj.getJSONObject("language");
                Language lang = new Language(languageObj.getString("code"), languageObj.getString("name"));
                result.add(new Link(obj.getString("id"), obj.getString("server"), lang, obj.getString("videoQuality"), obj.getString("audioQuality")));
            }
        } catch (JSONException e) {
            throw new MovieServiceException("Can't parse response from url: " + url, e);
        }
        return result;
    }

    public String getLinkUrl(Context context, String store, String movie, String link) throws MovieServiceException {
        final String url = String.format("/%s/movie/%s/%s?token=%s", store, movie, link, getToken(context, store));
        try {
            return callApiUrl(store, url).getString("url");
        } catch(JSONException e) {
            throw new MovieServiceException("Can\'t parse response from url: " + url, e);
        }
    }
}
