package net.labhackercd.edemocracia.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import net.labhackercd.edemocracia.application.EDMApplication;

public class InjectableFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((EDMApplication) getActivity().getApplication()).inject(this);
    }
}
