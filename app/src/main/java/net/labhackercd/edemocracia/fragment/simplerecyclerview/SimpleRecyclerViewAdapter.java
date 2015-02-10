package net.labhackercd.edemocracia.fragment.simplerecyclerview;

import android.support.v7.widget.RecyclerView;

import java.util.List;

public abstract class SimpleRecyclerViewAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private final List<T> items;

    public SimpleRecyclerViewAdapter(List<T> items) {
        this.items = items;
    }

    public T getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
