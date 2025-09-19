package com.example.multiversofit;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
        tvNoCuenta.setOnClickListener(v -> showCustomToast("Comuníquese con recepción"));
    }

    private void doLogin() {
        String u = etUsuario.getText().toString().trim();
        String p = etContrasena.getText().toString();

        if (u.isEmpty() || p.isEmpty()) {
            showCustomToast("Completa usuario y contraseña");
            return;
        }

        SqliteUsuario db = SqliteUsuario.get(this);
        SqliteUsuario.User user = db.authenticate(u, p);

        if (user == null) {
            showCustomToast("Credenciales inválidas");
            return;
        }

        session.save(user.username, user.role, user.dni);
        goToRole(user.role);  // <-- sin paréntesis
        finish();
    }

    private void goToRole(String role) {
        Intent i = "ADMIN".equals(role)
                ? new Intent(this, AdminActivity.class)
                : new Intent(this, SocioActivity.class);
        startActivity(i);
    }

    // Toast personalizado (tuyo)
    private void showCustomToast(String msg) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, findViewById(android.R.id.content), false);
        TextView text = layout.findViewById(R.id.tvMessage);
        text.setText(msg);

        Toast toast = new Toast(this);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 120);
        toast.setView(layout);
        toast.show();
    }
}
