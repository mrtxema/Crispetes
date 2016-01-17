package cat.mrtxema.crispetes.adapter;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.content.Context;
import java.util.List;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import cat.mrtxema.crispetes.R;

public class ItemViewAdapter<T> extends ArrayAdapter<T> {
    private ItemViewCustomizer<T> customizer;

    public ItemViewAdapter(Context context, List<T> objects, ItemViewCustomizer<T> customizer) {
        super(context, R.layout.listitem_image, objects);
        this.customizer = customizer;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View rowView = inflater.inflate(R.layout.listitem_image, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.text);
        T item = getItem(position);
        txtTitle.setText(item.toString());
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        Drawable image = customizer.getImage(item);
        if (image != null) {
            imageView.setImageDrawable(image);
        }
        return rowView;
    }
}
