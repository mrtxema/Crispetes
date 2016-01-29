package cat.mrtxema.crispetes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cat.mrtxema.crispetes.adapter.LinkViewAdapter;
import cat.mrtxema.crispetes.model.FavoriteMovie;
import cat.mrtxema.crispetes.model.Link;
import cat.mrtxema.crispetes.model.NavigationAction;
import cat.mrtxema.crispetes.model.NavigationRequest;
import cat.mrtxema.crispetes.model.NavigationResponse;
import cat.mrtxema.crispetes.service.MovieServiceClient;
import cat.mrtxema.crispetes.service.MovieServiceException;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View;

import cat.mrtxema.crispetes.model.VideoUrl;
import cat.mrtxema.crispetes.service.NavigationException;
import cat.mrtxema.crispetes.service.NavigationHelper;
import cat.mrtxema.crispetes.service.VideoServiceClient;
import cat.mrtxema.crispetes.service.VideoServiceException;
import cat.mrtxema.crispetes.store.DatabaseManager;
import cat.mrtxema.crispetes.store.StoreException;

import android.widget.CheckBox;
import android.content.Intent;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.SupposeBackground;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.DrawableRes;
import org.androidannotations.annotations.res.StringRes;

@EActivity(R.layout.activity_movie)
public class MovieActivity extends BaseActivity {
    private static final int MAX_NAVIGATION_STEPS = 3;
    private static final List<String> VIDEO_FORMATS = Arrays.asList("avi", "mkv", "mp4");
    private static final String WEBVIDEO_PACKAGE = "com.instantbits.cast.webvideo";

    @Bean MovieServiceClient movieServiceClient;
    @Bean VideoServiceClient videoServiceClient;
    @Bean NavigationHelper navigationHelper;
    @Bean DatabaseManager database;
    @SystemService ClipboardManager clipboard;
    @StringRes(R.string.chooser_title)
    String chooserTitle;

    @Extra
    FavoriteMovie movie;
    @ViewById CheckBox btnAddMovie;
    @ViewById ListView lstLinks;
    @ViewById TextView title;
    @DrawableRes(android.R.drawable.checkbox_on_background)
    Drawable checkboxDrawable;

    private final Map<String,String> videoUrls = new HashMap<>();

    @AfterViews
    void initViews() {
        videoUrls.clear();
        title.setText(movie.getMovieName());
        btnAddMovie.setChecked(movie.isSaved());
        clearMessage();
        findLinks();
    }

    @CheckedChange
    void btnAddMovie(boolean checked) {
        clearMessage();
        toggleFavoriteMovie(checked);
    }

    @ItemClick
    void lstLinks(Link link) {
        clearMessage();
        setLoadingPanelVisibility(View.VISIBLE);
        retrieveUrlAndOpen(link.getId());
    }

    @ItemLongClick(R.id.lstLinks)
    void onLinkLongClick(Link link) {
        clearMessage();
        setLoadingPanelVisibility(View.VISIBLE);
        retrieveUrlAndCopyToClipboard(link.getId());
    }

    @Background
    void findLinks() {
        try {
            List<Link> links = movieServiceClient.getMovieLinks(this, movie.getStore(), movie.getMovieId());
            showLinks(links);
        } catch(MovieServiceException e) {
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
            setMessage(e.getMessage());
        }
        setLoadingPanelVisibility(View.GONE);
    }

    @UiThread
    void showLinks(List<Link> links) {
        lstLinks.setAdapter(new LinkViewAdapter(this, links));
    }

    @Background
    void retrieveUrlAndOpen(String linkId) {
        String url = retrieveUrl(linkId);
        if (url != null) {
            openWebPage(url);
        }
    }

    @Background
    void retrieveUrlAndCopyToClipboard(String linkId) {
        String url = retrieveUrl(linkId);
        if (url != null) {
            clipboard.setPrimaryClip(ClipData.newRawUri("URI", Uri.parse(url)));
            showClipboardOkMessage();
        }
    }

    @UiThread
    void showClipboardOkMessage() {
        Toast.makeText(this, R.string.clipboard_ok_message, Toast.LENGTH_LONG).show();
    }

    @SupposeBackground
    String retrieveUrl(String linkId) {
        String finalUrl = videoUrls.get(linkId);
        if (finalUrl == null) {
            try {
                VideoUrl videoUrl = movieServiceClient.getLinkUrl(this, movie.getStore(), movie.getMovieId(), linkId);
                finalUrl = videoUrl.getUrl();
                if (videoUrl.getNavigationAction() != null) {
                    String navigationUrl = navigate(videoUrl.getNavigationAction(), 1);
                    if (navigationUrl != null) {
                        finalUrl = navigationUrl;
                    }
                }
                videoUrls.put(linkId, finalUrl);
            } catch (MovieServiceException e) {
                Log.e(getClass().getSimpleName(), e.getMessage(), e);
                setMessage(e.getMessage());
            }
        }
        setLoadingPanelVisibility(View.GONE);
        return finalUrl;
    }

    private String navigate(NavigationAction navigationAction, int stepNumber) {
        try {
            String response = (navigationAction.getPostData() == null) ?
                    navigationHelper.get(navigationAction.getUri()) :
                    navigationHelper.post(navigationAction.getUri(), navigationAction.getPostData());
            NavigationResponse navigationResponse = videoServiceClient.navigate(new NavigationRequest(navigationAction, response));
            if (navigationResponse.getNavigationAction() == null) {
                return navigationResponse.getVideoUrl();
            } else if (stepNumber < MAX_NAVIGATION_STEPS) {
                return navigate(navigationResponse.getNavigationAction(), stepNumber + 1);
            } else {
                setMessage("Too many steps navigating to " + navigationAction.getUri());
            }
        } catch (NavigationException | VideoServiceException e) {
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
            setMessage(e.getMessage());
        }
        return null;
    }

    @UiThread
    void openWebPage(String url) {
        Log.i(getClass().getSimpleName(), "openWebPage: " + url);
        String extension = url.substring(url.lastIndexOf('.') + 1).toLowerCase();
        String contentType = VIDEO_FORMATS.contains(extension) ? "video/" + extension : "video/mkv";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), contentType);
        if (!isIntentResolvedToPackage(intent, WEBVIDEO_PACKAGE)) {
            intent.setData(Uri.parse(url));
        }
        if (intent.resolveActivity(getPackageManager()) != null) {
            Intent chooserIntent = Intent.createChooser(intent, chooserTitle);
            startActivity(chooserIntent);
        } else {
            setMessage("Didn't find any compatible app. You can long click on any link to copy URL to clipboard");
        }
    }

    private boolean isIntentResolvedToPackage(Intent intent, String packageName) {
        for (ResolveInfo info : getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_ALL)) {
            if (info.activityInfo.applicationInfo.packageName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    @Background
    void toggleFavoriteMovie(boolean add) {
        try {
            if (add && !movie.isSaved()) {
                database.saveMovie(this, movie);
            } else if (!add && movie.isSaved()) {
                database.deleteMovie(this, movie);
            }
        } catch (StoreException e) {
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
            setMessage(e.getMessage());
        }
    }
}
