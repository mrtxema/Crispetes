package cat.mrtxema.crispetes.adapter;

import android.graphics.drawable.Drawable;

public interface ItemViewCustomizer<T> {

    Drawable getImage(T object);
}
