package com.example.multiversofit;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class AgregarSocioActivity extends AppCompatActivity {

    private EditText etFechaInicio, etFechaFin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_socio);

        etFechaInicio = findViewById(R.id.etFechaInicio);
        etFechaFin = findViewById(R.id.etFechaFin);

        // Listener para fecha inicio
        etFechaInicio.setOnClickListener(v -> mostrarCalendario(etFechaInicio));

        // Listener para fecha fin
        etFechaFin.setOnClickListener(v -> mostrarCalendario(etFechaFin));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainAgregarSocio), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    //Metodo para mostrar calendario
    private void mostrarCalendario(EditText campoFecha) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, month1, dayOfMonth) -> {
                    // Mostrar fecha seleccionada en el EditText
                    String fecha = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                    campoFecha.setText(fecha);
                },
                year, month, day
        );

        datePickerDialog.show();
    }
}