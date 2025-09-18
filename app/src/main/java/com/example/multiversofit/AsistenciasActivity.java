package com.example.multiversofit;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.database.Cursor;

public class AsistenciasActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AsistenciasAdapter adapter;
    private ConexionBD db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asistencias);

        recyclerView = findViewById(R.id.recyclerAsistencias);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }
}

