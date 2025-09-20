package com.example.multiversofit;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.admin), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment f = null;
            int id = item.getItemId();

            if (id == R.id.nav_clientes) {
                f = new SociosFragment();                 // pestaña "Socios"
            } else if (id == R.id.nav_consulta) {
                f = new ConsultaSociosFragment();         // pestaña "Consulta" (listado/buscar/editar/baja)
            } /*else if (id == R.id.nav_qr) {
                f = new QrFragment();
            }*/

            if (f == null) return false;

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragment_container, f)
                    .commit();
            return true;
        });

        // Selecciona la pestaña inicial
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_clientes);
        }
    }
}
