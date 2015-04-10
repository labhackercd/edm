package net.labhackercd.edemocracia.ui.message;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.data.LocalMessageStore;
import net.labhackercd.edemocracia.data.model.Message;
import net.labhackercd.edemocracia.ui.BaseActivity;
import net.labhackercd.edemocracia.ui.preference.PreferenceActivity;
import net.labhackercd.edemocracia.ui.preference.PreferenceFragment;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ComposeActivity extends BaseActivity {

    /** The parent message. */
    public static final String PARAM_PARENT_MESSAGE = "parentMessage";

    /** The inserted message. Used in results. */
    public static final String PARAM_INSERTED_MESSAGE = "insertedMessage";

    private static final int REQUEST_ATTACH_VIDEO = 18;

    private Uri attachedVideoUri;
    private Message parentMessage;

    @Inject LocalMessageStore messageRepository;

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
                attachVideo();
                return true;
            case R.id.send:
                return sendMessage();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ATTACH_VIDEO:
                if (resultCode == Activity.RESULT_OK) {
                    Uri videoUri = data.getData();
                    if (videoUri != null) {
                        setAttachedVideoUri(videoUri);
                        setVideoFrameShown(true);
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
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

    private void attachVideo() {
        if (selectedYouTubeAccount() == null) {
            setupYoutubeAccount();
            return;
        }

        List<Pair<String, Integer>> items = Lists.newArrayList(
                new Pair<>(getResources().getString(R.string.pick_video), R.drawable.ic_video_collection_black_36dp),
                new Pair<>(getResources().getString(R.string.record_video), R.drawable.ic_videocam_black_36dp)
        );

        ListAdapter adapter = new ArrayAdapter<Pair<String, Integer>>(
                this,
                R.layout.dialog_item_with_icon,
                android.R.id.text1,
                items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                Pair<String, Integer> item = getItem(position);

                ((TextView) view.findViewById(android.R.id.text1))
                        .setText(item.first);

                ((ImageView) view.findViewById(android.R.id.icon))
                        .setImageResource(item.second);

                return view;
            }
        };

        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setOnCancelListener(DialogInterface::dismiss)
                .setAdapter(adapter, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            // Pick from gallery
                            pickFile();
                            break;
                        case 1:
                            recordVideo();
                            break;
                    }
                })
                .create()
                .show();
    }

    private String selectedYouTubeAccount() {
        return PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext())
                .getString(PreferenceFragment.PREF_YOUTUBE_ACCOUNT, null);
    }

    private void setupYoutubeAccount() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.missing_youtube_account_dialog_title)
                .setMessage(R.string.missing_youtube_account_dialog_message)
                .setCancelable(true)
                .setOnCancelListener(DialogInterface::dismiss)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel())
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    Intent intent = new Intent(ComposeActivity.this, PreferenceActivity.class);
                    intent.setAction(PreferenceActivity.ACTION_SET_YOUTUBE_ACCOUNT);
                    // XXX We use startActivityForResult here so that Android sends the user
                    // back to this same activity when he presses back from that activity.
                    // TODO check result and keep the flow if its positive.
                    startActivityForResult(intent, 0);
                })
                .create()
                .show();
    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, REQUEST_ATTACH_VIDEO);
    }

    private void recordVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        // Workaround for Nexus 7 Android 4.3 Intent Returning Null problem
        // create a file to save the video in specific folder (this works for
        // video only)
        // mFileURI = getOutputMediaFile(MEDIA_TYPE_VIDEO);
        // intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileURI);

        // set the video image quality to high
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        // start the Video Capture Intent
        startActivityForResult(intent, REQUEST_ATTACH_VIDEO);
    }

    protected boolean sendMessage() {
        // Reset errors.
        bodyView.setError(null);
        subjectView.setError(null);

        // Store values at the time of the login attempt.
        String body = bodyView.getText().toString();
        String subject = subjectView.getText().toString();

        Pair<Long, UUID> inserted = messageRepository.insert(parentMessage, subject, body, attachedVideoUri);

        Toast.makeText(this, R.string.sending_message, Toast.LENGTH_SHORT).show();

        Intent result = new Intent();
        result.putExtra(PARAM_INSERTED_MESSAGE, inserted.second);
        setResult(RESULT_OK, result);
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