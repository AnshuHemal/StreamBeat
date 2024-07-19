package com.white.streambeat.Activities;

import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.white.streambeat.Fragments.ExploreFragment;
import com.white.streambeat.Fragments.HomeFragment;
import com.white.streambeat.Fragments.LibraryFragment;
import com.white.streambeat.Fragments.ProfileFragment;
import com.white.streambeat.R;
import com.white.streambeat.databinding.ActivityDashboardBinding;

public class DashboardActivity extends AppCompatActivity {

    ActivityDashboardBinding binding;
    FrameLayout frameLayout;
    Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        frameLayout = findViewById(R.id.frameLayout);
        currentFragment = new HomeFragment();
        loadFragment(currentFragment, false);

        binding.bottomNavBar.setOnNavigationItemSelectedListener(item -> {
            Fragment fragment;

            if (item.getItemId() == R.id.navHome) {
                fragment = new HomeFragment();
            } else if (item.getItemId() == R.id.navExplore) {
                fragment = new ExploreFragment();
            } else if (item.getItemId() == R.id.navLibrary) {
                fragment = new LibraryFragment();
            } else {
                fragment = new ProfileFragment();
            }

            currentFragment = fragment;
            loadFragment(fragment, false);
            return true;

        });

    }
    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
    }

    public void onBackPressed() {
        if (currentFragment instanceof HomeFragment) {
            super.onBackPressed();
        } else {
            loadFragment(new HomeFragment(), false);
            currentFragment = new HomeFragment();
            binding.bottomNavBar.setSelectedItemId(R.id.navHome);
        }
    }
}