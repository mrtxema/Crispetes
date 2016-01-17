package cat.mrtxema.crispetes.store;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import cat.mrtxema.crispetes.model.Credentials;
import cat.mrtxema.crispetes.model.FavoriteMovie;

import org.androidannotations.annotations.EBean;

@EBean(scope = EBean.Scope.Singleton)
public class DatabaseManager {
    private List<FavoriteMovie> moviesCache;
    private Map<String, Credentials> credentialsCache;

    private MoviesDatabase getMoviesDatabase(Context context) {
        return new MoviesDatabase(context);
    }

    public List<FavoriteMovie> getAllMovies(Context context) throws StoreException {
        if (moviesCache == null) {
            moviesCache = getMoviesDatabase(context).getAll(FavoriteMovie.class);
        }
        return moviesCache;
    }

    public void saveMovie(Context context, FavoriteMovie show) throws StoreException {
        if (show.isSaved()) {
            getMoviesDatabase(context).update(show);
        } else {
            getMoviesDatabase(context).add(show);
        }
        moviesCache = null;
    }

    public void deleteMovie(Context context, FavoriteMovie show) throws StoreException {
        getMoviesDatabase(context).delete(show);
        moviesCache = null;
    }

    private void ensureCredentials(Context context) throws StoreException {
        if (credentialsCache == null) {
            Map<String, Credentials> credentialsMap = new LinkedHashMap<>();
            for (Credentials item : getMoviesDatabase(context).getAll(Credentials.class)) {
                credentialsMap.put(item.getStore(), item);
            }
            credentialsCache = credentialsMap;
        }
    }

    public List<Credentials> getAllCredentials(Context context) throws StoreException {
        ensureCredentials(context);
        return new ArrayList<>(credentialsCache.values());
    }

    public Credentials getCredentials(Context context, String store) throws StoreException {
        ensureCredentials(context);
        return credentialsCache.get(store);
    }

    public void saveCredentials(Context context, Credentials credentials) throws StoreException {
        getMoviesDatabase(context).add(credentials);
        credentialsCache = null;
    }

    public void deleteCredentials(Context context, Credentials credentials) throws StoreException {
        getMoviesDatabase(context).delete(credentials);
        credentialsCache = null;
    }
}
