package net.labhackercd.edemocracia.ui.message;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Splitter;
import com.path.android.jobqueue.JobManager;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.data.api.model.Message;
import net.labhackercd.edemocracia.job.AddMessageJob;
import net.labhackercd.edemocracia.job.VideoUploadJob;
import net.labhackercd.edemocracia.ui.BaseActivity;
import net.labhackercd.edemocracia.ui.VideoPickerActivity;

import java.io.FileNotFoundException;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ComposeActivity extends BaseActivity {
    public static final String PARAM_PARENT_MESSAGE = "parentMessage";

    private static final int RESULT_ATTACH_VIDEO = 17;

    private Uri attachedVideoUri;
    private String videoAccount;
    private Message parentMessage;

    @Inject JobManager jobManager;

    @InjectView(R.id.message) TextView bodyView;
    @InjectView(R.id.subject) TextView subjectView;
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

        parentMessage = (Message) intent.getSerializableExtra(PARAM_PARENT_MESSAGE);
        if (parentMessage == null)
            throw new IllegalArgumentException("No parent message given.");

        String subject = parentMessage.getSubject();
        if (subject != null)
            subject = getReplySubject(subject);
        if (subject != null) {
            bodyView.requestFocus();
            subjectView.setText(subject);
        } else {
            subjectView.requestFocus();
        }

        setVideoFrameShown(false);
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
        bodyView.setError(null);

        // Store values at the time of the login attempt.
        String body = bodyView.getText().toString();
        String subject = subjectView.getText().toString();

        Message message = Message.create(parentMessage, subject, body);

        if (attachedVideoUri != null) {
            jobManager.addJob(new VideoUploadJob(attachedVideoUri, videoAccount, message));
        } else {
            jobManager.addJob(new AddMessageJob(message));
        }

        // FIXME We should probably trigger this from AddMessageJob.onJobAdded
        Toast.makeText(this, R.string.sending_message, Toast.LENGTH_SHORT).show();

        finish();

        return true;
    }

    private static String getReplySubject(String subject) {
        String prefix = "RE:";
        if (!subject.startsWith(prefix)) {
            subject = prefix + " " + subject;
        }
        return subject;
    }
}