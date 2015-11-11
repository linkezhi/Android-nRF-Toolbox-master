package no.nordicsemi.android.nrftoolbox.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import no.nordicsemi.android.nrftoolbox.R;

/**
 * Created by hj on 2015/11/2.
 */
public class MainTabHts extends Fragment
{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return  inflater.inflate(R.layout.activity_feature_hts, container, false);

    }

}  