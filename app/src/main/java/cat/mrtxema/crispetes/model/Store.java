package cat.mrtxema.crispetes.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Store implements Parcelable {
    private final String code;
    private final List<String> loginParameters;

    public Store(String code, List<String> loginParameters) {
        this.code = code;
        this.loginParameters = Collections.unmodifiableList(loginParameters);
    }

    public String getCode() {
        return code;
    }

    public List<String> getLoginParameters() {
        return loginParameters;
    }

    @Override
    public String toString() {
        return code;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(code);
        out.writeStringList(loginParameters);
    }

    private Store(Parcel in) {
        code = in.readString();
        List<String> params = new ArrayList<>();
        in.readStringList(params);
        loginParameters = Collections.unmodifiableList(params);
    }

    public static final Parcelable.Creator<Store> CREATOR = new Parcelable.Creator<Store>() {
        public Store createFromParcel(Parcel in) {
            return new Store(in);
        }

        public Store[] newArray(int size) {
            return new Store[size];
        }
    };
}
