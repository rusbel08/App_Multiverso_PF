package com.example.multiversofit;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AsistenciasActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AsistenciasAdapter adapter;
    private SqliteUsuario db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asistencias);

        recyclerView = findViewById(R.id.recyclerAsistencias);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }
}

