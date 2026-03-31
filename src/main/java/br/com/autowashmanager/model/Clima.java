/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.autowashmanager.model;

/**
 *
 * @author h24he
 */
public class Clima {

    private String cidade;
    private double temperatura;
    private String descricao;

    public Clima(String cidade, double temperatura, String descricao) {
        this.cidade = cidade;
        this.temperatura = temperatura;
        this.descricao = descricao;
    }

    public String getCidade() {
        return cidade;
    }

    public double getTemperatura() {
        return temperatura;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return "Cidade: " + cidade
                + " | Temperatura: " + temperatura + "°C"
                + " | Clima: " + descricao;
    }
}
