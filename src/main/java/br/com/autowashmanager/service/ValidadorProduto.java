/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.autowashmanager.service;

/**
 *
 * @author h24he
 */
public class ValidadorProduto {

    // valida preço (ex: 10 ou 10.50)
    public static boolean validarPreco(String preco) {
        return preco.matches("^\\d+(\\.\\d{1,2})?$");
    }

    // valida se é número inteiro positivo
    public static boolean validarNumeroPositivo(String valor) {
        try {
            int numero = Integer.parseInt(valor);
            return numero > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // valida texto (nome não pode ser vazio)
    public static boolean validarTexto(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    // regra de negócio: preço não pode ser negativo
    public static boolean precoMaiorQueZero(String preco) {
        try {
            double valor = Double.parseDouble(preco);
            return valor > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
