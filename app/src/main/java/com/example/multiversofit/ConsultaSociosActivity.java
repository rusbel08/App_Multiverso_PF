package com.example.multiversofit;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConsultaSociosActivity extends AppCompatActivity
        implements SocioAdapter.OnSocioActionListener {

    private FirebaseFirestore db;
    private SocioAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_consulta_socios); // reutiliza el mismo layout

        EditText etBuscar = findViewById(R.id.etBuscarDni);
        RecyclerView rv = findViewById(R.id.rvSocios);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SocioAdapter(this);
        rv.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        db.collection("socios")
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;

                    List<Socio> list = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        String dni = d.getId();
                        Map<String,Object> m = d.getData();
                        if (m == null) continue;
                        String nombre = (String) m.get("nombre");
                        Number estadoN = (Number) m.get("estado");
                        int estado = estadoN != null ? estadoN.intValue() : 0;
                        list.add(new Socio(dni, nombre, estado));
                    }
                    adapter.setItems(list);
                });

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int i,int i1,int i2){}
            @Override public void onTextChanged(CharSequence s,int i,int i1,int i2){
                adapter.filterByDni(s.toString());
            }
            @Override public void afterTextChanged(Editable s){}
        });
    }

    @Override
    public void onEdit(Socio socio) {
        //Editar socio
        Intent i = new Intent(ConsultaSociosActivity.this, EditarMiembroActivity.class);
        i.putExtra("dni", socio.dni);
        startActivity(i);
    }

    @Override
    public void onDelete(Socio socio) {
        new AlertDialog.Builder(this)
                .setTitle("Dar de baja")
                .setMessage("¿Marcar al socio " + socio.dni + " como inactivo?")
                .setPositiveButton("Sí", (d, w) -> {
                    db.collection("socios")
                            .document(socio.dni)
                            .update("estado", 1)
                            .addOnSuccessListener(v -> ToastUtils.showCustomToast(this, "Socio dado de baja"))
                            .addOnFailureListener(err -> ToastUtils.showCustomToast(this, "Error: " + err.getMessage()));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
