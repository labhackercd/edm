package net.labhackercd.edemocracia.activity;

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
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.util.Auth;


public class VideoPickerActivity extends Activity {
    private static final String ARG_ACCOUNT_NAME = "accountName";
    private String chosenAccountName;
    private GoogleAccountCredential credential;

    public static final int RESULT_PICK = 4;

    private static final int REQUEST_DIRECT_TAG = 12;
    public static final int REQUEST_AUTHORIZATION = 13;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 14;
    private static final int RESULT_PICK_IMAGE_CROP = 15;
    private static final int RESULT_VIDEO_CAP = 16;
    private static final int REQUEST_ACCOUNT_PICKER = 17;
    private static final int REQUEST_ACCOUNT_PICKER_AND_THEN_ATTACH = 18;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFinishOnTouchOutside(true);

        chosenAccountName = loadChosenAccount();

        credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(Auth.SCOPES));
        credential.setBackOff(new ExponentialBackOff());
        credential.setSelectedAccountName(chosenAccountName);

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

        // XXX Finish activity if anything is cancelled through the flow.
        // Be wary that if you need to deal with any RESULT_CANCELED below you
        // should change this.
        if (resultCode == Activity.RESULT_CANCELED) {
            finish();
        }

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
        }
    }

    public boolean attachVideo() {
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

        new AlertDialog.Builder(
                new ContextThemeWrapper(this, android.R.style.Theme_Material_Light_Dialog))
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        VideoPickerActivity.this.finish();
                    }
                })
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                // Pick from gallery
                                pickFile();
                                break;
                            case 1:
                                recordVideo();
                                break;
                        }
                    }
                })
                .show();

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
