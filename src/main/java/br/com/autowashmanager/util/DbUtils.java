/*
 * Classe utilitária para converter ResultSet em TableModel
 * Substitui a biblioteca rs2xml.jar
 */
package br.com.autowashmanager.util;

import javax.swing.table.DefaultTableModel;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Vector;

/**
 *
 * @author h24he
 */
public class DbUtils {
    
    /**
     * Converte um ResultSet em um DefaultTableModel para JTable
     * @param rs ResultSet com os dados da consulta SQL
     * @return DefaultTableModel pronto para ser usado em JTable
     */
    public static DefaultTableModel resultSetToTableModel(ResultSet rs) {
        try {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            // Obter nomes das colunas
            Vector<String> columnNames = new Vector<>();
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnName(i));
            }
            
            // Obter dados das linhas
            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getObject(i));
                }
                data.add(row);
            }
            
            return new DefaultTableModel(data, columnNames);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new DefaultTableModel();
        }
    }
}
