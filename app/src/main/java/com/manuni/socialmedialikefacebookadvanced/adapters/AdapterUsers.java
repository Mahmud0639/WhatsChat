package com.manuni.socialmedialikefacebookadvanced.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manuni.socialmedialikefacebookadvanced.R;
import com.manuni.socialmedialikefacebookadvanced.activities.ChatActivity;
import com.manuni.socialmedialikefacebookadvanced.databinding.RowUsersBinding;
import com.manuni.socialmedialikefacebookadvanced.models.ModelUser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.AdapterUsersViewHolder>{
    private Context context;
    public List<ModelUser> list;

    public AdapterUsers(Context context, List<ModelUser> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public AdapterUsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_users,parent,false);
        return new AdapterUsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterUsersViewHolder holder, int position) {
        ModelUser data = list.get(position);

        String hisUid = data.getUid();

        holder.binding.userName.setText(data.getName());
        holder.binding.userEmail.setText(data.getEmail());

        try {
            Picasso.get().load(data.getImage()).placeholder(R.drawable.placeholder).into(holder.binding.avatarIV);
        } catch (Exception e) {
            Picasso.get().load(R.drawable.placeholder).into(holder.binding.avatarIV);
            e.printStackTrace();
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context, ""+data.getEmail(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid",hisUid);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class AdapterUsersViewHolder extends RecyclerView.ViewHolder {
        RowUsersBinding binding;

        public AdapterUsersViewHolder(@NonNull View itemView){
            super(itemView);
            binding = RowUsersBinding.bind(itemView);
        }

    }
}
