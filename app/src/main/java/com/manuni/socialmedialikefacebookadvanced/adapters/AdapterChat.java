package com.manuni.socialmedialikefacebookadvanced.adapters;



import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.manuni.socialmedialikefacebookadvanced.R;
import com.manuni.socialmedialikefacebookadvanced.models.ModelChat;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyViewHolder> {


    private static final int MESSAGE_TYPE_LEFT = 0;
    private static final int MESSAGE_TYPE_RIGHT = 1;

    Context context;
    List<ModelChat> chatList;
    String imageUrl;
    FirebaseUser user;
    FirebaseAuth mAuth;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //here we need to inflate two layout..for message sending and receiving..MESSAGE_TYPE_RIGHT means sender..other receiver
        if (viewType==MESSAGE_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right,parent,false);
            return new MyViewHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left,parent,false);
            return new MyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ModelChat data = chatList.get(position);
        String message = data.getMessage();
        String timestamp = data.getTimestamp();

        //convert timestamp to dd/MM/yyyy hh:mm am/pm
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();

        holder.messageTV.setText(message);
        holder.timeTV.setText(dateTime);

        try {
            Picasso.get().load(imageUrl).into(holder.profileImageView);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //seen/delivered status of messages
        if (position==chatList.size()-1){
            if (chatList.get(position).isSeen()){
                holder.isSeenTV.setText("seen");
                //holder.isSeenTV.setVisibility(View.VISIBLE);
            }else {
                holder.isSeenTV.setText("delivered");
                //holder.isSeenTV.setVisibility(View.VISIBLE);
            }
        }else {
            holder.isSeenTV.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {

        //eta korar karonei decision neya jabe sender hole sender er layout use hobe...ar receiver hole receiver er layout use hobe
        if (chatList.get(position).getSender().equals(user.getUid())){
            return MESSAGE_TYPE_RIGHT;
        }else {
            return MESSAGE_TYPE_LEFT;
        }

    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView profileImageView;
        TextView messageTV, timeTV, isSeenTV;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImageView = itemView.findViewById(R.id.profileImageIVFor);
            messageTV = itemView.findViewById(R.id.messageTV);
            timeTV = itemView.findViewById(R.id.timeTV);
            isSeenTV = itemView.findViewById(R.id.isDeliveredTV);
        }
    }
}
