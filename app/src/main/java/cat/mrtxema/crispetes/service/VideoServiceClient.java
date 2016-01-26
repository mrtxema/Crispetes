package cat.mrtxema.crispetes.service;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cat.mrtxema.crispetes.model.NavigationAction;
import cat.mrtxema.crispetes.model.NavigationRequest;
import cat.mrtxema.crispetes.model.NavigationResponse;

@EBean(scope = EBean.Scope.Singleton)
public class VideoServiceClient {
    private static final String BASE_PATH = "/video/v1";

    @Bean
    RestApiClient restApiClient;

    private JSONObject postUrl(String urlPath, JSONObject request) throws JSONException, VideoServiceException {
        try {
            return restApiClient.postUrl(RestApiClient.TVSHOWS_SERVER + BASE_PATH + urlPath, request.toString());
        } catch (RestApiException e) {
            JSONObject error = e.getError();
            throw new VideoServiceException(error.getString("message"));
        } catch (IOException e) {
            throw new VideoServiceException("Can't connect to endpoint: " + urlPath, e);
        }
    }

    public NavigationResponse navigate(NavigationRequest navigationRequest) throws VideoServiceException {
        final String url = "/navigate";
        try {
            JSONObject navigationObj = new JSONObject();
            navigationObj.put("uri", navigationRequest.getNavigationAction().getUri());
            navigationObj.put("postData", navigationRequest.getNavigationAction().getPostData());
            JSONObject requestObj = new JSONObject();
            requestObj.put("navigationAction", navigationObj);
            requestObj.put("serverResponse", navigationRequest.getServerResponse());
            JSONObject responseObj = postUrl(url, requestObj);
            JSONObject responseNavigationObj = responseObj.optJSONObject("navigationAction");
            NavigationAction navigationAction = null;
            if (responseNavigationObj != null) {
                navigationAction = new NavigationAction(responseNavigationObj.getString("uri"), responseNavigationObj.optString("postData"));
            }
            return new NavigationResponse(responseObj.optString("videoUrl"), navigationAction);
        } catch(JSONException e) {
            throw new VideoServiceException("Can\'t parse response from url: " + url, e);
        }
    }
}
