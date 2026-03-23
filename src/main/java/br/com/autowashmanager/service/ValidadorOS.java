package br.com.autowashmanager.service;

import java.time.LocalDate;
import java.time.LocalTime;

public class ValidadorOS {

    public static boolean validarHorario(String horario) {
        return horario.matches("^([01]\\d|2[0-3]):([0-5]\\d)$");
    }

    public static boolean validarPreco(String preco) {
        return preco.matches("^\\d+(\\.\\d{1,2})?$");
    }

    public static boolean validarDataFormato(String data) {
        return data.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }

    public static boolean validarDataReal(String data) {
        try {
            LocalDate.parse(data);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validarOrdemHorario(String entrada, String entrega) {
        try {
            LocalTime hEntrada = LocalTime.parse(entrada);
            LocalTime hEntrega = LocalTime.parse(entrega);
            return hEntrega.isAfter(hEntrada);
        } catch (Exception e) {
            return false;
        }
    }
}
