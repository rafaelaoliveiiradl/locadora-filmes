package com.locadora.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Métodos para ler dados digitados no terminal, repetindo a pergunta
 * quando o usuário digita algo inválido.
 */
public class Entrada {

    private static final Scanner scanner = new Scanner(System.in);

    public static String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return scanner.nextLine().trim();
    }

    public static int lerInt(String mensagem) {
        while (true) {
            try {
                return Integer.parseInt(lerTexto(mensagem));
            } catch (NumberFormatException e) {
                System.out.println("Digite um número inteiro válido.");
            }
        }
    }

    public static LocalDate lerData(String mensagem) {
        while (true) {
            try {
                return LocalDate.parse(lerTexto(mensagem), Formatador.FORMATO_DATA);
            } catch (DateTimeParseException e) {
                System.out.println("Data inválida. Use o formato dd/MM/aaaa.");
            }
        }
    }

    /** Se o usuário apenas apertar Enter, usa a data de hoje. */
    public static LocalDate lerDataOuHoje(String mensagem) {
        while (true) {
            String texto = lerTexto(mensagem + " (Enter = hoje): ");
            if (texto.isEmpty()) {
                return LocalDate.now();
            }
            try {
                return LocalDate.parse(texto, Formatador.FORMATO_DATA);
            } catch (DateTimeParseException e) {
                System.out.println("Data inválida. Use o formato dd/MM/aaaa.");
            }
        }
    }
}
