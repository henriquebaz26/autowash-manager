package br.com.autowashmanager.dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Conexão com o banco SQLite
 * 
 * @author h24he
 * @version 1.1
 */
public class ModuloConexao {

    // Método responsável por estabelecer a conexão com o banco
    public static Connection conector() {
        Connection conexao = null;

        // Caminho do banco SQLite
        String url = "jdbc:sqlite:database/autowash.db";

        try {
            // Cria a conexão com o arquivo .db
            conexao = DriverManager.getConnection(url);
            System.out.println("Conexão estabelecida com sucesso!");
            return conexao;
        } catch (SQLException e) {
            System.err.println("Erro ao conectar no banco: " + e.getMessage());
            return null;
        }
    }
}
