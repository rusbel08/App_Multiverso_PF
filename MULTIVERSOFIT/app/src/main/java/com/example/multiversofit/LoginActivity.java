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
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    // false = MOCK (usuarios en duro), true = SQLite (tabla usuarios en ConexionBD)
    private static final boolean USE_SQLITE = false;

    // Controles
    private EditText etUsuario, etContrasena;
    private Button btnIngresar;
    private TextView tvNoCuenta;

    private SessionManager session;

    // Usuarios MOCK
    private final Map<String, Cred> MOCK_USERS = new HashMap<String, Cred>() {{
        put("admin",  new Cred("123456", "ADMIN", null));
        put("socio1", new Cred("123456", "SOCIO", "12345678"));
    }};
    private static class Cred {
        String pass, role, dni;
        Cred(String pass, String role, String dni){ this.pass = pass; this.role = role; this.dni = dni; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Comportamiento de insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Vincular controles
        etUsuario    = findViewById(R.id.etUsuario);
        etContrasena = findViewById(R.id.etContrasena);
        btnIngresar  = findViewById(R.id.btnIngresar);
        tvNoCuenta   = findViewById(R.id.tvNoCuenta);

        // Sesión
        session = new SessionManager(this);
        session.clear();

        // Si ya hay sesión, redirige directo según rol
        if (session.isLogged()) {
            goToRole(session.role());
            finish();
            return;
        }

        // Listeners
        btnIngresar.setOnClickListener(v -> doLogin());
        tvNoCuenta.setOnClickListener(v ->
                Toast.makeText(this, "Función de registro aún no implementada", Toast.LENGTH_SHORT).show()
        );
    }

    // Método encargado de procesar el login y aplicar las validaciones necesarias
    private void doLogin() {
        // 1) Obtenemos los valores ingresados en los campos de usuario y contraseña
        String u = etUsuario.getText().toString().trim();
        String p = etContrasena.getText().toString();

        // 2) Validación básica: que no estén vacíos
        if (u.isEmpty() || p.isEmpty()) {
            toast("Completa usuario y contraseña");
            return;
        }

        // 3) Validamos según el modo configurado
        if (!USE_SQLITE) {
            // --- MODO MOCK: validación en memoria con usuarios de prueba ---
            Cred c = MOCK_USERS.get(u);
            if (c != null && c.pass.equals(p)) {
                // Si existe y la contraseña coincide, guardamos sesión y redirigimos
                session.save(u, c.role, c.dni);
                goToRole(c.role);
                finish();
            } else {
                // Usuario/contraseña incorrectos
                toast("Credenciales inválidas");
            }
        } else {
            // --- MODO SQLITE: validación contra la base de datos ---
            ConexionBD db = ConexionBD.get(this);
            ConexionBD.User user = db.authenticate(u, p);
            if (user == null) {
                // No existe el usuario en la BD o credenciales incorrectas
                toast("Credenciales inválidas");
                return;
            }
            // Guardamos la sesión con los datos obtenidos desde la BD
            session.save(user.username, user.role, user.dni);
            goToRole(user.role);
            finish();
        }
    }


    private void goToRole(String role) {
        Intent i = "ADMIN".equals(role)
                ? new Intent(this, AdminActivity.class)
                : new Intent(this, SocioActivity.class);
        startActivity(i);
    }

    private void toast(String s) {
        showCustomToast(s);
    }

    // Metodo para mostrar un Toast personalizado.
    private void showCustomToast(String msg) {
        LayoutInflater inflater = getLayoutInflater();

        View layout = inflater.inflate(R.layout.custom_toast, findViewById(android.R.id.content), false);
        TextView text = layout.findViewById(R.id.tvMessage);
        text.setText(msg);

        Toast toast = new Toast(this);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 120); // posición
        toast.setView(layout);
        toast.show();
    }


}
