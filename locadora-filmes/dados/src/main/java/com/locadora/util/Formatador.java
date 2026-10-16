package com.locadora.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Métodos auxiliares para formatar datas e valores em dinheiro.
 */
public class Formatador {

    public static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static String data(LocalDate data) {
        if (data == null) {
            return "-";
        }
        return data.format(FORMATO_DATA);
    }

    public static String moeda(double valor) {
        return String.format("R$ %.2f", valor);
    }
}
