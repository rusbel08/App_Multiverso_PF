package com.example.multiversofit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsuario, etContrasena;
    private Button btnIngresar;
    private TextView tvNoCuenta;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etUsuario    = findViewById(R.id.etUsuario);
        etContrasena = findViewById(R.id.etContrasena);
        btnIngresar  = findViewById(R.id.btnIngresar);
        tvNoCuenta   = findViewById(R.id.tvNoCuenta);

        session = new SessionManager(this);
        session.clear();

        if (session.isLogged()) { // Si ya inició, salta directo
            goToRole(session.role());
            finish();
            return;
        }

        btnIngresar.setOnClickListener(v -> doLogin());
        tvNoCuenta.setOnClickListener(v -> ToastUtils.showCustomToast(this,"Comuníquese con recepción"));
    }

    private void doLogin() {
        String u = etUsuario.getText().toString().trim();
        String p = etContrasena.getText().toString().trim();

        if (u.isEmpty() || p.isEmpty()) {
            ToastUtils.showCustomToast(this, "Completa usuario y contraseña");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("usuarios")
                .whereEqualTo("username", u)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult().isEmpty()) {
                        ToastUtils.showCustomToast(this, "Credenciales inválidas");
                        limpiarCampos();
                        return;
                    }

                    QueryDocumentSnapshot doc = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);

                    String storedPass = doc.getString("pass");
                    if (storedPass == null || !storedPass.equals(p)) {
                        ToastUtils.showCustomToast(this, "Credenciales inválidas");
                        limpiarCampos();
                        return;
                    }

                    String userId = doc.getId();
                    String username = doc.getString("username");
                    String role = doc.getString("role");

                    // Si es SOCIO, validar estado
                    if ("SOCIO".equalsIgnoreCase(role)) {
                        db.collection("socios").document(userId)
                                .get()
                                .addOnSuccessListener(socioDoc -> {
                                    if (!socioDoc.exists()) {
                                        ToastUtils.showCustomToast(this, "No se encontró socio asociado");
                                        limpiarCampos();
                                        return;
                                    }

                                    Object estadoObj = socioDoc.get("estado");
                                    int estado = 0;
                                    if (estadoObj instanceof Number) {
                                        estado = ((Number) estadoObj).intValue();
                                    }

                                    if (estado == 1) {
                                        ToastUtils.showCustomToast(this, "Tu usuario está INACTIVO, comunícate con recepción");
                                        limpiarCampos();
                                        return;
                                    }

                                    // Guardar sesión y continuar
                                    session.save(username, role, userId);
                                    goToRole(role);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    ToastUtils.showCustomToast(this, "Error al validar socio: " + e.getMessage());
                                    limpiarCampos();
                                });

                    } else {
                        // Si NO es SOCIO, entra directo
                        session.save(username, role, userId);
                        goToRole(role);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    ToastUtils.showCustomToast(this, "Error al conectar: " + e.getMessage());
                    limpiarCampos();
                });
    }

    private void goToRole(String role) {
        Intent i = "ADMIN".equals(role)
                ? new Intent(this, AdminActivity.class)
                : new Intent(this, SocioActivity.class);
        startActivity(i);
    }

    private void limpiarCampos(){
        etUsuario.setText("");
        etContrasena.setText("");
    }

}
