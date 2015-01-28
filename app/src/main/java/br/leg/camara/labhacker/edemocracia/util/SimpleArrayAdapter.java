package br.leg.camara.labhacker.edemocracia.util;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Simple convenience extension of {@link ArrayAdapter} which is easier to instantiate
 * (because it has less parameters) and uses Identifiable.getId() as id.
 *
 * @param <T extends SimpleArrayAdapter.Identifiable>
 */
public class SimpleArrayAdapter<T extends Identifiable> extends ArrayAdapter<T> {

    public SimpleArrayAdapter(Context context, List<T> objects) {
        super(context, android.R.layout.simple_list_item_1, android.R.id.text1, objects);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

}