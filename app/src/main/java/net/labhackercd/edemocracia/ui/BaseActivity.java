package net.labhackercd.edemocracia.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;

import net.labhackercd.edemocracia.EDMApplication;
import net.labhackercd.edemocracia.data.api.model.User;

import javax.inject.Inject;

import dagger.ObjectGraph;

public class BaseActivity extends ActionBarActivity {
    private static final String PARAM_CURRENT_USER = "BaseActivity.user";

    @Inject User user;

    private ObjectGraph objectGraph;

    public static Intent createIntent(Context context,
                                      Class<? extends BaseActivity> activityClass, User user) {
        Intent intent = new Intent(context, activityClass);

        intent.putExtra(PARAM_CURRENT_USER, user);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        user = (User) intent.getSerializableExtra(PARAM_CURRENT_USER);

        if (user == null)
            throw new IllegalArgumentException(PARAM_CURRENT_USER + " not present.");

        ObjectGraph og = EDMApplication.get(this).getObjectGraph();
        objectGraph = og.plus(new UiModule(user));

        objectGraph.inject(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(PARAM_CURRENT_USER, user);
        super.onSaveInstanceState(outState);
    }

    public static BaseActivity get2(FragmentActivity activity) {
        return (BaseActivity) activity;
    }

    public ObjectGraph getObjectGraph() {
        return objectGraph;
    }
}
