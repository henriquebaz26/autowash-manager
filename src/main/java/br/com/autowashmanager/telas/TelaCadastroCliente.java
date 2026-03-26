/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package br.com.autowashmanager.telas;

import br.com.autowashmanager.dal.ModuloConexao;
import br.com.autowashmanager.model.Endereco;
import br.com.autowashmanager.service.BuscaCEP;
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
public class TelaCadastroCliente extends javax.swing.JFrame {

    Connection conexao = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(TelaCadastroCliente.class.getName());

    /**
     * Creates new form TelaCadastroCliente
     */
    public TelaCadastroCliente() {
        initComponents();
        conexao = ModuloConexao.conector();
    }
    
    // metodo para adicionar clientes
    private void adicionar() {
        String sql = "insert into customer (name, phone, email, cep, street, number, neighborhood, city, state) values(?, ?, ?, ?, ?, ?, ?, ?, ?)"; 
        try {
            pst = conexao.prepareStatement(sql);
            pst.setString(1, txtClienteNome.getText());
            pst.setString(2, txtClienteTelefone.getText());
            pst.setString(3, txtClienteEmail.getText());
            pst.setString(4, txtClienteCEP.getText());
            pst.setString(5, txtClienteRua.getText());
            pst.setString(6, txtClienteNumero.getText());
            pst.setString(7, txtClienteBairro.getText());
            pst.setString(8, txtClienteCidade.getText());
            pst.setString(9, txtClienteEstado.getText());           

            // validação dos campos obrigatórios            
            if ((txtClienteNome.getText().isEmpty()) || (txtClienteTelefone.getText().isEmpty()) || (txtClienteCEP.getText().isEmpty()) || (txtClienteRua.getText().isEmpty()) || (txtClienteNumero.getText().isEmpty()) || (txtClienteBairro.getText().isEmpty()) || (txtClienteCidade.getText().isEmpty()) || (txtClienteEstado.getText().isEmpty())) {
                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios");
            } else {
                // a linha abaixo atualiza a tabela clientes com os dados do formulário
                // a estrutura abaixo é usada para confirmar a inserção dos dados na tabela
                int adicionado = pst.executeUpdate();
                // a linha abaixo serve de apoio ao entendimento da lógica
                System.out.println(adicionado);
                if (adicionado > 0) {
                    JOptionPane.showMessageDialog(null, "Cliente adicionado com sucesso");
                    limpar();
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);

        }
    }
    
    // metodo para limpar os campos do formulário
    private void limpar() {
        txtClienteId.setText(null);
        txtClienteNome.setText(null);
        txtClienteTelefone.setText(null);
        txtClienteEmail.setText(null);
        txtClienteCEP.setText(null);
        txtClienteRua.setText(null);
        txtClienteNumero.setText(null);
        txtClienteBairro.setText(null);
        txtClienteCidade.setText(null);
        txtClienteEstado.setText(null);
        ((DefaultTableModel) tblClientes.getModel()).setRowCount(0);
        
        btnClienteUpdate.setEnabled(false);
    }

    // método para pesquisar cliente pelo id, nome ou telefone com filtro
    private void pesquisar_cliente() {

        String coluna = "";

        if (rbtIdPesquisar.isSelected()) {
            coluna = "id";
        } else if (rbtNomePesquisar.isSelected()) {
            coluna = "name";
        } else if (rbtTelefonePesquisar.isSelected()) {
            coluna = "phone";
        } else {
            JOptionPane.showMessageDialog(null, "Selecione um filtro!");
            return;
        }

        try {
            String texto = txtClientePesquisar.getText().trim();

            String sql;

            if (coluna.equals("id")) {
                sql = "select id as id, name as nome, phone as telefone, email as email, cep as cep, street as rua, number as numero, neighborhood as bairro, city as cidade, state as estado from customer where id = ?";

                pst = conexao.prepareStatement(sql);
                try {
                    pst.setInt(1, Integer.parseInt(texto));
                } catch (NumberFormatException e) {
                    return;
                }

            } else {
                sql = "select id as id, name as nome, phone as telefone, email as email, cep as cep, street as rua, number as numero, neighborhood as bairro, city as cidade, state as estado from customer where " + coluna + " like ?";

                pst = conexao.prepareStatement(sql);
                pst.setString(1, "%" + texto + "%");
            }

            rs = pst.executeQuery();

            // a linha abaixo usa a classe DbUtils para preencher a tabela
            tblClientes.setModel(DbUtils.resultSetToTableModel(rs));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    // método para setar os campos do formulário com o conteúdo da tabela
    public void setar_campos() {
        limpar_campos();

        int setar = tblClientes.getSelectedRow();

        txtClienteId.setText(tblClientes.getModel().getValueAt(setar, 0).toString());
        txtClienteNome.setText(tblClientes.getModel().getValueAt(setar, 1).toString());
        txtClienteTelefone.setText(tblClientes.getModel().getValueAt(setar, 2).toString());
        txtClienteEmail.setText(tblClientes.getModel().getValueAt(setar, 3).toString());
        txtClienteCEP.setText(tblClientes.getModel().getValueAt(setar, 4).toString());
        txtClienteRua.setText(tblClientes.getModel().getValueAt(setar, 5).toString());
        txtClienteNumero.setText(tblClientes.getModel().getValueAt(setar, 6).toString());
        txtClienteBairro.setText(tblClientes.getModel().getValueAt(setar, 7).toString());
        txtClienteCidade.setText(tblClientes.getModel().getValueAt(setar, 8).toString());
        txtClienteEstado.setText(tblClientes.getModel().getValueAt(setar, 9).toString());

    }

    private void buscarCep() {
        try {
            String cep = txtClienteCEP.getText();

            Endereco endereco = BuscaCEP.buscar(cep);

            if (endereco != null) {
                txtClienteRua.setText(endereco.getRua());
                txtClienteBairro.setText(endereco.getBairro());
                txtClienteCidade.setText(endereco.getCidade());
                txtClienteEstado.setText(endereco.getEstado());
            } else {
                JOptionPane.showMessageDialog(null, "CEP não encontrado.");
            }

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao buscar CEP.");
        }
    }
    
    // método para alterar os dados do cliente
    private void alterar() {
        String sql = "update customer set name=?, phone=?, email=?, cep=?, street=?, number=?, neighborhood=?, city=?, state=? where id=?";
        try {
            pst = conexao.prepareStatement(sql);
            pst.setString(1, txtClienteNome.getText());
            pst.setString(2, txtClienteTelefone.getText());
            pst.setString(3, txtClienteEmail.getText());
            pst.setString(4, txtClienteCEP.getText());
            pst.setString(5, txtClienteRua.getText());
            pst.setString(6, txtClienteNumero.getText());
            pst.setString(7, txtClienteBairro.getText());
            pst.setString(8, txtClienteCidade.getText());
            pst.setString(9, txtClienteEstado.getText()); 
            
            pst.setString(10, txtClienteId.getText());

            if ((txtClienteNome.getText().isEmpty()) || (txtClienteTelefone.getText().isEmpty()) || (txtClienteCEP.getText().isEmpty()) || (txtClienteRua.getText().isEmpty()) || (txtClienteNumero.getText().isEmpty()) || (txtClienteBairro.getText().isEmpty()) || (txtClienteCidade.getText().isEmpty()) || (txtClienteEstado.getText().isEmpty())) {
                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios");
            } else {
                // a linha abaixo atualiza a tabela clientes com os dados do formulário
                // a estrutura abaixo é usada para confirmar a alteração dos dados na tabela
                int alterado = pst.executeUpdate();
                // a linha abaixo serve de apoio ao entendimento da lógica
                System.out.println(alterado);
                if (alterado > 0) {
                    JOptionPane.showMessageDialog(null, "Dados do cliente alterados com sucesso");
                    limpar();

                    btnClienteCreate.setEnabled(true);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }
    
    // método responsável pela remoção de cliente
    private void remover() {
        // VALIDAÇÃO: verificar se algum cliente foi selecionado
    if (txtClienteId.getText().isEmpty()) {
        JOptionPane.showMessageDialog(null, "Selecione um cliente para excluir");
        return;
    }
        
        //a estrutura abaixo confirma a remoção do cliente
        int confirma = JOptionPane.showConfirmDialog(null, "Tem certeza que deseja remover este cliente?", "ATENÇÃO", JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_OPTION) {
            String sql = "delete from customer where id=?";
            try {
                pst = conexao.prepareStatement(sql);
                pst.setString(1, txtClienteId.getText());
                int apagado = pst.executeUpdate();

                if (apagado > 0) {
                    JOptionPane.showMessageDialog(null, "Cliente removido com sucesso");
                    limpar();

                    btnClienteCreate.setEnabled(true);
                    btnClienteUpdate.setEnabled(false);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }
    
    private void limpar_campos() {
        
        txtClienteId.setText(null);
        txtClienteNome.setText(null);
        txtClienteTelefone.setText(null);
        txtClienteEmail.setText(null);
        txtClienteCEP.setText(null);
        txtClienteRua.setText(null);
        txtClienteNumero.setText(null);
        txtClienteBairro.setText(null);
        txtClienteCidade.setText(null);
        txtClienteEstado.setText(null);
        
        // a linha abaixo desabilita os botões de adicionar e habilita o de atualizar
        btnClienteCreate.setEnabled(true);
        btnClienteUpdate.setEnabled(false);
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rbtFiltroPesquisar = new javax.swing.ButtonGroup();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblClientes = new javax.swing.JTable();
        txtClientePesquisar = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        rbtIdPesquisar = new javax.swing.JRadioButton();
        rbtNomePesquisar = new javax.swing.JRadioButton();
        rbtTelefonePesquisar = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtClienteId = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtClienteNome = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtClienteTelefone = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtClienteCEP = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtClienteRua = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtClienteNumero = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtClienteBairro = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtClienteCidade = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txtClienteEstado = new javax.swing.JTextField();
        lblBuscarCEP = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        txtClienteEmail = new javax.swing.JTextField();
        btnClienteCreate = new javax.swing.JButton();
        btnClienteUpdate = new javax.swing.JButton();
        btnClienteDelete = new javax.swing.JButton();
        btnLimparCampos = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("AutoWash Manager - Cadastro de Clientes");
        setPreferredSize(new java.awt.Dimension(1181, 609));
        setResizable(false);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel2.setText("* Campos Obrigatórios");

        tblClientes = new javax.swing.JTable() {
            public boolean isCellEditable(int rolIndex, int colIndex) {
                return false;
            }
        };
        tblClientes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "id", "nome", "telefone", "email", "cep", "rua", "número", "bairro", "cidade", "estado"
            }
        ));
        tblClientes.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tblClientes.setFocusable(false);
        tblClientes.getTableHeader().setReorderingAllowed(false);
        tblClientes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblClientesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblClientes);

        txtClientePesquisar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtClientePesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        txtClientePesquisar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtClientePesquisarKeyReleased(evt);
            }
        });

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/loupe.png"))); // NOI18N
        jLabel1.setText("jLabel1");

        rbtFiltroPesquisar.add(rbtIdPesquisar);
        rbtIdPesquisar.setText("id");
        rbtIdPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rbtIdPesquisar.addActionListener(this::rbtIdPesquisarActionPerformed);

        rbtFiltroPesquisar.add(rbtNomePesquisar);
        rbtNomePesquisar.setSelected(true);
        rbtNomePesquisar.setText("nome");
        rbtNomePesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        rbtFiltroPesquisar.add(rbtTelefonePesquisar);
        rbtTelefonePesquisar.setText("telefone");
        rbtTelefonePesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Pesquisar pelo:");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Id");

        txtClienteId.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtClienteId.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        txtClienteId.setEnabled(false);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("* Nome");

        txtClienteNome.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("* Telefone");

        txtClienteTelefone.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("* CEP");

        txtClienteCEP.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtClienteCEP.setAutoscrolls(false);
        txtClienteCEP.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        txtClienteCEP.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtClienteCEPFocusLost(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setText("* Rua");

        txtClienteRua.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel9.setText("* Número");

        txtClienteNumero.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel10.setText("* Bairro");

        txtClienteBairro.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel11.setText("* Cidade");

        txtClienteCidade.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel12.setText("* Estado");

        txtClienteEstado.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        lblBuscarCEP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/address.png"))); // NOI18N
        lblBuscarCEP.setText("jLabel13");
        lblBuscarCEP.setToolTipText("Buscar CEP");
        lblBuscarCEP.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblBuscarCEP.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblBuscarCEPMouseClicked(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel13.setText("Email");

        txtClienteEmail.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        btnClienteCreate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/create.png"))); // NOI18N
        btnClienteCreate.setToolTipText("Adicionar");
        btnClienteCreate.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnClienteCreate.addActionListener(this::btnClienteCreateActionPerformed);

        btnClienteUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/update.png"))); // NOI18N
        btnClienteUpdate.setToolTipText("Atualizar");
        btnClienteUpdate.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnClienteUpdate.setEnabled(false);
        btnClienteUpdate.addActionListener(this::btnClienteUpdateActionPerformed);

        btnClienteDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/delete.png"))); // NOI18N
        btnClienteDelete.setToolTipText("Deletar");
        btnClienteDelete.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnClienteDelete.addActionListener(this::btnClienteDeleteActionPerformed);

        btnLimparCampos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/clear.png"))); // NOI18N
        btnLimparCampos.setToolTipText("Limpar Campos");
        btnLimparCampos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnLimparCampos.addActionListener(this::btnLimparCamposActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(51, 51, 51)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(txtClientePesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel2))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtClienteId, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(55, 55, 55)
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtClienteNome, javax.swing.GroupLayout.PREFERRED_SIZE, 502, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(56, 56, 56)
                                .addComponent(jLabel6)
                                .addGap(9, 9, 9)
                                .addComponent(txtClienteTelefone))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtClienteCEP, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblBuscarCEP, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(56, 56, 56)
                                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtClienteRua, javax.swing.GroupLayout.PREFERRED_SIZE, 474, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(9, 93, Short.MAX_VALUE)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtClienteNumero, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtClienteBairro, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(81, 81, 81)
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtClienteCidade, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(57, 57, 57)
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtClienteEstado)))
                        .addGap(52, 52, 52))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(18, 18, 18)
                                .addComponent(rbtIdPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(rbtNomePesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(rbtTelefonePesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtClienteEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addGap(266, 266, 266)
                .addComponent(btnLimparCampos)
                .addGap(100, 100, 100)
                .addComponent(btnClienteCreate)
                .addGap(100, 100, 100)
                .addComponent(btnClienteUpdate)
                .addGap(100, 100, 100)
                .addComponent(btnClienteDelete)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbtIdPesquisar)
                    .addComponent(rbtNomePesquisar)
                    .addComponent(rbtTelefonePesquisar)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtClientePesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtClienteId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(txtClienteNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(txtClienteTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(31, 31, 31)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(txtClienteEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(34, 34, 34)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtClienteCEP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(txtClienteRua, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(txtClienteNumero, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblBuscarCEP))
                .addGap(32, 32, 32)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtClienteBairro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(txtClienteCidade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(txtClienteEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 64, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnClienteCreate, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnClienteUpdate, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnClienteDelete, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnLimparCampos, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(32, 32, 32))
        );

        lblBuscarCEP.getAccessibleContext().setAccessibleName("BuscarCEP");
        btnLimparCampos.getAccessibleContext().setAccessibleName("Limpar ");
        btnLimparCampos.getAccessibleContext().setAccessibleDescription("Limpar ");

        getAccessibleContext().setAccessibleDescription("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void rbtIdPesquisarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbtIdPesquisarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rbtIdPesquisarActionPerformed

    private void txtClientePesquisarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtClientePesquisarKeyReleased
        // Chamando o método de pesquisar clientes
        pesquisar_cliente();

    }//GEN-LAST:event_txtClientePesquisarKeyReleased

    private void tblClientesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblClientesMouseClicked
        // Chamando o método de setar campos

        setar_campos();
    }//GEN-LAST:event_tblClientesMouseClicked

    private void txtClienteCEPFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtClienteCEPFocusLost
        // Chamando o método de buscaCEP

        if (txtClienteCEP.getText().length() != 8) {
            JOptionPane.showMessageDialog(null,
                    "CEP inválido. Por favor, informe um CEP com 8 dígitos numéricos.",
                    "Erro de validação",
                    JOptionPane.WARNING_MESSAGE
            );
        } else {
            buscarCep();
        }
    }//GEN-LAST:event_txtClienteCEPFocusLost

    private void lblBuscarCEPMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblBuscarCEPMouseClicked
        // Chamando o método de buscaCEP

        if (txtClienteCEP.getText().length() != 8) {
            JOptionPane.showMessageDialog(null,
                    "CEP inválido. Por favor, informe um CEP com 8 dígitos numéricos.",
                    "Erro de validação",
                    JOptionPane.WARNING_MESSAGE
            );
        } else {
            buscarCep();
        }
    }//GEN-LAST:event_lblBuscarCEPMouseClicked

    private void btnClienteCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClienteCreateActionPerformed
        // Chamando o metodo de adicionar cliente

        adicionar();
    }//GEN-LAST:event_btnClienteCreateActionPerformed

    private void btnClienteUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClienteUpdateActionPerformed
        // Chamando o método de alterar informações de um cliente

        alterar();
    }//GEN-LAST:event_btnClienteUpdateActionPerformed

    private void btnClienteDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClienteDeleteActionPerformed
        // Chamando o método de exclusão de cliente

        remover();
    }//GEN-LAST:event_btnClienteDeleteActionPerformed

    private void btnLimparCamposActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparCamposActionPerformed
        // Chamando o método de limpar campos
        
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
        java.awt.EventQueue.invokeLater(() -> new TelaCadastroCliente().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClienteCreate;
    private javax.swing.JButton btnClienteDelete;
    private javax.swing.JButton btnClienteUpdate;
    private javax.swing.JButton btnLimparCampos;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblBuscarCEP;
    private javax.swing.ButtonGroup rbtFiltroPesquisar;
    private javax.swing.JRadioButton rbtIdPesquisar;
    private javax.swing.JRadioButton rbtNomePesquisar;
    private javax.swing.JRadioButton rbtTelefonePesquisar;
    private javax.swing.JTable tblClientes;
    private javax.swing.JTextField txtClienteBairro;
    private javax.swing.JTextField txtClienteCEP;
    private javax.swing.JTextField txtClienteCidade;
    private javax.swing.JTextField txtClienteEmail;
    private javax.swing.JTextField txtClienteEstado;
    private javax.swing.JTextField txtClienteId;
    private javax.swing.JTextField txtClienteNome;
    private javax.swing.JTextField txtClienteNumero;
    private javax.swing.JTextField txtClientePesquisar;
    private javax.swing.JTextField txtClienteRua;
    private javax.swing.JTextField txtClienteTelefone;
    // End of variables declaration//GEN-END:variables
}
