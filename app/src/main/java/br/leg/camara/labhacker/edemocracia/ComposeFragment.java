package br.leg.camara.labhacker.edemocracia;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;

import java.util.Arrays;
import java.util.List;

import br.leg.camara.labhacker.edemocracia.content.Thread;


public class ComposeFragment extends Fragment {
    private static final String ARG_THREADLIKE = "threadLike";

    private Thread threadLike;

    private static final int RESULT_PICK_IMAGE_CROP = 4;
    private static final int RESULT_VIDEO_CAP = 5;

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_VIDEO_CAP:
                if (resultCode == Activity.RESULT_OK) {
                    Uri fileUri = data.getData();
                    if (fileUri != null) {
                        /*
                        TODO Upload the video
                        Intent uploadIntent = new Intent(this, UploadService.class);
                        uploadIntent.setData(fileUri);
                        uploadIntent.putExtra(MainActivity.ACCOUNT_KEY, mChosenAccountName);
                        startService(uploadIntent);
                        */
                    }
                }
                break;
        }
    }

    public boolean attachVideo() {
        // TODO FIXME ALL THIS CODE SUCKS. PLEASE FIX.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        List<String> items = Arrays.asList(getResources().getStringArray(R.array.attachmentTypeItems));
        final int[] icons = getResources().getIntArray(R.array.attachmentTypeIcons);

        ListAdapter adapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.simple_list_item_with_icon,
                android.R.id.text1,
                items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                ImageView imageView = (ImageView) view.findViewById(android.R.id.icon);

                // FIXME Doesn't work. icons is full o zeroes.
                imageView.setImageResource(icons[position]);

                return view;
            }
        };

        builder.setTitle(R.string.attach_video)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // FIXME How to identify which *which* is which?
                        switch (which) {
                            case 0:
                                // First is "pick video"
                                pickFile();
                                break;
                            case 1:
                                // Second is "record video"
                                recordVideo();
                                break;
                        }
                    }
                });

        builder.create().show();

        return true;
    }

    public void pickFile() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, RESULT_PICK_IMAGE_CROP);
    }

    public void recordVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        // Workaround for Nexus 7 Android 4.3 Intent Returning Null problem
        // create a file to save the video in specific folder (this works for
        // video only)
        // mFileURI = getOutputMediaFile(MEDIA_TYPE_VIDEO);
        // intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileURI);

        // set the video image quality to high
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        // start the Video Capture Intent
        startActivityForResult(intent, RESULT_VIDEO_CAP);
    }

    public void sendMessage() {
        // TODO Implement actual logic to submit the message
    }
}
