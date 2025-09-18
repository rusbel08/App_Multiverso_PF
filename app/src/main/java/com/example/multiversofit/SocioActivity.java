package com.example.multiversofit;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class SocioActivity extends AppCompatActivity {

    private TextView tvDni, tvEstadoMembresia, tvFinMembresia;
    private ImageView imgQr;
    private Button btnAsistencias;

    String dniSocio, estadoMembresia, finMembresia;

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
        imgQr = findViewById(R.id.imgQr);
        btnAsistencias = findViewById(R.id.btnAsistencias);

        String dniSocio = getIntent().getStringExtra("dni");
        String estadoMembresia = getIntent().getStringExtra("estado");
        String finMembresia = getIntent().getStringExtra("fin");

        tvDni.setText("DNI: " + dniSocio);
        tvEstadoMembresia.setText("Estado: " + estadoMembresia);
        tvFinMembresia.setText("Vence: " + finMembresia);

        generarQR("SOCIO-" + dniSocio);



        // Botón ver asistencias
        Button btnAsistencias = findViewById(R.id.btnAsistencias);
        btnAsistencias.setOnClickListener(v -> {
            Intent intent = new Intent(this, AsistenciasActivity.class);
            startActivity(intent);
        });
    }

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