package br.leg.camara.labhacker.edemocracia;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import br.leg.camara.labhacker.edemocracia.content.Thread;


public class ComposeFragment extends Fragment {
    private static final String ARG_THREADLIKE = "threadLike";
    private Thread threadLike;

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
        return inflater.inflate(R.layout.fragment_compose, container, false);
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

    private boolean attachVideo() {
        VideoAttachmentFragment videoSelectorFragment =
                (VideoAttachmentFragment) getActivity().getFragmentManager()
                .findFragmentByTag(MainActivity.VIDEO_ATTACHMENT_FRAGMENT);

        return videoSelectorFragment.attachVideo();
    }

    public void sendMessage() {
        // TODO Implement actual logic to submit the message
    }
}
