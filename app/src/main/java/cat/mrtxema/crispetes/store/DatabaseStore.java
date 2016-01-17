package cat.mrtxema.crispetes.store;

import android.database.sqlite.SQLiteDatabase;

import java.util.List;

public interface DatabaseStore<T> {

    void onCreate(SQLiteDatabase db);

    void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

    void add(SQLiteDatabase db, T item);

    T find(SQLiteDatabase db, T item);

    List<T> getAll(SQLiteDatabase db);

    void update(SQLiteDatabase db, T item);

    void delete(SQLiteDatabase db, T item);
}
