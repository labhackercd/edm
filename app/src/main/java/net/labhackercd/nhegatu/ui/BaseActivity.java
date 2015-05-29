package net.labhackercd.nhegatu.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import net.labhackercd.nhegatu.EDMApplication;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EDMApplication.get(this).getObjectGraph().inject(this);
    }
}