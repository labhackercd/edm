package net.labhackercd.edemocracia.ui.message;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import net.labhackercd.edemocracia.data.api.model.Message;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    private List<Message> messages = Collections.emptyList();

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.message_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.bindMessage(messages.get(i));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public MessageListAdapter replaceWith(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
        return this;
    }

    public Message getRootMessage() {
        return messages.isEmpty() ? null : messages.get(0);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.body) LinearLayout bodyView;
        @InjectView(R.id.date) TextView dateView;
        @InjectView(R.id.portrait) ImageView portraitView;
        @InjectView(android.R.id.text1) TextView userView;
        @InjectView(android.R.id.text2) TextView subjectView;

        private Message message;
        private final Transformation videoThumbnailTransformation;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
            view.setOnClickListener(this::handleClick);
            this.videoThumbnailTransformation = new VideoPlayButtonTransformation(view.getContext());
        }

        public void bindMessage(Message message) {
            this.message = message;

            // Get the body text
            String body = message.getBody();

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

                    // TODO Make less instances of this transformation.
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

            userView.setText(message.getUserName());
            subjectView.setText(message.getSubject());

            // Set the date
            Date date = message.getCreateDate();

            if (date != null) {
                PrettyTime formatter = new PrettyTime(dateView.getResources().getConfiguration().locale);
                dateView.setText(formatter.format(date));
            }

            // Fill the user portrait
            String letter = message.getUserName().trim().substring(0, 1).toUpperCase();
            TextDrawable textDrawable = TextDrawable.builder().buildRect(letter, Color.LTGRAY);

            portraitView.setImageDrawable(textDrawable);

            /*
            TODO Display user portraits

            Uri portrait = message.getUserPortrait();
            if (portrait == null) {
                portraitView.setImageDrawable(textDrawable);
            } else {
                Picasso.with(context)
                        .load(portrait)
                        .placeholder(textDrawable)
                        .resize(100, 100)
                        .centerCrop()
                        .into(portraitView);
            }
             */
        }

        public void handleClick(View v) {
            // TODO Do something when a item is clicked
        }

        @OnClick(R.id.reply)
        @SuppressWarnings("UnusedDeclaration")
        public void onReplyClick(View v) {
            /*
            TODO
            if (message != null) {
                Context context = v.getContext();
                Intent intent = new Intent(context, ComposeActivity.class);
                intent.setAction(Intent.ACTION_INSERT);
                intent.putExtra(ComposeActivity.PARENT_EXTRA, (Parcelable) message);
                context.startActivity(intent);
            }
             */
        }
    }
}
