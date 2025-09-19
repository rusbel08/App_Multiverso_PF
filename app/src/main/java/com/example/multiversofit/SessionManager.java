package com.example.multiversofit;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF = "session_multiversofit";
    private static final String K_USERNAME = "u";
    private static final String K_ROLE = "r";   // "ADMIN" | "SOCIO"
    private static final String K_DNI = "dni";

    private final SharedPreferences sp;

    public SessionManager(Context ctx) {
        sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    /** Guarda la sesión del usuario logueado */
    public void save(String username, String role, String dni) {
        sp.edit()
                .putString(K_USERNAME, username)
                .putString(K_ROLE, role)
                .putString(K_DNI, dni)
                .apply();
    }

    /** ¿Hay sesión activa? */
    public boolean isLogged() {
        return sp.contains(K_ROLE);
    }

    /** Rol actual ("ADMIN" o "SOCIO") */
    public String role() {
        return sp.getString(K_ROLE, "");
    }

    /** Usuario actual */
    public String username() {
        return sp.getString(K_USERNAME, "");
    }

    /** DNI guardado (útil para SOCIO) */
    public String dni() {
        return sp.getString(K_DNI, null);
    }

    /** Cerrar sesión */
    public void clear() {
        sp.edit().clear().apply();
    }
}
