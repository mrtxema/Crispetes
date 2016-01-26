package cat.mrtxema.crispetes;

import java.util.Arrays;
import java.util.List;

import cat.mrtxema.crispetes.adapter.LinkViewAdapter;
import cat.mrtxema.crispetes.model.FavoriteMovie;
import cat.mrtxema.crispetes.model.Link;
import cat.mrtxema.crispetes.model.NavigationAction;
import cat.mrtxema.crispetes.model.NavigationRequest;
import cat.mrtxema.crispetes.model.NavigationResponse;
import cat.mrtxema.crispetes.service.MovieServiceClient;
import cat.mrtxema.crispetes.service.MovieServiceException;

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

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.DrawableRes;
import org.androidannotations.annotations.res.StringRes;

@EActivity(R.layout.activity_movie)
public class MovieActivity extends BaseActivity {
    private static final int MAX_NAVIGATION_STEPS = 3;
    private static final List<String> VIDEO_FORMATS = Arrays.asList("avi", "mkv", "mp4");

    @Bean MovieServiceClient movieServiceClient;
    @Bean VideoServiceClient videoServiceClient;
    @Bean NavigationHelper navigationHelper;

    @Bean DatabaseManager database;
    @StringRes(R.string.chooser_title)
    String chooserTitle;

    @Extra
    FavoriteMovie movie;
    @ViewById CheckBox btnAddMovie;
    @ViewById ListView lstLinks;
    @ViewById TextView title;
    @DrawableRes(android.R.drawable.checkbox_on_background)
    Drawable checkboxDrawable;

    @AfterViews
    void initViews() {
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
        retrieveUrl(link.getId());
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
    void retrieveUrl(String linkId) {
        try {
            VideoUrl videoUrl = movieServiceClient.getLinkUrl(this, movie.getStore(), movie.getMovieId(), linkId);
            if (videoUrl.getNavigationAction() == null) {
                openWebPage(videoUrl.getUrl());
            } else {
                String finalUrl = navigate(videoUrl.getNavigationAction(), 1);
                if (finalUrl == null) {
                    openWebPage(videoUrl.getUrl());
                } else {
                    openWebPage(finalUrl);
                }
            }
        } catch(MovieServiceException e) {
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
            setMessage(e.getMessage());
        }
        setLoadingPanelVisibility(View.GONE);
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
        Intent chooserIntent = Intent.createChooser(intent, chooserTitle);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooserIntent);
        }
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
