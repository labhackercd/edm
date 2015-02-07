package net.labhackercd.edemocracia.activity;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Splitter;

import java.io.FileNotFoundException;
import java.util.List;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.content.Message;
import net.labhackercd.edemocracia.content.Thread;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class ComposeActivity extends ActionBarActivity {
    public static final String THREAD_EXTRA = "thread";
    public static final String MESSAGE_EXTRA = "message";
    public static final String VIDEO_ATTACHMENT_EXTRA = "videoAttachment";
    public static final String VIDEO_ATTACHMENT_ACCOUNT_EXTRA = "videoAttachmentAccount";

    private static final int RESULT_ATTACH_VIDEO = 17;

    private Thread thread;
    private Uri attachedVideoUri;

    private String videoAccount;

    @InjectView(R.id.subject) TextView subjectView;
    @InjectView(R.id.message) TextView messageView;
    @InjectView(R.id.videoFrame) View videoFrame;
    @InjectView(R.id.videoThumb) ImageView videoThumbView;
    @InjectView(R.id.videoTitle) TextView videoTitleView;
    @InjectView(R.id.videoSize) TextView videoSizeView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_compose);

        ButterKnife.inject(this);

        Intent intent = getIntent();

        thread = intent.getParcelableExtra(THREAD_EXTRA);

        // TODO Refactor this two into something *unique* or remove account entirely
        videoAccount = intent.getStringExtra(VIDEO_ATTACHMENT_ACCOUNT_EXTRA);
        attachedVideoUri = intent.getParcelableExtra(VIDEO_ATTACHMENT_EXTRA);

        if (thread == null) {
            throw new IllegalStateException("A non-null thread must be supplied");
        }

        if (attachedVideoUri != null) {
            setAttachedVideoUri(attachedVideoUri);
        }

        setVideoFrameShown(attachedVideoUri != null);

        if (thread != null && subjectView != null) {
            subjectView.setText(getSubjectFor(thread));
        }

        if (messageView != null) {
            messageView.requestFocus();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.compose_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.attach_video:
                return attachVideo();
            case R.id.send:
                return sendMessage();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_ATTACH_VIDEO:
                if (resultCode == Activity.RESULT_OK) {
                    Uri videoUri = data.getData();
                    if (videoUri != null) {
                        setAttachedVideoUri(videoUri);
                        setVideoFrameShown(true);
                        videoAccount = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    }
                }
                break;
        }
    }

    private String getSubjectFor(Thread threadLike) {
        String subject = threadLike.getSubject();

        if (!subject.startsWith("RE:")) {
            subject = "RE: " + subject;
        }

        return subject;
    }

    protected void setAttachedVideoUri(Uri uri) {
        attachedVideoUri = uri;
        if (videoThumbView != null) {
            try {
                ContentResolver contentResolver = getApplication().getContentResolver();

                String[] projection = {MediaStore.Images.Media.DATA};
                Cursor cursor = contentResolver.query(attachedVideoUri, projection, null, null, null);
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String path = cursor.getString(columnIndex);

                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);

                videoThumbView.setImageBitmap(thumb);

                if (videoTitleView != null) {
                    List<String> parts = Splitter
                            .onPattern("/")
                            .trimResults()
                            .omitEmptyStrings()
                            .splitToList(path);
                    if (parts.size() > 0) {
                        videoTitleView.setText(parts.get(parts.size()-1));
                    }
                }

                if (videoSizeView != null) {
                    long fileSize = contentResolver.openFileDescriptor(attachedVideoUri, "r").getStatSize();
                    videoSizeView.setText(Long.toString(fileSize));
                }
            } catch (FileNotFoundException e) {
                // NOOP
            }
        }
    }

    protected void setVideoFrameShown(boolean show) {
        if (videoFrame != null) {
            videoFrame.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private boolean attachVideo() {
        Intent intent = new Intent(this, VideoPickerActivity.class);
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent, RESULT_ATTACH_VIDEO);
        return true;
    }

    protected boolean sendMessage() {
        // Reset errors.
        subjectView.setError(null);
        messageView.setError(null);

        // Store values at the time of the login attempt.
        String body = messageView.getText().toString();
        String subject = subjectView.getText().toString();

        Message message = Message.create(thread, subject, body, "bbcode", false, 0.0, true);

        Intent intent = new Intent();

        intent.putExtra(MESSAGE_EXTRA, (Parcelable) message);

        if (attachedVideoUri != null) {
            intent.putExtra(VIDEO_ATTACHMENT_EXTRA, attachedVideoUri);
            intent.putExtra(VIDEO_ATTACHMENT_ACCOUNT_EXTRA, videoAccount);
        }

        setResult(Activity.RESULT_OK, intent);

        finish();

        return true;
    }
}