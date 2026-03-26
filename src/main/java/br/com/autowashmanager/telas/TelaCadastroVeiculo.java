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
public class TelaCadastroVeiculo extends javax.swing.JFrame {

    Connection conexao = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(TelaCadastroVeiculo.class.getName());

    /**
     * Creates new form TelaCadastroVeiculo
     */
    public TelaCadastroVeiculo() {
        initComponents();
        conexao = ModuloConexao.conector();
    }

    // metodo para adicionar veiculos
    private void adicionar() {
        String sql = "insert into vehicle (plate, brand, model, color, customer_id) values (?, ?, ?, ?, ?)";

        try {
            String placa = txtVeiculoPlaca.getText().trim().toUpperCase();
            String marca = txtVeiculoMarca.getText().trim();
            String modelo = txtVeiculoModelo.getText().trim();
            String cor = txtVeiculoCor.getText().trim();
            String idCliente = txtVeiculoIdCliente.getText().trim();

            if (placa.isEmpty() || marca.isEmpty() || modelo.isEmpty()
                    || cor.isEmpty() || idCliente.isEmpty()) {

                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios");
                return;
            }

            if (!validarPlaca(placa)) {
                JOptionPane.showMessageDialog(null, "Placa inválida. Use o formato ABC1234 ou ABC1D23.");
                txtVeiculoPlaca.requestFocus();
                return;
            }

            if (!clienteExiste(idCliente)) {
                JOptionPane.showMessageDialog(null, "Cliente não encontrado. Utilize a tabela de pesquisa ao lado.");
                txtVeiculoIdCliente.requestFocus();
                return;
            }

            pst = conexao.prepareStatement(sql);
            pst.setString(1, placa);
            pst.setString(2, marca);
            pst.setString(3, modelo);
            pst.setString(4, cor);
            pst.setString(5, idCliente);

            int adicionado = pst.executeUpdate();

            if (adicionado > 0) {
                JOptionPane.showMessageDialog(null, "Veículo adicionado com sucesso");
                limpar();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private boolean validarPlaca(String placa) {
        // Remove espaços e deixa tudo maiúsculo
        placa = placa.trim().toUpperCase();

        // Regex:
        // ABC1234 ou ABC1D23
        String regex = "^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$";

        return placa.matches(regex);
    }

    private boolean clienteExiste(String idCliente) {
        String sql = "select id from customer where id = ?";
        try {
            PreparedStatement pstCliente = conexao.prepareStatement(sql);
            pstCliente.setString(1, idCliente);
            ResultSet rs = pstCliente.executeQuery();
            return rs.next();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
            return false;
        }
    }

    // metodo para limpar os campos do formulário
    private void limpar() {
        txtVeiculoPlaca.setText(null);
        txtVeiculoMarca.setText(null);
        txtVeiculoModelo.setText(null);
        txtVeiculoCor.setText(null);
        txtVeiculoIdCliente.setText(null);
        txtVeiculoPesquisar.setText(null);
        ((DefaultTableModel) tblVeiculos.getModel()).setRowCount(0);
        ((DefaultTableModel) tblClientes.getModel()).setRowCount(0);

        btnVeiculoUpdate.setEnabled(false);
    }

    // método para pesquisar veiculo pela placa, marca, modelo e cor com filtro
    private void pesquisar_veiculo() {

        String coluna = "";

        if (rbtPlacaPesquisar.isSelected()) {
            coluna = "v.plate";
        } else if (rbtMarcaPesquisar.isSelected()) {
            coluna = "v.brand";
        } else if (rbtModeloPesquisar.isSelected()) {
            coluna = "v.model";
        } else if (rbtCorPesquisar.isSelected()) {
            coluna = "v.color";
        } else if (rbtNomeClientePesquisar.isSelected()) {
            coluna = "c.name";
        } else {
            JOptionPane.showMessageDialog(null, "Selecione um filtro!");
            return;
        }

        try {
            String texto = txtVeiculoPesquisar.getText().trim();

            String sql;

            sql = "select v.id as id, plate as placa, brand as marca, model as modelo, color as cor, customer_id as idCliente, c.name as nomeCliente from vehicle v inner join customer c on v.customer_id = c.id where " + coluna + " like ?";

            pst = conexao.prepareStatement(sql);
            pst.setString(1, "%" + texto + "%");

            rs = pst.executeQuery();

            // a linha abaixo usa a classe DbUtils para preencher a tabela
            tblVeiculos.setModel(DbUtils.resultSetToTableModel(rs));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
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
                sql = "select id as id, name as nome, phone as telefone, email as email from customer where id = ?";

                pst = conexao.prepareStatement(sql);
                try {
                    pst.setInt(1, Integer.parseInt(texto));
                } catch (NumberFormatException e) {
                    return;
                }

            } else {
                sql = "select id as id, name as nome, phone as telefone, email as email from customer where " + coluna + " like ?";

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

    private void setar_campos() {
        limpar_campos();

        int setar = tblVeiculos.getSelectedRow();

        txtVeiculoId.setText(tblVeiculos.getModel().getValueAt(setar, 0).toString());
        txtVeiculoPlaca.setText(tblVeiculos.getModel().getValueAt(setar, 1).toString());
        txtVeiculoMarca.setText(tblVeiculos.getModel().getValueAt(setar, 2).toString());
        txtVeiculoModelo.setText(tblVeiculos.getModel().getValueAt(setar, 3).toString());
        txtVeiculoCor.setText(tblVeiculos.getModel().getValueAt(setar, 4).toString());
        txtVeiculoIdCliente.setText(tblVeiculos.getModel().getValueAt(setar, 5).toString());
        
    }

    private void setar_campo_id() {
        txtVeiculoIdCliente.setText(null);

        int setar = tblClientes.getSelectedRow();

        txtVeiculoIdCliente.setText(tblClientes.getModel().getValueAt(setar, 0).toString());
    }
    
    // método para alterar os dados do veiculo
    private void alterar() {
        String sql = "update vehicle set plate=?, brand=?, model=?, color=?, customer_id=? where id=?";

        try {
            String placa = txtVeiculoPlaca.getText().trim().toUpperCase();
            String marca = txtVeiculoMarca.getText().trim();
            String modelo = txtVeiculoModelo.getText().trim();
            String cor = txtVeiculoCor.getText().trim();
            String idCliente = txtVeiculoIdCliente.getText().trim();
            String id = txtVeiculoId.getText().trim();

            if (placa.isEmpty() || marca.isEmpty() || modelo.isEmpty()
                    || cor.isEmpty() || idCliente.isEmpty()) {

                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios");
                return;
            }

            if (!validarPlaca(placa)) {
                JOptionPane.showMessageDialog(null, "Placa inválida. Use o formato ABC1234 ou ABC1D23.");
                txtVeiculoPlaca.requestFocus();
                return;
            }

            if (!clienteExiste(idCliente)) {
                JOptionPane.showMessageDialog(null, "Cliente não encontrado. Utilize a tabela de pesquisa ao lado.");
                txtVeiculoIdCliente.requestFocus();
                return;
            }

            pst = conexao.prepareStatement(sql);
            pst.setString(1, placa);
            pst.setString(2, marca);
            pst.setString(3, modelo);
            pst.setString(4, cor);
            pst.setString(5, idCliente);
            pst.setString(6, id);

            int alterado = pst.executeUpdate();

            if (alterado > 0) {
                JOptionPane.showMessageDialog(null, "Dados do veículo alterados com sucesso");
                limpar();
                
                btnVeiculoCreate.setEnabled(true);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }
    
    // método responsável pela remoção dos veiculos
    private void remover() {
        // VALIDAÇÃO: verificar se algum veiculo foi selecionado
    if (txtVeiculoId.getText().isEmpty()) {
        JOptionPane.showMessageDialog(null, "Selecione um veículo para excluir");
        return;
    }
        
        //a estrutura abaixo confirma a remoção do veículo
        int confirma = JOptionPane.showConfirmDialog(null, "Tem certeza que deseja remover este veículo?", "ATENÇÃO", JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_OPTION) {
            String sql = "delete from vehicle where id=?";
            try {
                pst = conexao.prepareStatement(sql);
                pst.setString(1, txtVeiculoId.getText());
                int apagado = pst.executeUpdate();

                if (apagado > 0) {
                    JOptionPane.showMessageDialog(null, "Veículo removido com sucesso");
                    limpar();

                    btnVeiculoCreate.setEnabled(true);
                    btnVeiculoUpdate.setEnabled(false);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }
    
    private void limpar_campos() {
        
        txtVeiculoId.setText(null);
        txtVeiculoPlaca.setText(null);
        txtVeiculoMarca.setText(null);
        txtVeiculoModelo.setText(null);
        txtVeiculoCor.setText(null);
        
        // a linha abaixo desabilita os botões de adicionar e habilita o de atualizar
        btnVeiculoCreate.setEnabled(true);
        btnVeiculoUpdate.setEnabled(false);
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rbtFiltroPesquisarVeiculo = new javax.swing.ButtonGroup();
        rbtFiltroPesquisarCliente = new javax.swing.ButtonGroup();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblVeiculos = new javax.swing.JTable();
        txtVeiculoPesquisar = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        rbtPlacaPesquisar = new javax.swing.JRadioButton();
        rbtMarcaPesquisar = new javax.swing.JRadioButton();
        rbtModeloPesquisar = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        rbtCorPesquisar = new javax.swing.JRadioButton();
        rbtNomeClientePesquisar = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        rbtIdPesquisar = new javax.swing.JRadioButton();
        rbtTelefonePesquisar = new javax.swing.JRadioButton();
        rbtNomePesquisar = new javax.swing.JRadioButton();
        txtClientePesquisar = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblClientes = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        txtVeiculoId = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtVeiculoPlaca = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtVeiculoMarca = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtVeiculoModelo = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtVeiculoCor = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txtVeiculoIdCliente = new javax.swing.JTextField();
        btnVeiculoDelete = new javax.swing.JButton();
        btnVeiculoCreate = new javax.swing.JButton();
        btnVeiculoUpdate = new javax.swing.JButton();
        btnLimparCampos = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("AutoWash Manager - Cadastro de Veículo");
        setResizable(false);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel2.setText("* Campos Obrigatórios");

        tblVeiculos = new javax.swing.JTable() {
            public boolean isCellEditable(int rolIndex, int colIndex) {
                return false;
            }
        };
        tblVeiculos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Id", "Placa", "Marca", "Modelo", "Cor", "Id do Cliente", "Nome do Cliente"
            }
        ));
        tblVeiculos.setFocusable(false);
        tblVeiculos.getTableHeader().setReorderingAllowed(false);
        tblVeiculos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblVeiculosMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblVeiculos);

        txtVeiculoPesquisar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtVeiculoPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        txtVeiculoPesquisar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtVeiculoPesquisarKeyReleased(evt);
            }
        });

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/loupe.png"))); // NOI18N
        jLabel1.setText("jLabel1");

        rbtFiltroPesquisarVeiculo.add(rbtPlacaPesquisar);
        rbtPlacaPesquisar.setText("placa");
        rbtPlacaPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        rbtFiltroPesquisarVeiculo.add(rbtMarcaPesquisar);
        rbtMarcaPesquisar.setText("marca");
        rbtMarcaPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        rbtFiltroPesquisarVeiculo.add(rbtModeloPesquisar);
        rbtModeloPesquisar.setSelected(true);
        rbtModeloPesquisar.setText("modelo");
        rbtModeloPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Pesquisar pelo:");

        rbtFiltroPesquisarVeiculo.add(rbtCorPesquisar);
        rbtCorPesquisar.setText("cor");
        rbtCorPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        rbtFiltroPesquisarVeiculo.add(rbtNomeClientePesquisar);
        rbtNomeClientePesquisar.setText("nome do cliente");
        rbtNomeClientePesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Cliente"));

        jLabel4.setText("Pesquisar pelo:");

        rbtFiltroPesquisarCliente.add(rbtIdPesquisar);
        rbtIdPesquisar.setText("id");
        rbtIdPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        rbtFiltroPesquisarCliente.add(rbtTelefonePesquisar);
        rbtTelefonePesquisar.setText("telefone");
        rbtTelefonePesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        rbtFiltroPesquisarCliente.add(rbtNomePesquisar);
        rbtNomePesquisar.setSelected(true);
        rbtNomePesquisar.setText("nome");
        rbtNomePesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        txtClientePesquisar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtClientePesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        txtClientePesquisar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtClientePesquisarKeyReleased(evt);
            }
        });

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/loupe.png"))); // NOI18N

        tblClientes = new javax.swing.JTable() {
            public boolean isCellEditable(int rolIndex, int colIndex) {
                return false;
            }
        };
        tblClientes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "id", "nome", "telefone"
            }
        ));
        tblClientes.setFocusable(false);
        tblClientes.getTableHeader().setReorderingAllowed(false);
        tblClientes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblClientesMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblClientes);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)
                        .addComponent(rbtIdPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rbtNomePesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rbtTelefonePesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(txtClientePesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel5)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbtIdPesquisar)
                    .addComponent(rbtNomePesquisar)
                    .addComponent(rbtTelefonePesquisar)
                    .addComponent(jLabel4))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtClientePesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel5)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Id");

        txtVeiculoId.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtVeiculoId.setToolTipText("");
        txtVeiculoId.setEnabled(false);

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("* Placa");

        txtVeiculoPlaca.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel8.setText("* Campos Obrigatórios");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel9.setText("* Marca");

        txtVeiculoMarca.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel10.setText("* Modelo");

        txtVeiculoModelo.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel11.setText("* Cor");

        txtVeiculoCor.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtVeiculoCor.setToolTipText("");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel12.setText("* Id do Cliente");

        txtVeiculoIdCliente.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtVeiculoIdCliente.setToolTipText("");

        btnVeiculoDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/delete.png"))); // NOI18N
        btnVeiculoDelete.setToolTipText("Deletar");
        btnVeiculoDelete.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnVeiculoDelete.addActionListener(this::btnVeiculoDeleteActionPerformed);

        btnVeiculoCreate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/create.png"))); // NOI18N
        btnVeiculoCreate.setToolTipText("Adicionar");
        btnVeiculoCreate.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnVeiculoCreate.addActionListener(this::btnVeiculoCreateActionPerformed);

        btnVeiculoUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/update.png"))); // NOI18N
        btnVeiculoUpdate.setToolTipText("Atualizar");
        btnVeiculoUpdate.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnVeiculoUpdate.setEnabled(false);
        btnVeiculoUpdate.addActionListener(this::btnVeiculoUpdateActionPerformed);

        btnLimparCampos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/clear.png"))); // NOI18N
        btnLimparCampos.setToolTipText("Limpar Campos");
        btnLimparCampos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnLimparCampos.addActionListener(this::btnLimparCamposActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(txtVeiculoPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 326, Short.MAX_VALUE)
                        .addComponent(jLabel2))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(rbtPlacaPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rbtMarcaPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rbtModeloPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rbtCorPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rbtNomeClientePesquisar)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(39, 39, 39)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtVeiculoId, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel8))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel7)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtVeiculoPlaca, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(57, 57, 57)
                                        .addComponent(jLabel9))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel10)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtVeiculoModelo, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtVeiculoCor))
                                    .addComponent(txtVeiculoMarca)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtVeiculoIdCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnLimparCampos)
                        .addGap(100, 100, 100)
                        .addComponent(btnVeiculoCreate)
                        .addGap(100, 100, 100)
                        .addComponent(btnVeiculoUpdate)
                        .addGap(100, 100, 100)
                        .addComponent(btnVeiculoDelete)
                        .addGap(216, 216, 216)))
                .addGap(62, 62, 62))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbtPlacaPesquisar)
                    .addComponent(rbtMarcaPesquisar)
                    .addComponent(rbtModeloPesquisar)
                    .addComponent(jLabel3)
                    .addComponent(rbtCorPesquisar)
                    .addComponent(rbtNomeClientePesquisar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtVeiculoPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(txtVeiculoId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(txtVeiculoPlaca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9)
                            .addComponent(txtVeiculoMarca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(44, 44, 44)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(txtVeiculoModelo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11)
                            .addComponent(txtVeiculoCor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(44, 44, 44)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(txtVeiculoIdCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnVeiculoCreate)
                    .addComponent(btnVeiculoUpdate)
                    .addComponent(btnVeiculoDelete)
                    .addComponent(btnLimparCampos))
                .addGap(24, 24, 24))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void tblVeiculosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblVeiculosMouseClicked
        // Chamando o método de setar campos

        setar_campos();
    }//GEN-LAST:event_tblVeiculosMouseClicked

    private void txtVeiculoPesquisarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtVeiculoPesquisarKeyReleased
        // Chamando o método de pesquisar veiculo

        pesquisar_veiculo();
    }//GEN-LAST:event_txtVeiculoPesquisarKeyReleased

    private void txtClientePesquisarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtClientePesquisarKeyReleased
        // Chamando o método de pesquisar clientes

        pesquisar_cliente();
    }//GEN-LAST:event_txtClientePesquisarKeyReleased

    private void tblClientesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblClientesMouseClicked
        // Chamando o método de setar campos

        setar_campo_id();
    }//GEN-LAST:event_tblClientesMouseClicked

    private void btnVeiculoDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVeiculoDeleteActionPerformed
        // Chamando o método de exclusão de funcionário

        remover();
    }//GEN-LAST:event_btnVeiculoDeleteActionPerformed

    private void btnVeiculoCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVeiculoCreateActionPerformed
        // Chamando o metodo de adicionar funcionário

        adicionar();
    }//GEN-LAST:event_btnVeiculoCreateActionPerformed

    private void btnVeiculoUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVeiculoUpdateActionPerformed
        // Chamando o método de alterar informações de um funcionário

        alterar();
    }//GEN-LAST:event_btnVeiculoUpdateActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new TelaCadastroVeiculo().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLimparCampos;
    private javax.swing.JButton btnVeiculoCreate;
    private javax.swing.JButton btnVeiculoDelete;
    private javax.swing.JButton btnVeiculoUpdate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JRadioButton rbtCorPesquisar;
    private javax.swing.ButtonGroup rbtFiltroPesquisarCliente;
    private javax.swing.ButtonGroup rbtFiltroPesquisarVeiculo;
    private javax.swing.JRadioButton rbtIdPesquisar;
    private javax.swing.JRadioButton rbtMarcaPesquisar;
    private javax.swing.JRadioButton rbtModeloPesquisar;
    private javax.swing.JRadioButton rbtNomeClientePesquisar;
    private javax.swing.JRadioButton rbtNomePesquisar;
    private javax.swing.JRadioButton rbtPlacaPesquisar;
    private javax.swing.JRadioButton rbtTelefonePesquisar;
    private javax.swing.JTable tblClientes;
    private javax.swing.JTable tblVeiculos;
    private javax.swing.JTextField txtClientePesquisar;
    private javax.swing.JTextField txtVeiculoCor;
    private javax.swing.JTextField txtVeiculoId;
    private javax.swing.JTextField txtVeiculoIdCliente;
    private javax.swing.JTextField txtVeiculoMarca;
    private javax.swing.JTextField txtVeiculoModelo;
    private javax.swing.JTextField txtVeiculoPesquisar;
    private javax.swing.JTextField txtVeiculoPlaca;
    // End of variables declaration//GEN-END:variables

}
