package net.labhackercd.edemocracia.ui.message;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.util.Pair;
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
import com.squareup.picasso.RequestCreator;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.data.ImageLoader;
import net.labhackercd.edemocracia.data.LocalMessageStore;
import net.labhackercd.edemocracia.data.api.model.Message;
import net.labhackercd.edemocracia.data.api.model.User;
import net.labhackercd.edemocracia.data.db.LocalMessage;

import org.kefirsf.bb.TextProcessor;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    private final User user;
    private final ImageLoader imageLoader;
    private final TextProcessor textProcessor;
    private final LocalMessageStore messageRepository;
    private List<Message> messages = Collections.emptyList();
    private List<LocalMessage> localMessages = Collections.emptyList();

    public MessageListAdapter(LocalMessageStore messageRepository, User user, TextProcessor textProcessor, ImageLoader imageLoader) {
        this.user = user;
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
        // TODO Place "not yet submitted messages" in their right place on the list
        // (I mean, like under the message they are replying to.)
        int remoteCount = messages.size();
        if (i < remoteCount)
            holder.bindMessage(messages.get(i));
        else
            holder.bindLocalMessage(localMessages.get(i - remoteCount));
    }

    @Override
    public int getItemCount() {
        return messages.size() + localMessages.size();
    }

    public int getItemPosition(LocalMessage item) {
        return messages.size() + localMessages.indexOf(item);
    }

    public int getLocalMessagePositionById(long id) {
        int position = 0;
        for (LocalMessage item : localMessages) {
            if (item.id().equals(id))
                return messages.size() + position;
        }
        return -1;
    }

    public MessageListAdapter replaceWith(Pair<List<Message>, List<LocalMessage>> lists) {
        return replaceWith(lists.first, lists.second);
    }

    public MessageListAdapter replaceWith(List<Message> messages, List<LocalMessage> localMessages) {
        this.messages = messages;
        this.localMessages = localMessages;
        notifyDataSetChanged();
        return this;
    }

    public Message getRootMessage() {
        return messages.isEmpty() ? null : messages.get(0);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.date) StatusView dateView;
        @InjectView(R.id.body) MessageView bodyView;
        @InjectView(R.id.portrait) ImageView portraitView;
        @InjectView(android.R.id.text1) TextView userView;
        @InjectView(android.R.id.text2) TextView subjectView;
        @InjectView(R.id.error_button) ImageView errorIcon;

        private Message message;
        private LocalMessage localMessage;
        private Subscription subscription;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
            view.setOnClickListener(this::handleClick);
            errorIcon.setOnClickListener(this::handleErrorClick);
        }

        public void bindMessage(Message message) {
            beforeBind(message, null);

            setAuthor(message.getUserName());

            setSubject(message.getSubject());

            setBody(message.getBody());

            setStatus(message.getCreateDate());

            // TODO Display author's portrait.
            setPortrait2(message.getUserName(), message.getUserId());
        }

        public void bindLocalMessage(LocalMessage localMessage) {
            beforeBind(null, localMessage);

            // Note: we startWith the given localMessage, but subscribe for when
            // it's updated somewhere else
            subscription = messageRepository.getMessage(localMessage.id())
                    .subscribeOn(Schedulers.io())
                    .startWith(Observable.just(localMessage))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((LocalMessage message) -> {
                        setAuthor(user.getScreenName());

                        setSubject(message.subject());

                        setBody(message.body());

                        setLocalMessageStatus(message.status(), message.insertionDate());

                        setPortrait(user.getScreenName(), user.getPortraitId());
                    });
        }

        private void beforeBind(Message message, LocalMessage localMessage) {
            this.message = message;
            this.localMessage = localMessage;

            if (subscription != null) {
                if (subscription.isUnsubscribed())
                    subscription.unsubscribe();
                subscription = null;
            }
        }

        public void handleClick(View v) {
            // TODO Do something when a item is clicked
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
                        if (localMessage != null)
                            messageRepository.retry(localMessage);
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        }

        @OnClick(R.id.reply)
        @SuppressWarnings("UnusedDeclaration")
        public void onReplyClick(View v) {
            if (localMessage != null && localMessage.status() == LocalMessage.Status.CANCEL) {
                // TODO Retry message submission
            }
        }

        private void setAuthor(String author) {
            userView.setText(author);
        }

        private void setSubject(String subject) {
            subjectView.setText(subject);
        }

        private void setStatus(Date date) {
            String text;
            if (date != null) {
                Locale locale = dateView.getResources().getConfiguration().locale;
                text = new PrettyTime(locale).format(date);
            } else {
                text = null;
            }
            setStatus(text);
        }

        private void setStatus(String text) {
            dateView.setText(text);
        }

        private void setStatus(int resId) {
            setStatus(resId, false);
        }

        private void setStatus(int resId, boolean error) {
            dateView.setText(resId);
            dateView.setMessageSubmissionError(error);

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

            setErrorIconVisible(error);
        }

        private void setErrorIconVisible(boolean show) {
            errorIcon.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        private void setLocalMessageStatus(LocalMessage.Status status, Date insertionDate) {
            if (status.equals(LocalMessage.Status.SUCCESS))
                setStatus(insertionDate);
            else if (status.equals(LocalMessage.Status.QUEUE))
                setStatus(R.string.sending_message);
            else
                setStatus(R.string.message_submission_failed, true);
        }

        private void setPortrait(String userName, long portraitId) {
            Drawable placeholder = getPortraitPlaceholder(userName);
            if (portraitId <= 0)
                setPortrait(placeholder);
            else
                setPortrait(imageLoader.userPortrait(portraitId).placeholder(placeholder));
        }

        private void setPortrait2(String userName, long userId) {
            Drawable placeholder = getPortraitPlaceholder(userName);
            portraitView.setImageDrawable(placeholder);
            imageLoader.userPortrait2(userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(request -> {
                        setPortrait(request.placeholder(placeholder));
                    });
        }

        private void setPortrait(RequestCreator requestCreator) {
            requestCreator.fit().centerCrop().into(portraitView);
        }

        private void setPortrait(Drawable drawable) {
            portraitView.setImageDrawable(drawable);
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
    }
}
