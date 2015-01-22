package br.leg.camara.labhacker.edemocracia;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;

import java.util.Arrays;
import java.util.List;

import br.leg.camara.labhacker.edemocracia.ytdl.Auth;

public class VideoPickerActivity extends Activity {
    private static final String ARG_ACCOUNT_NAME = "accountName";
    private String chosenAccountName;
    private GoogleAccountCredential credential;

    public static final int RESULT_PICK = 4;

    private static final int REQUEST_DIRECT_TAG = 12;
    private static final int REQUEST_AUTHORIZATION = 13;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 14;
    private static final int RESULT_PICK_IMAGE_CROP = 15;
    private static final int RESULT_VIDEO_CAP = 16;
    private static final int REQUEST_ACCOUNT_PICKER = 17;
    private static final int REQUEST_ACCOUNT_PICKER_AND_THEN_ATTACH = 18;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFinishOnTouchOutside(true);

        credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(Auth.SCOPES));
        credential.setBackOff(new ExponentialBackOff());

        // Process intents
        Intent intent = getIntent();

        if (Intent.ACTION_PICK.equals(intent.getAction())) {
            attachVideo();
        } else {
            // FIXME What to do when no action is requested?
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == Activity.RESULT_OK) {
                    haveGooglePlayServices();
                } else {
                    checkGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != Activity.RESULT_OK) {
                    chooseGoogleAccount();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
            case REQUEST_ACCOUNT_PICKER_AND_THEN_ATTACH:
                if (resultCode == Activity.RESULT_OK
                        && data != null
                        && data.getExtras() != null) {
                    String accountName = data.getExtras().getString(
                            AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        chosenAccountName = accountName;
                        credential.setSelectedAccountName(accountName);
                        storeChosenAccount(chosenAccountName);

                        attachVideo();
                    }
                }
                break;
            case RESULT_PICK_IMAGE_CROP:
            case RESULT_VIDEO_CAP:
                if (resultCode == Activity.RESULT_OK) {
                    Uri fileUri = data.getData();
                    if (fileUri != null) {
                        Intent intent = new Intent();
                        intent.setData(fileUri);
                        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, credential.getSelectedAccountName());
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                }
                break;

            /*
            case REQUEST_DIRECT_TAG:
                if (resultCode == Activity.RESULT_OK && data != null
                        && data.getExtras() != null) {
                    String youtubeId = data.getStringExtra(YOUTUBE_ID);
                    if (youtubeId.equals(mVideoData.getYouTubeId())) {
                        directTag(mVideoData);
                    }
                }
                break;
            */
        }
    }

    public boolean attachVideo() {
        // TODO Is this the right way/place to check this?
        if (!checkGooglePlayServicesAvailable()) {
            return false;
        }

        if (chosenAccountName == null) {
            chosenAccountName = loadChosenAccount();

            if (chosenAccountName == null) {
                chooseGoogleAccountAndThenAttach();
                return false;
            }
        }

        // TODO FIXME ALL THIS CODE SUCKS. PLEASE FIX.
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(this, android.R.style.Theme_Material_Light_Dialog));

        List<String> items = Arrays.asList(getResources().getStringArray(R.array.attachmentTypeItems));
        final int[] icons = getResources().getIntArray(R.array.attachmentTypeIcons);

        ListAdapter adapter = new ArrayAdapter<String>(
                this,
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

        // TODO Finish the activity when the user clicks outside the dialog, but how do we do that?

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

    @Nullable
    private String loadChosenAccount() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getString(getClass().getSimpleName() + "." + ARG_ACCOUNT_NAME, null);
    }

    private void storeChosenAccount(String accountName) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(getClass().getSimpleName() + "." + ARG_ACCOUNT_NAME, accountName)
                .apply();
    }

    private void haveGooglePlayServices() {
        // check if there is already an account selected
        if (credential.getSelectedAccountName() == null) {
            // ask user to choose account
            chooseGoogleAccount();
        }
    }

    private void chooseGoogleAccount() {
        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    private void chooseGoogleAccountAndThenAttach() {
        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER_AND_THEN_ATTACH);
    }

    private boolean checkGooglePlayServicesAvailable() {
        final int connectionStatusCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);

        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                            connectionStatusCode, VideoPickerActivity.this,
                            REQUEST_GOOGLE_PLAY_SERVICES);
                    dialog.show();
                }
            });
            return false;
        }
        return true;
    }
}
