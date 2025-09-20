package com.example.multiversofit;

//import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConsultaSociosFragment extends Fragment
        implements SocioAdapter.OnSocioActionListener {

    private FirebaseFirestore db;
    private SocioAdapter adapter;

    private EditText etBuscar;
    private RecyclerView rv;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_consulta_socios, container, false);

        etBuscar = root.findViewById(R.id.etBuscarDni);
        rv = root.findViewById(R.id.rvSocios);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SocioAdapter(this);
        rv.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Carga en vivo de socios (DNI como ID)
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

        // Filtro por DNI
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int i,int i1,int i2){}
            @Override public void onTextChanged(CharSequence s,int i,int i1,int i2){
                adapter.filterByDni(s.toString());
            }
            @Override public void afterTextChanged(Editable s){}
        });

        return root;
    }

    // === Acciones ===

    @Override
    public void onEdit(Socio socio) {
        //Editar:
        Intent i = new Intent(requireContext(), EditarMiembroActivity.class);
        i.putExtra("dni", socio.dni);
        startActivity(i);
    }


    @Override
    public void onDelete(Socio socio) {
        try {
            final AlertDialog dialog = new MaterialAlertDialogBuilder(
                    requireContext(),
                    R.style.ThemeOverlay_MultiversoFit_Dialog   // tu overlay oscuro
            )
                    .setTitle("Eliminar socio")
                    .setMessage("¿Eliminar definitivamente al socio " + socio.dni + "?\nEsta acción no se puede deshacer.")
                    .setPositiveButton("Eliminar", (d, w) -> {
                        db.collection("socios").document(socio.dni)
                                .delete()
                                .addOnSuccessListener(v -> {
                                    ToastUtils.showCustomToast(requireActivity(), "Socio eliminado");
                                })
                                .addOnFailureListener(err ->
                                        ToastUtils.showCustomToast(requireActivity(), "Error: " + err.getMessage()));
                    })
                    .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                    .create();

            dialog.setCanceledOnTouchOutside(false);

            // Colorear botones cuando el diálogo ya está "mostrado"
            dialog.setOnShowListener(dlg -> {
                if (!isAdded()) return;
                if (dialog.getButton(DialogInterface.BUTTON_POSITIVE) != null) {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                            .setTextColor(ContextCompat.getColor(requireContext(), R.color.dialog_positive));
                }
                if (dialog.getButton(DialogInterface.BUTTON_NEGATIVE) != null) {
                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                            .setTextColor(ContextCompat.getColor(requireContext(), R.color.dialog_negative));
                }
            });

            dialog.show();

        } catch (Exception ex) {
            // Fallback sin overlay si hubiera algún problema de tema/overlay
            final AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Eliminar socio")
                    .setMessage("¿Eliminar definitivamente al socio " + socio.dni + "?\nEsta acción no se puede deshacer.")
                    .setPositiveButton("Eliminar", (d, w) -> {
                        db.collection("socios").document(socio.dni)
                                .delete()
                                .addOnSuccessListener(v ->
                                        ToastUtils.showCustomToast(requireActivity(), "Socio eliminado"))
                                .addOnFailureListener(err ->
                                        ToastUtils.showCustomToast(requireActivity(), "Error: " + err.getMessage()));
                    })
                    .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                    .create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }

}
