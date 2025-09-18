package com.example.multiversofit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ConexionBD extends SQLiteOpenHelper {

    private static final String DB_NAME = "multiversofit.db";
    private static final int DB_VERSION = 1;
    private static ConexionBD INSTANCE;

    public static synchronized ConexionBD get(Context ctx) {
        if (INSTANCE == null) INSTANCE = new ConexionBD(ctx.getApplicationContext());
        return INSTANCE;
    }

    private ConexionBD(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // ===== TABLA USUARIOS (para login) =====
        db.execSQL("CREATE TABLE IF NOT EXISTS usuarios(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL UNIQUE," +
                "pass TEXT NOT NULL," +
                "role TEXT NOT NULL," +      // 'ADMIN' o 'SOCIO'
                "dni TEXT)");

        // ===== TABLA CLIENTES =====
        db.execSQL("CREATE TABLE IF NOT EXISTS clientes(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "dni TEXT NOT NULL UNIQUE," +
                "nombres TEXT," +
                "activa INTEGER NOT NULL DEFAULT 1," +
                "inicioMillis INTEGER," +
                "finMillis INTEGER)");

        // ===== TABLA ASISTENCIAS =====
        db.execSQL("CREATE TABLE IF NOT EXISTS asistencias(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "clienteId INTEGER NOT NULL," +
                "fechaMillis INTEGER NOT NULL," +
                "origen TEXT," +
                "FOREIGN KEY(clienteId) REFERENCES clientes(id) ON DELETE CASCADE)");

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_asist_cliente ON asistencias(clienteId)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_asist_fecha ON asistencias(fechaMillis)");

        // Semilla inicial: un admin y un socio
        seedIfEmpty(db);
    }

    private void seedIfEmpty(SQLiteDatabase db) {
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM usuarios", null);
        try {
            if (c.moveToFirst() && c.getInt(0) == 0) {
                db.execSQL("INSERT INTO usuarios(username,pass,role,dni) VALUES" +
                        "('admin','123456','ADMIN',NULL)," +
                        "('socio1','123456','SOCIO','12345678')");
            }
        } finally {
            c.close();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        // Aquí manejas migraciones si cambias el esquema
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
        } finally {
            c.close();
        }
    }

    // ===== CRUD CLIENTES =====
    public long insertarCliente(String dni, String nombres, boolean activa,
                                long inicioMillis, long finMillis) {
        ContentValues v = new ContentValues();
        v.put("dni", dni);
        v.put("nombres", nombres);
        v.put("activa", activa ? 1 : 0);
        v.put("inicioMillis", inicioMillis);
        v.put("finMillis", finMillis);
        return getWritableDatabase().insert("clientes", null, v);
    }

    public int actualizarCliente(long id, String nombres, boolean activa,
                                 long inicioMillis, long finMillis) {
        ContentValues v = new ContentValues();
        v.put("nombres", nombres);
        v.put("activa", activa ? 1 : 0);
        v.put("inicioMillis", inicioMillis);
        v.put("finMillis", finMillis);
        return getWritableDatabase().update("clientes", v, "id=?",
                new String[]{String.valueOf(id)});
    }

    public int darBajaCliente(long id) {
        ContentValues v = new ContentValues();
        v.put("activa", 0);
        return getWritableDatabase().update("clientes", v, "id=?",
                new String[]{String.valueOf(id)});
    }

    public Cursor clientePorDni(String dni) {
        return getReadableDatabase().rawQuery(
                "SELECT id,dni,nombres,activa,inicioMillis,finMillis FROM clientes WHERE dni=? LIMIT 1",
                new String[]{dni});
    }

    // ===== ASISTENCIAS =====
    public long insertarAsistencia(long clienteId, long fechaMillis, String origen) {
        ContentValues v = new ContentValues();
        v.put("clienteId", clienteId);
        v.put("fechaMillis", fechaMillis);
        v.put("origen", origen);
        return getWritableDatabase().insert("asistencias", null, v);
    }
}
