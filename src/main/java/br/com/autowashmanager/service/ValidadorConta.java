package br.com.autowashmanager.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ValidadorConta {

    // valida texto
    public static boolean validarTexto(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    // valida valor (ex: 100 ou 100.50)
    public static boolean validarValor(String valor) {
        return valor.matches("^\\d+(\\.\\d{1,2})?$");
    }

    // valor maior que zero
    public static boolean valorMaiorQueZero(String valor) {
        try {
            double v = Double.parseDouble(valor);
            return v > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // valida data (yyyy-MM-dd)
    public static boolean validarData(String data) {
        try {
            LocalDate.parse(data, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
