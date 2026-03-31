/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package br.com.autowashmanager.telas;

import br.com.autowashmanager.dal.ModuloConexao;
import br.com.autowashmanager.model.Clima;
import br.com.autowashmanager.service.ClimaService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author h24he
 */
public class TelaPrincipal extends javax.swing.JFrame {

    Connection conexao = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(TelaPrincipal.class.getName());

    /**
     * Creates new form TelaPrincipal
     */
    public TelaPrincipal() {
        initComponents();

        tblOSDia.getColumnModel().getColumn(0).setMinWidth(0);
        tblOSDia.getColumnModel().getColumn(0).setMaxWidth(0);
        tblOSDia.getColumnModel().getColumn(0).setWidth(0);

        conexao = ModuloConexao.conector();

        setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        tblOSDia.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        confirmarSaida();

        atualizarTabela();

        atualizarTotais();
    }

    private void confirmarSaida() {
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {

                Object[] opcoes = {"Sim", "Não"};

                int resposta = javax.swing.JOptionPane.showOptionDialog(
                        null,
                        "Deseja realmente sair do sistema?",
                        "Confirmação de saída",
                        javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.QUESTION_MESSAGE,
                        null,
                        opcoes,
                        opcoes[1] // botão padrão (Não)
                );

                if (resposta == 0) {
                    System.exit(0);
                }
            }
        });
    }

    private void atualizarDadosClimaticos() {

        Clima dados = ClimaService.obterClimaHoje();

        lblCidade.setText("Cidade: " + dados.getCidade());
        lblTemperatura.setText("Temperatura: " + dados.getTemperatura() + "°C");
        lblClima.setText("Clima: " + dados.getDescricao());

    }

    private void atualizarData() {

        Date data = new Date();
        DateFormat formatador = DateFormat.getDateInstance(DateFormat.SHORT);
        lblData.setText(formatador.format(data));

    }

    public void atualizarTabela() {

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{
                    "ID", "descrição", "entrada", "entrega", "preço", "status",
                    "placa", "marca", "modelo", "funcionário"
                },
                0
        );

        tblOSDia.setModel(model);
        ocultarColunaID();

        String sql = "SELECT "
                + "so.id, "
                + "so.notes, "
                + "so.entry_time, "
                + "so.delivery_time, "
                + "so.price, "
                + "so.status, "
                + "v.plate, "
                + "v.brand, "
                + "v.model, "
                + "e.name "
                + "FROM service_order so "
                + "JOIN vehicle v ON so.vehicle_id = v.id "
                + "JOIN employee e ON so.employee_id = e.id "
                + "WHERE so.service_date = ? ";

        // 🔎 filtro por status
        if (rbtFinalizados.isSelected()) {
            sql += "AND so.status = 'Finalizado' ";
        } else if (rbtAguardando.isSelected()) {
            sql += "AND so.status = 'Aguardando' ";
        } else if (rbtInterna.isSelected()) {
            sql += "AND so.status = 'Na Limpeza Interna' ";
        } else if (rbtExterna.isSelected()) {
            sql += "AND so.status = 'Na Lavação Externa' ";
        } else if (rbtRemoção.isSelected()) {
            sql += "AND so.status LIKE 'Remoção%' ";
        }

        // 📊 ordem de chegada
        sql += "ORDER BY so.entry_time ASC";

        try {
            pst = conexao.prepareStatement(sql);

            // 📅 data atual
            pst.setString(1, java.time.LocalDate.now().toString());

            rs = pst.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("notes"),
                    rs.getString("entry_time"),
                    rs.getString("delivery_time"),
                    rs.getDouble("price"),
                    rs.getString("status"),
                    rs.getString("plate"),
                    rs.getString("brand"),
                    rs.getString("model"),
                    rs.getString("name")
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao atualizar tabela: " + e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pst != null) {
                    pst.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void atualizarTotais() {

        String sql = "SELECT COUNT(*) AS total_servicos, "
                + "SUM(price) AS total_valor "
                + "FROM service_order "
                + "WHERE service_date = ?";

        try {
            pst = conexao.prepareStatement(sql);

            // 📅 Data atual (Java)
            pst.setString(1, java.time.LocalDate.now().toString());

            rs = pst.executeQuery();

            if (rs.next()) {

                int totalServicos = rs.getInt("total_servicos");
                double totalValor = rs.getDouble("total_valor");

                // 💰 Formatação BR
                java.text.NumberFormat nf = java.text.NumberFormat
                        .getCurrencyInstance(new java.util.Locale("pt", "BR"));

                lblNumeroServicos.setText("NÚMERO DE SERVIÇOS: " + totalServicos);
                lblFaturamentoTotal.setText("FATURAMENTO TOTAL: " + nf.format(totalValor));
            }

        } catch (Exception e) {
            System.out.println("Erro ao atualizar totais: " + e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pst != null) {
                    pst.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void ocultarColunaID() {
        tblOSDia.getColumnModel().getColumn(0).setMinWidth(0);
        tblOSDia.getColumnModel().getColumn(0).setMaxWidth(0);
        tblOSDia.getColumnModel().getColumn(0).setWidth(0);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuItem1 = new javax.swing.JMenuItem();
        rbtFiltrar = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        lblData = new javax.swing.JLabel();
        lblCidade = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        lblTemperatura = new javax.swing.JLabel();
        lblClima = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        lblUsuárioLogado = new javax.swing.JLabel();
        lblNome = new javax.swing.JLabel();
        lblTipo = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        rbtFinalizados = new javax.swing.JRadioButton();
        rbtAguardando = new javax.swing.JRadioButton();
        rbtInterna = new javax.swing.JRadioButton();
        rbtExterna = new javax.swing.JRadioButton();
        rbtRemoção = new javax.swing.JRadioButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        tblOSDia = new javax.swing.JTable();
        rbtTodos = new javax.swing.JRadioButton();
        lblNumeroServicos = new javax.swing.JLabel();
        lblFaturamentoTotal = new javax.swing.JLabel();
        btnNovaOS = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnFinalizar = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        btnAguardando = new javax.swing.JButton();
        btnInterna = new javax.swing.JButton();
        btnExterna = new javax.swing.JButton();
        btnCimento = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        btnCadastroClientes = new javax.swing.JButton();
        btnCadastroVeiculos = new javax.swing.JButton();
        Menu = new javax.swing.JMenuBar();
        MenuCadastro = new javax.swing.JMenu();
        MenuCadastroFuncionario = new javax.swing.JMenuItem();
        MenuCadastroCliente = new javax.swing.JMenuItem();
        MenuCadastroVeiculo = new javax.swing.JMenuItem();
        MenuEmissao = new javax.swing.JMenu();
        MenuEmissaoOS = new javax.swing.JMenuItem();
        MenuGestao = new javax.swing.JMenu();
        MenuGestaoProdutos = new javax.swing.JMenuItem();
        MenuGestaoContas = new javax.swing.JMenuItem();
        MenuRelatorios = new javax.swing.JMenu();
        MenuRelatóriosRelatóriosGestão = new javax.swing.JMenuItem();

        jMenuItem1.setText("jMenuItem1");

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("AutoWash Manager - Menu");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("Sistema de Emissão de OS AutoWash Manager");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        lblData.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblData.setText("Data");

        lblCidade.setFont(new java.awt.Font("Verdana", 1, 18)); // NOI18N
        lblCidade.setText("CIDADE");

        lblTemperatura.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        lblTemperatura.setText("Temperatura: 28°C");

        lblClima.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        lblClima.setText("Clima: Ensolarado");

        lblUsuárioLogado.setFont(new java.awt.Font("Verdana", 1, 18)); // NOI18N
        lblUsuárioLogado.setText("USUÁRIO LOGADO");

        lblNome.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        lblNome.setText("Nome: Usuário");

        lblTipo.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        lblTipo.setText("Tipo: Administrador");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblData)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblClima)
                            .addComponent(lblCidade)
                            .addComponent(lblTemperatura)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(57, 57, 57)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTipo)
                            .addComponent(lblNome)
                            .addComponent(lblUsuárioLogado)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(23, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblData)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(lblCidade)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTemperatura)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblClima)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblUsuárioLogado)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblNome)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTipo)
                        .addGap(33, 33, 33))))
        );

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Filtrar por: ");

        rbtFiltrar.add(rbtFinalizados);
        rbtFinalizados.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rbtFinalizados.setText("Finalizados");
        rbtFinalizados.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rbtFinalizados.addActionListener(this::rbtFinalizadosActionPerformed);

        rbtFiltrar.add(rbtAguardando);
        rbtAguardando.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rbtAguardando.setText("Aguardando");
        rbtAguardando.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rbtAguardando.addActionListener(this::rbtAguardandoActionPerformed);

        rbtFiltrar.add(rbtInterna);
        rbtInterna.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rbtInterna.setText("Interna");
        rbtInterna.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rbtInterna.addActionListener(this::rbtInternaActionPerformed);

        rbtFiltrar.add(rbtExterna);
        rbtExterna.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rbtExterna.setText("Externa");
        rbtExterna.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rbtExterna.addActionListener(this::rbtExternaActionPerformed);

        rbtFiltrar.add(rbtRemoção);
        rbtRemoção.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rbtRemoção.setText("Remoção");
        rbtRemoção.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rbtRemoção.addActionListener(this::rbtRemoçãoActionPerformed);

        tblOSDia = new javax.swing.JTable() {
            public boolean isCellEditable(int rolIndex, int colIndex) {
                return false;
            }
        };
        tblOSDia.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "descrição", "entrada", "entrega", "preço", "status", "placa", "marca", "modelo", "funcionário"
            }
        ));
        tblOSDia.setFocusable(false);
        tblOSDia.getTableHeader().setReorderingAllowed(false);
        jScrollPane6.setViewportView(tblOSDia);

        rbtFiltrar.add(rbtTodos);
        rbtTodos.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rbtTodos.setSelected(true);
        rbtTodos.setText("Todos");
        rbtTodos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rbtTodos.addActionListener(this::rbtTodosActionPerformed);

        lblNumeroServicos.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblNumeroServicos.setText("NÚMERO DE SERVIÇOS:");

        lblFaturamentoTotal.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblFaturamentoTotal.setText("FATURAMENTO TOTAL: R$");

        btnNovaOS.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnNovaOS.setText("+ Nova OS");
        btnNovaOS.setToolTipText("Emitir Nova OS");
        btnNovaOS.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNovaOS.addActionListener(this::btnNovaOSActionPerformed);

        btnEditar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnEditar.setText("- Editar");
        btnEditar.setToolTipText("Editar OS");
        btnEditar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEditar.addActionListener(this::btnEditarActionPerformed);

        btnFinalizar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnFinalizar.setText("Finalizar");
        btnFinalizar.setToolTipText("Finalizar Serviço");
        btnFinalizar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnFinalizar.addActionListener(this::btnFinalizarActionPerformed);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel3.setText("Status:");

        btnAguardando.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnAguardando.setText("Aguardando");
        btnAguardando.setToolTipText("Mudar Status para Aguardando");
        btnAguardando.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAguardando.addActionListener(this::btnAguardandoActionPerformed);

        btnInterna.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnInterna.setText("Interna");
        btnInterna.setToolTipText("Mudar Status para Interna");
        btnInterna.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnInterna.addActionListener(this::btnInternaActionPerformed);

        btnExterna.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnExterna.setText("Externa");
        btnExterna.setToolTipText("Mudar Status para Externa");
        btnExterna.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnExterna.addActionListener(this::btnExternaActionPerformed);

        btnCimento.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnCimento.setText("Cimento");
        btnCimento.setToolTipText("Mudar Status para Remoção");
        btnCimento.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCimento.addActionListener(this::btnCimentoActionPerformed);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel4.setText("Cadastro:");

        btnCadastroClientes.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnCadastroClientes.setText("Clientes");
        btnCadastroClientes.setToolTipText("Cadastrar Novo Cliente");
        btnCadastroClientes.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCadastroClientes.addActionListener(this::btnCadastroClientesActionPerformed);

        btnCadastroVeiculos.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnCadastroVeiculos.setText("Veículos");
        btnCadastroVeiculos.setToolTipText("Cadastrar Novo Veículo");
        btnCadastroVeiculos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCadastroVeiculos.addActionListener(this::btnCadastroVeiculosActionPerformed);

        MenuCadastro.setText("Cadastro");
        MenuCadastro.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        MenuCadastroFuncionario.setText("Funcionário");
        MenuCadastroFuncionario.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        MenuCadastroFuncionario.setEnabled(false);
        MenuCadastroFuncionario.addActionListener(this::MenuCadastroFuncionarioActionPerformed);
        MenuCadastro.add(MenuCadastroFuncionario);

        MenuCadastroCliente.setText("Cliente");
        MenuCadastroCliente.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        MenuCadastroCliente.addActionListener(this::MenuCadastroClienteActionPerformed);
        MenuCadastro.add(MenuCadastroCliente);

        MenuCadastroVeiculo.setText("Veículo");
        MenuCadastroVeiculo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        MenuCadastroVeiculo.addActionListener(this::MenuCadastroVeiculoActionPerformed);
        MenuCadastro.add(MenuCadastroVeiculo);

        Menu.add(MenuCadastro);

        MenuEmissao.setText("Emissão");
        MenuEmissao.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        MenuEmissaoOS.setText("Ordem de Serviço");
        MenuEmissaoOS.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        MenuEmissaoOS.addActionListener(this::MenuEmissaoOSActionPerformed);
        MenuEmissao.add(MenuEmissaoOS);

        Menu.add(MenuEmissao);

        MenuGestao.setText("Gestão");
        MenuGestao.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        MenuGestao.setEnabled(false);

        MenuGestaoProdutos.setText("Produtos");
        MenuGestaoProdutos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        MenuGestaoProdutos.addActionListener(this::MenuGestaoProdutosActionPerformed);
        MenuGestao.add(MenuGestaoProdutos);

        MenuGestaoContas.setText("Contas");
        MenuGestaoContas.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        MenuGestaoContas.addActionListener(this::MenuGestaoContasActionPerformed);
        MenuGestao.add(MenuGestaoContas);

        Menu.add(MenuGestao);

        MenuRelatorios.setText("Relatórios");
        MenuRelatorios.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        MenuRelatorios.setEnabled(false);

        MenuRelatóriosRelatóriosGestão.setText("Relatórios de Gestão");
        MenuRelatóriosRelatóriosGestão.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        MenuRelatóriosRelatóriosGestão.addActionListener(this::MenuRelatóriosRelatóriosGestãoActionPerformed);
        MenuRelatorios.add(MenuRelatóriosRelatóriosGestão);

        Menu.add(MenuRelatorios);

        setJMenuBar(Menu);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAguardando)
                        .addGap(30, 30, 30)
                        .addComponent(btnInterna)
                        .addGap(30, 30, 30)
                        .addComponent(btnExterna)
                        .addGap(30, 30, 30)
                        .addComponent(btnCimento))
                    .addComponent(jLabel4)
                    .addComponent(jLabel3)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnCadastroClientes)
                        .addGap(30, 30, 30)
                        .addComponent(btnCadastroVeiculos))
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 1147, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(71, 71, 71)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblFaturamentoTotal)
                            .addComponent(lblNumeroServicos)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rbtTodos, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rbtFinalizados, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rbtAguardando, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rbtInterna, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rbtExterna, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rbtRemoção, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnNovaOS)
                        .addGap(30, 30, 30)
                        .addComponent(btnEditar)
                        .addGap(30, 30, 30)
                        .addComponent(btnFinalizar)))
                .addContainerGap(27, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(jLabel1)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(rbtTodos)
                            .addComponent(rbtFinalizados)
                            .addComponent(rbtAguardando)
                            .addComponent(rbtInterna)
                            .addComponent(rbtExterna)
                            .addComponent(rbtRemoção))
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addComponent(lblFaturamentoTotal)
                        .addGap(81, 81, 81)
                        .addComponent(lblNumeroServicos)))
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNovaOS)
                    .addComponent(btnEditar)
                    .addComponent(btnFinalizar))
                .addGap(34, 34, 34)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAguardando)
                    .addComponent(btnInterna)
                    .addComponent(btnExterna)
                    .addComponent(btnCimento))
                .addGap(42, 42, 42)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCadastroClientes)
                    .addComponent(btnCadastroVeiculos))
                .addContainerGap(48, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void MenuCadastroFuncionarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuCadastroFuncionarioActionPerformed
        // Chamando a Tela de Cadastro de Funcionário

        TelaCadastroFuncionario telaFuncionario = new TelaCadastroFuncionario();
        telaFuncionario.setVisible(true);
    }//GEN-LAST:event_MenuCadastroFuncionarioActionPerformed

    private void MenuCadastroClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuCadastroClienteActionPerformed
        // Chamando a Tela de Cadastro de Cliente

        TelaCadastroCliente telaCliente = new TelaCadastroCliente();
        telaCliente.setVisible(true);
    }//GEN-LAST:event_MenuCadastroClienteActionPerformed

    private void MenuCadastroVeiculoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuCadastroVeiculoActionPerformed
        // Chamando a Tela de Cadastro de Veículo

        TelaCadastroVeiculo telaVeiculo = new TelaCadastroVeiculo();
        telaVeiculo.setVisible(true);
    }//GEN-LAST:event_MenuCadastroVeiculoActionPerformed

    private void MenuEmissaoOSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuEmissaoOSActionPerformed
        // Chamando a Tela de Emissão de Ordem de Serviço

        TelaEmissaoOS telaOS = new TelaEmissaoOS();
        telaOS.setVisible(true);
    }//GEN-LAST:event_MenuEmissaoOSActionPerformed

    private void MenuGestaoProdutosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuGestaoProdutosActionPerformed
        // Chamando a Tela de Gestão de Produtos

        TelaGestaoProdutos gestaoProdutos = new TelaGestaoProdutos();
        gestaoProdutos.setVisible(true);
    }//GEN-LAST:event_MenuGestaoProdutosActionPerformed

    private void MenuGestaoContasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuGestaoContasActionPerformed
        // Chamando a Tela de Gestão de Contas

        TelaGestaoContas gestaoContas = new TelaGestaoContas();
        gestaoContas.setVisible(true);
    }//GEN-LAST:event_MenuGestaoContasActionPerformed

    private void MenuRelatóriosRelatóriosGestãoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuRelatóriosRelatóriosGestãoActionPerformed
        // Chamando a Tela de Relatórios de Gestão

        TelaRelatórios telaRelatorios = new TelaRelatórios();
        telaRelatorios.setVisible(true);
    }//GEN-LAST:event_MenuRelatóriosRelatóriosGestãoActionPerformed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        // Chamando todos os métodos de atualização pós ativação da tela principal

        atualizarData();
        atualizarDadosClimaticos();

    }//GEN-LAST:event_formWindowActivated

    private void rbtTodosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbtTodosActionPerformed
        atualizarTabela();
    }//GEN-LAST:event_rbtTodosActionPerformed

    private void rbtFinalizadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbtFinalizadosActionPerformed
        atualizarTabela();
    }//GEN-LAST:event_rbtFinalizadosActionPerformed

    private void rbtAguardandoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbtAguardandoActionPerformed
        atualizarTabela();
    }//GEN-LAST:event_rbtAguardandoActionPerformed

    private void rbtInternaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbtInternaActionPerformed
        atualizarTabela();
    }//GEN-LAST:event_rbtInternaActionPerformed

    private void rbtExternaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbtExternaActionPerformed
        atualizarTabela();
    }//GEN-LAST:event_rbtExternaActionPerformed

    private void rbtRemoçãoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbtRemoçãoActionPerformed
        atualizarTabela();
    }//GEN-LAST:event_rbtRemoçãoActionPerformed

    private void btnNovaOSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNovaOSActionPerformed
        // Chamando a tela de cadastro de OS

        TelaEmissaoOS tela = new TelaEmissaoOS();
        tela.setVisible(true);
    }//GEN-LAST:event_btnNovaOSActionPerformed

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
        int linha = tblOSDia.getSelectedRow();

        if (linha == -1) {
            JOptionPane.showMessageDialog(null, "Selecione uma OS!");
            return;
        }

        int idOS = (int) tblOSDia.getValueAt(linha, 0);

        TelaEmissaoOS tela = new TelaEmissaoOS();
        tela.setVisible(true);

        tela.carregarOSPorId(idOS);
    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnFinalizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFinalizarActionPerformed
        int linha = tblOSDia.getSelectedRow();

        if (linha == -1) {
            JOptionPane.showMessageDialog(null, "Selecione uma OS!");
            return;
        }

        int idOS = (int) tblOSDia.getValueAt(linha, 0);

        String sql = "UPDATE service_order SET status = 'Finalizado' WHERE id = ?";

        try {
            pst = conexao.prepareStatement(sql);
            pst.setInt(1, idOS);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(null, "OS finalizada com sucesso!");

            atualizarTabela();
            atualizarTotais();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao finalizar: " + e);
        }
    }//GEN-LAST:event_btnFinalizarActionPerformed

    private void btnAguardandoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAguardandoActionPerformed
        int linha = tblOSDia.getSelectedRow();

        if (linha == -1) {
            JOptionPane.showMessageDialog(null, "Selecione uma OS!");
            return;
        }

        int idOS = (int) tblOSDia.getValueAt(linha, 0);

        String sql = "UPDATE service_order SET status = 'Aguardando' WHERE id = ?";

        try {
            pst = conexao.prepareStatement(sql);
            pst.setInt(1, idOS);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(null, "Status alterado para Aguardando!");

            atualizarTabela();
            atualizarTotais();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao atualizar: " + e);
        }
    }//GEN-LAST:event_btnAguardandoActionPerformed

    private void btnInternaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInternaActionPerformed
        int linha = tblOSDia.getSelectedRow();

        if (linha == -1) {
            JOptionPane.showMessageDialog(null, "Selecione uma OS!");
            return;
        }

        int idOS = (int) tblOSDia.getValueAt(linha, 0);

        String sql = "UPDATE service_order SET status = 'Na Limpeza Interna' WHERE id = ?";

        try {
            pst = conexao.prepareStatement(sql);
            pst.setInt(1, idOS);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(null, "Status alterado para Limpeza Interna!");

            atualizarTabela();
            atualizarTotais();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao atualizar: " + e);
        }
    }//GEN-LAST:event_btnInternaActionPerformed

    private void btnExternaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExternaActionPerformed
        int linha = tblOSDia.getSelectedRow();

        if (linha == -1) {
            JOptionPane.showMessageDialog(null, "Selecione uma OS!");
            return;
        }

        int idOS = (int) tblOSDia.getValueAt(linha, 0);

        String sql = "UPDATE service_order SET status = 'Na Lavação Externa' WHERE id = ?";

        try {
            pst = conexao.prepareStatement(sql);
            pst.setInt(1, idOS);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(null, "Status alterado para Lavação Externa!");

            atualizarTabela();
            atualizarTotais();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao atualizar: " + e);
        }
    }//GEN-LAST:event_btnExternaActionPerformed

    private void btnCimentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCimentoActionPerformed
        int linha = tblOSDia.getSelectedRow();

        if (linha == -1) {
            JOptionPane.showMessageDialog(null, "Selecione uma OS!");
            return;
        }

        int idOS = (int) tblOSDia.getValueAt(linha, 0);

        String sql = "UPDATE service_order SET status = 'Remoção de Cimento' WHERE id = ?";

        try {
            pst = conexao.prepareStatement(sql);
            pst.setInt(1, idOS);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(null, "Status alterado para Remoção de Cimento!");

            atualizarTabela();
            atualizarTotais();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao atualizar: " + e);
        }
    }//GEN-LAST:event_btnCimentoActionPerformed

    private void btnCadastroClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCadastroClientesActionPerformed
        // Chamando tela de cadastro de clientes
        
        TelaCadastroCliente telaCliente = new TelaCadastroCliente();
        telaCliente.setVisible(true);
    }//GEN-LAST:event_btnCadastroClientesActionPerformed

    private void btnCadastroVeiculosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCadastroVeiculosActionPerformed
        // Chamando tela de cadastro de veiculos
        
        TelaCadastroVeiculo telaVeiculo = new TelaCadastroVeiculo();
        telaVeiculo.setVisible(true);
    }//GEN-LAST:event_btnCadastroVeiculosActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new TelaPrincipal().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar Menu;
    private javax.swing.JMenu MenuCadastro;
    private javax.swing.JMenuItem MenuCadastroCliente;
    public javax.swing.JMenuItem MenuCadastroFuncionario;
    private javax.swing.JMenuItem MenuCadastroVeiculo;
    private javax.swing.JMenu MenuEmissao;
    private javax.swing.JMenuItem MenuEmissaoOS;
    public javax.swing.JMenu MenuGestao;
    private javax.swing.JMenuItem MenuGestaoContas;
    private javax.swing.JMenuItem MenuGestaoProdutos;
    public javax.swing.JMenu MenuRelatorios;
    private javax.swing.JMenuItem MenuRelatóriosRelatóriosGestão;
    private javax.swing.JButton btnAguardando;
    private javax.swing.JButton btnCadastroClientes;
    private javax.swing.JButton btnCadastroVeiculos;
    private javax.swing.JButton btnCimento;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnExterna;
    private javax.swing.JButton btnFinalizar;
    private javax.swing.JButton btnInterna;
    private javax.swing.JButton btnNovaOS;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lblCidade;
    private javax.swing.JLabel lblClima;
    private javax.swing.JLabel lblData;
    private javax.swing.JLabel lblFaturamentoTotal;
    public javax.swing.JLabel lblNome;
    private javax.swing.JLabel lblNumeroServicos;
    private javax.swing.JLabel lblTemperatura;
    public javax.swing.JLabel lblTipo;
    private javax.swing.JLabel lblUsuárioLogado;
    private javax.swing.JRadioButton rbtAguardando;
    private javax.swing.JRadioButton rbtExterna;
    private javax.swing.ButtonGroup rbtFiltrar;
    private javax.swing.JRadioButton rbtFinalizados;
    private javax.swing.JRadioButton rbtInterna;
    private javax.swing.JRadioButton rbtRemoção;
    private javax.swing.JRadioButton rbtTodos;
    private javax.swing.JTable tblOS;
    private javax.swing.JTable tblOS1;
    private javax.swing.JTable tblOS2;
    private javax.swing.JTable tblOS3;
    private javax.swing.JTable tblOS4;
    private javax.swing.JTable tblOSDia;
    // End of variables declaration//GEN-END:variables
}
