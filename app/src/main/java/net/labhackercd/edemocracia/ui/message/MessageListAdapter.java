package net.labhackercd.edemocracia.ui.message;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Parcelable;
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

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.data.model.Message;
import net.labhackercd.edemocracia.ui.SimpleRecyclerViewAdapter;
import net.labhackercd.edemocracia.util.OverlayTransformation;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MessageListAdapter extends SimpleRecyclerViewAdapter<Message, MessageListAdapter.ViewHolder> {

    private final Context context;

    public MessageListAdapter(Context context, List<Message> items) {
        super(items);
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.message_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.bindMessage(getItem(i));
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        @InjectView(R.id.body) LinearLayout bodyView;
        @InjectView(R.id.date) TextView dateView;
        @InjectView(R.id.portrait) ImageView portraitView;
        @InjectView(android.R.id.text1) TextView userView;
        @InjectView(android.R.id.text2) TextView subjectView;

        private Message message;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
            view.setOnClickListener(this);
        }

        public void bindMessage(Message message) {
            this.message = message;

            String body = message.getBody();

            bodyView.removeAllViewsInLayout();

            if (body != null) {
                Pattern p = Pattern.compile("\\[youtube\\](.*?)\\[\\/youtube\\]");
                Matcher m = p.matcher(body);

                while (m.find()) {
                    final String videoId = m.group(1);
                    String prevText = body.substring(0, m.start());

                    body = body.substring(m.end());

                    TextView textView = new TextView(bodyView.getContext(), null, R.style.Widget_BodyTextChunk);
                    textView.setText(prevText);

                    bodyView.addView(textView);

                    ImageView videoThumbView = new ImageView(bodyView.getContext());
                    videoThumbView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://www.youtube.com/watch?v=" + videoId));
                            v.getContext().startActivity(intent);
                        }
                    });


                    Picasso.with(context).load(R.drawable.ic_play_circle_fill_black_36dp).
                            into(videoThumbView);


                    Picasso.with(context)
                            .load(Uri.parse("http://img.youtube.com/vi/" + videoId + "/0.jpg"))
                            .transform(new OverlayTransformation((BitmapFactory.decodeResource(context.getResources(),
                                    R.drawable.ic_play_circle_outline_white_36dp))))
                            .resize(480, 360)
                            .centerCrop()


                            .into(videoThumbView);

                    bodyView.addView(videoThumbView);
                }

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
                PrettyTime formatter = new PrettyTime(context.getResources().getConfiguration().locale);
                dateView.setText(formatter.format(date));
            }

            // Fill the user portrait
            String letter = message.getUserName().trim().substring(0, 1).toUpperCase();
            TextDrawable textDrawable = TextDrawable.builder().buildRect(letter, Color.LTGRAY);

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
        }




        @Override
        public void onClick(View v) {
            if (message != null) {
                // TODO Do something when a message is clicked
            }
        }

        @OnClick(R.id.reply)
        public void onReplyClick(View v) {
            if (message != null) {
                Intent intent = new Intent(context, ComposeActivity.class);
                intent.setAction(Intent.ACTION_INSERT);
                intent.putExtra(ComposeActivity.PARENT_EXTRA, (Parcelable) message);
                context.startActivity(intent);
            }
        }


    }


}
