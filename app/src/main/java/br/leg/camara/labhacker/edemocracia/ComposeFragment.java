package br.leg.camara.labhacker.edemocracia;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;
import android.widget.TextView;
import android.widget.Toast;

import com.liferay.mobile.android.service.JSONObjectWrapper;
import com.liferay.mobile.android.v62.mbmessage.MBMessageService;

import org.json.JSONArray;
import org.json.JSONObject;

import br.leg.camara.labhacker.edemocracia.content.Message;
import br.leg.camara.labhacker.edemocracia.content.Thread;
import br.leg.camara.labhacker.edemocracia.util.EDMSession;
import br.leg.camara.labhacker.edemocracia.util.SessionWrapper;


public class ComposeFragment extends Fragment {
    private static final String ARG_THREADLIKE = "threadLike";

    private static final int RESULT_ATTACH_VIDEO = 17;

    private Thread threadLike;

    private SendMessageTask sendMessageTask;
    private TextView subjectView;
    private TextView messageView;
    private VideoView videoView;

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
        videoView = (VideoView) view.findViewById(R.id.videoView);

        // Hide the video attachment view
        videoView.setVisibility(View.GONE);

        return view;
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
                        videoView.setVideoURI(videoUri);
                        videoView.setVisibility(View.VISIBLE);
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
        if (sendMessageTask != null) {
            return false;
        }

        // Reset errors.
        subjectView.setError(null);
        messageView.setError(null);

        // Store values at the time of the login attempt.
        String subject = subjectView.getText().toString();
        String message = messageView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (cancel) {
            focusView.requestFocus();
        } else {
            sendMessageTask = new SendMessageTask(subject, message);
            sendMessageTask.execute((Void) null);
        }

        return !cancel;
    }

    protected void onMessageSubmitted() {
        getActivity().getFragmentManager().popBackStack();
    }

    private class SendMessageTask extends AsyncTask<Void, Void, Boolean> {
        private final String subject;
        private final String message;

        public SendMessageTask(String subject, String message) {
            this.subject = subject;
            this.message = message;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            final ProgressDialog[] dialog = new ProgressDialog[1];
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog[0] = new ProgressDialog(getActivity());

                    dialog[0].setIndeterminate(true);
                    dialog[0].setTitle("Sending message...");

                    dialog[0].show();
                }
            });

            Message message = null;

            try {
                message = Message.JSON_READER.fromJSON(doPostIt());
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), e.toString());
            } finally {
                dialog[0].dismiss();
            }

            return message != null;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            String message;

            if (success) {
                message = "Message submitted";
            } else {
                message = "Failed to submit messsage";
            }

            Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
            toast.show();

            if (success) {
                onMessageSubmitted();
            }

            sendMessageTask = null;
        }

        private JSONObject doPostIt() throws Exception {
            final EDMSession session = EDMSession.get(getActivity().getApplicationContext());

            assert session != null;

            JSONObject serviceContextJson = new JSONObject();

            serviceContextJson.put("addGuestPermissions", "true");

            // XXX We gotta use this wrapped session because the original webservice is bugged
            JSONObjectWrapper serviceContext = new JSONObjectWrapper(
                    "com.liferay.portal.service.ServiceContext", serviceContextJson);

            return new MBMessageService(new SessionWrapper(session)).addMessage(
                    threadLike.getGroupId(), threadLike.getCategoryId(),
                    threadLike.getThreadId(), threadLike.getRootMessageId(),
                    subject, message, "bbcode", new JSONArray(), false, 0,
                    true, serviceContext);
        }
    }
}
