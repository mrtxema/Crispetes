package cat.mrtxema.crispetes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import cat.mrtxema.crispetes.R;
import cat.mrtxema.crispetes.model.FavoriteMovie;
import cat.mrtxema.crispetes.service.Link;

import java.text.SimpleDateFormat;
import java.util.List;

public class LinkViewAdapter extends ArrayAdapter<Link> {
    public LinkViewAdapter(Context context, List<Link> objects) {
        super(context, R.layout.listitem_multiline, objects);
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View rowView = inflater.inflate(R.layout.listitem_multiline, null, true);
        Link item = getItem(position);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.title);
        txtTitle.setText(item.getServer());

        TextView txtStore = (TextView) rowView.findViewById(R.id.sidenote);
        txtStore.setText(item.getLanguage().getName());

        TextView txtLastUpdate = (TextView) rowView.findViewById(R.id.subtitle);
        txtLastUpdate.setText(String.format("Video %s, Audio %s", item.getVideoQuality(), item.getAudioQuality()));

        return rowView;
    }
}
