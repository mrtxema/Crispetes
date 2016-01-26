package cat.mrtxema.crispetes.model;

public class VideoUrl {
    private final String url;
    private final NavigationAction navigationAction;

    public VideoUrl(String url, NavigationAction navigationAction) {
        this.url = url;
        this.navigationAction = navigationAction;
    }

    public String getUrl() {
        return url;
    }

    public NavigationAction getNavigationAction() {
        return navigationAction;
    }
}
