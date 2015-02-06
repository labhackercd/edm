package net.labhackercd.edemocracia.fragment;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Splitter;
import com.path.android.jobqueue.JobManager;

import java.io.FileNotFoundException;
import java.util.List;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.activity.VideoPickerActivity;
import net.labhackercd.edemocracia.content.Message;
import net.labhackercd.edemocracia.content.Thread;
import net.labhackercd.edemocracia.jobqueue.AddMessageJob;
import net.labhackercd.edemocracia.jobqueue.VideoUploadJob;

import javax.inject.Inject;


public class ComposeFragment extends InjectableFragment {
    private static final String ARG_THREADLIKE = "threadLike";
    private static final int RESULT_ATTACH_VIDEO = 17;

    @Inject JobManager jobManager;

    private Thread threadLike;
    private Uri attachedVideoUri;

    private String videoAccount;

    private TextView subjectView;
    private TextView messageView;
    private View videoFrame;
    private ImageView videoThumbView;
    private TextView videoTitleView;
    private TextView videoSizeView;

    public static ComposeFragment newInstance(Thread thread) {
        ComposeFragment fragment = new ComposeFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_THREADLIKE, thread);
        fragment.setArguments(args);

        return fragment;
    }

    public ComposeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        Bundle args = getArguments();
        if (args != null) {
            threadLike = args.getParcelable(ARG_THREADLIKE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_compose, container, false);

        subjectView = (TextView) view.findViewById(R.id.subject);
        messageView = (TextView) view.findViewById(R.id.message);

        videoFrame = view.findViewById(R.id.videoFrame);
        videoThumbView = (ImageView) videoFrame.findViewById(R.id.videoThumb);
        videoTitleView = (TextView) videoFrame.findViewById(R.id.videoTitle);
        videoSizeView = (TextView) videoFrame.findViewById(R.id.videoSize);

        if (attachedVideoUri != null) {
            setAttachedVideoUri(attachedVideoUri);
        }

        setVideoFrameShown(attachedVideoUri != null);

        if (threadLike != null) {
            if (subjectView != null) {
                subjectView.setText(getSubjectFor(threadLike));
            }
        }

        if (messageView != null) {
            messageView.requestFocus();
        }

        return view;
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
                ContentResolver contentResolver = getActivity().getApplication().getContentResolver();

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.compose_menu, menu);
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

    private boolean attachVideo() {
        Intent intent = new Intent(getActivity(), VideoPickerActivity.class);
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

        Message message = Message.create(threadLike, subject, body, "bbcode", false, 0.0, true);

        if (attachedVideoUri == null) {
            jobManager.addJob(new AddMessageJob(message));
        } else {
            jobManager.addJob(new VideoUploadJob(attachedVideoUri, videoAccount, message));
        }

        // FIXME We should probably trigger this from AddMessageJob.onJobAdded
        Toast.makeText(getActivity(), R.string.sending_message, Toast.LENGTH_SHORT).show();

        return true;
    }
}