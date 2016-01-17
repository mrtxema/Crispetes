package cat.mrtxema.crispetes.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Credentials implements Parcelable {
    private final String store;
    private final Map<String, String> parameters;

    public Credentials(String store, Map<String, String> parameters) {
        this.store = store;
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    public Credentials(String store) {
        this.store = store;
        this.parameters = Collections.emptyMap();
    }

    public String getStore() {
        return store;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public boolean containsParameters(List<String> loginParameters) {
        return parameters.keySet().containsAll(loginParameters);
    }

    @Override
    public String toString() {
        return store;
    }


    private Bundle buildBundle(Map<String, String> map) {
        final Bundle bundle = new Bundle();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }
        return bundle;
    }

    private Map<String, String> buildMap(Bundle bundle) {
        final Map<String, String> map = new HashMap<>();
        for (String key : bundle.keySet()) {
            map.put(key, bundle.getString(key));
        }
        return map;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(store);
        out.writeBundle(buildBundle(parameters));
    }

    private Credentials(Parcel in) {
        store = in.readString();
        parameters = Collections.unmodifiableMap(buildMap(in.readBundle()));
    }

    public static final Parcelable.Creator<Credentials> CREATOR = new Parcelable.Creator<Credentials>() {
        public Credentials createFromParcel(Parcel in) {
            return new Credentials(in);
        }

        public Credentials[] newArray(int size) {
            return new Credentials[size];
        }
    };
}
