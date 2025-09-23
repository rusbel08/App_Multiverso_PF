package com.example.multiversofit;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConsultaSociosFragment extends Fragment
        implements SocioAdapter.OnSocioActionListener {

    private FirebaseFirestore db;
    private SocioAdapter adapter;

    private EditText etBuscar;
    private RecyclerView rv;
    private FloatingActionButton fabAgregarSocios;
    private Spinner spFiltroEstado;

    private final List<Socio> sociosOriginal = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_consulta_socios, container, false);

        etBuscar = root.findViewById(R.id.etBuscarDni);
        rv = root.findViewById(R.id.rvSocios);
        spFiltroEstado = root.findViewById(R.id.spFiltroEstado);
        fabAgregarSocios = root.findViewById(R.id.fabAgregarSocios);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SocioAdapter(this);
        rv.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Spinner: Todos, Activo, Inactivo
        String[] estados = {"Todos", "Activo", "Inactivo"};
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(requireContext(),
                R.layout.spinner_item, estados);
        spAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spFiltroEstado.setAdapter(spAdapter);
        spFiltroEstado.setSelection(0);

        // Cuando cambie el spinner
        spFiltroEstado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                aplicarFiltros();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Escucha en vivo de Firestore
        db.collection("socios")
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;

                    sociosOriginal.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        String dni = d.getId();
                        Map<String, Object> m = d.getData();
                        if (m == null) continue;
                        String nombre = m.get("nombre") instanceof String ? (String) m.get("nombre") : "";
                        // normalizar estado (puede venir como Long/Double/Number)
                        int estado = 0;
                        Object estadoObj = m.get("estado");
                        if (estadoObj instanceof Number) {
                            estado = ((Number) estadoObj).intValue();
                        }
                        sociosOriginal.add(new Socio(dni, nombre, estado));
                    }

                    // cada vez que recargamos datos aplicamos los filtros vigentes
                    aplicarFiltros();
                });

        // Filtro por DNI
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            @Override public void onTextChanged(CharSequence s,int start,int before,int count){
                aplicarFiltros();
            }
            @Override public void afterTextChanged(Editable s){}
        });

        fabAgregarSocios.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AgregarSocioActivity.class);
            startActivity(intent);
        });

        return root;
    }

    // Aplica filtros combinados (estado + DNI)
    private void aplicarFiltros() {
        String q = etBuscar.getText() != null ? etBuscar.getText().toString().trim() : "";
        int spinnerPos = spFiltroEstado.getSelectedItemPosition(); // 0=Todos,1=Activo,2=Inactivo

        List<Socio> out = new ArrayList<>();
        for (Socio s : sociosOriginal) {
            // filtro por estado
            boolean okEstado = true;
            if (spinnerPos == 1) okEstado = (s.estado == 0); // Activo
            else if (spinnerPos == 2) okEstado = (s.estado == 1); // Inactivo

            // filtro por dni
            boolean okDni = q.isEmpty() || (s.dni != null && s.dni.contains(q));

            if (okEstado && okDni) out.add(s);
        }

        adapter.setItems(out);
    }

    // === Acciones ===
    @Override
    public void onEdit(Socio socio) {
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
