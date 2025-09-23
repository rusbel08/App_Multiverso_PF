package com.example.multiversofit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AsistenciasAdapter extends RecyclerView.Adapter<AsistenciasAdapter.ViewHolder> {

    private List<Asistencia> lista;

    public AsistenciasAdapter(List<Asistencia> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_asistencia, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Asistencia asistencia = lista.get(position);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvNombre, tvFecha, tvDni;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvId);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvDni = itemView.findViewById(R.id.tvDni);
        }
    }
}
