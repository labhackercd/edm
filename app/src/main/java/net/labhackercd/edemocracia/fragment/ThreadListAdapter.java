package net.labhackercd.edemocracia.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.util.SimpleArrayAdapter;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class ThreadListAdapter extends SimpleArrayAdapter<ThreadItem> {
    public ThreadListAdapter(Context context, List<ThreadItem> objects) {
        super(context, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.thread_list_item, parent, false);

        ThreadItem item = getItem(position);

        // Fill the icon
        ImageView iconView = (ImageView) view.findViewById(android.R.id.icon);

        Picasso.with(getContext())
                .load(item.getIconUri())
                .resize(100, 100)
                .centerCrop()
                .into(iconView);

        // Fill the main text view with the item title
        ((TextView) view.findViewById(android.R.id.text1))
                .setText(item.toString());

        // Fill the other text view with the item content, if available
        String body = item.getBody();
        if (body != null) {
            ((TextView) view.findViewById(android.R.id.text2))
                    .setText(body.replaceAll("\\n+", " "));
        }

        // Fill the item count field
        int itemCount = item.getItemCount();
        if (itemCount > 0) {
            ((TextView) view.findViewById(R.id.itemCount))
                    .setText(Integer.toString(itemCount));
        }

        // Fill the date view if any date is available
        Date date = item.getLastPostDate();
        if (date == null) {
            date = item.getCreateDate();
        }
        if (date != null) {
            // TODO format date to localized dd MMM (21 de jan)
            DateFormat dateFormat = DateFormat
                    .getDateInstance(DateFormat.DATE_FIELD | DateFormat.MONTH_FIELD);

            ((TextView) view.findViewById(R.id.date))
                    .setText(dateFormat.format(date));
        }

        return view;
    }
}
