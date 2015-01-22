package br.leg.camara.labhacker.edemocracia;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import br.leg.camara.labhacker.edemocracia.content.Thread;


public class ComposeFragment extends Fragment {
    private static final String ARG_THREADLIKE = "threadLike";

    private static final int RESULT_ATTACH_VIDEO = 17;

    private Thread threadLike;
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

        // Hide the video attachment view
        videoView = (VideoView) view.findViewById(R.id.videoView);

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

    public void sendMessage() {
        // TODO Implement actual logic to submit the message
    }
}
