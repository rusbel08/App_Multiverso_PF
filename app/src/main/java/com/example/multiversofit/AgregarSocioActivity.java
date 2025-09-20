package com.example.multiversofit;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AgregarSocioActivity extends AppCompatActivity {

    private EditText etNombre, etEdad, etDni, etCelular, etFechaInicio, etFechaFin;
    private RadioGroup rgSexo;
    // OJO: estos ids mapean a tus CheckBox actuales del XML
    private CheckBox cbPrincipiante, cbIntermedio, cbAvanzado; // Atletismo, Natación, Gimnasio
    private Button btnGuardar;

    private FirebaseFirestore db; // instancia de Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_socio);

        // Referencias a los campos
        etNombre      = findViewById(R.id.etNombre);
        etEdad        = findViewById(R.id.etEdad);
        etDni         = findViewById(R.id.etDni);
        etCelular     = findViewById(R.id.etCelular);
        rgSexo        = findViewById(R.id.rgSexo);
        etFechaInicio = findViewById(R.id.etFechaInicio);
        etFechaFin    = findViewById(R.id.etFechaFin);

        // CheckBox del XML (ids actuales)
        cbPrincipiante = findViewById(R.id.cbAtletismo);
        cbIntermedio   = findViewById(R.id.cbNatacion);
        cbAvanzado     = findViewById(R.id.cbgimnacio); // id tal cual en tu XML

        btnGuardar = findViewById(R.id.btnGuardar);

        // Instancia Firestore
        db = FirebaseFirestore.getInstance();

        // Calendarios
        etFechaInicio.setOnClickListener(v -> mostrarCalendario(etFechaInicio));
        etFechaFin.setOnClickListener(v -> mostrarCalendario(etFechaFin));

        // Guardar socio
        btnGuardar.setOnClickListener(v -> validarDniYGuardar());

        // IMPORTANTE: Eliminamos cualquier lógica que desmarque otras casillas.
        // (No hay setOnCheckedChangeListener aquí)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainAgregarSocio), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Metodo para mostrar Calendario
    private void mostrarCalendario(EditText campoFecha) {
        final Calendar calendar = Calendar.getInstance();
        int year  = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day   = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, month1, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year1, month1, dayOfMonth);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    campoFecha.setText(sdf.format(selectedDate.getTime()));

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
            ToastUtils.showCustomToast(this, "Debes ingresar un DNI");
            return;
        }

        db.collection("socios")
                .document(dni)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ToastUtils.showCustomToast(this, "Ya existe un socio registrado con este DNI");
                    } else {
                        guardarSocio(dni);
                    }
                })
                .addOnFailureListener(e ->
                        ToastUtils.showCustomToast(this, "Error al validar DNI: " + e.getMessage()));
    }

    // Metodo para guardar socio en Firestore (con experiencias MÚLTIPLES)
    private void guardarSocio(String dni) {
        String nombre  = etNombre.getText().toString().trim();
        String edad    = etEdad.getText().toString().trim();
        String celular = etCelular.getText().toString().trim();

        // Validación básica
        if (nombre.isEmpty() || edad.isEmpty() || dni.isEmpty() || celular.isEmpty()) {
            ToastUtils.showCustomToast(this, "Completa todos los campos obligatorios");
            return;
        }

        // Sexo
        int selectedId = rgSexo.getCheckedRadioButtonId();
        String sexo = "";
        if (selectedId != -1) {
            RadioButton rbSexo = findViewById(selectedId);
            sexo = rbSexo.getText().toString();
        }

        // EXPERIENCIAS MÚLTIPLES -> se guarda como ARRAY en Firestore
        List<String> experiencias = new ArrayList<>();
        if (cbPrincipiante.isChecked()) experiencias.add("Atletismo");
        if (cbIntermedio.isChecked())   experiencias.add("Natación");
        if (cbAvanzado.isChecked())     experiencias.add("Gimnasio"); // corrige ortografía en tus valores si deseas

        if (experiencias.isEmpty()) {
            ToastUtils.showCustomToast(this, "Selecciona al menos una experiencia");
            return;
        }

        // Fechas como Timestamp
        Timestamp fechaInicio = etFechaInicio.getTag() instanceof Timestamp ? (Timestamp) etFechaInicio.getTag() : null;
        Timestamp fechaFin    = etFechaFin.getTag() instanceof Timestamp ? (Timestamp) etFechaFin.getTag() : null;

        // Map (dni será el ID del documento)
        Map<String, Object> socio = new HashMap<>();
        socio.put("nombre", nombre);
        socio.put("edad",  edad);
        socio.put("celular", celular);
        socio.put("sexo",   sexo);
        socio.put("fechaInicio", fechaInicio);
        socio.put("fechaFin",    fechaFin);
        socio.put("experiencia", experiencias); // <-- ARRAY en el mismo campo "experiencia"
        socio.put("estado", 0); // 0 = activo, 1 = inactivo

        // Guardar en Firestore
        db.collection("socios")
                .document(dni)
                .set(socio)
                .addOnSuccessListener(aVoid -> {
                    ToastUtils.showCustomToast(this, "Socio registrado correctamente");
                    finish(); // opcional
                })
                .addOnFailureListener(e ->
                        ToastUtils.showCustomToast(this, "Error: " + e.getMessage()));
    }
}
