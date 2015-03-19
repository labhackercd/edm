package net.labhackercd.edemocracia.ui.message;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class MessageView extends TextView {
    private final List<Target> targets = new ArrayList<>();
    private final VideoPlayButtonTransformation transforms;

    public MessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.transforms = new VideoPlayButtonTransformation(context);
    }

    public void setHTMLText(String html) {
        Spanned spanned = Html.fromHtml(html);
        SpannableStringBuilder spannable = new SpannableStringBuilder(spanned);

        // Make youtube thumbnails
        spannable = makeYoutubeThumbnails(spannable);

        setText(spannable, TextView.BufferType.SPANNABLE);
    }

    // TODO Pull this into it's own component for improved awesomeness.
    private SpannableStringBuilder makeYoutubeThumbnails(SpannableStringBuilder spannable) {
        Context context = getContext();
        URLSpan[] urls = spannable.getSpans(0, spannable.length(), URLSpan.class);
        for (URLSpan span : urls) {
            String videoId = getVideoId(span.getURL());

            if (TextUtils.isEmpty(videoId))
                continue;

            int end = spannable.getSpanEnd(span);
            int start = spannable.getSpanStart(span);
            //int flags = spannable.getSpanFlags(span);

            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    ImageSpan newSpan = new ImageSpan(context, bitmap);
                    spannable.setSpan(newSpan, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    setText(spannable, TextView.BufferType.SPANNABLE);

                    // Remove the target from the list in order for it to be GCed.
                    targets.remove(this);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    Timber.d("Failed to load image.");
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    // Nothing to do here.
                }
            };

            // Add targets to a list so they aren't GCed.
            targets.add(target);

            Picasso.with(context)
                    .load(Uri.parse("http://img.youtube.com/vi/" + videoId + "/hqdefault.jpg"))
                    .transform(transforms)
                    .into(target);
        }

        return spannable;
    }

    private String getVideoId(String url) {
        if (TextUtils.isEmpty(url))
            return null;
        Matcher matcher = YOUTUBE_VIDEO_URL_PATTERN.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    // (?:youtube(?:-nocookie)?\.com\/(?:[^\/\n\s]+\/\S+\/|(?:v|e(?:mbed)?)\/|\S*?[?&]v=)|youtu\.be\/)([a-zA-Z0-9_-]{11})
    final static Pattern YOUTUBE_VIDEO_URL_PATTERN = Pattern.compile(
            "(?:youtube(?:-nocookie)?\\.com\\/(?:[^\\/\\n\\s]+\\/\\S+\\/|(?:v|e(?:mbed)?)\\/|\\S*?[?&]v=)|youtu\\.be\\/)([a-zA-Z0-9_-]{11})",
            Pattern.CASE_INSENSITIVE
    );
}
