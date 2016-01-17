package cat.mrtxema.crispetes.store;

import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.database.Cursor;

import cat.mrtxema.crispetes.model.Credentials;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class CredentialsStore implements DatabaseStore<Credentials> {
    private static final String TABLE_NAME = "credentials";
    private static final String FIELD_STORE = "store";
    private static final String FIELD_PARAM_NAME = "param_name";
    private static final String FIELD_PARAM_VALUE = "param_value";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
            FIELD_STORE + " TEXT, " +
            FIELD_PARAM_NAME + " TEXT, " +
            FIELD_PARAM_VALUE + " TEXT, " +
            "PRIMARY KEY (" + FIELD_STORE + ", " + FIELD_PARAM_NAME + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    private List<ContentValues> buildContentValues(Credentials item) {
        List<ContentValues> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : item.getParameters().entrySet()) {
            ContentValues values = new ContentValues();
            values.put(FIELD_STORE, item.getStore());
            values.put(FIELD_PARAM_NAME, entry.getKey());
            values.put(FIELD_PARAM_VALUE, entry.getValue());
            result.add(values);
        }
        return result;
    }

    private String[] idValues(Credentials item) {
        return new String[] {item.getStore()};
    }

    private String[] getAllFields() {
        return new String[] {FIELD_STORE, FIELD_PARAM_NAME, FIELD_PARAM_VALUE};
    }

    private Credentials buildItem(Cursor cursor) {
        final String store = cursor.getString(cursor.getColumnIndex(FIELD_STORE));
        Map<String, String> params = new HashMap<>();
        do {
            params.put(
                cursor.getString(cursor.getColumnIndex(FIELD_PARAM_NAME)),
                cursor.getString(cursor.getColumnIndex(FIELD_PARAM_VALUE))
            );
        } while (cursor.moveToNext() && cursor.getString(cursor.getColumnIndex(FIELD_STORE)).equals(store));
        return new Credentials(store, params);
    }

    @Override
    public void add(SQLiteDatabase db, Credentials item) {
        for (ContentValues values : buildContentValues(item)) {
            db.insertOrThrow(TABLE_NAME, null, values);
        }
    }

    @Override
    public Credentials find(SQLiteDatabase db, Credentials item) {
        Cursor cursor = db.query(TABLE_NAME, getAllFields(), FIELD_STORE + "=?", idValues(item), null, null, null);
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
    public List<Credentials> getAll(SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_NAME, getAllFields(), null, null, null, null, FIELD_STORE);
        try {
            final List<Credentials> result = new ArrayList<>();
            if (cursor.moveToFirst()) {
                do {
                    result.add(buildItem(cursor));
                } while (!cursor.isAfterLast());
            }
            return result;
        } finally {
            cursor.close();
        }
    }

    @Override
    public void update(SQLiteDatabase db, Credentials item) {
        for (Map.Entry<String, String> entry : item.getParameters().entrySet()) {
            ContentValues values = new ContentValues();
            values.put(FIELD_PARAM_VALUE, entry.getValue());
            db.update(TABLE_NAME, values, FIELD_STORE + "=? AND " + FIELD_PARAM_NAME + "=?", new String[] {item.getStore(), entry.getKey()});
        }
    }

    @Override
    public void delete(SQLiteDatabase db, Credentials item) {
        db.delete(TABLE_NAME, FIELD_STORE + "=?", idValues(item));
    }
}
