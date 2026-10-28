package com.hydrogest.consulta.service;

public final class Enmascarador {

    private Enmascarador() { }

    // "Juan" + "Perez" -> "Juan P***" ; solo nombre -> "J***" ; vacio -> "***"
    public static String enmascarar(String nombre, String apellido) {
        String n = nombre == null ? "" : nombre.trim();
        String a = apellido == null ? "" : apellido.trim();
        if (n.isEmpty()) {
            return "***";
        }
        String primera = n.split("\\s+")[0];
        if (a.isEmpty()) {
            return Character.toUpperCase(primera.charAt(0)) + "***";
        }
        return primera + " " + Character.toUpperCase(a.charAt(0)) + "***";
    }
}