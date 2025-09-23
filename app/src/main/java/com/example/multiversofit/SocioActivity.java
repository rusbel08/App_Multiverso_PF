package com.example.multiversofit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SocioActivity extends AppCompatActivity {

    private TextView tvDni, tvEstadoMembresia, tvFinMembresia, tvNombre;
    private ImageView imgQr;
    private Button btnAsistencias;

    private FirebaseFirestore db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_socio);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.socio), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvDni = findViewById(R.id.tvDni);
        tvEstadoMembresia = findViewById(R.id.tvEstadoMembresia);
        tvFinMembresia = findViewById(R.id.tvFinMembresia);
        tvNombre = findViewById(R.id.tvNombre);
        imgQr = findViewById(R.id.imgQr);
        btnAsistencias = findViewById(R.id.btnAsistencias);

        db = FirebaseFirestore.getInstance();
        session = new SessionManager(this);

        // El DNI del socio logeado es el idDocumento
        String dniSocio = session.dni();

        cargarDatosSocio(dniSocio);

        // Botón ver asistencias
        btnAsistencias.setOnClickListener(v -> {
            Intent intent = new Intent(this, AsistenciasActivity.class);
            startActivity(intent);
        });

        //Boton para salir / cerrar sesión
        ImageView btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            session.clear(); // opcional si quieres cerrar sesión
            Intent intent = new Intent(SocioActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void cargarDatosSocio(String dniSocio) {
        if (dniSocio == null || dniSocio.isEmpty()) {
            tvEstadoMembresia.setText("Error: No se encontró DNI en sesión");
            return;
        }

        DocumentReference docRef = db.collection("socios").document(dniSocio);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String nombre = documentSnapshot.getString("nombre");

                tvDni.setText("DNI: " + dniSocio);
                tvNombre.setText(nombre != null ? nombre : "Sin nombre");

                Date fechaFinDate = null;

                Timestamp ts = documentSnapshot.getTimestamp("fechaFin");
                if (ts != null) {
                    fechaFinDate = ts.toDate();
                } else {
                    //(formato dd/MM/yyyy)
                    String fechaFinStr = documentSnapshot.getString("fechaFin");
                    if (fechaFinStr != null) {
                        try {
                            fechaFinDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fechaFinStr);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (fechaFinDate != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String fechaStr = sdf.format(fechaFinDate);

                    tvFinMembresia.setText("Vence: " + fechaStr);

                    boolean activa = new Date().before(fechaFinDate);

                    // Cambiar colores según estado
                    ImageView iconMembership = findViewById(R.id.ic_membership_icon);

                    if (activa) {
                        tvEstadoMembresia.setText("Membresía: Activa");
                        tvEstadoMembresia.setTextColor(getResources().getColor(android.R.color.holo_green_light));

                        if (iconMembership != null) {
                            iconMembership.setColorFilter(getResources().getColor(android.R.color.holo_green_light));
                        }
                    } else {
                        tvEstadoMembresia.setText("Membresía: Vencida");
                        tvEstadoMembresia.setTextColor(getResources().getColor(android.R.color.holo_red_light));

                        if (iconMembership != null) {
                            iconMembership.setColorFilter(getResources().getColor(android.R.color.holo_red_light));
                        }
                    }

                } else {
                    tvFinMembresia.setText("Vence: -");
                    tvEstadoMembresia.setText("Membresía: -");
                }

                // Generar QR
                generarQR("SOCIO-" + dniSocio);

            } else {
                tvEstadoMembresia.setText("No existe el socio en la BD");
            }
        }).addOnFailureListener(e -> {
            tvEstadoMembresia.setText("Error al cargar datos: " + e.getMessage());
        });
    }

    //Metodo para generar QR
    private void generarQR(String contenido) {
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = writer.encode(contenido, BarcodeFormat.QR_CODE, 600, 600);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(bitMatrix);
            imgQr.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}
