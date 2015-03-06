package net.labhackercd.edemocracia.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import net.labhackercd.edemocracia.EDMApplication;

public class BaseFragment extends Fragment {
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EDMApplication.get(getActivity()).getObjectGraph().inject(this);
    }
}
