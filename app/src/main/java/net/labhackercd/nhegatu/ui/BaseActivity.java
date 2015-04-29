package net.labhackercd.nhegatu.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import net.labhackercd.nhegatu.EDMApplication;

public class BaseActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EDMApplication.get(this).getObjectGraph().inject(this);
    }
}