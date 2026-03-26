/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package br.com.autowashmanager.telas;

import br.com.autowashmanager.dal.ModuloConexao;
import br.com.autowashmanager.service.ValidadorConta;
import br.com.autowashmanager.util.DbUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author h24he
 */
public class TelaGestaoContas extends javax.swing.JFrame {

    Connection conexao = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(TelaGestaoContas.class.getName());

    /**
     * Creates new form TelaGestaoContas
     */
    public TelaGestaoContas() {
        initComponents();
        conexao = ModuloConexao.conector();
    }

    // metodo responsavel por atualizar status das contas em comparação a data do sistema
    private void atualizar_status_dia() throws SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate hoje = LocalDate.now();
        String selectSql = "SELECT id, due_date, payment_date from expense";
        String updateSql = "UPDATE expense SET status = ? WHERE id = ?";

        try (PreparedStatement pstSelect = conexao.prepareStatement(selectSql); ResultSet rsLocal = pstSelect.executeQuery(); PreparedStatement pstUpdate = conexao.prepareStatement(updateSql)) {

            while (rsLocal.next()) {
                int id = rsLocal.getInt("id");
                String dataVencimento = rsLocal.getString("due_date");
                String dataPagamento = rsLocal.getString("payment_date");

                LocalDate vencimento = LocalDate.parse(dataVencimento, formatter);
                String status;

                if (dataPagamento == null || dataPagamento.isEmpty()) {
                    status = vencimento.isBefore(hoje) ? "VENCIDO" : "PARA PAGAMENTO";
                } else {
                    status = "PAGO";
                }

                pstUpdate.setString(1, status);
                pstUpdate.setInt(2, id);
                pstUpdate.executeUpdate();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao atualizar status: " + e);
        }
    }

    // método responsavel por adicionar novas contas
    private void adicionar_nova_conta() {
        String sql = "insert into expense (description, value, due_date, payment_date, status) values (?, ?, ?, ?, ?)";

        try {
            String descricao = txtContasDescricao.getText().trim();
            String valor = txtContasValor.getText().trim();
            String dataVenc = txtContasDataVencimento.getText().trim();
            String dataPag = txtContasDataPagamento.getText().trim();

            // CAMPOS OBRIGATÓRIOS
            if (!ValidadorConta.validarTexto(descricao)
                    || !ValidadorConta.validarTexto(valor)
                    || !ValidadorConta.validarTexto(dataVenc)) {

                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios");
                return;
            }

            // VALOR - FORMATO
            if (!ValidadorConta.validarValor(valor)) {
                JOptionPane.showMessageDialog(null, "Valor inválido (ex: 100 ou 100.50)");
                return;
            }

            // VALOR - MAIOR QUE ZERO
            if (!ValidadorConta.valorMaiorQueZero(valor)) {
                JOptionPane.showMessageDialog(null, "Valor deve ser maior que zero");
                return;
            }

            // DATA VENCIMENTO
            if (!ValidadorConta.validarData(dataVenc)) {
                JOptionPane.showMessageDialog(null, "Data de vencimento inválida (formato: dd/MM/yyyy)");
                return;
            }

            // DATA PAGAMENTO (opcional)
            if (!dataPag.isEmpty() && !ValidadorConta.validarData(dataPag)) {
                JOptionPane.showMessageDialog(null, "Data de pagamento inválida (formato: yyyy-MM-dd)");
                return;
            }

            // STATUS DA CONTA
            LocalDate hoje = LocalDate.now();

            DateTimeFormatter telaFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter bancoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // datas obrigatórias
            LocalDate dataVencimento = LocalDate.parse(dataVenc, telaFormatter);

            // data pagamento (opcional)
            LocalDate dataPagamento = null;
            if (!dataPag.isEmpty()) {
                dataPagamento = LocalDate.parse(dataPag, telaFormatter);
            }

            // STATUS
            String status;

            if (dataPagamento != null) {
                status = "PAGO";
            } else if (dataVencimento.isBefore(hoje)) {
                status = "VENCIDO";
            } else {
                status = "PARA PAGAMENTO";
            }

            // CONVERSÃO PARA BANCO
            String dataVencimentoBanco = dataVencimento.format(bancoFormatter);

            String dataPagamentoBanco = null;
            if (dataPagamento != null) {
                dataPagamentoBanco = dataPagamento.format(bancoFormatter);
            }

            // INSERT
            pst = conexao.prepareStatement(sql);
            pst.setString(1, descricao);
            pst.setDouble(2, Double.parseDouble(valor));
            pst.setString(3, dataVencimentoBanco);
            pst.setString(4, dataPagamentoBanco);
            pst.setString(5, status);

            int adicionado = pst.executeUpdate();

            if (adicionado > 0) {
                JOptionPane.showMessageDialog(null, "Conta cadastrada com sucesso");
                limpar();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    // método responsavel por vericar existencia de conta
    private boolean contaExiste(int id) {
        String sql = "select id from expense where id = ?";

        try {
            pst = conexao.prepareStatement(sql);
            pst.setInt(1, id);
            rs = pst.executeQuery();
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }

    // metodo responsavel por pesquisar contas e exibir na tabela
    private void pesquisar_conta() {

        try {
            String texto = txtContasPesquisar.getText().trim();
            String statusSelecionado = cboContasStatusFiltrar.getSelectedItem().toString();

            String sql = "select id as id, description as descricao, value as valor, due_date as vencimento, payment_date as pagamento, status as status from expense where description like ?";

            // FILTRO POR STATUS
            if (!statusSelecionado.equals("TODOS")) {
                sql += " and status = ?";
            }

            pst = conexao.prepareStatement(sql);

            pst.setString(1, "%" + texto + "%");

            // se não for TODOS, adiciona o status no SQL
            if (!statusSelecionado.equals("TODOS")) {
                pst.setString(2, statusSelecionado);
            }

            rs = pst.executeQuery();

            // preenche tabela
            tblContas.setModel(DbUtils.resultSetToTableModel(rs));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void setar_campos_conta() {

        txtContasId.setText(null);
        txtContasDescricao.setText(null);
        txtContasValor.setText(null);
        txtContasDataVencimento.setText(null);
        txtContasDataPagamento.setText(null);

        int setar = tblContas.getSelectedRow();

        txtContasId.setText(tblContas.getModel().getValueAt(setar, 0).toString());
        txtContasDescricao.setText(tblContas.getModel().getValueAt(setar, 1).toString());
        txtContasValor.setText(tblContas.getModel().getValueAt(setar, 2).toString());

        // DATA VENCIMENTO
        String dataVenc = tblContas.getModel().getValueAt(setar, 3).toString();
        txtContasDataVencimento.setText(formatarDataTela(dataVenc));

        // DATA PAGAMENTO (pode ser null)
        Object dataPagObj = tblContas.getModel().getValueAt(setar, 4);
        if (dataPagObj != null) {
            txtContasDataPagamento.setText(formatarDataTela(dataPagObj.toString()));
        } else {
            txtContasDataPagamento.setText("");
        }

        txtContasStatus.setText(tblContas.getModel().getValueAt(setar, 5).toString());

        // BOTÕES
        btnContasNova.setEnabled(false);
        btnContasAtualizar.setEnabled(true);
    }

    private String formatarDataTela(String dataBanco) {
        DateTimeFormatter bancoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter telaFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate data = LocalDate.parse(dataBanco, bancoFormatter);
        return data.format(telaFormatter);
    }

    private void limpar() {
        txtContasId.setText(null);
        txtContasDescricao.setText(null);
        txtContasValor.setText(null);
        txtContasDataVencimento.setText(null);
        txtContasDataPagamento.setText(null);
        txtContasStatus.setText(null);

        // se tiver campo de pesquisa
        txtContasPesquisar.setText(null);
        // limpa tabela (se existir)
        ((DefaultTableModel) tblContas.getModel()).setRowCount(0);
        // desabilita botão de update (se existir)
        btnContasAtualizar.setEnabled(false);

        carregarListaAlertas();
    }

    private void limpar_campos() {
        txtContasId.setText(null);
        txtContasDescricao.setText(null);
        txtContasValor.setText(null);
        txtContasDataVencimento.setText(null);
        txtContasDataPagamento.setText(null);
        txtContasStatus.setText(null);

        // desabilita botão de update (se existir)
        btnContasNova.setEnabled(true);
        btnContasAtualizar.setEnabled(false);
    }

    private void atualizar_conta() {
        String sql = "update expense set description=?, value=?, due_date=?, payment_date=?, status=? where id=?";

        try {
            String descricao = txtContasDescricao.getText().trim();
            String valor = txtContasValor.getText().trim();
            String dataVenc = txtContasDataVencimento.getText().trim();
            String dataPag = txtContasDataPagamento.getText().trim();
            String id = txtContasId.getText(); // campo ID

            // CAMPOS OBRIGATÓRIOS
            if (!ValidadorConta.validarTexto(descricao)
                    || !ValidadorConta.validarTexto(valor)
                    || !ValidadorConta.validarTexto(dataVenc)
                    || id.isEmpty()) {

                JOptionPane.showMessageDialog(null, "Selecione uma conta e preencha os campos obrigatórios");
                return;
            }

            // VALOR - FORMATO
            if (!ValidadorConta.validarValor(valor)) {
                JOptionPane.showMessageDialog(null, "Valor inválido (ex: 100 ou 100.50)");
                return;
            }

            // VALOR - MAIOR QUE ZERO
            if (!ValidadorConta.valorMaiorQueZero(valor)) {
                JOptionPane.showMessageDialog(null, "Valor deve ser maior que zero");
                return;
            }

            // DATA VENCIMENTO
            if (!ValidadorConta.validarData(dataVenc)) {
                JOptionPane.showMessageDialog(null, "Data de vencimento inválida (dd/MM/yyyy)");
                return;
            }

            // DATA PAGAMENTO (opcional)
            if (!dataPag.isEmpty() && !ValidadorConta.validarData(dataPag)) {
                JOptionPane.showMessageDialog(null, "Data de pagamento inválida (dd/MM/yyyy)");
                return;
            }

            // FORMATADORES
            DateTimeFormatter telaFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter bancoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // DATAS
            LocalDate dataHoje = LocalDate.now();
            LocalDate dataVencimento = LocalDate.parse(dataVenc, telaFormatter);

            LocalDate dataPagamento = null;
            if (!dataPag.isEmpty()) {
                dataPagamento = LocalDate.parse(dataPag, telaFormatter);
            }

            // STATUS AUTOMÁTICO
            String status;

            if (dataPagamento != null) {
                status = "PAGO";
            } else if (dataVencimento.isBefore(dataHoje)) {
                status = "VENCIDO";
            } else {
                status = "PARA PAGAMENTO";
            }

            // CONVERSÃO PARA BANCO
            String dataVencimentoBanco = dataVencimento.format(bancoFormatter);

            String dataPagamentoBanco = null;
            if (dataPagamento != null) {
                dataPagamentoBanco = dataPagamento.format(bancoFormatter);
            }

            // UPDATE
            pst = conexao.prepareStatement(sql);
            pst.setString(1, descricao);
            pst.setDouble(2, Double.parseDouble(valor));
            pst.setString(3, dataVencimentoBanco);
            pst.setString(4, dataPagamentoBanco);
            pst.setString(5, status);
            pst.setInt(6, Integer.parseInt(id));

            int atualizado = pst.executeUpdate();

            if (atualizado > 0) {
                JOptionPane.showMessageDialog(null, "Conta atualizada com sucesso");
                limpar();

                btnContasNova.setEnabled(true);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void carregarListaAlertas() {

        DefaultListModel<String> modelo = new DefaultListModel<>();
        listContasAlertas.setModel(modelo);

        modelo.clear();

        String sql = "SELECT id as id, description as descrição, value as valor, due_date as vencimento, payment_date as pagamento FROM expense";

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate hoje = LocalDate.now();

        try {

            pst = conexao.prepareStatement(sql);
            rs = pst.executeQuery();

            while (rs.next()) {

                int idConta = rs.getInt("id");
                String descricaoConta = rs.getString("descrição");
                double valorConta = rs.getDouble("valor");
                LocalDate vencimentoContaLocal = LocalDate.parse(rs.getString("vencimento"), formato);

                LocalDate pagamentoContaLocal = null;
                String pagamentoContaBanco = rs.getString("pagamento");
                if (pagamentoContaBanco != null && !pagamentoContaBanco.isEmpty()) {
                    pagamentoContaLocal = LocalDate.parse(pagamentoContaBanco, formato);
                }

                if (pagamentoContaLocal == null) {

                    long diferencaDias = ChronoUnit.DAYS.between(hoje, vencimentoContaLocal);

                    if (diferencaDias == 0) {
                        modelo.addElement(descricaoConta + " de valor R$" + valorConta + " vence hoje!");

                    } else if (diferencaDias > 0 && diferencaDias <= 5) {
                        modelo.addElement(descricaoConta + " de valor R$" + valorConta
                                + " vence em " + diferencaDias + " dias! ID: " + idConta);

                    } else if (diferencaDias < 0) {
                        modelo.addElement(descricaoConta + " de valor R$" + valorConta
                                + " está vencida há " + Math.abs(diferencaDias) + " dias! ID: " + idConta);
                    }
                }

                int contadorAlertas = modelo.getSize();
                lblContasContadorAlertas.setText(String.valueOf(contadorAlertas));
            }
            rs.close();
            pst.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

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
        btnContasNova = new javax.swing.JButton();
        btnContasAtualizar = new javax.swing.JButton();
        btnContasLimpar = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtContasId = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtContasDescricao = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        txtContasValor = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtContasDataVencimento = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtContasDataPagamento = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtContasStatus = new javax.swing.JTextField();
        lblDataHoje = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblContas = new javax.swing.JTable();
        txtContasPesquisar = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        cboContasStatusFiltrar = new javax.swing.JComboBox<>();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        listContasAlertas = new javax.swing.JList<>();
        jLabel11 = new javax.swing.JLabel();
        lblContasContadorAlertas = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("AutoWash Manager - Gestão de Contas");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("Contas a Pagar");

        btnContasNova.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/novaConta.png"))); // NOI18N
        btnContasNova.setToolTipText("Nova Conta");
        btnContasNova.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnContasNova.addActionListener(this::btnContasNovaActionPerformed);

        btnContasAtualizar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/atualizarConta.png"))); // NOI18N
        btnContasAtualizar.setToolTipText("Atualizar Conta");
        btnContasAtualizar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnContasAtualizar.setEnabled(false);
        btnContasAtualizar.addActionListener(this::btnContasAtualizarActionPerformed);

        btnContasLimpar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/limparConta.png"))); // NOI18N
        btnContasLimpar.setToolTipText("Limpar");
        btnContasLimpar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnContasLimpar.addActionListener(this::btnContasLimparActionPerformed);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Id");

        txtContasId.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtContasId.setEnabled(false);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel3.setText("* Campos Obrigatórios");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("* Descrição");

        txtContasDescricao.setColumns(20);
        txtContasDescricao.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtContasDescricao.setRows(5);
        jScrollPane1.setViewportView(txtContasDescricao);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("* Valor");

        txtContasValor.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtContasValor.setToolTipText("");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("* Data Vencimento");

        txtContasDataVencimento.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtContasDataVencimento.setActionCommand("<Not Set>");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Data Pagamento");

        txtContasDataPagamento.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtContasDataPagamento.setToolTipText("");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setText("* Status");

        txtContasStatus.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        txtContasStatus.setToolTipText("");
        txtContasStatus.setEnabled(false);

        lblDataHoje.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblDataHoje.setText("Data de Hoje");

        tblContas = new javax.swing.JTable() {
            public boolean isCellEditable(int rolIndex, int colIndex) {
                return false;
            }
        };
        tblContas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "id", "decrição", "vencimento", "pagamento", "status"
            }
        ));
        tblContas.setFocusable(false);
        tblContas.getTableHeader().setReorderingAllowed(false);
        tblContas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblContasMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblContas);

        txtContasPesquisar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtContasPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        txtContasPesquisar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtContasPesquisarKeyReleased(evt);
            }
        });

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/loupe.png"))); // NOI18N
        jLabel9.setText("jLabel1");

        cboContasStatusFiltrar.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        cboContasStatusFiltrar.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "TODOS", "PAGO", "VENCIDO", "PARA PAGAMENTO" }));
        cboContasStatusFiltrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel10.setText("Alertas!!!");

        listContasAlertas.setFont(new java.awt.Font("Segoe UI", 2, 18)); // NOI18N
        listContasAlertas.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jScrollPane3.setViewportView(listContasAlertas);

        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/alerta.png"))); // NOI18N

        lblContasContadorAlertas.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblContasContadorAlertas.setText("0");
        lblContasContadorAlertas.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(122, 122, 122)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jScrollPane1))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtContasId, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(45, 45, 45)
                                        .addComponent(jLabel5)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtContasValor, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(48, 48, 48)
                                        .addComponent(jLabel6)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtContasDataVencimento, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(55, 55, 55)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel8)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtContasStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel7)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtContasDataPagamento))))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(lblDataHoje)
                                        .addGap(642, 642, 642)
                                        .addComponent(jLabel3))
                                    .addComponent(jLabel1))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnContasNova)
                        .addGap(42, 42, 42)
                        .addComponent(btnContasAtualizar)
                        .addGap(44, 44, 44)
                        .addComponent(btnContasLimpar)))
                .addGap(153, 153, 153))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(58, 58, 58)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtContasPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(101, 101, 101)
                        .addComponent(cboContasStatusFiltrar, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblContasContadorAlertas)
                        .addGap(94, 94, 94))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 512, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(43, 43, 43))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(btnContasLimpar)
                        .addComponent(btnContasAtualizar)
                        .addComponent(btnContasNova)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(lblDataHoje))
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtContasId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(txtContasValor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(txtContasDataVencimento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(txtContasDataPagamento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(29, 29, 29)
                                .addComponent(jLabel4))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(62, 62, 62)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(txtContasStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtContasPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9)
                            .addComponent(cboContasStatusFiltrar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(42, 42, 42))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addComponent(lblContasContadorAlertas, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(26, Short.MAX_VALUE))))
        );

        btnContasLimpar.getAccessibleContext().setAccessibleName("Limpar");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        // Atualizando a lblDataHoje para a data do sistema ao inicializar a tela de contas

        Date data = new Date();
        DateFormat formatador = DateFormat.getDateInstance(DateFormat.SHORT);
        lblDataHoje.setText(formatador.format(data));

    }//GEN-LAST:event_formWindowActivated

    private void btnContasNovaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnContasNovaActionPerformed
        // Chamando o método de adicionar uma nova conta

        adicionar_nova_conta();
    }//GEN-LAST:event_btnContasNovaActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        try {
            // Chamando o metodo para atualizar os status das contas em relação ao dia
            atualizar_status_dia();
            carregarListaAlertas();
        } catch (SQLException ex) {
            System.getLogger(TelaGestaoContas.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }//GEN-LAST:event_formWindowOpened

    private void btnContasAtualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnContasAtualizarActionPerformed
        // Chamando metodo para atualizar dados de uma conta

        atualizar_conta();
    }//GEN-LAST:event_btnContasAtualizarActionPerformed

    private void btnContasLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnContasLimparActionPerformed
        // Chamando o metodo de limpar os campos

        limpar_campos();
    }//GEN-LAST:event_btnContasLimparActionPerformed

    private void tblContasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblContasMouseClicked
        // Chamando o método de seta campos

        setar_campos_conta();
    }//GEN-LAST:event_tblContasMouseClicked

    private void txtContasPesquisarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtContasPesquisarKeyReleased
        // Chamndo o método pesquisar contas

        pesquisar_conta();
    }//GEN-LAST:event_txtContasPesquisarKeyReleased

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
        java.awt.EventQueue.invokeLater(() -> new TelaGestaoContas().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnContasAtualizar;
    private javax.swing.JButton btnContasLimpar;
    private javax.swing.JButton btnContasNova;
    private javax.swing.JComboBox<String> cboContasStatusFiltrar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblContasContadorAlertas;
    private javax.swing.JLabel lblDataHoje;
    private javax.swing.JList<String> listContasAlertas;
    private javax.swing.JTable tblContas;
    private javax.swing.JTextField txtContasDataPagamento;
    private javax.swing.JTextField txtContasDataVencimento;
    private javax.swing.JTextArea txtContasDescricao;
    private javax.swing.JTextField txtContasId;
    private javax.swing.JTextField txtContasPesquisar;
    private javax.swing.JTextField txtContasStatus;
    private javax.swing.JTextField txtContasValor;
    // End of variables declaration//GEN-END:variables
}
