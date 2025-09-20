package com.example.multiversofit;

public class Socio {
    public String dni;
    public String nombre;
    public int estado; // 0 = Activo, 1 = Inactivo

    public Socio() {}
    public Socio(String dni, String nombre, int estado) {
        this.dni = dni;
        this.nombre = nombre;
        this.estado = estado;
    }
}
