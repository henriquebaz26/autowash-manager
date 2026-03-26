/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package br.com.autowashmanager.telas;

import br.com.autowashmanager.dal.ModuloConexao;
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
public class TelaCadastroFuncionario extends javax.swing.JFrame {

    Connection conexao = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(TelaCadastroFuncionario.class.getName());

    /**
     * Creates new form TelaCadastroFuncionario
     */
    public TelaCadastroFuncionario() {
        initComponents();
        conexao = ModuloConexao.conector();
    }

    // metodo para adicionar funcionários
    private void adicionar() {
        String sql = "insert into employee (name, phone, role, username, password, active) values(?, ?, ?, ?, ?, ?)";
        try {
            pst = conexao.prepareStatement(sql);
            pst.setString(1, txtFuncionarioNome.getText());
            pst.setString(2, txtFuncionarioTelefone.getText());
            pst.setString(3, cboFuncionarioAtividade.getSelectedItem().toString());
            pst.setString(4, txtFuncionarioLogin.getText());
            pst.setString(5, txtFuncionarioSenha.getText());
            pst.setString(6, cboFuncionarioStatus.getSelectedItem().toString());

            // validação dos campos obrigatórios            
            if ((txtFuncionarioNome.getText().isEmpty()) || (txtFuncionarioLogin.getText().isEmpty()) || (txtFuncionarioSenha.getText().isEmpty())) {
                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios");
            } else {
                // a linha abaixo atualiza a tabela funcionarios com os dados do formulário
                // a estrutura abaixo é usada para confirmar a inserção dos dados na tabela
                int adicionado = pst.executeUpdate();
                // a linha abaixo serve de apoio ao entendimento da lógica
                System.out.println(adicionado);
                if (adicionado > 0) {
                    JOptionPane.showMessageDialog(null, "Funcionário adicionado com sucesso");
                    limparTudo();
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);

        }
    }

    // método para pesquisar funcionario pelo nome com filtro
    private void pesquisarFuncionario() {
        String sql = "select id as id, name as nome, phone as telefone, role as atividade, username as login, password as senha, active as status from employee where name like ?";

        try {
            pst = conexao.prepareStatement(sql);
            // passando o conteúdo da caixa de pesquisa para o interroga
            // atenção ao % que é a continuação da String sql

            pst.setString(1, txtFuncionarioPesquisar.getText().trim() + "%");
            rs = pst.executeQuery();
            // a linha abaixo usa a classe DbUtils para preencher a tabela
            tblFuncionarios.setModel(DbUtils.resultSetToTableModel(rs));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    // método para setar os campos do formulário com o conteúdo da tabela
    public void setarCampos() {

        int setar = tblFuncionarios.getSelectedRow();
        if (setar == -1) {
            return;
        }

        txtFuncionarioId.setText(tblFuncionarios.getModel().getValueAt(setar, 0).toString());
        txtFuncionarioNome.setText(tblFuncionarios.getModel().getValueAt(setar, 1).toString());
        txtFuncionarioTelefone.setText(tblFuncionarios.getModel().getValueAt(setar, 2).toString());
        cboFuncionarioAtividade.setSelectedItem(tblFuncionarios.getModel().getValueAt(setar, 3).toString());
        txtFuncionarioLogin.setText(tblFuncionarios.getModel().getValueAt(setar, 4).toString());
        txtFuncionarioSenha.setText(tblFuncionarios.getModel().getValueAt(setar, 5).toString());
        cboFuncionarioStatus.setSelectedItem(tblFuncionarios.getModel().getValueAt(setar, 6).toString());

        btnFuncionarioUpdate.setEnabled(true);
        btnFuncionarioCreate.setEnabled(false);

    }

    // método para alterar os dados do funcionário
    private void alterar() {
        String sql = "update employee set name=?, phone=?, role=?, username=?, password=?, active=? where id=?";
        try {
            pst = conexao.prepareStatement(sql);
            pst.setString(1, txtFuncionarioNome.getText());
            pst.setString(2, txtFuncionarioTelefone.getText());
            pst.setString(3, cboFuncionarioAtividade.getSelectedItem().toString());
            pst.setString(4, txtFuncionarioLogin.getText());
            pst.setString(5, txtFuncionarioSenha.getText());
            pst.setString(6, cboFuncionarioStatus.getSelectedItem().toString());
            pst.setString(7, txtFuncionarioId.getText());

            if ((txtFuncionarioNome.getText().isEmpty()) || (txtFuncionarioLogin.getText().isEmpty()) || (txtFuncionarioSenha.getText().isEmpty())) {
                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios");
            } else {
                // a linha abaixo atualiza a tabela funcionários com os dados do formulário
                // a estrutura abaixo é usada para confirmar a alteração dos dados na tabela
                int alterado = pst.executeUpdate();
                // a linha abaixo serve de apoio ao entendimento da lógica
                System.out.println(alterado);
                if (alterado > 0) {
                    JOptionPane.showMessageDialog(null, "Dados do funcionário alterados com sucesso");
                    limparTudo();

                    btnFuncionarioCreate.setEnabled(true);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    // método responsável pela remoção dos funcionários
    private void remover() {
        // VALIDAÇÃO: verificar se algum funcionário foi selecionado
        if (txtFuncionarioId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Selecione um funcionário para excluir");
            return;
        }

        //a estrutura abaixo confirma a remoção do funcionário
        int confirma = JOptionPane.showConfirmDialog(null, "Tem certeza que deseja remover este funcionário?", "ATENÇÃO", JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_OPTION) {
            String sql = "delete from employee where id=?";
            try {
                pst = conexao.prepareStatement(sql);
                pst.setString(1, txtFuncionarioId.getText());
                int apagado = pst.executeUpdate();

                if (apagado > 0) {
                    JOptionPane.showMessageDialog(null, "Funcionário removido com sucesso");
                    limparTudo();

                    btnFuncionarioCreate.setEnabled(true);
                    btnFuncionarioUpdate.setEnabled(false);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    // metodo para limpar os campos do formulário
    private void limparTudo() {
        txtFuncionarioNome.setText(null);
        txtFuncionarioTelefone.setText(null);
        cboFuncionarioAtividade.setSelectedIndex(0);
        cboFuncionarioStatus.setSelectedIndex(0);
        txtFuncionarioLogin.setText(null);
        txtFuncionarioSenha.setText(null);
        txtFuncionarioPesquisar.setText(null);
        tblFuncionarios.setModel(new DefaultTableModel(null,
                new String[]{"id", "nome", "telefone", "atividade", "login", "senha", "status"}));

        btnFuncionarioCreate.setEnabled(true);
        btnFuncionarioUpdate.setEnabled(false);
    }

    private void limparCampos() {
        txtFuncionarioNome.setText(null);
        txtFuncionarioTelefone.setText(null);
        cboFuncionarioAtividade.setSelectedIndex(0);
        cboFuncionarioStatus.setSelectedIndex(0);
        txtFuncionarioLogin.setText(null);
        txtFuncionarioSenha.setText(null);

        // a linha abaixo desabilita os botões de adicionar e habilita o de atualizar
        btnFuncionarioCreate.setEnabled(true);
        btnFuncionarioUpdate.setEnabled(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtFuncionarioPesquisar = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblFuncionarios = new javax.swing.JTable();
        txtFuncionarioId = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtFuncionarioNome = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtFuncionarioTelefone = new javax.swing.JTextField();
        cboFuncionarioAtividade = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtFuncionarioLogin = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtFuncionarioSenha = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        cboFuncionarioStatus = new javax.swing.JComboBox<>();
        btnFuncionarioCreate = new javax.swing.JButton();
        btnFuncionarioUpdate = new javax.swing.JButton();
        btnFuncionarioDelete = new javax.swing.JButton();
        btnLimparCampos = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("AutoWash Manager - Cadastro de Funcionário");
        setResizable(false);

        txtFuncionarioPesquisar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtFuncionarioPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        txtFuncionarioPesquisar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtFuncionarioPesquisarKeyReleased(evt);
            }
        });

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/loupe.png"))); // NOI18N
        jLabel1.setText("jLabel1");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel2.setText("* Campos Obrigatórios");

        tblFuncionarios = new javax.swing.JTable() {
            public boolean isCellEditable(int rolIndex, int colIndex) {
                return false;
            }
        };
        tblFuncionarios.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "id", "nome", "telefone", "atividade", "login", "senha", "status"
            }
        ));
        tblFuncionarios.setFocusable(false);
        tblFuncionarios.getTableHeader().setReorderingAllowed(false);
        tblFuncionarios.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblFuncionariosMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblFuncionarios);

        txtFuncionarioId.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtFuncionarioId.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        txtFuncionarioId.setEnabled(false);
        txtFuncionarioId.addActionListener(this::txtFuncionarioIdActionPerformed);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Id");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("* Nome");

        txtFuncionarioNome.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtFuncionarioNome.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Telefone");

        txtFuncionarioTelefone.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        cboFuncionarioAtividade.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cboFuncionarioAtividade.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "USER", "ADMIN" }));
        cboFuncionarioAtividade.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        cboFuncionarioAtividade.addActionListener(this::cboFuncionarioAtividadeActionPerformed);

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("* Atividade");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("* Login");

        txtFuncionarioLogin.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setText("* Senha");

        txtFuncionarioSenha.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel9.setText("* Status");

        cboFuncionarioStatus.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cboFuncionarioStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ATIVO", "INATIVO" }));
        cboFuncionarioStatus.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        btnFuncionarioCreate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/create.png"))); // NOI18N
        btnFuncionarioCreate.setToolTipText("Adicionar");
        btnFuncionarioCreate.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnFuncionarioCreate.addActionListener(this::btnFuncionarioCreateActionPerformed);

        btnFuncionarioUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/update.png"))); // NOI18N
        btnFuncionarioUpdate.setToolTipText("Atualizar");
        btnFuncionarioUpdate.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnFuncionarioUpdate.setEnabled(false);
        btnFuncionarioUpdate.addActionListener(this::btnFuncionarioUpdateActionPerformed);

        btnFuncionarioDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/delete.png"))); // NOI18N
        btnFuncionarioDelete.setToolTipText("Deletar");
        btnFuncionarioDelete.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnFuncionarioDelete.addActionListener(this::btnFuncionarioDeleteActionPerformed);

        btnLimparCampos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/clear.png"))); // NOI18N
        btnLimparCampos.setToolTipText("Limpar Campos");
        btnLimparCampos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnLimparCampos.addActionListener(this::btnLimparCamposActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtFuncionarioId, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtFuncionarioPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 326, Short.MAX_VALUE)
                                .addComponent(jLabel2))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(jLabel6)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(cboFuncionarioAtividade, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(jLabel4)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(txtFuncionarioNome, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(jLabel7)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(127, 127, 127)
                                                .addComponent(btnLimparCampos)
                                                .addGap(100, 100, 100)
                                                .addComponent(btnFuncionarioCreate))
                                            .addComponent(txtFuncionarioLogin))))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGap(133, 133, 133)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel5)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(txtFuncionarioTelefone))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel9)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(cboFuncionarioStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE))))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGap(134, 134, 134)
                                        .addComponent(jLabel8)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtFuncionarioSenha))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGap(100, 100, 100)
                                        .addComponent(btnFuncionarioUpdate)
                                        .addGap(100, 100, 100)
                                        .addComponent(btnFuncionarioDelete)
                                        .addGap(0, 0, Short.MAX_VALUE)))))
                        .addGap(58, 58, 58))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFuncionarioPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFuncionarioId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtFuncionarioNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(txtFuncionarioTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboFuncionarioAtividade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel9)
                    .addComponent(cboFuncionarioStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtFuncionarioLogin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(txtFuncionarioSenha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(40, 40, 40)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(btnFuncionarioCreate)
                        .addComponent(btnFuncionarioUpdate)
                        .addComponent(btnFuncionarioDelete))
                    .addComponent(btnLimparCampos))
                .addContainerGap(49, Short.MAX_VALUE))
        );

        btnLimparCampos.getAccessibleContext().setAccessibleDescription("Limpar Campos");

        getAccessibleContext().setAccessibleDescription("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void txtFuncionarioIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFuncionarioIdActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFuncionarioIdActionPerformed

    private void txtFuncionarioPesquisarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtFuncionarioPesquisarKeyReleased
        // Chamndo o método de pesquisa de funcionário

        pesquisarFuncionario();
    }//GEN-LAST:event_txtFuncionarioPesquisarKeyReleased

    private void cboFuncionarioAtividadeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboFuncionarioAtividadeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cboFuncionarioAtividadeActionPerformed

    private void btnFuncionarioCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFuncionarioCreateActionPerformed
        // Chamando o metodo de adicionar funcionário

        adicionar();
    }//GEN-LAST:event_btnFuncionarioCreateActionPerformed

    private void tblFuncionariosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblFuncionariosMouseClicked
        // Chamando o método de seta campos

        setarCampos();
    }//GEN-LAST:event_tblFuncionariosMouseClicked

    private void btnFuncionarioUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFuncionarioUpdateActionPerformed
        // Chamando o método de alterar informações de um funcionário

        alterar();
    }//GEN-LAST:event_btnFuncionarioUpdateActionPerformed

    private void btnFuncionarioDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFuncionarioDeleteActionPerformed
        // Chamando o método de exclusão de funcionário

        remover();
    }//GEN-LAST:event_btnFuncionarioDeleteActionPerformed

    private void btnLimparCamposActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparCamposActionPerformed
        // Chamando a tela de limpar campos

        limparCampos();
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
        java.awt.EventQueue.invokeLater(() -> new TelaCadastroFuncionario().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFuncionarioCreate;
    private javax.swing.JButton btnFuncionarioDelete;
    private javax.swing.JButton btnFuncionarioUpdate;
    private javax.swing.JButton btnLimparCampos;
    private javax.swing.JComboBox<String> cboFuncionarioAtividade;
    private javax.swing.JComboBox<String> cboFuncionarioStatus;
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
    private javax.swing.JTable tblFuncionarios;
    private javax.swing.JTextField txtFuncionarioId;
    private javax.swing.JTextField txtFuncionarioLogin;
    private javax.swing.JTextField txtFuncionarioNome;
    private javax.swing.JTextField txtFuncionarioPesquisar;
    private javax.swing.JTextField txtFuncionarioSenha;
    private javax.swing.JTextField txtFuncionarioTelefone;
    // End of variables declaration//GEN-END:variables
}
