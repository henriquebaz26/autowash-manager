/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.autowashmanager.test;

import br.com.autowashmanager.dal.ModuloConexao;
import java.sql.Connection;

/**
 *
 * @author h24he
 */
public class AutoWashManager {
    
    public static void main(String[] args) {
        Connection conn = ModuloConexao.conector();
        if (conn != null) {
            System.out.println("Conexão funcionando!");
        }
    }
    
}
