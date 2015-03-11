package net.labhackercd.edemocracia.ui.message;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.ocpsoft.pretty.time.PrettyTime;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.data.LocalMessageRepository;
import net.labhackercd.edemocracia.data.api.model.Message;
import net.labhackercd.edemocracia.data.api.model.User;
import net.labhackercd.edemocracia.data.db.LocalMessage;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    private final User user;
    private final LocalMessageRepository messageRepository;
    private List<Message> messages = Collections.emptyList();
    private List<LocalMessage> localMessages = Collections.emptyList();

    public MessageListAdapter(LocalMessageRepository messageRepository, User user) {
        this.user = user;
        this.messageRepository = messageRepository;
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.message_list_item, viewGroup, false);
        return new ViewHolder(view, user, messageRepository);
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

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.date) TextView dateView;
        @InjectView(R.id.body) LinearLayout bodyView;
        @InjectView(R.id.portrait) ImageView portraitView;
        @InjectView(android.R.id.text1) TextView userView;
        @InjectView(android.R.id.text2) TextView subjectView;

        private Message message;
        private LocalMessage localMessage;
        private final User user;
        private final LocalMessageRepository messageRepository;
        private final Transformation videoThumbnailTransformation;
        private Subscription subscription;

        public ViewHolder(View view, User user, LocalMessageRepository messageRepository) {
            super(view);
            ButterKnife.inject(this, view);
            view.setOnClickListener(this::handleClick);
            this.user = user;
            this.messageRepository = messageRepository;
            // TODO Make less instances of this transformation.
            // I guess we could instantiate it only once at the Adapter.
            this.videoThumbnailTransformation = new VideoPlayButtonTransformation(view.getContext());
        }

        public void bindMessage(Message message) {
            this.message = message;
            this.localMessage = null;

            setAuthor(message.getUserName());

            setSubject(message.getSubject());

            setBody(message.getBody());

            setStatus(message.getCreateDate());

            // TODO Display author's portrait.
            setPortrait(message.getUserName(), null);
        }

        public void bindLocalMessage(LocalMessage message) {
            this.message = null;
            this.localMessage = message;
            subscription = messageRepository.getMessage(localMessage.id())
                    .subscribeOn(Schedulers.io())
                    .startWith(Observable.just(message))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::setLocalMessage);
        }

        private void setLocalMessage(LocalMessage message) {
            Timber.d("Updating local message.");

            setAuthor(user.getScreenName());

            setSubject(message.subject());

            setBody(message.body());

            setLocalMessageStatus(message.status());

            // TODO Display author's portrait.
            setPortrait(user.getScreenName(), null);
        }

        public void handleClick(View v) {
            // TODO Do something when a item is clicked
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
            dateView.setText(resId);
        }

        private void setLocalMessageStatus(LocalMessage.Status status) {
            int statusId;
            if (!status.equals(LocalMessage.Status.CANCEL))
                statusId = R.string.sending_message;
            else
                statusId = R.string.message_submission_failed;
            setStatus(statusId);
        }

        private void setPortrait(String author, Uri portrait) {
            String letter = author.trim().substring(0, 1).toUpperCase();
            TextDrawable textDrawable = TextDrawable.builder().buildRect(letter, Color.LTGRAY);

            if (portrait == null) {
                portraitView.setImageDrawable(textDrawable);
            } else {
                Picasso.with(portraitView.getContext().getApplicationContext())
                        .load(portrait)
                        .placeholder(textDrawable)
                        .resize(100, 100)
                        .centerCrop()
                        .into(portraitView);
            }
        }

        private void setBody(String body) {
            // Remove all views in *body layout*, in case there are any view remaining
            // from previously bound messages.
            bodyView.removeAllViewsInLayout();

            // TODO Parse, support or ignore other bbcode tags.
            if (body != null) {
                // Find all youtube tags.
                Pattern p = Pattern.compile("\\[youtube\\](.*?)\\[\\/youtube\\]");
                Matcher m = p.matcher(body);

                // Do this for all video thumbnails in the body.
                while (m.find()) {
                    final String videoId = m.group(1);
                    String prevText = body.substring(0, m.start());

                    body = body.substring(m.end());

                    // Create a text view with any text preceding the video and show that.
                    TextView textView = new TextView(bodyView.getContext(), null, R.style.Widget_BodyTextChunk);
                    textView.setText(prevText);

                    bodyView.addView(textView);

                    // Add the video thumbnail
                    ImageView videoThumbView = new ImageView(bodyView.getContext());
                    videoThumbView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://www.youtube.com/watch?v=" + videoId));
                            v.getContext().startActivity(intent);
                        }
                    });

                    // TODO Ensure all the thumbnails are of the same size (if needed)
                    Picasso.with(videoThumbView.getContext())
                            .load(Uri.parse("http://img.youtube.com/vi/" + videoId + "/hqdefault.jpg"))
                            .transform(videoThumbnailTransformation)
                            .into(videoThumbView);

                    bodyView.addView(videoThumbView);
                }

                // If there is any text *after* the video, add that too.
                if (body.length() > 0) {
                    TextView textView = new TextView(bodyView.getContext(), null, R.style.Widget_BodyTextChunk);
                    textView.setText(body);
                    bodyView.addView(textView);
                }
            }
        }
    }
}
