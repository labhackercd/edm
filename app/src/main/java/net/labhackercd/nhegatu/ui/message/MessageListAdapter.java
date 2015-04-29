package net.labhackercd.nhegatu.ui.message;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.ocpsoft.pretty.time.PrettyTime;

import net.labhackercd.nhegatu.R;
import net.labhackercd.nhegatu.data.ImageLoader;
import net.labhackercd.nhegatu.data.LocalMessageStore;
import net.labhackercd.nhegatu.data.db.LocalMessage;

import net.labhackercd.nhegatu.data.model.Message;
import org.kefirsf.bb.TextProcessor;

import java.util.*;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    private final ImageLoader imageLoader;
    private final TextProcessor textProcessor;
    private final LocalMessageStore messageRepository;

    private List<? extends Item> items = Collections.emptyList();

    public MessageListAdapter(LocalMessageStore messageRepository, TextProcessor textProcessor, ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
        this.textProcessor = textProcessor;
        this.messageRepository = messageRepository;
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.message_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {
        holder.bindItem(items.get(i));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public MessageListAdapter replaceWith(List<? extends Item> items) {
        // TODO Notify better changes so we don't have to redraw everything every single time.
        this.items = items;
        notifyDataSetChanged();
        return this;
    }

    public int getItemPosition(UUID uuid) {
        // FIXME Shouldn't we synchronize this?
        for (int i = 0; i < getItemCount(); i++) {
            if (items.get(i).getMessage().getUuid().equals(uuid)) {
                return i;
            }
        }
        return -1;
    }

    interface Item {
        Message getMessage();
        String getUserName();
        LocalMessage.Status getStatus();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.date) StatusView dateView;
        @InjectView(R.id.body) MessageView bodyView;
        @InjectView(R.id.portrait) ImageView portraitView;
        @InjectView(android.R.id.text1) TextView userView;
        @InjectView(android.R.id.text2) TextView subjectView;
        @InjectView(R.id.error_button) ImageView errorIcon;

        private Item item;
        private Subscription subscription;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
            view.setOnClickListener(this::handleClick);
            ButterKnife.findById(view, R.id.reply).setOnClickListener(this::handleReplyClick);
            errorIcon.setOnClickListener(this::handleErrorClick);
        }

        public void bindItem(Item item) {

            // Don't update if not required.
            if (this.item != null && this.item.equals(item))
                return;

            /** Empty everything before binding. */
            if (subscription != null) {
                if (!subscription.isUnsubscribed())
                    subscription.unsubscribe();
                subscription = null;
            }

            this.item = item;
            Message message = item.getMessage();

            /** Finally, bind the item. */

            setUser(message.getUserId(), item.getUserName());

            setSubject(message.getSubject());

            setBody(message.getBody());

            setStatus(item.getStatus(), message.getCreateDate());
        }

        private void setUser(long userId, String userName) {
            userView.setText(userName);

            final Drawable placeholder = userName == null ? null : getPortraitPlaceholder(userName);

            portraitView.setImageDrawable(placeholder);

            if (userId != 0) {
                subscription = imageLoader.userPortrait2(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(request -> request.placeholder(placeholder))
                        .subscribe(
                                request -> request.fit().centerCrop().into(portraitView),
                                error -> Timber.e(error, "Failed to load message portrait."));
            }
        }

        private void setSubject(String subject) {
            subjectView.setText(subject);
        }

        private void setStatus(LocalMessage.Status status, Date createDate) {
            if (status.equals(LocalMessage.Status.SUCCESS)) {
                String text;
                if (createDate != null) {
                    Locale locale = dateView.getResources().getConfiguration().locale;
                    text = new PrettyTime(locale).format(createDate);
                } else {
                    text = null;
                }
                setStatus(text);
                setHasError(false);
            } else if (status.equals(LocalMessage.Status.QUEUE)) {
                setStatus(R.string.sending_message);
                setHasError(false);
            } else {
                setStatus(R.string.message_submission_failed);
                setHasError(true);
            }
        }

        private void setStatus(String text) {
            dateView.setText(text);
        }

        private void setStatus(int resId) {
            dateView.setText(resId);
        }

        private void setHasError(boolean hasError) {
            /*
            TODO The text get BOLD only when there is an error.
            Typeface tf = dateView.getTypeface();
            int style = tf.getStyle();
            if (error)
                style |= Typeface.BOLD;
            else
                style &= Typeface.BOLD;
            dateView.setTypeface(tf, style);
            */

            dateView.setMessageSubmissionError(hasError);
            errorIcon.setVisibility(hasError ? View.VISIBLE : View.GONE);
        }

        private Drawable getPortraitPlaceholder(String userName) {
            String letter = userName.trim().substring(0, 1).toUpperCase();
            return TextDrawable.builder().buildRect(letter, Color.LTGRAY);
        }

        private void setBody(String body) {
            if (!TextUtils.isEmpty(body)) {
                bodyView.setHTMLText(textProcessor.process(body));
                bodyView.setLinksClickable(true);
                bodyView.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                bodyView.setText(null);
            }
        }

        private void handleClick(View v) {
            Timber.d("TODO Do something when an item is clicked.");
        }

        private void handleReplyClick(View view) {
            if (item != null && item.getStatus().equals(LocalMessage.Status.SUCCESS)) {
                Timber.d("TODO Do something when the reply button of an item is clicked.");
            } else {
                Timber.e("Ignoring invalid reply request for item: %s", item);
            }
        }

        private void handleErrorClick(View view) {
            new AlertDialog.Builder(view.getContext())
                    .setTitle(R.string.message_submission_failed)
                    .setMessage(R.string.message_submission_failed_description)
                    .setNegativeButton(R.string.cancel_message_submission, (dialog, which) -> {
                        // TODO Stop lying to the user!
                        dialog.dismiss();
                    })
                    .setPositiveButton(R.string.retry_message_submission, (dialog, which) -> {
                        if (item != null) {
                            LocalMessage.Status status = item.getStatus();
                            if (LocalMessage.Status.SUCCESS.equals(status) || LocalMessage.Status.QUEUE.equals(status)) {
                                Timber.e("Ignoring invalid retry request for item: %s", item);
                            } else {
                                messageRepository.retry(item.getMessage().getUuid());
                            }
                        }
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        }
    }
}
