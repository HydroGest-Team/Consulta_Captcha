package com.hydrogest.consulta.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EnmascaradorTest {

    @Test
    void nombreYApellido() {
        assertEquals("Juan P***", Enmascarador.enmascarar("Juan", "Perez"));
    }

    @Test
    void soloNombre() {
        assertEquals("J***", Enmascarador.enmascarar("Juan", null));
    }

    @Test
    void espaciosExtra() {
        assertEquals("Juan P***", Enmascarador.enmascarar("  Juan  ", "  perez "));
    }

    @Test
    void nombreVacio() {
        assertEquals("***", Enmascarador.enmascarar("", ""));
    }

    @Test
    void nombreCompuesto() {
        assertEquals("Maria L***", Enmascarador.enmascarar("Maria Jose", "Lopez"));
    }
}