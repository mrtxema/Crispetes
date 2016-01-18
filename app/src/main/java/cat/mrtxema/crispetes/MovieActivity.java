package cat.mrtxema.crispetes;

import java.util.List;

import cat.mrtxema.crispetes.adapter.LinkViewAdapter;
import cat.mrtxema.crispetes.model.FavoriteMovie;
import cat.mrtxema.crispetes.service.Link;
import cat.mrtxema.crispetes.service.MovieServiceClient;
import cat.mrtxema.crispetes.service.MovieServiceException;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View;
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
    @Bean MovieServiceClient client;
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
        retrieveUrl(link.getId());
    }

    @Background
    void findLinks() {
        try {
            List<Link> links = client.getMovieLinks(this, movie.getStore(), movie.getMovieId());
            showLinks(links);
        } catch(MovieServiceException e) {
            Log.e("TvShowClient", e.getMessage(), e);
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
            String url = client.getLinkUrl(this, movie.getStore(), movie.getMovieId(), linkId);
            openWebPage(url);
        } catch(MovieServiceException e) {
            Log.e("TvShowClient", e.getMessage(), e);
            setMessage(e.getMessage());
        }
    }

    @UiThread
    void openWebPage(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), "video/mkv");
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
            Log.e("TvShowClient", e.getMessage(), e);
            setMessage(e.getMessage());
        }
    }
}
