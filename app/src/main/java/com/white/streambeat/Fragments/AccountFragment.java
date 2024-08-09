package com.white.streambeat.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.white.streambeat.Connections.ServerConnector;
import com.white.streambeat.R;

public class AccountFragment extends Fragment {

    ImageView btnBack;
    TextView txtUserIdAccount, txtEmailAccount, txtPhoneAccount;
    FirebaseUser firebaseUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        initialize(view);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.small_push);

        txtUserIdAccount.setText(firebaseUser.getUid());
        txtEmailAccount.setText(ServerConnector.userEmailAddress);
        txtPhoneAccount.setText(firebaseUser.getPhoneNumber());

        btnBack.setOnClickListener(v -> {
            v.startAnimation(animation);

            new Handler().postDelayed(() -> {
                Fragment settingsFragment = new SettingsFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayout, settingsFragment);
                transaction.commit();
            },100);
        });
        return view;

    }

    public void initialize(View view) {
        btnBack = view.findViewById(R.id.btnBackAcc);
        txtUserIdAccount = view.findViewById(R.id.txtUserIdAccount);
        txtEmailAccount = view.findViewById(R.id.txtEmailAddressAccount);
        txtPhoneAccount = view.findViewById(R.id.txtPhoneAccount);
    }
}