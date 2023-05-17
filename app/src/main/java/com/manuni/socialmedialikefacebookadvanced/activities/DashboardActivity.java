package com.manuni.socialmedialikefacebookadvanced.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.manuni.socialmedialikefacebookadvanced.databinding.ActivityDashboardBinding;
import com.manuni.socialmedialikefacebookadvanced.fragments.ChatListFragment;
import com.manuni.socialmedialikefacebookadvanced.fragments.HomeFragment;
import com.manuni.socialmedialikefacebookadvanced.fragments.ProfileFragment;
import com.manuni.socialmedialikefacebookadvanced.R;
import com.manuni.socialmedialikefacebookadvanced.fragments.UsersFragment;


public class DashboardActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    ActivityDashboardBinding binding;
    private FirebaseAuth mAuth;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        mAuth = FirebaseAuth.getInstance();






        binding.bottomNavigation.setOnItemSelectedListener(this);


        loadFragments(new HomeFragment());
    }

    public boolean loadFragments(Fragment fragment){
        if (fragment != null){
            getSupportFragmentManager().beginTransaction().replace(R.id.content,fragment).commit();
        }
        return true;
    }


    private void checkUserStatus(){
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){

        }else{
            startActivity(new Intent(DashboardActivity.this,LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment myFragments = null;
        switch (item.getItemId()){
            case R.id.nav_home:
                myFragments = new HomeFragment();
                actionBar.setTitle("Home");
                break;
            case R.id.nav_profile:
                myFragments = new ProfileFragment();
                actionBar.setTitle("Profile");
                break;
            case R.id.nav_users:
                myFragments = new UsersFragment();
                actionBar.setTitle("Users");
                break;
            case R.id.nav_chat:
                myFragments = new ChatListFragment();
                actionBar.setTitle("Chat");
                break;
        }


        return loadFragments(myFragments);
    }

    @Override
    public void onBackPressed() {
        if (binding.bottomNavigation.getSelectedItemId()==R.id.nav_home){
            super.onBackPressed();
            finish();
        }else {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        }

    }
}