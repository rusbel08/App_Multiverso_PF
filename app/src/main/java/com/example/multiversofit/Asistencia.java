package com.example.multiversofit;

public class Asistencia {
    private long id;
    private String nombre;
    private String fecha;
    private String dni;

    public Asistencia(long id, String nombre,String dni, String fecha) {
        this.id = id;
        this.nombre = nombre;
        this.fecha = fecha;
        this.dni = dni;
    }

    public long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getFecha() { return fecha; }
    public String getDni(){return dni;}
}
