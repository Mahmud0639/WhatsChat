package com.manuni.socialmedialikefacebookadvanced.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.manuni.socialmedialikefacebookadvanced.R;
import com.manuni.socialmedialikefacebookadvanced.databinding.FragmentChatListBinding;

public class ChatListFragment extends Fragment {


    public ChatListFragment() {
        // Required empty public constructor
    }
    FragmentChatListBinding binding;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatListBinding.inflate(inflater,container,false);






        return binding.getRoot();
    }
}