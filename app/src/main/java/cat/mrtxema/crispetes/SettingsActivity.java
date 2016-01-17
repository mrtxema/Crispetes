package cat.mrtxema.crispetes;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import cat.mrtxema.crispetes.model.Credentials;
import cat.mrtxema.crispetes.service.MovieServiceException;
import cat.mrtxema.crispetes.service.Store;
import cat.mrtxema.crispetes.service.MovieServiceClient;
import cat.mrtxema.crispetes.store.DatabaseManager;
import cat.mrtxema.crispetes.store.StoreException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EActivity(R.layout.activity_settings)
public class SettingsActivity extends BaseActivity {
    private static final int CREDENTIALS_REQUEST = 4;
    private Map<String, Store> stores;
    @Bean
    MovieServiceClient client;
    @Bean DatabaseManager database;
    @StringRes(R.string.noresults)
    String noResults;

    @ViewById ListView lstCredentials;

    @Override
    protected void onResume() {
        super.onResume();
        setLoadingPanelVisibility(View.VISIBLE);
        clearMessage();
        retrieveCredentials();
    }

    @AfterViews
    void initViews() {
        retrieveStores();
    }

    @ItemClick
    void lstCredentials(Credentials credentials) {
        if (stores != null) {
            Store store = stores.get(credentials.getStore());
            CredentialsActivity_.intent(this).store(store).credentials(credentials).startForResult(CREDENTIALS_REQUEST);
        }
    }

    @Background
    void retrieveCredentials() {
        try {
            List<Credentials> credentialsList = database.getAllCredentials(this);
            showCredentials(credentialsList);
        } catch (StoreException e) {
            Log.e("TvShowClient", e.getMessage(), e);
            setMessage(e.getMessage());
        }
        setLoadingPanelVisibility(View.GONE);
    }

    @UiThread
    void showCredentials(List<Credentials> credentialsList) {
        lstCredentials.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, credentialsList));
        if (credentialsList.isEmpty()) {
            setMessage(noResults);
        }
    }

    @Background
    void retrieveStores() {
        try {
            List<Store> storeList = client.getAllStores();
            showStores(storeList);
        } catch(MovieServiceException e) {
            Log.e("TvShowClient", e.getMessage(), e);
            setMessage(e.getMessage());
        }
    }

    @UiThread
    void showStores(List<Store> storeList) {
        stores = new HashMap<>();
        for (Store store : storeList) {
            stores.put(store.getCode(), store);
        }
        if (storeList.isEmpty()) {
            setMessage(noResults);
        }
    }

    /*
    @OnActivityResult(CREDENTIALS_REQUEST)
    void onCredentialsResult(int resultCode) {
        if (resultCode == RESULT_OK) {
            setLoadingPanelVisibility(View.VISIBLE);
            clearMessage();
            retrieveCredentials();
        }
    }
    */
}
