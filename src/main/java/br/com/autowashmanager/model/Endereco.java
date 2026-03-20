/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.autowashmanager.model;

/**
 *
 * @author h24he
 */
public class Endereco {

    private String rua;
    private String bairro;
    private String cidade;
    private String estado;

    public Endereco(String rua, String bairro, String cidade, String estado) {
        this.rua = rua;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
    }

    public String getRua() {
        return rua;
    };

    public String getBairro() {
        return bairro;
    };

    public String getCidade() {
        return cidade;
    };

    public String getEstado() {
        return estado;
    };
}
