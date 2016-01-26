package cat.mrtxema.crispetes.model;

public class NavigationResponse {
    private final String videoUrl;
    private final NavigationAction navigationAction;

    public NavigationResponse(String videoUrl, NavigationAction navigationAction) {
        this.videoUrl = videoUrl;
        this.navigationAction = navigationAction;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public NavigationAction getNavigationAction() {
        return navigationAction;
    }
}
