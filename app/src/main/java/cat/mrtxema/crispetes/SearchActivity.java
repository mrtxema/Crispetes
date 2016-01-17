package cat.mrtxema.crispetes;

import java.util.Collections;
import java.util.List;

import cat.mrtxema.crispetes.model.Credentials;
import cat.mrtxema.crispetes.model.FavoriteMovie;
import cat.mrtxema.crispetes.service.Movie;
import cat.mrtxema.crispetes.service.MovieServiceException;
import cat.mrtxema.crispetes.service.Store;
import cat.mrtxema.crispetes.service.MovieServiceClient;
import cat.mrtxema.crispetes.store.DatabaseManager;
import cat.mrtxema.crispetes.store.StoreException;

import android.util.Log;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.view.View;
import android.widget.EditText;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemSelect;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

@EActivity(R.layout.activity_search)
public class SearchActivity extends BaseActivity {
    private static final int CREDENTIALS_REQUEST = 3;
    private Store selectedStore;
    @Bean
    MovieServiceClient client;
    @Bean DatabaseManager database;
    @StringRes(R.string.noresults)
    String noResults;

    @ViewById ListView lstMovies;
    @ViewById EditText txtMovie;
    @ViewById Spinner selectProvider;

    @AfterViews
    void initViews() {
        clearMessage();
        retrieveStores();
    }

    @Background
    void retrieveStores() {
        try {
            showStores(client.getAllStores());
        } catch(MovieServiceException e) {
            Log.e("TvShowClient", e.getMessage(), e);
            setMessage(e.getMessage());
        }
        setLoadingPanelVisibility(View.GONE);
    }

    @UiThread
    void showStores(List<Store> stores) {
        ArrayAdapter<Store> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stores);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectProvider.setAdapter(adapter);
        if (stores.isEmpty()) {
            setMessage(noResults);
        } else {
            setSelectedStore(stores.get(0).getCode());
        }
    }

    @ItemSelect
    void selectProvider(boolean selected, Store store) {
        if (selected) {
            if (!store.getLoginParameters().isEmpty()) {
                retrieveCredentials(store);
            } else {
                selectedStore = store;
            }
        }
    }

    @Background
    void retrieveCredentials(Store store) {
        try {
            Credentials credentials = database.getCredentials(this, store.getCode());
            showCredentials(store, credentials);
        } catch (StoreException e) {
            Log.e("TvShowClient", e.getMessage(), e);
            setMessage(e.getMessage());
        }
    }

    @UiThread
    void showCredentials(Store store, Credentials credentials) {
        if (!store.getLoginParameters().isEmpty() && (credentials == null || !credentials.containsParameters(store.getLoginParameters()))) {
            CredentialsActivity_.intent(this).store(store).startForResult(CREDENTIALS_REQUEST);
        } else {
            selectedStore = store;
        }
    }

    @Click
    void btnSearchMovie() {
        if ((selectProvider.getCount() > 0) && (selectProvider.getSelectedItem() != null)) {
            setLoadingPanelVisibility(View.VISIBLE);
            clearMessage();
            lstMovies.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Collections.emptyList()));
            searchMovies(txtMovie.getText().toString(), selectedStore.getCode());
        }
    }

    @Background
    void searchMovies(String searchString, String store) {
        try {
            List<Movie> movies = client.searchMovies(this, store, searchString);
            showMovies(movies);
        } catch(MovieServiceException e) {
            Log.e("TvShowClient", e.getMessage(), e);
            setMessage(e.getMessage());
        }
        setLoadingPanelVisibility(View.GONE);
    }

    @UiThread
    void showMovies(List<Movie> movies) {
        lstMovies.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, movies));
        if (movies.isEmpty()) {
            setMessage(noResults);
        }
    }

    @ItemClick
    void lstMovies(Movie basicMovie) {
        FavoriteMovie movie = new FavoriteMovie(selectedStore.getCode(), basicMovie.getId(), basicMovie.getName());
        MovieActivity_.intent(this).movie(movie).start();
    }

    @OnActivityResult(CREDENTIALS_REQUEST)
    void onCredentialsResult(int resultCode) {
        if (resultCode == RESULT_OK) {
            selectedStore = Store.class.cast(selectProvider.getSelectedItem());
        } else {
            setSelectedStore(selectedStore.getCode());
        }
    }

    private void setSelectedStore(String storeCode) {
        for (int i=0; i<selectProvider.getCount(); i++) {
            if (Store.class.cast(selectProvider.getItemAtPosition(i)).getCode().equals(storeCode)) {
                selectProvider.setSelection(i);
                return;
            }
        }
        selectProvider.setSelection(0);
    }
}
