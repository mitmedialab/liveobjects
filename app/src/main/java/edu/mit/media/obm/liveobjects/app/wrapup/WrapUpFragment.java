package edu.mit.media.obm.liveobjects.app.wrapup;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.mit.media.obm.shair.liveobjects.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WrapUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WrapUpFragment extends Fragment {


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment WrapUpFragment.
     */
    public static WrapUpFragment newInstance() {
        WrapUpFragment fragment = new WrapUpFragment();
        return fragment;
    }

    public WrapUpFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wrap_up, container, false);


        return rootView;
    }





}
