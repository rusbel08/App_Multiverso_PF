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
        String p = etContrasena.getText().toString();

        if (u.isEmpty() || p.isEmpty()) {
            ToastUtils.showCustomToast(this,"Completa usuario y contraseña");
            return;
        }

        SqliteUsuario db = SqliteUsuario.get(this);
        SqliteUsuario.User user = db.authenticate(u, p);

        if (user == null) {
            ToastUtils.showCustomToast(this,"Credenciales inválidas");
            return;
        }

        session.save(user.username, user.role, user.dni);
        goToRole(user.role);
        finish();
    }

    private void goToRole(String role) {
        Intent i = "ADMIN".equals(role)
                ? new Intent(this, AdminActivity.class)
                : new Intent(this, SocioActivity.class);
        startActivity(i);
    }

}
