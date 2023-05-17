package com.manuni.socialmedialikefacebookadvanced.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.manuni.socialmedialikefacebookadvanced.R;
import com.manuni.socialmedialikefacebookadvanced.adapters.AdapterChat;
import com.manuni.socialmedialikefacebookadvanced.databinding.ActivityChatBinding;
import com.manuni.socialmedialikefacebookadvanced.models.ModelChat;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    ActivityChatBinding binding;
    private Toolbar toolbar;
    private FirebaseAuth auth;
    private String hisUid,myUid;
    private DatabaseReference databaseReference;
    String hisImage;

    //for checking if use has seen message or not
    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;

    List<ModelChat> modelChats;
    AdapterChat adapterChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        auth = FirebaseAuth.getInstance();

        hisUid = getIntent().getStringExtra("hisUid");

        modelChats = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        linearLayoutManager.setStackFromEnd(true);

        binding.chatRecyclerView.setHasFixedSize(true);
        binding.chatRecyclerView.setLayoutManager(linearLayoutManager);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        Query usersQuery = databaseReference.orderByChild("uid").equalTo(hisUid);//orderByChild holo database er sokol child je gulor name uid tader ke select kora...equalTo holo tader sathe milano

        usersQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String name = ""+dataSnapshot.child("name").getValue();
                    hisImage = ""+dataSnapshot.child("image").getValue();

                    binding.userNameTV.setText(name);

                    try {
                        Picasso.get().load(hisImage).placeholder(R.drawable.placeholder).into(binding.profileImageIV);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.placeholder).into(binding.profileImageIV);
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Error "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
          /*  Chats node will be created that will contain all chats
                    Whenever user send messages it will create new child
            in Chats node and that child will contain the following key values
                    -sender: uid of sender
                    -receiver: uid of receiver
                    -message: the actual message*/





            @Override
            public void onClick(View view) {
                String message = binding.messageET.getText().toString().trim();
                if (TextUtils.isEmpty(message)){

                }else {
                    sendMessage(message);
                }
            }
        });

        readMessages();
        seenMessage();

    }
    private void seenMessage(){
        userRefForSeen = FirebaseDatabase.getInstance().getReference().child("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)){
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen",true);

                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        userRefForSeen.removeEventListener(seenListener);
    }

    private void readMessages(){
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Chats");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelChats.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelChat modelChat = ds.getValue(ModelChat.class);
                    if (modelChat.getReceiver().equals(myUid) && modelChat.getSender().equals(hisUid)||
                    modelChat.getReceiver().equals(hisUid) && modelChat.getSender().equals(myUid)){
                        modelChats.add(modelChat);
                    }
                    adapterChat = new AdapterChat(ChatActivity.this,modelChats,hisImage);
                    adapterChat.notifyDataSetChanged();
                    binding.chatRecyclerView.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message) {
        String timestamp = String.valueOf(System.currentTimeMillis());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",myUid);
        hashMap.put("receiver",hisUid);
        hashMap.put("message",message);
        hashMap.put("timestamp",timestamp);
        hashMap.put("isSeen",false);

        reference.child("Chats").push().setValue(hashMap);

        binding.messageET.setText("");
    }

    private void checkUserStatus(){
        FirebaseUser user = auth.getCurrentUser();
        if (user != null){
            myUid = user.getUid();

        }else {
            startActivity(new Intent(ChatActivity.this,MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        //hide actionSearch cause we don't need here
        menu.findItem(R.id.menuSearch).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id==R.id.logout){
            auth.signOut();
            checkUserStatus();
        }

        return super.onOptionsItemSelected(item);
    }
}