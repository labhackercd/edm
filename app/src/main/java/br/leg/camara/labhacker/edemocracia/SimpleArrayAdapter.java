package br.leg.camara.labhacker.edemocracia;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import br.leg.camara.labhacker.edemocracia.content.Content;

class SimpleArrayAdapter<T> extends ArrayAdapter<T> {

    private static int DEFAULT_RESOURCE_ID = android.R.layout.simple_list_item_1;
    private static int DEFAULT_TEXT_VIEW_ID = android.R.id.text1;

    public SimpleArrayAdapter(Context context, List<T> objects) {
        super(context, DEFAULT_RESOURCE_ID, DEFAULT_TEXT_VIEW_ID, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(DEFAULT_RESOURCE_ID, parent, false);
        } else {
            view = convertView;
        }

        TextView name;

        try {
            name = (TextView) view.findViewById(DEFAULT_TEXT_VIEW_ID);
        } catch (ClassCastException e) {
            Log.e(getClass().getSimpleName(), "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                    getClass().getSimpleName() + " requires the resource ID to be a TextView", e);
        }

        if (name != null) {
            T item = getItem(position);
            name.setText(item.toString());
        } else {
            Log.w(getClass().getSimpleName(), "Missing text view");
        }

        return view;
    }

    @Override
    public long getItemId(int position) {
        T item = getItem(position);
        if (item instanceof Content) {
            return ((Content) item).getId();
        }
        return position;
    }
}
