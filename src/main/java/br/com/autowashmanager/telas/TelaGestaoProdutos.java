/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package br.com.autowashmanager.telas;

import br.com.autowashmanager.dal.ModuloConexao;
import br.com.autowashmanager.service.ValidadorProduto;
import br.com.autowashmanager.util.DbUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author h24he
 */
public class TelaGestaoProdutos extends javax.swing.JFrame {

    Connection conexao = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(TelaGestaoProdutos.class.getName());

    /**
     * Creates new form TelaGestaoProdutos
     */
    public TelaGestaoProdutos() {
        initComponents();
        conexao = ModuloConexao.conector();
    }

    private void adicionar() {
        String sql = "insert into product (name, price, stock, min_stock) values (?, ?, ?, ?)";

        try {
            String nome = txtProdutoNome.getText().trim();
            String preco = txtProdutoPreco.getText().trim();
            String estoque = txtProdutoEstoque.getText().trim();
            String estoqueMin = txtProdutoEstoqueMinimo.getText().trim();

            // CAMPOS OBRIGATÓRIOS
            if (!ValidadorProduto.validarTexto(nome)
                    || !ValidadorProduto.validarTexto(preco)
                    || !ValidadorProduto.validarTexto(estoque)
                    || !ValidadorProduto.validarTexto(estoqueMin)) {

                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios");
                return;
            }

            // PREÇO - FORMATO
            if (!ValidadorProduto.validarPreco(preco)) {
                JOptionPane.showMessageDialog(null, "Preço inválido (ex: 100 ou 100.50)");
                return;
            }

            // PREÇO - VALOR
            if (!ValidadorProduto.precoMaiorQueZero(preco)) {
                JOptionPane.showMessageDialog(null, "Preço deve ser maior que zero");
                return;
            }

            // ESTOQUE
            if (!ValidadorProduto.validarNumeroPositivo(estoque)) {
                JOptionPane.showMessageDialog(null, "Estoque deve ser um número inteiro maior que zero");
                return;
            }

            // ESTOQUE MÍNIMO
            if (!ValidadorProduto.validarNumeroPositivo(estoqueMin)) {
                JOptionPane.showMessageDialog(null, "Estoque mínimo deve ser maior que zero");
                return;
            }

            // CONVERSÃO
            int estoqueInt = Integer.parseInt(estoque);
            int estoqueMinInt = Integer.parseInt(estoqueMin);

            // REGRA DE NEGÓCIO (opcional, mas recomendado)
            if (estoqueMinInt > estoqueInt) {
                JOptionPane.showMessageDialog(null, "Estoque mínimo não pode ser maior que o estoque atual");
                return;
            }

            // INSERT
            pst = conexao.prepareStatement(sql);
            pst.setString(1, nome);
            pst.setString(2, preco);
            pst.setInt(3, estoqueInt);
            pst.setInt(4, estoqueMinInt);

            int adicionado = pst.executeUpdate();

            if (adicionado > 0) {
                JOptionPane.showMessageDialog(null, "Produto cadastrado com sucesso");
                limpar();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    // metodo para pesquisar produto
    private void pesquisar_produto() {
        String sql = "select id as id, name as nome, price as preco, stock as estoque, min_stock as estoque_minimo from product where name like ?";

        try {
            pst = conexao.prepareStatement(sql);

            // passando o conteúdo da caixa de pesquisa
            pst.setString(1, txtProdutoPesquisar.getText() + "%");

            rs = pst.executeQuery();

            // preenchendo a tabela
            tblProdutos.setModel(DbUtils.resultSetToTableModel(rs));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    // metodo para limpar os campos do formulário
    private void limpar() {
        txtProdutoId.setText(null);
        txtProdutoNome.setText(null);
        txtProdutoPreco.setText(null);
        txtProdutoEstoque.setText(null);
        txtProdutoEstoqueMinimo.setText(null);
        txtProdutoPesquisar.setText(null);

        ((DefaultTableModel) tblProdutos.getModel()).setRowCount(0);

        btnProdutoUpdate.setEnabled(false);
    }

    // metodo para alterar dados de um produto
    private void alterar() {
        String sql = "update product set name=?, price=?, stock=?, min_stock=? where id=?";

        try {
            String id = txtProdutoId.getText().trim();
            String nome = txtProdutoNome.getText().trim();
            String preco = txtProdutoPreco.getText().trim();
            String estoque = txtProdutoEstoque.getText().trim();
            String estoqueMin = txtProdutoEstoqueMinimo.getText().trim();

            // CAMPOS OBRIGATÓRIOS
            if (id.isEmpty()
                    || !ValidadorProduto.validarTexto(nome)
                    || !ValidadorProduto.validarTexto(preco)
                    || !ValidadorProduto.validarTexto(estoque)
                    || !ValidadorProduto.validarTexto(estoqueMin)) {

                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios");
                return;
            }

            // ID NUMÉRICO
            int produtoId;
            try {
                produtoId = Integer.parseInt(id);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "ID inválido");
                return;
            }

            // PREÇO - FORMATO
            if (!ValidadorProduto.validarPreco(preco)) {
                JOptionPane.showMessageDialog(null, "Preço inválido (ex: 100 ou 100.50)");
                return;
            }

            // PREÇO - VALOR
            if (!ValidadorProduto.precoMaiorQueZero(preco)) {
                JOptionPane.showMessageDialog(null, "Preço deve ser maior que zero");
                return;
            }

            // ESTOQUE
            if (!ValidadorProduto.validarNumeroPositivo(estoque)) {
                JOptionPane.showMessageDialog(null, "Estoque deve ser maior que zero");
                return;
            }

            // ESTOQUE MÍNIMO
            if (!ValidadorProduto.validarNumeroPositivo(estoqueMin)) {
                JOptionPane.showMessageDialog(null, "Estoque mínimo deve ser maior que zero");
                return;
            }

            int estoqueInt = Integer.parseInt(estoque);
            int estoqueMinInt = Integer.parseInt(estoqueMin);

            // REGRA DE NEGÓCIO
            if (estoqueMinInt > estoqueInt) {
                JOptionPane.showMessageDialog(null, "Estoque mínimo não pode ser maior que o estoque atual");
                return;
            }

            // (OPCIONAL) VALIDAR SE PRODUTO EXISTE
            if (!produtoExiste(produtoId)) {
                JOptionPane.showMessageDialog(null, "Produto não encontrado");
                return;
            }

            // UPDATE
            pst = conexao.prepareStatement(sql);
            pst.setString(1, nome);
            pst.setString(2, preco);
            pst.setInt(3, estoqueInt);
            pst.setInt(4, estoqueMinInt);
            pst.setInt(5, produtoId);

            int atualizado = pst.executeUpdate();

            if (atualizado > 0) {
                JOptionPane.showMessageDialog(null, "Produto atualizado com sucesso");
                limpar();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private boolean produtoExiste(int id) {
        String sql = "select id from product where id = ?";

        try {
            pst = conexao.prepareStatement(sql);
            pst.setInt(1, id);
            rs = pst.executeQuery();
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }

    // método responsável pela remoção de uma produto
    private void remover() {
        // VALIDAÇÃO: verificar se algum produto foi selecionado
        if (txtProdutoId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Selecione um produto para excluir");
            return;
        }

        //a estrutura abaixo confirma a remoção de um produto
        int confirma = JOptionPane.showConfirmDialog(null, "Tem certeza que deseja remover este produto?", "ATENÇÃO", JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_OPTION) {
            String sql = "delete from product where id=?";
            try {
                pst = conexao.prepareStatement(sql);
                pst.setString(1, txtProdutoId.getText());
                int apagado = pst.executeUpdate();

                if (apagado > 0) {
                    JOptionPane.showMessageDialog(null, "Produto removido com sucesso");
                    limpar();

                    btnProdutoCreate.setEnabled(true);
                    btnProdutoUpdate.setEnabled(false);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    private void setar_campos() {

        int setar = tblProdutos.getSelectedRow();

        txtProdutoId.setText(tblProdutos.getModel().getValueAt(setar, 0).toString());
        txtProdutoNome.setText(tblProdutos.getModel().getValueAt(setar, 1).toString());
        txtProdutoPreco.setText(tblProdutos.getModel().getValueAt(setar, 2).toString());
        txtProdutoEstoque.setText(tblProdutos.getModel().getValueAt(setar, 3).toString());
        txtProdutoEstoqueMinimo.setText(tblProdutos.getModel().getValueAt(setar, 4).toString());
        
        btnProdutoCreate.setEnabled(false);
        btnProdutoUpdate.setEnabled(true);

    }
    
    private void limpar_campos() {
        
        txtProdutoId.setText(null);
        txtProdutoNome.setText(null);
        txtProdutoPreco.setText(null);
        txtProdutoEstoque.setText(null);
        txtProdutoEstoqueMinimo.setText(null);  
        
        // controle de botões
        btnProdutoCreate.setEnabled(true);
        btnProdutoUpdate.setEnabled(false);
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtProdutoNome = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtProdutoPreco = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtProdutoEstoque = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtProdutoEstoqueMinimo = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        btnProdutoCreate = new javax.swing.JButton();
        btnProdutoUpdate = new javax.swing.JButton();
        btnProdutoDelete = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblProdutos = new javax.swing.JTable();
        txtProdutoPesquisar = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtProdutoId = new javax.swing.JTextField();
        btnLimparCampos = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("AutoWash Manager - Gestão de Produtos");
        setResizable(false);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("Cadastro de Produtos");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("* Nome");

        txtProdutoNome.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("* Preço");

        txtProdutoPreco.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("* Estoque");

        txtProdutoEstoque.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("* Estoque Mínimo");

        txtProdutoEstoqueMinimo.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel6.setText("* Campos Obrigatórios");

        btnProdutoCreate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/addProduct.png"))); // NOI18N
        btnProdutoCreate.setToolTipText("Cadastrar");
        btnProdutoCreate.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnProdutoCreate.addActionListener(this::btnProdutoCreateActionPerformed);

        btnProdutoUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/updateProduct.png"))); // NOI18N
        btnProdutoUpdate.setToolTipText("Atualizar");
        btnProdutoUpdate.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnProdutoUpdate.setEnabled(false);
        btnProdutoUpdate.addActionListener(this::btnProdutoUpdateActionPerformed);

        btnProdutoDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/deleteProduct.png"))); // NOI18N
        btnProdutoDelete.setToolTipText("Deletar");
        btnProdutoDelete.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnProdutoDelete.addActionListener(this::btnProdutoDeleteActionPerformed);

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel7.setText("Pesquisa de Produtos");

        tblProdutos = new javax.swing.JTable() {
            public boolean isCellEditable(int rolIndex, int colIndex) {
                return false;
            }
        };
        tblProdutos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "id", "nome", "preço", "estoque", "estoque mínimo"
            }
        ));
        tblProdutos.setFocusable(false);
        tblProdutos.getTableHeader().setReorderingAllowed(false);
        tblProdutos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblProdutosMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblProdutos);

        txtProdutoPesquisar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtProdutoPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        txtProdutoPesquisar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtProdutoPesquisarKeyReleased(evt);
            }
        });

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/loupe.png"))); // NOI18N
        jLabel8.setText("jLabel1");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel9.setText("Id");

        txtProdutoId.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtProdutoId.setToolTipText("");
        txtProdutoId.setEnabled(false);

        btnLimparCampos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/clear.png"))); // NOI18N
        btnLimparCampos.setToolTipText("Limpar Campos");
        btnLimparCampos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnLimparCampos.addActionListener(this::btnLimparCamposActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(378, 378, 378)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel6)
                .addGap(73, 73, 73))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtProdutoEstoqueMinimo, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(btnLimparCampos)
                                        .addGap(100, 100, 100)
                                        .addComponent(btnProdutoCreate)
                                        .addGap(100, 100, 100)
                                        .addComponent(btnProdutoUpdate)
                                        .addGap(100, 100, 100)
                                        .addComponent(btnProdutoDelete))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                            .addComponent(txtProdutoPreco, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(49, 49, 49)
                                            .addComponent(jLabel4)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(txtProdutoEstoque, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(305, 305, 305))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                            .addComponent(jLabel9)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(txtProdutoId, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel2)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(txtProdutoNome, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(47, 47, 47)))))))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(101, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 800, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addGap(279, 279, 279))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtProdutoPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(164, 164, 164)))))
                .addGap(99, 99, 99))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel6))
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtProdutoNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(txtProdutoId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(42, 42, 42)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtProdutoEstoque, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5)
                            .addComponent(txtProdutoEstoqueMinimo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)
                            .addComponent(txtProdutoPreco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(42, 42, 42)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnProdutoCreate)
                            .addComponent(btnProdutoUpdate)
                            .addComponent(btnProdutoDelete)))
                    .addComponent(btnLimparCampos))
                .addGap(29, 29, 29)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtProdutoPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(37, Short.MAX_VALUE))
        );

        btnProdutoCreate.getAccessibleContext().setAccessibleDescription("");
        btnProdutoUpdate.getAccessibleContext().setAccessibleDescription("");
        btnProdutoDelete.getAccessibleContext().setAccessibleDescription("");

        getAccessibleContext().setAccessibleDescription("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnProdutoCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProdutoCreateActionPerformed
        // Chamando o metodo de adicionar produtos

        adicionar();
    }//GEN-LAST:event_btnProdutoCreateActionPerformed

    private void btnProdutoUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProdutoUpdateActionPerformed
        // Chamando o método de alterar dados de um produto

        alterar();
    }//GEN-LAST:event_btnProdutoUpdateActionPerformed

    private void btnProdutoDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProdutoDeleteActionPerformed
        // Chamando o método de exclusão de um produto

        remover();
    }//GEN-LAST:event_btnProdutoDeleteActionPerformed

    private void tblProdutosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblProdutosMouseClicked
        // Chamando o método de seta campos

        setar_campos();
    }//GEN-LAST:event_tblProdutosMouseClicked

    private void txtProdutoPesquisarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtProdutoPesquisarKeyReleased
        // Chamndo o método pesquisar produto

        pesquisar_produto();
    }//GEN-LAST:event_txtProdutoPesquisarKeyReleased

    private void btnLimparCamposActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparCamposActionPerformed
        // Chamando a tela de limpar campos

        limpar_campos();
    }//GEN-LAST:event_btnLimparCamposActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new TelaGestaoProdutos().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLimparCampos;
    private javax.swing.JButton btnProdutoCreate;
    private javax.swing.JButton btnProdutoDelete;
    private javax.swing.JButton btnProdutoUpdate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblProdutos;
    private javax.swing.JTextField txtProdutoEstoque;
    private javax.swing.JTextField txtProdutoEstoqueMinimo;
    private javax.swing.JTextField txtProdutoId;
    private javax.swing.JTextField txtProdutoNome;
    private javax.swing.JTextField txtProdutoPesquisar;
    private javax.swing.JTextField txtProdutoPreco;
    // End of variables declaration//GEN-END:variables
}
