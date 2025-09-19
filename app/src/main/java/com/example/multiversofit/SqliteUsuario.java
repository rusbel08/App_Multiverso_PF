package com.example.multiversofit;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqliteUsuario extends SQLiteOpenHelper {

    private static final String DB_NAME = "multiversofit.db";
    private static final int DB_VERSION = 3; // ↑ subimos versión para limpiar tablas antiguas
    private static SqliteUsuario INSTANCE;

    public static synchronized SqliteUsuario get(Context ctx) {
        if (INSTANCE == null) INSTANCE = new SqliteUsuario(ctx.getApplicationContext());
        return INSTANCE;
    }

    private SqliteUsuario(Context context) { super(context, DB_NAME, null, DB_VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {
        // === SOLO USUARIOS (login) ===
        db.execSQL("CREATE TABLE IF NOT EXISTS usuarios(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL UNIQUE," +
                "pass TEXT NOT NULL," +
                "role TEXT NOT NULL," +      // 'ADMIN' | 'SOCIO'
                "dni TEXT)");

        // Seed
        db.execSQL("INSERT OR IGNORE INTO usuarios(username,pass,role,dni) VALUES" +
                "('admin','12345','ADMIN',NULL)," +
                "('socio1','12345','SOCIO','12345678')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        // Si cambias la versión de la DB, borramos la tabla usuarios y la recreamos
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        onCreate(db);
    }

    // ===== LOGIN =====
    public static class User {
        public long id;
        public String username;
        public String role;
        public String dni;
    }

    public User authenticate(String username, String pass) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id,username,role,dni FROM usuarios WHERE username=? AND pass=? LIMIT 1",
                new String[]{username, pass});
        try {
            if (c.moveToFirst()) {
                User u = new User();
                u.id = c.getLong(0);
                u.username = c.getString(1);
                u.role = c.getString(2);
                u.dni = c.getString(3);
                return u;
            }
            return null;
        } finally { c.close(); }
    }

}
