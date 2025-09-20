package com.example.multiversofit;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditarMiembroActivity extends AppCompatActivity {

    private EditText etDni, etNombre, etEdad, etCelular, etFechaInicio, etFechaVencimiento;
    private RadioGroup rgSexo;
    private RadioButton rbMasculino, rbFemenino;
    private Switch swEstado;
    private Button btnActualizar;

    private FirebaseFirestore db;
    private String dni;

    // Para reglas
    private int estadoOriginal = 0; // 0=activo, 1=inactivo
    private Timestamp fiOriginal, fvOriginal;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_miembro);

        // Referencias UI (ids exactamente como en tu XML)
        etDni = findViewById(R.id.etDni);
        etNombre = findViewById(R.id.etNombre);
        etEdad = findViewById(R.id.etEdad);
        rgSexo = findViewById(R.id.rgSexo);
        rbMasculino = findViewById(R.id.rbMasculino);
        rbFemenino = findViewById(R.id.rbFemenino);
        etCelular = findViewById(R.id.etCelular);
        etFechaInicio = findViewById(R.id.etFechaInicio);
        etFechaVencimiento = findViewById(R.id.etFechaVencimiento);
        swEstado = findViewById(R.id.swEstado);
        btnActualizar = findViewById(R.id.btnActualizar);

        db = FirebaseFirestore.getInstance();

        // DNI desde el intent
        dni = getIntent().getStringExtra("dni");
        if (dni == null || dni.trim().isEmpty()) {
            ToastUtils.showCustomToast(this, "DNI no recibido");
            finish();
            return;
        }

        etDni.setText(dni);
        etDni.setEnabled(false); // DNI ineditable

        // DatePickers (solo si están habilitados)
        etFechaInicio.setOnClickListener(v -> { if (etFechaInicio.isEnabled()) mostrarCalendario(etFechaInicio); });
        etFechaVencimiento.setOnClickListener(v -> { if (etFechaVencimiento.isEnabled()) mostrarCalendario(etFechaVencimiento); });

        // Precargar datos
        cargarSocio(dni);

        // Guardar cambios
        btnActualizar.setOnClickListener(v -> actualizarSocio());
    }

    private void cargarSocio(String dni) {
        db.collection("socios").document(dni).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        ToastUtils.showCustomToast(this, "No existe el socio");
                        finish();
                        return;
                    }

                    etNombre.setText(doc.getString("nombre"));

                    Object edadObj = doc.get("edad");
                    etEdad.setText(edadObj != null ? String.valueOf(edadObj) : "");

                    etCelular.setText(doc.getString("celular"));

                    String sexo = doc.getString("sexo");
                    if ("Masculino".equalsIgnoreCase(sexo)) rbMasculino.setChecked(true);
                    else if ("Femenino".equalsIgnoreCase(sexo)) rbFemenino.setChecked(true);
                    else rgSexo.clearCheck();

                    // Estado original (0=activo, 1=inactivo)
                    Number estN = (Number) doc.get("estado");
                    estadoOriginal = estN != null ? estN.intValue() : 0;
                    swEstado.setChecked(estadoOriginal == 0);
                    // Switch solo habilitado si estaba inactivo
                    swEstado.setEnabled(estadoOriginal == 1);

                    // Fechas (acepta fechaFin o fechaVencimiento)
                    fiOriginal = doc.getTimestamp("fechaInicio");
                    Timestamp fin = doc.getTimestamp("fechaFin");
                    if (fin == null) fin = doc.getTimestamp("fechaVencimiento");
                    fvOriginal = fin;

                    if (fiOriginal != null) {
                        etFechaInicio.setText(sdf.format(fiOriginal.toDate()));
                        etFechaInicio.setTag(fiOriginal);
                    } else {
                        etFechaInicio.setText("");
                        etFechaInicio.setTag(null);
                    }

                    if (fvOriginal != null) {
                        etFechaVencimiento.setText(sdf.format(fvOriginal.toDate()));
                        etFechaVencimiento.setTag(fvOriginal);
                    } else {
                        etFechaVencimiento.setText("");
                        etFechaVencimiento.setTag(null);
                    }

                    // Fechas solo editables si estaba inactivo
                    setFechasEnabled(estadoOriginal == 1);
                })
                .addOnFailureListener(e ->
                        ToastUtils.showCustomToast(this, "Error al cargar: " + e.getMessage()));
    }

    private void actualizarSocio() {
        String nombre = etNombre.getText().toString().trim();
        String edad = etEdad.getText().toString().trim();
        String celular = etCelular.getText().toString().trim();

        if (nombre.isEmpty()) {
            ToastUtils.showCustomToast(this, "Ingresa el nombre");
            return;
        }

        // Sexo
        String sexo = "";
        int sel = rgSexo.getCheckedRadioButtonId();
        if (sel == R.id.rbMasculino) sexo = "Masculino";
        else if (sel == R.id.rbFemenino) sexo = "Femenino";

        // Estado final: solo cambia si originalmente estaba inactivo
        int estadoFinal = estadoOriginal;
        if (swEstado.isEnabled()) {
            estadoFinal = swEstado.isChecked() ? 0 : 1;
        }

        // Fechas: si estaba activo, conserva las originales; si estaba inactivo, toma las nuevas
        Timestamp fi = (Timestamp) etFechaInicio.getTag();
        Timestamp fv = (Timestamp) etFechaVencimiento.getTag();
        if (estadoOriginal == 0) {
            fi = fiOriginal;
            fv = fvOriginal;
        }

        Map<String, Object> cambios = new HashMap<>();
        cambios.put("nombre", nombre);
        cambios.put("edad", edad);
        cambios.put("celular", celular);
        cambios.put("sexo", sexo);
        cambios.put("estado", estadoFinal);
        cambios.put("fechaInicio", fi);
        cambios.put("fechaFin", fv); // usamos "fechaFin" como clave canónica

        db.collection("socios").document(dni)
                .set(cambios, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    ToastUtils.showCustomToast(this, "Socio actualizado");
                    finish();
                })
                .addOnFailureListener(e ->
                        ToastUtils.showCustomToast(this, "Error al actualizar: " + e.getMessage()));
    }

    private void setFechasEnabled(boolean enabled) {
        etFechaInicio.setEnabled(enabled);
        etFechaVencimiento.setEnabled(enabled);
        etFechaInicio.setAlpha(enabled ? 1f : 0.6f);
        etFechaVencimiento.setAlpha(enabled ? 1f : 0.6f);
    }

    private void mostrarCalendario(EditText campo) {
        final Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR), m = c.get(Calendar.MONTH), d = c.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, yy, mm, dd) -> {
            Calendar sel = Calendar.getInstance();
            sel.set(yy, mm, dd);
            campo.setText(sdf.format(sel.getTime()));
            campo.setTag(new Timestamp(sel.getTime()));
        }, y, m, d).show();
    }
}
