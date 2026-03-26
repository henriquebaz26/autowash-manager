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
import br.com.autowashmanager.service.ValidadorOS;

/**
 *
 * @author h24he
 */
public class TelaEmissaoOS extends javax.swing.JFrame {

    Connection conexao = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(TelaEmissaoOS.class.getName());

    /**
     * Creates new form TelaEmissaoOS
     */
    public TelaEmissaoOS() {
        initComponents();
        conexao = ModuloConexao.conector();
    }

    // metodo para adicionar os
    private void adicionar() {
        String sql = "insert into service_order (entry_time, delivery_time, price, status, vehicle_id, employee_id, notes, service_date) values (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            String horarioEntrada = txtOSHorarioEntrada.getText().trim();
            String horarioEntrega = txtOSHorarioEntrega.getText().trim();
            String preco = txtOSPreco.getText().trim();
            String status = cboOSStatus.getSelectedItem().toString();
            String idVeiculo = txtOSIdVeiculo.getText().trim();
            String idFuncionario = txtOSIdFuncionario.getText().trim();
            String descricao = txtOSDescricao.getText().trim();
            String dataServico = txtOSData.getText().trim();

            // campos obrigatórios
            if (horarioEntrada.isEmpty() || horarioEntrega.isEmpty() || preco.isEmpty()
                    || status.isEmpty() || idVeiculo.isEmpty()
                    || idFuncionario.isEmpty() || descricao.isEmpty()
                    || dataServico.isEmpty()) {

                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios");
                return;
            }

            // horários (formato)
            if (!ValidadorOS.validarHorario(horarioEntrada)) {
                JOptionPane.showMessageDialog(null, "Horário de entrada inválido (HH:mm)");
                return;
            }

            if (!ValidadorOS.validarHorario(horarioEntrega)) {
                JOptionPane.showMessageDialog(null, "Horário de entrega inválido (HH:mm)");
                return;
            }

            // regra de negócio (ordem)
            if (!ValidadorOS.validarOrdemHorario(horarioEntrada, horarioEntrega)) {
                JOptionPane.showMessageDialog(null, "Entrega deve ser após entrada");
                return;
            }

            // preço
            if (!ValidadorOS.validarPreco(preco)) {
                JOptionPane.showMessageDialog(null, "Preço inválido (ex: 100 ou 100.50)");
                return;
            }

            // data
            if (!ValidadorOS.validarDataFormato(dataServico)) {
                JOptionPane.showMessageDialog(null, "Formato de data inválido (YYYY-MM-DD)");
                return;
            }

            if (!ValidadorOS.validarDataReal(dataServico)) {
                JOptionPane.showMessageDialog(null, "Data inexistente");
                return;
            }

            // IDs numéricos
            int veiculoId;
            int funcionarioId;

            try {
                veiculoId = Integer.parseInt(idVeiculo);
                funcionarioId = Integer.parseInt(idFuncionario);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "IDs devem ser numéricos");
                return;
            }

            // existência
            if (!veiculoExiste(veiculoId)) {
                JOptionPane.showMessageDialog(null, "Veículo não encontrado");
                return;
            }

            if (!funcionarioExiste(funcionarioId)) {
                JOptionPane.showMessageDialog(null, "Funcionário não encontrado");
                return;
            }

            // insert
            pst = conexao.prepareStatement(sql);
            pst.setString(1, horarioEntrada);
            pst.setString(2, horarioEntrega);
            pst.setString(3, preco);
            pst.setString(4, status);
            pst.setString(5, idVeiculo);
            pst.setString(6, idFuncionario);
            pst.setString(7, descricao);
            pst.setString(8, dataServico);

            int adicionado = pst.executeUpdate();

            if (adicionado > 0) {
                JOptionPane.showMessageDialog(null, "OS emitida com sucesso");
                limpar();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private boolean veiculoExiste(int id) {
        String sql = "select id from vehicle where id = ?";

        try {
            PreparedStatement pst = conexao.prepareStatement(sql);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            return rs.next();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
            return false;
        }
    }

    private boolean funcionarioExiste(int id) {
        String sql = "select id from employee where id = ?";

        try {
            PreparedStatement pst = conexao.prepareStatement(sql);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            return rs.next();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
            return false;
        }
    }

    // método para pesquisar os pelo id, data do serviço, preço, id do veículo e modelo do veiculo
    private void pesquisar_os() {

        String coluna = "";
        boolean joinVehicle = false;
        boolean joinEmployee = false;
        boolean isNumero = false;

        if (rbtIdPesquisar.isSelected()) {
            coluna = "o.id";
            isNumero = true;
        } else if (rbtDataServicoPesquisar.isSelected()) {
            coluna = "o.service_date";
        } else if (rbtPrecoPesquisar.isSelected()) {
            coluna = "o.price";
            isNumero = true;
        } else if (rbtIdVeiculoPesquisar.isSelected()) {
            coluna = "o.vehicle_id";
            isNumero = true;
        } else if (rbtNomeVeiculoPesquisar.isSelected()) {
            coluna = "v.model";
            joinVehicle = true;
        } else if (rbtNomeFuncionarioPesquisar.isSelected()) {
            coluna = "e.name";
            joinEmployee = true;
        } else {
            JOptionPane.showMessageDialog(null, "Selecione um filtro!");
            return;
        }

        try {
            String texto = txtOSPesquisar.getText().trim();

            String sql;

            if (joinVehicle || joinEmployee) {

                sql = "SELECT o.id as id, o.service_date as dataServico, o.entry_time as horarioEntrada, o.delivery_time as horarioEntrega, o.price as preco, o.status as status, v.model as veiculo, e.name as funcionario, o.notes as descricao FROM service_order o LEFT JOIN vehicle v ON o.vehicle_id = v.id LEFT JOIN employee e ON o.employee_id = e.id WHERE " + coluna + " LIKE ?";

                pst = conexao.prepareStatement(sql);
                pst.setString(1, "%" + texto + "%");

            } else if (isNumero) {

                sql = "SELECT o.id as id, o.service_date as dataServico, o.entry_time as horarioEntrada, o.delivery_time as horarioEntrega, o.price as preco, o.status as status, v.model as veiculo, e.name as funcionario, o.notes as descricao FROM service_order o LEFT JOIN vehicle v ON o.vehicle_id = v.id LEFT JOIN employee e ON o.employee_id = e.id WHERE " + coluna + " = ?";

                pst = conexao.prepareStatement(sql);
                pst.setString(1, texto);

            } else {

                sql = "SELECT o.id as id, o.service_date as dataServico, o.entry_time as horarioEntrada, o.delivery_time as horarioEntrega, o.price as preco, o.status as status, v.model as veiculo, e.name as funcionario, o.notes as descricao FROM service_order o LEFT JOIN vehicle v ON o.vehicle_id = v.id LEFT JOIN employee e ON o.employee_id = e.id WHERE " + coluna + " LIKE ?";

                pst = conexao.prepareStatement(sql);
                pst.setString(1, "%" + texto + "%");
            }

            rs = pst.executeQuery();
            tblOS.setModel(DbUtils.resultSetToTableModel(rs));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    // método para pesquisar veiculo pela placa, marca, modelo com filtro
    private void pesquisar_veiculo() {

        String coluna = "";

        if (rbtPlacaPesquisar.isSelected()) {
            coluna = "v.plate";
        } else if (rbtMarcaPesquisar.isSelected()) {
            coluna = "v.brand";
        } else if (rbtModeloPesquisar.isSelected()) {
            coluna = "v.model";
        } else {
            JOptionPane.showMessageDialog(null, "Selecione um filtro!");
            return;
        }

        try {
            String texto = txtVeiculoPesquisar.getText().trim();

            String sql;

            sql = "SELECT v.id as id, v.plate as placa, v.brand as marca, v.model as modelo FROM vehicle v WHERE " + coluna + " LIKE ?";

            pst = conexao.prepareStatement(sql);
            pst.setString(1, "%" + texto + "%");

            rs = pst.executeQuery();

            // a linha abaixo usa a classe DbUtils para preencher a tabela
            tblVeiculos.setModel(DbUtils.resultSetToTableModel(rs));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    // método para pesquisar funcionario pelo nome, telefone e atividade com filtro
    private void pesquisar_funcionario() {

        String coluna = "";

        if (rbtNomeFuncionarioPesquisar2.isSelected()) {
            coluna = "f.name";
        } else if (rbtTelefoneFuncionarioPesquisar.isSelected()) {
            coluna = "f.phone";
        } else if (rbtAtividadeFuncionarioPesquisar.isSelected()) {
            coluna = "f.role";
        } else {
            JOptionPane.showMessageDialog(null, "Selecione um filtro!");
            return;
        }

        try {
            String texto = txtFuncionarioPesquisar.getText().trim();

            String sql;

            sql = "SELECT f.id as id, f.name as nome, f.phone as telefone, f.role as atividade FROM employee f WHERE " + coluna + " LIKE ?";

            pst = conexao.prepareStatement(sql);
            pst.setString(1, "%" + texto + "%");

            rs = pst.executeQuery();

            // a linha abaixo usa a classe DbUtils para preencher a tabela
            tblFuncionarios.setModel(DbUtils.resultSetToTableModel(rs));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    // metodo para setar o campos do formulario
    private void setar_campos() {

        try {

            limpar_campos();

            int setar = tblOS.getSelectedRow();
            String idOS = tblOS.getModel().getValueAt(setar, 0).toString();

            String sql = "SELECT * FROM service_order WHERE id = ?";
            pst = conexao.prepareStatement(sql);
            pst.setString(1, idOS);
            rs = pst.executeQuery();

            if (rs.next()) {

                txtOSId.setText(rs.getString("id"));
                txtOSData.setText(rs.getString("service_date"));
                txtOSHorarioEntrada.setText(rs.getString("entry_time"));
                txtOSHorarioEntrega.setText(rs.getString("delivery_time"));
                txtOSPreco.setText(rs.getString("price"));
                cboOSStatus.setSelectedItem(rs.getString("status"));
                txtOSIdVeiculo.setText(rs.getString("vehicle_id"));
                txtOSIdFuncionario.setText(rs.getString("employee_id"));
                txtOSDescricao.setText(rs.getString("notes"));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    // metodo para setar o campo de id do veiculo
    private void setar_campo_id_veiculo() {

        txtOSIdVeiculo.setText(null);

        int setar = tblVeiculos.getSelectedRow();

        txtOSIdVeiculo.setText(tblVeiculos.getModel().getValueAt(setar, 0).toString());

    }

    // metodo para setar o campo de id do funcionario
    private void setar_campo_id_funcionario() {

        txtOSIdFuncionario.setText(null);

        int setar = tblFuncionarios.getSelectedRow();

        txtOSIdFuncionario.setText(tblFuncionarios.getModel().getValueAt(setar, 0).toString());

    }

    // metodo para limpar os campos do formulário
    private void limpar() {
        txtOSId.setText(null);
        txtOSData.setText(null);
        txtOSHorarioEntrada.setText(null);
        txtOSHorarioEntrega.setText(null);
        txtOSPreco.setText(null);
        cboOSStatus.setSelectedIndex(0);
        txtOSIdVeiculo.setText(null);
        txtOSIdFuncionario.setText(null);
        txtOSDescricao.setText(null);
        txtVeiculoPesquisar.setText(null);

        ((DefaultTableModel) tblVeiculos.getModel()).setRowCount(0);
        ((DefaultTableModel) tblFuncionarios.getModel()).setRowCount(0);
        ((DefaultTableModel) tblOS.getModel()).setRowCount(0);

        btnOSUpdate.setEnabled(false);
    }

    private void alterar() {
        String sql = "update service_order set entry_time=?, delivery_time=?, price=?, status=?, vehicle_id=?, employee_id=?, notes=?, service_date=? where id=?";

        try {
            String id = txtOSId.getText().trim();
            String horarioEntrada = txtOSHorarioEntrada.getText().trim();
            String horarioEntrega = txtOSHorarioEntrega.getText().trim();
            String preco = txtOSPreco.getText().trim();
            String status = cboOSStatus.getSelectedItem().toString();
            String idVeiculo = txtOSIdVeiculo.getText().trim();
            String idFuncionario = txtOSIdFuncionario.getText().trim();
            String descricao = txtOSDescricao.getText().trim();
            String dataServico = txtOSData.getText().trim();

            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Selecione uma OS para alterar");
                return;
            }

            if (horarioEntrada.isEmpty() || horarioEntrega.isEmpty() || preco.isEmpty()
                    || status.isEmpty() || idVeiculo.isEmpty()
                    || idFuncionario.isEmpty() || descricao.isEmpty()
                    || dataServico.isEmpty()) {

                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios");
                return;
            }

            if (!ValidadorOS.validarHorario(horarioEntrada)) {
                JOptionPane.showMessageDialog(null, "Horário entrada inválido");
                return;
            }

            if (!ValidadorOS.validarHorario(horarioEntrega)) {
                JOptionPane.showMessageDialog(null, "Horário entrega inválido");
                return;
            }

            if (!ValidadorOS.validarOrdemHorario(horarioEntrada, horarioEntrega)) {
                JOptionPane.showMessageDialog(null, "Entrega deve ser após entrada");
                return;
            }

            if (!ValidadorOS.validarPreco(preco)) {
                JOptionPane.showMessageDialog(null, "Preço inválido");
                return;
            }

            if (!ValidadorOS.validarDataFormato(dataServico)) {
                JOptionPane.showMessageDialog(null, "Formato de data inválido");
                return;
            }

            if (!ValidadorOS.validarDataReal(dataServico)) {
                JOptionPane.showMessageDialog(null, "Data inválida");
                return;
            }

            int veiculoId;
            int funcionarioId;
            int osId;

            try {
                veiculoId = Integer.parseInt(idVeiculo);
                funcionarioId = Integer.parseInt(idFuncionario);
                osId = Integer.parseInt(id);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "IDs devem ser numéricos");
                return;
            }

            if (!veiculoExiste(veiculoId)) {
                JOptionPane.showMessageDialog(null, "Veículo não encontrado");
                return;
            }

            if (!funcionarioExiste(funcionarioId)) {
                JOptionPane.showMessageDialog(null, "Funcionário não encontrado");
                return;
            }

            pst = conexao.prepareStatement(sql);
            pst.setString(1, horarioEntrada);
            pst.setString(2, horarioEntrega);
            pst.setString(3, preco);
            pst.setString(4, status);
            pst.setString(5, idVeiculo);
            pst.setString(6, idFuncionario);
            pst.setString(7, descricao);
            pst.setString(8, dataServico);
            pst.setInt(9, osId);

            int atualizado = pst.executeUpdate();

            if (atualizado > 0) {
                JOptionPane.showMessageDialog(null, "OS atualizada com sucesso");
                limpar();
                btnOSCreate.setEnabled(true);
            } else {
                JOptionPane.showMessageDialog(null, "Nenhuma OS foi alterada");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    // método responsável pela remoção de OS
    private void remover() {
        // VALIDAÇÃO: verificar se alguma OS foi selecionado
        if (txtOSId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Selecione uma OS para excluir");
            return;
        }

        //a estrutura abaixo confirma a remoção do veículo
        int confirma = JOptionPane.showConfirmDialog(null, "Tem certeza que deseja remover esta OS?", "ATENÇÃO", JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_OPTION) {
            String sql = "delete from service_order where id=?";
            try {
                pst = conexao.prepareStatement(sql);
                pst.setString(1, txtOSId.getText());
                int apagado = pst.executeUpdate();

                if (apagado > 0) {
                    JOptionPane.showMessageDialog(null, "OS removida com sucesso");
                    limpar();

                    btnOSCreate.setEnabled(true);
                    btnOSUpdate.setEnabled(false);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }
    
    private void limpar_campos() {
        
        txtOSId.setText(null);
        txtOSData.setText(null);
        txtOSHorarioEntrada.setText(null);
        txtOSHorarioEntrega.setText(null);
        txtOSPreco.setText(null);
        cboOSStatus.setSelectedIndex(0);
        txtOSIdVeiculo.setText(null);
        txtOSIdFuncionario.setText(null);
        txtOSDescricao.setText(null);  
        
        // a linha abaixo desabilita os botões de adicionar e habilita o de atualizar
        btnOSCreate.setEnabled(true);
        btnOSUpdate.setEnabled(false);
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rbtFiltroPesquisarOS = new javax.swing.ButtonGroup();
        rbtFiltroPesquisarVeiculo = new javax.swing.ButtonGroup();
        rbtFiltroPesquisarFuncionario = new javax.swing.ButtonGroup();
        txtOSPesquisar = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        rbtIdPesquisar = new javax.swing.JRadioButton();
        rbtDataServicoPesquisar = new javax.swing.JRadioButton();
        rbtPrecoPesquisar = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblOS = new javax.swing.JTable();
        rbtIdVeiculoPesquisar = new javax.swing.JRadioButton();
        rbtNomeVeiculoPesquisar = new javax.swing.JRadioButton();
        rbtNomeFuncionarioPesquisar = new javax.swing.JRadioButton();
        jLabel4 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        rbtPlacaPesquisar = new javax.swing.JRadioButton();
        rbtModeloPesquisar = new javax.swing.JRadioButton();
        rbtMarcaPesquisar = new javax.swing.JRadioButton();
        txtVeiculoPesquisar = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        tblVeiculos = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        rbtNomeFuncionarioPesquisar2 = new javax.swing.JRadioButton();
        rbtAtividadeFuncionarioPesquisar = new javax.swing.JRadioButton();
        rbtTelefoneFuncionarioPesquisar = new javax.swing.JRadioButton();
        txtFuncionarioPesquisar = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        tblFuncionarios = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        txtOSId = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtOSData = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtOSHorarioEntrega = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtOSHorarioEntrada = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtOSPreco = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        cboOSStatus = new javax.swing.JComboBox<>();
        jLabel11 = new javax.swing.JLabel();
        txtOSIdVeiculo = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        txtOSIdFuncionario = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        btnOSCreate = new javax.swing.JButton();
        btnOSUpdate = new javax.swing.JButton();
        btnOSDelete = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtOSDescricao = new javax.swing.JTextArea();
        btnLimparCampos = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("AutoWash Manager - Tela de Emissão de Ordem de Serviço");

        txtOSPesquisar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtOSPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        txtOSPesquisar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtOSPesquisarKeyReleased(evt);
            }
        });

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/loupe.png"))); // NOI18N
        jLabel1.setText("jLabel1");

        rbtFiltroPesquisarOS.add(rbtIdPesquisar);
        rbtIdPesquisar.setText("id");
        rbtIdPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        rbtFiltroPesquisarOS.add(rbtDataServicoPesquisar);
        rbtDataServicoPesquisar.setSelected(true);
        rbtDataServicoPesquisar.setText("data do serviço");
        rbtDataServicoPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        rbtFiltroPesquisarOS.add(rbtPrecoPesquisar);
        rbtPrecoPesquisar.setText("preço");
        rbtPrecoPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Pesquisar pelo:");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel2.setText("* Campos Obrigatórios");

        tblOS = new javax.swing.JTable() {
            public boolean isCellEditable(int rolIndex, int colIndex) {
                return false;
            }
        };
        tblOS.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "id", "data", "horário entrada", "horário de entrega", "preço", "status", "modelo do veículo", "nome funcionário", "descrição"
            }
        ));
        tblOS.setFocusable(false);
        tblOS.getTableHeader().setReorderingAllowed(false);
        tblOS.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblOSMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblOS);

        rbtFiltroPesquisarOS.add(rbtIdVeiculoPesquisar);
        rbtIdVeiculoPesquisar.setText("id do veículo");

        rbtFiltroPesquisarOS.add(rbtNomeVeiculoPesquisar);
        rbtNomeVeiculoPesquisar.setText("modelo do veiculo");

        rbtFiltroPesquisarOS.add(rbtNomeFuncionarioPesquisar);
        rbtNomeFuncionarioPesquisar.setText("nome do funcionário");
        rbtNomeFuncionarioPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Dados da Emissão");

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Veículo"));

        jLabel14.setText("Pesquisar pelo:");

        rbtFiltroPesquisarVeiculo.add(rbtPlacaPesquisar);
        rbtPlacaPesquisar.setText("placa");
        rbtPlacaPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        rbtFiltroPesquisarVeiculo.add(rbtModeloPesquisar);
        rbtModeloPesquisar.setSelected(true);
        rbtModeloPesquisar.setText("modelo");
        rbtModeloPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        rbtFiltroPesquisarVeiculo.add(rbtMarcaPesquisar);
        rbtMarcaPesquisar.setText("marca");
        rbtMarcaPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        txtVeiculoPesquisar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtVeiculoPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        txtVeiculoPesquisar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtVeiculoPesquisarKeyReleased(evt);
            }
        });

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/loupe.png"))); // NOI18N

        tblVeiculos= new javax.swing.JTable() {
            public boolean isCellEditable(int rolIndex, int colIndex) {
                return false;
            }
        };
        tblVeiculos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "id", "placa", "marca", "modelo"
            }
        ));
        tblVeiculos.setFocusable(false);
        tblVeiculos.getTableHeader().setReorderingAllowed(false);
        tblVeiculos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblVeiculosMouseClicked(evt);
            }
        });
        jScrollPane6.setViewportView(tblVeiculos);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel14)
                        .addGap(18, 18, 18)
                        .addComponent(rbtPlacaPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rbtMarcaPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rbtModeloPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(txtVeiculoPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel15)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbtPlacaPesquisar)
                    .addComponent(rbtMarcaPesquisar)
                    .addComponent(rbtModeloPesquisar)
                    .addComponent(jLabel14))
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtVeiculoPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel15)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Funcionário"));

        jLabel16.setText("Pesquisar pelo:");

        rbtFiltroPesquisarFuncionario.add(rbtNomeFuncionarioPesquisar2);
        rbtNomeFuncionarioPesquisar2.setSelected(true);
        rbtNomeFuncionarioPesquisar2.setText("nome");
        rbtNomeFuncionarioPesquisar2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        rbtFiltroPesquisarFuncionario.add(rbtAtividadeFuncionarioPesquisar);
        rbtAtividadeFuncionarioPesquisar.setText("atividade");
        rbtAtividadeFuncionarioPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        rbtFiltroPesquisarFuncionario.add(rbtTelefoneFuncionarioPesquisar);
        rbtTelefoneFuncionarioPesquisar.setText("telefone");
        rbtTelefoneFuncionarioPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        txtFuncionarioPesquisar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtFuncionarioPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        txtFuncionarioPesquisar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtFuncionarioPesquisarKeyReleased(evt);
            }
        });

        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/loupe.png"))); // NOI18N

        tblFuncionarios = new javax.swing.JTable() {
            public boolean isCellEditable(int rolIndex, int colIndex) {
                return false;
            }
        };
        tblFuncionarios.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "id", "nome", "telefone", "atividade"
            }
        ));
        tblFuncionarios.setFocusable(false);
        tblFuncionarios.getTableHeader().setReorderingAllowed(false);
        tblFuncionarios.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblFuncionariosMouseClicked(evt);
            }
        });
        jScrollPane7.setViewportView(tblFuncionarios);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel16)
                        .addGap(18, 18, 18)
                        .addComponent(rbtNomeFuncionarioPesquisar2, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rbtTelefoneFuncionarioPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rbtAtividadeFuncionarioPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(txtFuncionarioPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel17)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbtNomeFuncionarioPesquisar2)
                    .addComponent(rbtTelefoneFuncionarioPesquisar)
                    .addComponent(rbtAtividadeFuncionarioPesquisar)
                    .addComponent(jLabel16))
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtFuncionarioPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel17)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(7, Short.MAX_VALUE))
        );

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Id");

        txtOSId.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtOSId.setToolTipText("");
        txtOSId.setEnabled(false);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("* Date");

        txtOSData.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtOSData.setToolTipText("");
        txtOSData.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("* Horário Entrada");

        txtOSHorarioEntrega.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setText("* Horário Entrega");

        txtOSHorarioEntrada.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel9.setText("* Preço");

        txtOSPreco.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtOSPreco.setToolTipText("");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel10.setText("* Status");

        cboOSStatus.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cboOSStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Aguardando", "Na Limpeza Interna", "Na Lavação Externa", "Remoção de Cimento", "Finalizado" }));
        cboOSStatus.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel11.setText("* Id do Veículo");

        txtOSIdVeiculo.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel13.setText("* Id do Funcionário");

        txtOSIdFuncionario.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel12.setText("* Descrição");

        btnOSCreate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/createOS.png"))); // NOI18N
        btnOSCreate.setToolTipText("Adicionar");
        btnOSCreate.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnOSCreate.addActionListener(this::btnOSCreateActionPerformed);

        btnOSUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/updateOS.png"))); // NOI18N
        btnOSUpdate.setToolTipText("Atualizar");
        btnOSUpdate.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnOSUpdate.setEnabled(false);
        btnOSUpdate.addActionListener(this::btnOSUpdateActionPerformed);

        btnOSDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/deleteOS.png"))); // NOI18N
        btnOSDelete.setToolTipText("Deletar");
        btnOSDelete.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnOSDelete.addActionListener(this::btnOSDeleteActionPerformed);

        txtOSDescricao.setColumns(20);
        txtOSDescricao.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtOSDescricao.setRows(5);
        jScrollPane2.setViewportView(txtOSDescricao);

        btnLimparCampos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/clear.png"))); // NOI18N
        btnLimparCampos.setToolTipText("Limpar Campos");
        btnLimparCampos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnLimparCampos.addActionListener(this::btnLimparCamposActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(62, 62, 62)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane1)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(txtOSPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel2))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(18, 18, 18)
                                .addComponent(rbtIdPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rbtDataServicoPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rbtPrecoPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rbtIdVeiculoPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rbtNomeVeiculoPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rbtNomeFuncionarioPesquisar)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(60, 60, 60))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(84, 84, 84)
                                .addComponent(btnLimparCampos)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                .addComponent(jLabel9)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(txtOSPreco, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel6)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(txtOSId, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(72, 72, 72)
                                                .addComponent(jLabel5)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(txtOSData, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(75, 75, 75)
                                                .addComponent(jLabel7)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(jLabel10)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(cboOSStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(77, 77, 77)))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(txtOSHorarioEntrada, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(94, 94, 94)
                                                .addComponent(jLabel8)
                                                .addGap(18, 18, 18)
                                                .addComponent(txtOSHorarioEntrega, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(jLabel11)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(txtOSIdVeiculo, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(92, 92, 92)
                                                .addComponent(jLabel13)
                                                .addGap(18, 18, 18)
                                                .addComponent(txtOSIdFuncionario, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel12)
                                        .addGap(18, 18, 18)
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 523, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(33, 33, 33)
                                        .addComponent(btnOSCreate)
                                        .addGap(104, 104, 104)
                                        .addComponent(btnOSUpdate)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 104, Short.MAX_VALUE)
                                        .addComponent(btnOSDelete)))))
                        .addGap(55, 55, 55))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(rbtNomeFuncionarioPesquisar)
                            .addComponent(rbtIdPesquisar)
                            .addComponent(rbtDataServicoPesquisar)
                            .addComponent(rbtPrecoPesquisar)
                            .addComponent(rbtIdVeiculoPesquisar)
                            .addComponent(rbtNomeVeiculoPesquisar))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtOSPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel3))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(btnLimparCampos)
                                .addGap(54, 54, 54))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel6)
                        .addComponent(txtOSId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtOSHorarioEntrega, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel8))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel5)
                        .addComponent(txtOSData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel7)
                        .addComponent(txtOSHorarioEntrada, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(31, 31, 31)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(txtOSPreco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(txtOSIdVeiculo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(cboOSStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(txtOSIdFuncionario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(94, 94, 94)
                                .addComponent(jLabel12))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(55, 55, 55)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(btnOSUpdate)
                                    .addComponent(btnOSCreate)
                                    .addComponent(btnOSDelete))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(19, 19, 19))))
        );

        getAccessibleContext().setAccessibleDescription("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void txtOSPesquisarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtOSPesquisarKeyReleased
        // Chamando o método de pesquisar os

        pesquisar_os();
    }//GEN-LAST:event_txtOSPesquisarKeyReleased

    private void tblOSMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblOSMouseClicked
        // Chamando o método de setar campos

        setar_campos();
    }//GEN-LAST:event_tblOSMouseClicked

    private void txtVeiculoPesquisarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtVeiculoPesquisarKeyReleased
        // Chamando o método de pesquisar veiculos

        pesquisar_veiculo();
    }//GEN-LAST:event_txtVeiculoPesquisarKeyReleased

    private void tblVeiculosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblVeiculosMouseClicked
        // Chamando o método setar campo id veiculo

        setar_campo_id_veiculo();
    }//GEN-LAST:event_tblVeiculosMouseClicked

    private void txtFuncionarioPesquisarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtFuncionarioPesquisarKeyReleased
        // Chamando o método de pesquisar funcionário

        pesquisar_funcionario();
    }//GEN-LAST:event_txtFuncionarioPesquisarKeyReleased

    private void tblFuncionariosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblFuncionariosMouseClicked
        // Chamando o metodo de setar campo id funcionario:

        setar_campo_id_funcionario();
    }//GEN-LAST:event_tblFuncionariosMouseClicked

    private void btnOSCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOSCreateActionPerformed
        // Chamando o metodo de adicionar uma OS

        adicionar();
    }//GEN-LAST:event_btnOSCreateActionPerformed

    private void btnOSUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOSUpdateActionPerformed
        // Chamando o método de alterar informações de uma OS

        alterar();
    }//GEN-LAST:event_btnOSUpdateActionPerformed

    private void btnOSDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOSDeleteActionPerformed
        // Chamando o método de exclusão de uma OS

        remover();
    }//GEN-LAST:event_btnOSDeleteActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new TelaEmissaoOS().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLimparCampos;
    private javax.swing.JButton btnOSCreate;
    private javax.swing.JButton btnOSDelete;
    private javax.swing.JButton btnOSUpdate;
    private javax.swing.JComboBox<String> cboOSStatus;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JRadioButton rbtAtividadeFuncionarioPesquisar;
    private javax.swing.JRadioButton rbtDataServicoPesquisar;
    private javax.swing.ButtonGroup rbtFiltroPesquisarFuncionario;
    private javax.swing.ButtonGroup rbtFiltroPesquisarOS;
    private javax.swing.ButtonGroup rbtFiltroPesquisarVeiculo;
    private javax.swing.JRadioButton rbtIdPesquisar;
    private javax.swing.JRadioButton rbtIdVeiculoPesquisar;
    private javax.swing.JRadioButton rbtMarcaPesquisar;
    private javax.swing.JRadioButton rbtModeloPesquisar;
    private javax.swing.JRadioButton rbtNomeFuncionarioPesquisar;
    private javax.swing.JRadioButton rbtNomeFuncionarioPesquisar2;
    private javax.swing.JRadioButton rbtNomeVeiculoPesquisar;
    private javax.swing.JRadioButton rbtPlacaPesquisar;
    private javax.swing.JRadioButton rbtPrecoPesquisar;
    private javax.swing.JRadioButton rbtTelefoneFuncionarioPesquisar;
    private javax.swing.JTable tblFuncionarios;
    private javax.swing.JTable tblOS;
    private javax.swing.JTable tblVeiculos;
    private javax.swing.JTextField txtFuncionarioPesquisar;
    private javax.swing.JTextField txtOSData;
    private javax.swing.JTextArea txtOSDescricao;
    private javax.swing.JTextField txtOSHorarioEntrada;
    private javax.swing.JTextField txtOSHorarioEntrega;
    private javax.swing.JTextField txtOSId;
    private javax.swing.JTextField txtOSIdFuncionario;
    private javax.swing.JTextField txtOSIdVeiculo;
    private javax.swing.JTextField txtOSPesquisar;
    private javax.swing.JTextField txtOSPreco;
    private javax.swing.JTextField txtVeiculoPesquisar;
    // End of variables declaration//GEN-END:variables
}
