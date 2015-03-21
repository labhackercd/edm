package net.labhackercd.edemocracia.ui.thread;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.ocpsoft.pretty.time.PrettyTime;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.data.ImageLoader;
import net.labhackercd.edemocracia.data.api.model.Category;
import net.labhackercd.edemocracia.data.api.model.Thread;
import net.labhackercd.edemocracia.ui.MainActivity;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ThreadListAdapter extends RecyclerView.Adapter<ThreadListAdapter.ViewHolder> {

    private final ImageLoader imageLoader;

    private List<Thread> threads = Collections.emptyList();
    private List<Category> categories = Collections.emptyList();

    public ThreadListAdapter(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    public ThreadListAdapter replaceWith(List<Category> categories, List<Thread> threads) {
        this.threads = threads;
        this.categories = categories;
        notifyDataSetChanged();
        return this;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.thread_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return threads.size() + categories.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {
        int categoryCount = categories.size();
        if (i < categoryCount)
            holder.bindCategory(categories.get(i));
        else
            holder.bindThread(threads.get(i - categoryCount));
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        @InjectView(android.R.id.text2) TextView titleView;
        @InjectView(R.id.body) TextView bodyView;
        @InjectView(R.id.itemCount) TextView countView;
        @InjectView(R.id.date) TextView dateView;
        @InjectView(R.id.portrait) ImageView portraitView;
        @InjectView(android.R.id.text1) TextView userView;

        private Thread thread;
        private Category category;
        private Subscription portraitSubscription = null;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
            view.setOnClickListener(this::handleClick);
        }

        public void bindCategory(Category category) {
            beforeBind(category, null);

            setPortrait(category.getUserName(), category.getUserId());

            setUserName(category.getUserName());

            setTitle(category.getName());

            setBody(category.getDescription());

            setCounter(category.getThreadCount());

            Date date = category.getLastPostDate();
            setDate(date != null ? date : category.getCreateDate());
        }

        public void bindThread(Thread thread) {
            beforeBind(null, thread);

            setPortrait(thread.getStatusByUserName(), thread.getRootMessageUserId());

            setUserName(thread.getStatusByUserName());

            // TODO Set the actual title.
            setTitle(thread.toString());

            // TODO Set the actual body.
            setBody(thread.toString());

            setCounter(thread.getMessageCount());

            setDate(thread.getLastPostDate());
        }

        private void beforeBind(Category category, Thread thread) {
            this.thread = thread;
            this.category = category;
            if (portraitSubscription != null) {
                portraitSubscription.unsubscribe();
                portraitSubscription = null;
            }
        }

        private void setDate(Date date) {
            String text = null;

            if (date != null) {
                Context context = dateView.getContext();
                PrettyTime formatter = new PrettyTime(context.getResources().getConfiguration().locale);
                text = formatter.format(date);
            }

            dateView.setText(text);
        }

        private void setCounter(int count) {
            countView.setText(count <= 0 ? null : Integer.toString(count));
        }

        private void setBody(String body) {
            if (body != null) {
                body = body.replaceAll("\\n+", " ");
            }
            bodyView.setText(body);
        }

        private void setTitle(String title) {
            titleView.setText(title);
        }

        private void setUserName(String userName) {
            userView.setText(userName);
        }

        private void setPortrait(String userName, long userId) {
            Drawable placeholder = portraitPlaceholder(userName);
            portraitView.setImageDrawable(placeholder);
            portraitSubscription = imageLoader.userPortrait2(userId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(r -> r.fit().centerCrop()
                            .placeholder(placeholder)
                            .into(portraitView));
        }

        private Drawable portraitPlaceholder(String userName) {
            String letter = userName.trim().substring(0, 1).toUpperCase();
            return TextDrawable.builder().buildRect(letter, Color.LTGRAY);
        }

        private void handleClick(View v) {
            if (thread != null)
                broadcastViewThread(v.getContext(), thread);
            else if (category != null)
                broadcastViewCategory(v.getContext(), category);
        }

        private void broadcastViewCategory(Context context, Category category) {
            Intent intent = MainActivity.createIntent(context, category);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }

        private void broadcastViewThread(Context context, Thread thread) {
            Intent intent = MainActivity.createIntent(context, thread);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }
}
