package cat.mrtxema.crispetes;

import java.util.List;

import cat.mrtxema.crispetes.adapter.MovieViewAdapter;
import cat.mrtxema.crispetes.model.FavoriteMovie;
import cat.mrtxema.crispetes.store.DatabaseManager;
import cat.mrtxema.crispetes.store.StoreException;

import android.util.Log;
import android.widget.ListView;
import android.view.View;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

@EActivity(R.layout.activity_main)
public class MainActivity extends BaseActivity {
    @Bean DatabaseManager database;
    @StringRes(R.string.noresults)
    String noResults;

    @ViewById ListView lstMovies;

    @ItemClick
    void lstMovies(FavoriteMovie movie) {
        MovieActivity_.intent(this).movie(movie).start();
    }

    protected void onResume() {
        super.onResume();
        setLoadingPanelVisibility(View.VISIBLE);
        clearMessage();
        retrieveMovies();
    }

    @Background
    void retrieveMovies() {
        try {
            List<FavoriteMovie> movies = database.getAllMovies(this);
            showMovies(movies);
        } catch (StoreException e) {
            Log.e("MovieServiceClient", e.getMessage(), e);
            setMessage(e.getMessage());
        }
        setLoadingPanelVisibility(View.GONE);
    }

    @UiThread
    void showMovies(List<FavoriteMovie> movies) {
        lstMovies.setAdapter(new MovieViewAdapter(this, movies));
        if (movies.isEmpty()) {
            setMessage(noResults);
        }
    }
}
