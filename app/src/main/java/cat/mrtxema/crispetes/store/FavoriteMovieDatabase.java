package cat.mrtxema.crispetes.store;

import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.database.Cursor;

import cat.mrtxema.crispetes.model.FavoriteMovie;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class FavoriteMovieDatabase implements DatabaseStore<FavoriteMovie> {
    private static final String TABLE_NAME = "movies";
    private static final String FIELD_ID = "id";
    private static final String FIELD_SHOW_ID = "movie_id";
    private static final String FIELD_SHOW_NAME = "movie_name";
    private static final String FIELD_STORE = "store";
    private static final String FIELD_LAST_UPDATE = "last_update";

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_SHOWS_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                FIELD_ID + " INTEGER PRIMARY KEY, " +
                FIELD_STORE + " TEXT, " +
                FIELD_SHOW_ID + " TEXT, " +
                FIELD_SHOW_NAME + " TEXT, " +
                FIELD_LAST_UPDATE + " INTEGER)";
        db.execSQL(CREATE_SHOWS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    private ContentValues buildContentValues(FavoriteMovie movie) {
        ContentValues values = new ContentValues();
        values.put(FIELD_STORE, movie.getStore());
        values.put(FIELD_SHOW_ID, movie.getMovieId());
        values.put(FIELD_SHOW_NAME, movie.getMovieName());
        values.put(FIELD_LAST_UPDATE, movie.getLastUpdate() == null ? null : movie.getLastUpdate().getTime());
        return values;
    }

    private String[] idValues(FavoriteMovie movie) {
        return new String[] {String.valueOf(movie.getId())};
    }

    private String[] getAllFields() {
        return new String[] {FIELD_ID, FIELD_STORE, FIELD_SHOW_ID, FIELD_SHOW_NAME, FIELD_LAST_UPDATE};
    }

    private Integer getCursorInteger(Cursor cursor, String field) {
        final int columnIndex = cursor.getColumnIndex(field);
        return cursor.isNull(columnIndex) ? null : cursor.getInt(columnIndex);
    }

    private Date getCursorDate(Cursor cursor, String field) {
        final int columnIndex = cursor.getColumnIndex(field);
        return cursor.isNull(columnIndex) ? null : new Date(cursor.getLong(columnIndex));
    }

    private FavoriteMovie buildItem(Cursor cursor) {
        return new FavoriteMovie(
                getCursorInteger(cursor, FIELD_ID),
                cursor.getString(cursor.getColumnIndex(FIELD_STORE)),
                cursor.getString(cursor.getColumnIndex(FIELD_SHOW_ID)),
                cursor.getString(cursor.getColumnIndex(FIELD_SHOW_NAME)),
                getCursorDate(cursor, FIELD_LAST_UPDATE)
        );
    }

    @Override
    public void add(SQLiteDatabase db, FavoriteMovie movie) {
        final long id = db.insertOrThrow(TABLE_NAME, null, buildContentValues(movie));
        movie.setId((int) id);
    }

    @Override
    public FavoriteMovie find(SQLiteDatabase db, FavoriteMovie item) {
        Cursor cursor = db.query(TABLE_NAME, getAllFields(), FIELD_ID + "=?", idValues(item), null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return buildItem(cursor);
            } else {
                return null;
            }
        } finally {
            cursor.close();
        }
    }

    @Override
    public List<FavoriteMovie> getAll(SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_NAME, getAllFields(), null, null, null, null, FIELD_LAST_UPDATE + " DESC, " + FIELD_SHOW_NAME);
        try {
            final List<FavoriteMovie> movieList = new ArrayList<>();
            if (cursor.moveToFirst()) {
                do {
                    movieList.add(buildItem(cursor));
                } while (cursor.moveToNext());
            }
            return movieList;
        } finally {
            cursor.close();
        }
    }

    @Override
    public void update(SQLiteDatabase db, FavoriteMovie movie) {
        db.update(TABLE_NAME, buildContentValues(movie), FIELD_ID + "=?", idValues(movie));
    }

    @Override
    public void delete(SQLiteDatabase db, FavoriteMovie movie) {
        db.delete(TABLE_NAME, FIELD_ID + "=?", idValues(movie));
        movie.setId(null);
    }
}
