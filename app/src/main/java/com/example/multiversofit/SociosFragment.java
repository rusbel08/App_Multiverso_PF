package com.example.multiversofit;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class SociosFragment extends Fragment {

    private RecyclerView recyclerSocios;
    private FloatingActionButton fabAgregarSocios;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_socios, container, false);

        recyclerSocios = view.findViewById(R.id.recyclerSocios);
        fabAgregarSocios = view.findViewById(R.id.fabAgregarSocios);

        recyclerSocios.setLayoutManager(new LinearLayoutManager(getContext()));

        fabAgregarSocios.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AgregarSocioActivity.class);
            startActivity(intent);
        });

        return view;
    }



}