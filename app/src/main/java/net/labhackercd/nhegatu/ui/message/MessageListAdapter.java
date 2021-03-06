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

package net.labhackercd.nhegatu.ui.message;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.ocpsoft.pretty.time.PrettyTime;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import net.labhackercd.nhegatu.R;
import net.labhackercd.nhegatu.data.ImageLoader;
import net.labhackercd.nhegatu.data.LocalMessageStore;
import net.labhackercd.nhegatu.data.db.LocalMessage;

import net.labhackercd.nhegatu.data.model.Message;
import net.labhackercd.nhegatu.service.VideoAttachmentUploader;
import net.labhackercd.nhegatu.ui.preference.PreferenceFragment;
import net.labhackercd.nhegatu.upload.YouTubeUploader;
import org.kefirsf.bb.TextProcessor;

import java.io.IOException;
import java.util.*;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.*;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    private final Picasso picasso;
    private final ImageLoader imageLoader;
    private final TextProcessor textProcessor;
    private final LocalMessageStore messageRepository;
    private final Transformation videoAttachmentTransformation;
    private final VideoAttachmentUploader uploader;

    private List<? extends Item> items = Collections.emptyList();

    public MessageListAdapter(Context context, LocalMessageStore messageRepository, TextProcessor textProcessor, ImageLoader imageLoader, Picasso picasso, VideoAttachmentUploader uploader) {
        this.picasso = picasso;
        this.imageLoader = imageLoader;
        this.textProcessor = textProcessor;
        this.messageRepository = messageRepository;
        this.videoAttachmentTransformation = new VideoPlayButtonTransformation(context);
        this.uploader = uploader;
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
        Uri getVideoAttachment();
        long getUploadId();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.date) StatusView dateView;
        @InjectView(R.id.body) MessageView bodyView;
        @InjectView(R.id.portrait) ImageView portraitView;
        @InjectView(android.R.id.text1) TextView userView;
        @InjectView(R.id.error_button) ImageView errorIcon;
        @InjectView(android.R.id.text2) TextView subjectView;
        @InjectView(R.id.progress_bar) ProgressBar progressBar;
        @InjectView(R.id.video_thumbnail) ImageView videoThumbnail;
        @InjectView(R.id.video_thumbnail_frame) FrameLayout videoThumbnailFrame;

        private Item item;
        private Subscription portraitSubscription;
        private Subscription uploadProgressSubscription;

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
            if (portraitSubscription != null) {
                if (!portraitSubscription.isUnsubscribed())
                    portraitSubscription.unsubscribe();
                portraitSubscription = null;
            }

            if (uploadProgressSubscription != null) {
                if (!uploadProgressSubscription.isUnsubscribed())
                    uploadProgressSubscription.unsubscribe();
                uploadProgressSubscription = null;
            }

            this.item = item;
            Message message = item.getMessage();

            /** Finally, bind the item. */

            setUser(message.getUserId(), item.getUserName());

            setSubject(message.getSubject());

            setBody(message.getBody(), item.getVideoAttachment());

            setStatus(item.getStatus(), message.getCreateDate(), item.getUploadId());
        }

        private void setUploadProgress(long uploadId) {
            progressBar.setVisibility(View.GONE);
            if (uploadId > 0) {
                String youtubeAccount = PreferenceManager
                        .getDefaultSharedPreferences(itemView.getContext().getApplicationContext())
                        .getString(PreferenceFragment.PREF_YOUTUBE_ACCOUNT, null);
                if (youtubeAccount != null) {
                    uploadProgressSubscription = uploader.getUploadProgressStream(uploadId, youtubeAccount)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::handleUploadProgress);
                }
            }
        }

        private void handleUploadProgress(YouTubeUploader.UploadProgress progress) {
            // TODO Animate the progress bar in and out?
            if (progress.getInsertedVideo() != null) {
                progressBar.setVisibility(View.GONE);
            } else {
                double uploadProgress = 0;
                boolean indeterminate = true;

                MediaHttpUploader uploader = progress.getMediaHttpUploader();
                if (uploader != null && (
                        uploader.getUploadState() == MediaHttpUploader.UploadState.MEDIA_IN_PROGRESS
                        || uploader.getUploadState() == MediaHttpUploader.UploadState.MEDIA_COMPLETE)) {
                    try {
                        uploadProgress = uploader.getProgress();
                        indeterminate = false;
                    } catch (IOException e) {
                        // Do nothing.
                    }
                }

                // XXX 1000 to make it smoother than 100.
                final int max = 1000;
                progressBar.setMax(max);
                progressBar.setProgress((int) (uploadProgress * max));
                progressBar.setIndeterminate(indeterminate);
                progressBar.setVisibility(View.VISIBLE);
            }
        }

        private void setUser(long userId, String userName) {
            userView.setText(userName);

            final Drawable placeholder = userName == null ? null : getPortraitPlaceholder(userName);

            portraitView.setImageDrawable(placeholder);

            if (userId != 0) {
                portraitSubscription = imageLoader.userPortrait2(userId)
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

        private void setStatus(LocalMessage.Status status, Date createDate, long uploadId) {
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
                setUploadProgress(0);
            } else if (status.equals(LocalMessage.Status.QUEUE)) {
                setStatus(R.string.sending_message);
                setHasError(false);
                setUploadProgress(uploadId);
            } else {
                setStatus(R.string.message_submission_failed);
                setHasError(true);
                setUploadProgress(0);
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

        private void setBody(String body, Uri videoAttachment) {
            if (!TextUtils.isEmpty(body)) {
                bodyView.setHTMLText(textProcessor.process(body));
                bodyView.setLinksClickable(true);
                bodyView.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                bodyView.setText(null);
            }

            if (videoAttachment == null) {
                videoThumbnailFrame.setVisibility(View.GONE);
            } else {
                videoThumbnail.setImageBitmap(null);
                videoThumbnail.setOnClickListener(view -> playVideo(videoAttachment));
                picasso.load(videoAttachment)
                        .centerCrop().fit()
                        .transform(videoAttachmentTransformation)
                        .into(videoThumbnail);
                videoThumbnailFrame.setVisibility(View.VISIBLE);
            }
        }

        private void playVideo(Uri uri) {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            videoThumbnail.getContext().startActivity(intent);
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
