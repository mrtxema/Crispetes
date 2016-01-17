package cat.mrtxema.crispetes.model;

import android.os.Parcelable;
import android.os.Parcel;

import java.util.Date;

public class FavoriteMovie implements Parcelable {
    private Integer id;
    private Date lastUpdate;
    private final String movieId;
    private final String movieName;
    private final String store;

    public FavoriteMovie(String store, String movieId, String movieName) {
        this.store = store;
        this.movieId = movieId;
        this.movieName = movieName;
    }

    public FavoriteMovie(int id, String store, String movieId, String movieName, Date lastUpdate) {
        this.id = id;
        this.store = store;
        this.movieId = movieId;
        this.movieName = movieName;
        this.lastUpdate = lastUpdate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isSaved() {
        return (id != null);
    }

    public String getStore() {
        return store;
    }

    public String getMovieId() {
        return movieId;
    }

    public String getMovieName() {
        return movieName;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public String toString() {
        return movieName;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        writeOptionalInteger(out, id);
        out.writeString(store);
        out.writeString(movieId);
        out.writeString(movieName);
        writeOptionalDate(out, lastUpdate);
    }

    private void writeOptionalInteger(Parcel out, Integer i) {
        out.writeInt((i != null) ? i : -1);
    }

    private Integer readOptionalInteger(Parcel in) {
        int result = in.readInt();
        return (result != -1) ? result : null;
    }

    private void writeOptionalDate(Parcel out, Date d) {
        out.writeLong((d != null) ? d.getTime() : -1);
    }

    private Date readOptionalDate(Parcel in) {
        long result = in.readLong();
        return (result != -1) ? new Date(result) : null;
    }

    private FavoriteMovie(Parcel in) {
        id = readOptionalInteger(in);
        store = in.readString();
        movieId = in.readString();
        movieName = in.readString();
        lastUpdate = readOptionalDate(in);
    }

    public static final Parcelable.Creator<FavoriteMovie> CREATOR = new Parcelable.Creator<FavoriteMovie>() {
        public FavoriteMovie createFromParcel(Parcel in) {
            return new FavoriteMovie(in);
        }

        public FavoriteMovie[] newArray(int size) {
            return new FavoriteMovie[size];
        }
    };
}
