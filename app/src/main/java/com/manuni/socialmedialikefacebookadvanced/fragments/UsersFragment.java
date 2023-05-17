package com.manuni.socialmedialikefacebookadvanced.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.socialmedialikefacebookadvanced.R;
import com.manuni.socialmedialikefacebookadvanced.activities.LoginActivity;
import com.manuni.socialmedialikefacebookadvanced.adapters.AdapterUsers;
import com.manuni.socialmedialikefacebookadvanced.databinding.FragmentUsersBinding;
import com.manuni.socialmedialikefacebookadvanced.models.ModelUser;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {

    private Context mContext;
    private AdapterUsers adapterUsers;
    private List<ModelUser> list;
    private FirebaseAuth mAuth;

    public UsersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    FragmentUsersBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentUsersBinding.inflate(inflater,container,false);

        mAuth = FirebaseAuth.getInstance();

        list = new ArrayList<>();
        getAllUsers();



        binding.userRV.setLayoutManager(new LinearLayoutManager(mContext));
        binding.userRV.setHasFixedSize(true);


        return binding.getRoot();
    }

    private void getAllUsers() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    ModelUser modelUser = dataSnapshot.getValue(ModelUser.class);
                    if (!modelUser.getUid().equals(user.getUid())){//ekhane user.getUid() eta holo main user er uid...
                        // .ar modelUser.getUid() eta holo firebase er sokol user. je gulo amra database theke datasnapshot er maddhome niye asci
                        list.add(modelUser);//jodi auth user ar firebase er user gulo na mile tahole firebase er user gulo add koro eta bujhacce ekhane
                    }


                }

                adapterUsers = new AdapterUsers(mContext,list);
                binding.userRV.setAdapter(adapterUsers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);//to show option menu in fragment
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);

        MenuItem item = menu.findItem(R.id.menuSearch);
       SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
       searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
           @Override
           public boolean onQueryTextSubmit(String query) {
               //called when user pressed search button from keyboard
               if (!TextUtils.isEmpty(query.trim())){
                    //search a kono kichu ache tai search koro
                   searchUsers(query);

               }else {
                   //search a kono kichu nai tai all user get koro
                   getAllUsers();
               }
               return false;
           }

           @Override
           public boolean onQueryTextChange(String newText) {

               //called when user pressed search button from keyboard
               if (!TextUtils.isEmpty(newText.trim())){
                   //search a kono kichu ache tai search koro
                   searchUsers(newText);
               }else {
                   //search a kono kichu nai tai all user get koro
                   getAllUsers();
               }
               return false;
           }
       });



        super.onCreateOptionsMenu(menu,inflater);
    }

    private void searchUsers(String query) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    ModelUser modelUser = dataSnapshot.getValue(ModelUser.class);
                    //search all user except currently logged in user
                    if (!modelUser.getUid().equals(user.getUid())){//ekhane user.getUid() eta holo main user er uid...
                        // .ar modelUser.getUid() eta holo firebase er sokol user. je gulo amra database theke datasnapshot er maddhome niye asci
                        if (modelUser.getName().toLowerCase().contains(query.toLowerCase()) || modelUser.getEmail().toLowerCase().contains(query.toLowerCase())){
                            list.add(modelUser);

                        }
                        //jodi auth user ar firebase er user gulo na mile tahole firebase er user gulo add koro eta bujhacce ekhane
                    }


                }

                adapterUsers = new AdapterUsers(mContext,list);
                adapterUsers.notifyDataSetChanged();
                binding.userRV.setAdapter(adapterUsers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.logout){
            mAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus(){
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){

        }else{
            startActivity(new Intent(mContext, LoginActivity.class));
            getActivity().finish();
        }
    }

}