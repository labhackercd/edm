/*
 * This file is part of Nhegatu, the e-Demoracia Client for Android.
 *
 * Nhegatu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Nhegatu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Nhegatu.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.labhackercd.nhegatu.ui.thread;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.ocpsoft.pretty.time.PrettyTime;

import net.labhackercd.nhegatu.R;
import net.labhackercd.nhegatu.data.ImageLoader;
import net.labhackercd.nhegatu.data.api.model.Category;
import net.labhackercd.nhegatu.data.api.model.Message;
import net.labhackercd.nhegatu.data.api.model.Thread;
import net.labhackercd.nhegatu.ui.MainActivity;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ThreadListAdapter extends RecyclerView.Adapter<ThreadListAdapter.ViewHolder> {

    public interface ThreadItem {
        Thread getThread();
        Observable<Message> getRootMessage();
    }

    private List<ThreadItem> threads = Collections.emptyList();
    private List<Category> categories = Collections.emptyList();
    private final ImageLoader imageLoader;

    public ThreadListAdapter(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    public ThreadListAdapter replaceWith(List<Category> categories, List<ThreadItem> threads) {
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

        private Category category;
        private ThreadItem threadItem;
        private Subscription bodySubscription;
        private Subscription titleSubscription;
        private Subscription portraitSubscription;

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

        public void bindThread(ThreadItem item) {
            beforeBind(null, item);

            Thread thread = item.getThread();

            // FIXME Use proper user name field from root message.
            // XXX This hack sucks. getStatusByUserName can return null but I don't know why.
            String userName = thread.getStatusByUserName();
            if (userName == null)
                userName = "Unknown";

            setPortrait(userName, thread.getRootMessageUserId());

            setUserName(userName);

            Observable<String> asyncTitle = item.getRootMessage().map(Message::getSubject);
            setTitle(thread.toString(), asyncTitle);

            // TODO Set the actual body.
            Observable<String> body = item.getRootMessage().map(Message::getBody);
            setBodyAsync(body);

            setCounter(thread.getMessageCount());

            setDate(thread.getLastPostDate());
        }

        private void beforeBind(Category category, ThreadItem thread) {
            this.threadItem = thread;
            this.category = category;
            if (portraitSubscription != null) {
                if (!portraitSubscription.isUnsubscribed())
                    portraitSubscription.unsubscribe();
                portraitSubscription = null;
            }
            if (titleSubscription != null) {
                if (!titleSubscription.isUnsubscribed())
                    titleSubscription.unsubscribe();
                titleSubscription = null;
            }
            if (bodySubscription != null) {
                if (!bodySubscription.isUnsubscribed())
                    bodySubscription.unsubscribe();
                bodySubscription = null;
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

        private void setBodyAsync(Observable<String> body) {
            bodySubscription = body
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::setBody, (error) -> Timber.e(error, "Failed to load message body."));
        }

        private void setTitle(String title) {
            titleView.setText(title);
        }

        private void setTitle(String placeholder, Observable<String> title) {
            setTitle(placeholder);
            titleSubscription = title
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::setTitle, (error) -> Timber.e(error, "Failed to load message title."));
        }

        private void setUserName(String userName) {
            userView.setText(userName);
        }

        private void setPortrait(String userName, long userId) {
            Drawable placeholder = portraitPlaceholder(userName);
            portraitView.setImageDrawable(placeholder);
            portraitSubscription = imageLoader.userPortrait2(userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(r -> r.fit().centerCrop()
                            .placeholder(placeholder)
                            .into(portraitView));
        }

        private Drawable portraitPlaceholder(String userName) {
            String letter = userName.trim().substring(0, 1).toUpperCase();
            return TextDrawable.builder().buildRect(letter, Color.LTGRAY);
        }

        private void handleClick(View v) {
            if (threadItem != null)
                broadcastViewThread(v.getContext(), threadItem.getThread());
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
