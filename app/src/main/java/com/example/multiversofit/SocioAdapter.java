package com.example.multiversofit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SocioAdapter extends RecyclerView.Adapter<SocioAdapter.VH> {

    public interface OnSocioActionListener {
        void onEdit(Socio socio);
        void onDelete(Socio socio);
    }

    private final List<Socio> data = new ArrayList<>();
    private final List<Socio> backup = new ArrayList<>();
    private final OnSocioActionListener listener;

    public SocioAdapter(OnSocioActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Socio> items) {
        backup.clear();
        backup.addAll(items);
        data.clear();
        data.addAll(items);
        notifyDataSetChanged();
    }

    public void filterByDni(String q) {
        data.clear();
        if (q == null || q.trim().isEmpty()) {
            data.addAll(backup);
        } else {
            String s = q.trim();
            for (Socio x : backup) {
                if (x.dni != null && x.dni.startsWith(s)) data.add(x);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_socio_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Socio s = data.get(pos);
        h.tvDni.setText(s.dni);
        h.tvNombre.setText(s.nombre != null ? s.nombre : "");
        h.tvEstado.setText(s.estado == 0 ? "Activo" : "Inactivo");
        h.tvEstado.setBackgroundResource(
                s.estado == 0 ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive
        );

        h.btnEditar.setOnClickListener(v -> listener.onEdit(s));
        h.btnBorrar.setOnClickListener(v -> listener.onDelete(s));
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDni, tvNombre, tvEstado;
        ImageButton btnEditar, btnBorrar;
        VH(@NonNull View v) {
            super(v);
            tvDni = v.findViewById(R.id.tvDni);
            tvNombre = v.findViewById(R.id.tvNombre);
            tvEstado = v.findViewById(R.id.tvEstado);
            btnEditar = v.findViewById(R.id.btnEditar);
            btnBorrar = v.findViewById(R.id.btnBorrar);
        }
    }
}