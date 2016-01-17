package cat.mrtxema.crispetes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import cat.mrtxema.crispetes.R;
import cat.mrtxema.crispetes.model.FavoriteMovie;

import java.text.SimpleDateFormat;
import java.util.List;

public class MovieViewAdapter extends ArrayAdapter<FavoriteMovie> {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    public MovieViewAdapter(Context context, List<FavoriteMovie> objects) {
        super(context, R.layout.listitem_multiline, objects);
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View rowView = inflater.inflate(R.layout.listitem_multiline, null, true);
        FavoriteMovie item = getItem(position);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.title);
        txtTitle.setText(item.getMovieName());

        TextView txtStore = (TextView) rowView.findViewById(R.id.store);
        txtStore.setText(item.getStore());

        if (item.getLastUpdate() != null) {
            TextView txtLastUpdate = (TextView) rowView.findViewById(R.id.last_update);
            txtLastUpdate.setText(DATE_FORMAT.format(item.getLastUpdate()));
        }

        return rowView;
    }
}
