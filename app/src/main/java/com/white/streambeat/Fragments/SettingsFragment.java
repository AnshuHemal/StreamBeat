package com.white.streambeat.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.white.streambeat.Activities.SplashActivity;
import com.white.streambeat.Connections.ServerConnector;
import com.white.streambeat.R;

import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends Fragment {

    LinearLayout llAbout, llAccount, llAudioVideo, llLocalFiles, llPlayback, llNotifications, llAdvertisement;
    ImageView btnBack;
    Button btnLogout;
    BottomSheetDialog sheetDialog;
    FirebaseUser firebaseUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        initialize(view);

        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.small_push);

        btnBack.setOnClickListener(v -> {
            v.startAnimation(animation);

            new Handler().postDelayed(() -> {
                Fragment profileFragment = new ProfileFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayout, profileFragment);
                transaction.commit();
            },100);
        });

        llAbout.setOnClickListener(v -> {
            v.startAnimation(animation);

            new Handler().postDelayed(() -> {
                Fragment aboutFragment = new AboutFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayout, aboutFragment);
                transaction.commit();
            },100);
        });

        llAccount.setOnClickListener(v -> {
            v.startAnimation(animation);

            new Handler().postDelayed(() -> {
                Fragment accountFragment = new AccountFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayout, accountFragment);
                transaction.commit();
            },100);
        });

        llLocalFiles.setOnClickListener(v -> {
            v.startAnimation(animation);

            new Handler().postDelayed(() -> {
                Fragment localFilesFragment = new LocalFilesFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayout, localFilesFragment);
                transaction.commit();
            },100);
        });

        llPlayback.setOnClickListener(v -> {
            v.startAnimation(animation);

            new Handler().postDelayed(() -> {
                Fragment playbackFragment = new PlaybackFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayout, playbackFragment);
                transaction.commit();
            },100);
        });

        llNotifications.setOnClickListener(v -> {
            v.startAnimation(animation);

            new Handler().postDelayed(() -> {
                Fragment notificationFragment = new NotificationsFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayout, notificationFragment);
                transaction.commit();
            },100);
        });

        llAudioVideo.setOnClickListener(v -> {
            v.startAnimation(animation);

            new Handler().postDelayed(() -> {
                Fragment audioVideoFragment = new AudioVideoFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayout, audioVideoFragment);
                transaction.commit();
            },100);
        });

        btnLogout.setOnClickListener(v -> {
            showLogoutBottomSheet();
        });

        return view;
    }

    private void showLogoutBottomSheet() {
        sheetDialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetStyle);
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.logout_sheet, null);

        Button cancelBtn = sheetView.findViewById(R.id.cancelBtn);
        Button logoutBtn = sheetView.findViewById(R.id.yesLogoutBtn);

        cancelBtn.setOnClickListener(v -> sheetDialog.dismiss());

        logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    ServerConnector.LOGOUT_URL,
                    response -> {
                        if (response.equals("Success")) {
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(requireContext(), SplashActivity.class));
                            requireActivity().finish();
                        } else {
                            Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
                        }
                    }, error -> Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
            ) {
                @Override
                protected Map<String, String> getParams() {
                    HashMap<String,String> hm = new HashMap<>();
                    hm.put("key_phone", firebaseUser.getPhoneNumber());
                    return hm;
                }
            };
            Volley.newRequestQueue(requireContext()).add(stringRequest);
        });
        sheetDialog.setCanceledOnTouchOutside(false);
        sheetDialog.setCancelable(false);
        sheetDialog.setContentView(sheetView);
        sheetDialog.show();
    }

    public void initialize(View view) {
        llAbout = view.findViewById(R.id.llAbout);
        llAccount = view.findViewById(R.id.llAccount);
        llAudioVideo = view.findViewById(R.id.llAudioVideo);
        llLocalFiles = view.findViewById(R.id.llLocalFiles);
        llPlayback = view.findViewById(R.id.llPlayback);
        llNotifications = view.findViewById(R.id.llNotification);
        llAdvertisement = view.findViewById(R.id.llAdvertisements);

        btnBack = view.findViewById(R.id.btnBack);
        btnLogout = view.findViewById(R.id.btnLogout);
    }
}