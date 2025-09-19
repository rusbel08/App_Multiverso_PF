package com.example.multiversofit;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AgregarSocioActivity extends AppCompatActivity {

    private EditText etNombre, etEdad, etDni, etCelular, etFechaInicio, etFechaFin;
    private RadioGroup rgSexo;
    private CheckBox cbPrincipiante, cbIntermedio, cbAvanzado;
    private Button btnGuardar;

    private FirebaseFirestore db; // instancia de Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_socio);

        // Referencias a los campos
        etNombre = findViewById(R.id.etNombre);
        etEdad = findViewById(R.id.etEdad);
        etDni = findViewById(R.id.etDni);
        etCelular = findViewById(R.id.etCelular);
        rgSexo = findViewById(R.id.rgSexo);
        etFechaInicio = findViewById(R.id.etFechaInicio);
        etFechaFin = findViewById(R.id.etFechaFin);
        cbPrincipiante = findViewById(R.id.cbPrincipiante);
        cbIntermedio = findViewById(R.id.cbIntermedio);
        cbAvanzado = findViewById(R.id.cbAvanzado);
        btnGuardar = findViewById(R.id.btnGuardar);

        // Instancia Firestore
        db = FirebaseFirestore.getInstance();

        // Abrir calendarios
        etFechaInicio.setOnClickListener(v -> mostrarCalendario(etFechaInicio));
        etFechaFin.setOnClickListener(v -> mostrarCalendario(etFechaFin));

        // Guardar socio
        btnGuardar.setOnClickListener(v -> validarDniYGuardar());

        // Hacer que los checkbox actúen como radio buttons (solo uno a la vez)
        cbPrincipiante.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbIntermedio.setChecked(false);
                cbAvanzado.setChecked(false);
            }
        });

        cbIntermedio.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbPrincipiante.setChecked(false);
                cbAvanzado.setChecked(false);
            }
        });

        cbAvanzado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbPrincipiante.setChecked(false);
                cbIntermedio.setChecked(false);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainAgregarSocio), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Metodo para mostrar Calendario
    private void mostrarCalendario(EditText campoFecha) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, month1, dayOfMonth) -> {
                    // Usamos Calendar para obtener la fecha en formato Date
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year1, month1, dayOfMonth);

                    // Mostrar en el EditText con formato dd/MM/yyyy
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    campoFecha.setText(sdf.format(selectedDate.getTime()));

                    // Guardamos la fecha como tag para Firestore (Timestamp)
                    campoFecha.setTag(new Timestamp(selectedDate.getTime()));
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    // Validar si ya existe un socio con el mismo DNI antes de guardar
    private void validarDniYGuardar() {
        String dni = etDni.getText().toString().trim();

        if (dni.isEmpty()) {
            Toast.makeText(this, "Debes ingresar un DNI", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("socios")
                .document(dni)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Toast.makeText(this, "Ya existe un socio registrado con este DNI", Toast.LENGTH_SHORT).show();
                    } else {
                        guardarSocio(dni);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al validar DNI: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    //Metodo para guardar socio en Firestore
    private void guardarSocio(String dni) {
        String nombre = etNombre.getText().toString().trim();
        String edad = etEdad.getText().toString().trim();
        String celular = etCelular.getText().toString().trim();

        // Validación básica
        if (nombre.isEmpty() || edad.isEmpty() || dni.isEmpty() || celular.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener sexo
        int selectedId = rgSexo.getCheckedRadioButtonId();
        String sexo = "";
        if (selectedId != -1) {
            RadioButton rbSexo = findViewById(selectedId);
            sexo = rbSexo.getText().toString();
        }

        // Experiencia
        String experiencia = "";
        if (cbPrincipiante.isChecked()) experiencia = "Principiante";
        else if (cbIntermedio.isChecked()) experiencia = "Intermedio";
        else if (cbAvanzado.isChecked()) experiencia = "Avanzado";

        // Fechas como Timestamp
        Timestamp fechaInicio = etFechaInicio.getTag() instanceof Timestamp ? (Timestamp) etFechaInicio.getTag() : null;
        Timestamp fechaFin = etFechaFin.getTag() instanceof Timestamp ? (Timestamp) etFechaFin.getTag() : null;

        // Map sin el campo dni, porque será el ID del documento
        Map<String, Object> socio = new HashMap<>();
        socio.put("nombre", nombre);
        socio.put("edad", edad);
        socio.put("celular", celular);
        socio.put("sexo", sexo);
        socio.put("fechaInicio", fechaInicio);
        socio.put("fechaFin", fechaFin);
        socio.put("experiencia", experiencia);
        socio.put("estado", 0); // (0 = activo, 1 = inactivo)

        // Guardar en Firestore, usando el DNI como ID del documento
        db.collection("socios")
                .document(dni)
                .set(socio)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Socio registrado correctamente", Toast.LENGTH_SHORT).show();
                    finish(); // opcional
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
