package br.leg.camara.labhacker.edemocracia;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

import br.leg.camara.labhacker.edemocracia.content.Content;

/**
 * Simple convenience extension of {@link ArrayAdapter} which is easier to instantiate
 * (because it has less parameters) and returns uses Content.getId() as id.
 *
 * @param <T extends Content>
 */
class ContentArrayAdapter<T extends Content> extends ArrayAdapter<T> {

    public ContentArrayAdapter(Context context, List<T> objects) {
        super(context, android.R.layout.simple_list_item_1, android.R.id.text1, objects);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }
}
