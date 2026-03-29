/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package br.com.autowashmanager.telas;

import br.com.autowashmanager.dal.ModuloConexao;
import br.com.autowashmanager.service.ValidadorOS;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import javax.swing.JOptionPane;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

/**
 *
 * @author h24he
 */
public class TelaRelatórios extends javax.swing.JFrame {

    Connection conexao = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(TelaRelatórios.class.getName());

    /**
     * Creates new form TelaRelatórios
     */
    public TelaRelatórios() {
        initComponents();
        conexao = ModuloConexao.conector();
    }

    private void imprimir_relatorio() {

        if (rbtFaturamentoDia.isSelected()) {
            imprimirFaturamentoDia();
        } else if (rbtOrdensAndamento.isSelected()) {
            imprimirOrdensAndamento();
        } else if (rbtServicosRealizadosDia.isSelected()) {
            imprimirServicosRealizadosDia();
        } else if (rbtContasParaPagar.isSelected()) {
            imprimirContasParaPagar();
        } else if (rbtEstoqueProdutos.isSelected()) {
            imprimirEstoqueProdutos();
        } else if (rbtFuncionarioMaisRealizouServicos.isSelected()) {
            imprimirFuncionarioMaisRealizouServicos();
        } else if (rbtFaturamentoPorFuncionario.isSelected()) {
            imprimirFaturamentoPorFuncionario();
        } else if (rbtServicosPorDia.isSelected() || rbtServicosPorMes.isSelected()) {
            imprimirRelatorioPeriodo();
        } else if (rbtFaturamentoPorMes.isSelected()) {
            imprimirFaturamentoPorMes();
        } else if (rbtClientesMaisUtilizamLavacao.isSelected()) {
            imprimirClientesMaisUtilizamLavacao();
        } else if (rbtTicketMedioPorServico.isSelected()) {
            imprimirTicketMedioPorServico();
        }

    }

    private void imprimirFaturamentoDia() {

        // Imprimindo o relatório
        int confirma = JOptionPane.showConfirmDialog(null, "Confirma a impressão deste relatório?", "Atenção", JOptionPane.YES_NO_OPTION);
        if (confirma == JOptionPane.YES_OPTION) {
            // imprimindo o relatório com o framework JasperReport
            try {
                // usando a classe HashMap para criar um filtro
                HashMap filtro = new HashMap();
                filtro.put("P_DATE", LocalDate.now());
                // usando a classe JasperPrint para preparar a impressão de um relatório
                JasperPrint print = JasperFillManager.fillReport(getClass().getResourceAsStream("/reports/faturamento_dia_autowashmanager.jasper"), filtro, conexao);
                // a linha abaixo exibe o relatório através da classe JasperViewer
                JasperViewer.viewReport(print, false);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Selecione uma opção para antes tentar imprimir!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }

    }

    private void imprimirOrdensAndamento() {

        // Imprimindo o relatório
        int confirma = JOptionPane.showConfirmDialog(null, "Confirma a impressão deste relatório?", "Atenção", JOptionPane.YES_NO_OPTION);
        if (confirma == JOptionPane.YES_OPTION) {
            // imprimindo o relatório com o framework JasperReport
            try {
                // usando a classe HashMap para criar um filtro
                HashMap filtro = new HashMap();
                // usando a classe JasperPrint para preparar a impressão de um relatório
                JasperPrint print = JasperFillManager.fillReport(getClass().getResourceAsStream("/reports/os_andamento_autowashmanager.jasper"), filtro, conexao);
                // a linha abaixo exibe o relatório através da classe JasperViewer
                JasperViewer.viewReport(print, false);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Selecione uma opção para antes tentar imprimir!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }

    }

    private void imprimirServicosRealizadosDia() {

        int confirma = JOptionPane.showConfirmDialog(null,
                "Confirma a impressão deste relatório?",
                "Atenção",
                JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_OPTION) {

            try {
                // pede a data pro usuário
                String dataDigitada = JOptionPane.showInputDialog(
                        null,
                        "Digite a data (dd/MM/yyyy):",
                        "Data do relatório",
                        JOptionPane.QUESTION_MESSAGE
                );

                // usuário cancelou
                if (dataDigitada == null) {
                    return;
                }

                // valida formato dd/MM/yyyy
                if (!dataDigitada.matches("^\\d{2}/\\d{2}/\\d{4}$")) {
                    JOptionPane.showMessageDialog(null, "Formato inválido! Use dd/MM/yyyy");
                    return;
                }

                // converte para yyyy-MM-dd
                DateTimeFormatter formatterEntrada = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                DateTimeFormatter formatterBanco = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                LocalDate dataConvertida = LocalDate.parse(dataDigitada, formatterEntrada);
                String dataParaBanco = dataConvertida.format(formatterBanco);

                // valida data real
                if (!ValidadorOS.validarDataReal(dataParaBanco)) {
                    JOptionPane.showMessageDialog(null, "Data inválida!");
                    return;
                }

                // monta filtro
                HashMap filtro = new HashMap();
                filtro.put("P_DATE", dataParaBanco);

                // chama relatório
                JasperPrint print = JasperFillManager.fillReport(
                        getClass().getResourceAsStream("/reports/servicos_realizados_dia_autowashmanager.jasper"),
                        filtro,
                        conexao
                );

                JasperViewer.viewReport(print, false);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
    }

    private void imprimirContasParaPagar() {
        // Imprimindo o relatório
        int confirma = JOptionPane.showConfirmDialog(null, "Confirma a impressão deste relatório?", "Atenção", JOptionPane.YES_NO_OPTION);
        if (confirma == JOptionPane.YES_OPTION) {
            // imprimindo o relatório com o framework JasperReport
            try {
                // usando a classe HashMap para criar um filtro
                // usando a classe JasperPrint para preparar a impressão de um relatório
                JasperPrint print = JasperFillManager.fillReport(getClass().getResourceAsStream("/reports/contas_pagar_autowashmanager.jasper"), null, conexao);
                // a linha abaixo exibe o relatório através da classe JasperViewer
                JasperViewer.viewReport(print, false);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Selecione uma opção para antes tentar imprimir!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    private void imprimirEstoqueProdutos() {
        // Imprimindo o relatório
        int confirma = JOptionPane.showConfirmDialog(null, "Confirma a impressão deste relatório?", "Atenção", JOptionPane.YES_NO_OPTION);
        if (confirma == JOptionPane.YES_OPTION) {
            // imprimindo o relatório com o framework JasperReport
            try {
                // usando a classe HashMap para criar um filtro
                // usando a classe JasperPrint para preparar a impressão de um relatório
                JasperPrint print = JasperFillManager.fillReport(getClass().getResourceAsStream("/reports/estoque_produtos_autowashmanager.jasper"), null, conexao);
                // a linha abaixo exibe o relatório através da classe JasperViewer
                JasperViewer.viewReport(print, false);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Selecione uma opção para antes tentar imprimir!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    private void imprimirFuncionarioMaisRealizouServicos() {
        // Imprimindo o relatório
        int confirma = JOptionPane.showConfirmDialog(null, "Confirma a impressão deste relatório?", "Atenção", JOptionPane.YES_NO_OPTION);
        if (confirma == JOptionPane.YES_OPTION) {
            // imprimindo o relatório com o framework JasperReport
            try {
                // usando a classe HashMap para criar um filtro
                // usando a classe JasperPrint para preparar a impressão de um relatório
                JasperPrint print = JasperFillManager.fillReport(getClass().getResourceAsStream("/reports/funcionario_mais_realizou_servicos.jasper"), null, conexao);
                // a linha abaixo exibe o relatório através da classe JasperViewer
                JasperViewer.viewReport(print, false);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Selecione uma opção para antes tentar imprimir!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    private void imprimirFaturamentoPorFuncionario() {
        // Imprimindo o relatório
        int confirma = JOptionPane.showConfirmDialog(null, "Confirma a impressão deste relatório?", "Atenção", JOptionPane.YES_NO_OPTION);
        if (confirma == JOptionPane.YES_OPTION) {
            // imprimindo o relatório com o framework JasperReport
            try {
                // usando a classe HashMap para criar um filtro
                // usando a classe JasperPrint para preparar a impressão de um relatório
                JasperPrint print = JasperFillManager.fillReport(getClass().getResourceAsStream("/reports/faturamento_por_funcionario_autowashmanager.jasper"), null, conexao);
                // a linha abaixo exibe o relatório através da classe JasperViewer
                JasperViewer.viewReport(print, false);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Selecione uma opção para antes tentar imprimir!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    private void imprimirServicosPorDia() {

        int confirma = JOptionPane.showConfirmDialog(null,
                "Confirma a impressão deste relatório?",
                "Atenção",
                JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_OPTION) {

            try {
                String dataDigitada = JOptionPane.showInputDialog(
                        null,
                        "Digite a data (dd/MM/yyyy):",
                        "Serviços por Dia",
                        JOptionPane.QUESTION_MESSAGE
                );

                if (dataDigitada == null) {
                    return;
                }

                // valida formato
                if (!dataDigitada.matches("^\\d{2}/\\d{2}/\\d{4}$")) {
                    JOptionPane.showMessageDialog(null, "Formato inválido! Use dd/MM/yyyy");
                    return;
                }

                // converter
                DateTimeFormatter entrada = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                DateTimeFormatter banco = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                LocalDate data = LocalDate.parse(dataDigitada, entrada);
                String dataFormatada = data.format(banco);

                // valida data real
                if (!ValidadorOS.validarDataReal(dataFormatada)) {
                    JOptionPane.showMessageDialog(null, "Data inválida!");
                    return;
                }

                // filtro
                HashMap filtro = new HashMap();
                filtro.put("P_DATA", dataFormatada);

                JasperPrint print = JasperFillManager.fillReport(
                        getClass().getResourceAsStream("/reports/servicos_por_dia.jasper"),
                        filtro,
                        conexao
                );

                JasperViewer.viewReport(print, false);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
    }

    private void imprimirServicosPorMes() {

        int confirma = JOptionPane.showConfirmDialog(null,
                "Confirma a impressão deste relatório?",
                "Atenção",
                JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_OPTION) {

            try {
                String mesDigitado = JOptionPane.showInputDialog(
                        null,
                        "Digite o mês (MM/yyyy):",
                        "Serviços por Mês",
                        JOptionPane.QUESTION_MESSAGE
                );

                if (mesDigitado == null) {
                    return;
                }

                // valida formato
                if (!mesDigitado.matches("^\\d{2}/\\d{4}$")) {
                    JOptionPane.showMessageDialog(null, "Formato inválido! Use MM/yyyy");
                    return;
                }

                String[] partes = mesDigitado.split("/");
                String mes = partes[0];
                String ano = partes[1];

                // valida mês
                int mesInt = Integer.parseInt(mes);
                if (mesInt < 1 || mesInt > 12) {
                    JOptionPane.showMessageDialog(null, "Mês inválido!");
                    return;
                }

                // filtro
                HashMap filtro = new HashMap();
                filtro.put("P_MES", mes);
                filtro.put("P_ANO", ano);

                JasperPrint print = JasperFillManager.fillReport(
                        getClass().getResourceAsStream("/reports/servicos_por_mes.jasper"),
                        filtro,
                        conexao
                );

                JasperViewer.viewReport(print, false);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
    }

    private void imprimirRelatorioPeriodo() {

        if (rbtServicosPorDia.isSelected()) {
            imprimirServicosPorDia();
        } else if (rbtServicosPorMes.isSelected()) {
            imprimirServicosPorMes();
        } else {
            JOptionPane.showMessageDialog(null, "Selecione Dia ou Mês!");
        }
    }

    private void imprimirFaturamentoPorMes() {

        int confirma = JOptionPane.showConfirmDialog(
                null,
                "Confirma a impressão do relatório de faturamento do mês?",
                "Atenção",
                JOptionPane.YES_NO_OPTION
        );

        if (confirma == JOptionPane.YES_OPTION) {

            try {
                // pede o mês
                String mesDigitado = JOptionPane.showInputDialog(
                        null,
                        "Digite o mês (MM/yyyy):",
                        "Faturamento por mês",
                        JOptionPane.QUESTION_MESSAGE
                );

                // cancelou
                if (mesDigitado == null) {
                    return;
                }

                // valida formato MM/yyyy
                if (!mesDigitado.matches("^\\d{2}/\\d{4}$")) {
                    JOptionPane.showMessageDialog(null, "Formato inválido! Use MM/yyyy");
                    return;
                }

                // separa mês e ano
                String mes = mesDigitado.substring(0, 2);
                String ano = mesDigitado.substring(3, 7);

                // valida mês (01 a 12)
                int mesInt = Integer.parseInt(mes);
                if (mesInt < 1 || mesInt > 12) {
                    JOptionPane.showMessageDialog(null, "Mês inválido!");
                    return;
                }

                // monta filtro
                HashMap filtro = new HashMap();
                filtro.put("P_MES", mes);
                filtro.put("P_ANO", ano);

                // chama relatório
                JasperPrint print = JasperFillManager.fillReport(
                        getClass().getResourceAsStream("/reports/faturamento_por_mes.jasper"),
                        filtro,
                        conexao
                );

                JasperViewer.viewReport(print, false);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
    }

    private void imprimirClientesMaisUtilizamLavacao() {

        // Imprimindo o relatório
        int confirma = JOptionPane.showConfirmDialog(null, "Confirma a impressão deste relatório?", "Atenção", JOptionPane.YES_NO_OPTION);
        if (confirma == JOptionPane.YES_OPTION) {
            // imprimindo o relatório com o framework JasperReport
            try {
                // usando a classe HashMap para criar um filtro
                // usando a classe JasperPrint para preparar a impressão de um relatório
                JasperPrint print = JasperFillManager.fillReport(getClass().getResourceAsStream("/reports/clientes_mais_utilizam_lavacao.jasper"), null, conexao);
                // a linha abaixo exibe o relatório através da classe JasperViewer
                JasperViewer.viewReport(print, false);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Selecione uma opção para antes tentar imprimir!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }

    }

    private void imprimirTicketMedioPorServico() {

        // Imprimindo o relatório
        int confirma = JOptionPane.showConfirmDialog(null, "Confirma a impressão deste relatório?", "Atenção", JOptionPane.YES_NO_OPTION);
        if (confirma == JOptionPane.YES_OPTION) {
            // imprimindo o relatório com o framework JasperReport
            try {
                // usando a classe HashMap para criar um filtro
                // usando a classe JasperPrint para preparar a impressão de um relatório
                JasperPrint print = JasperFillManager.fillReport(getClass().getResourceAsStream("/reports/ticket_medio_por_servico.jasper"), null, conexao);
                // a linha abaixo exibe o relatório através da classe JasperViewer
                JasperViewer.viewReport(print, false);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Selecione uma opção para antes tentar imprimir!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
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

        rbtRelatórios = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        rbtFaturamentoDia = new javax.swing.JRadioButton();
        btnImprimirRelatório = new javax.swing.JButton();
        rbtOrdensAndamento = new javax.swing.JRadioButton();
        rbtServicosRealizadosDia = new javax.swing.JRadioButton();
        rbtContasParaPagar = new javax.swing.JRadioButton();
        rbtEstoqueProdutos = new javax.swing.JRadioButton();
        rbtFuncionarioMaisRealizouServicos = new javax.swing.JRadioButton();
        rbtFaturamentoPorFuncionario = new javax.swing.JRadioButton();
        rbtServicosPorDia = new javax.swing.JRadioButton();
        rbtServicosPorMes = new javax.swing.JRadioButton();
        rbtFaturamentoPorMes = new javax.swing.JRadioButton();
        rbtTicketMedioPorServico = new javax.swing.JRadioButton();
        rbtClientesMaisUtilizamLavacao = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("AutoWash Manager - Relatórios");
        setFocusable(false);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("Relatórios de Gestão");

        rbtRelatórios.add(rbtFaturamentoDia);
        rbtFaturamentoDia.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rbtFaturamentoDia.setSelected(true);
        rbtFaturamentoDia.setText("Faturamento do Dia");
        rbtFaturamentoDia.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        btnImprimirRelatório.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/imprimir.png"))); // NOI18N
        btnImprimirRelatório.setToolTipText("Imprimir Relatório");
        btnImprimirRelatório.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnImprimirRelatório.addActionListener(this::btnImprimirRelatórioActionPerformed);

        rbtRelatórios.add(rbtOrdensAndamento);
        rbtOrdensAndamento.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rbtOrdensAndamento.setText("Ordens de Serviço em Andamento");
        rbtOrdensAndamento.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        rbtRelatórios.add(rbtServicosRealizadosDia);
        rbtServicosRealizadosDia.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rbtServicosRealizadosDia.setText("Serviços Realizados no Dia");
        rbtServicosRealizadosDia.setToolTipText("");
        rbtServicosRealizadosDia.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        rbtRelatórios.add(rbtContasParaPagar);
        rbtContasParaPagar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rbtContasParaPagar.setText("Contas para Pagar");
        rbtContasParaPagar.setToolTipText("");
        rbtContasParaPagar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        rbtRelatórios.add(rbtEstoqueProdutos);
        rbtEstoqueProdutos.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rbtEstoqueProdutos.setText("Estoque de Produtos");
        rbtEstoqueProdutos.setToolTipText("");
        rbtEstoqueProdutos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        rbtRelatórios.add(rbtFuncionarioMaisRealizouServicos);
        rbtFuncionarioMaisRealizouServicos.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rbtFuncionarioMaisRealizouServicos.setText("Funcionário que Mais Realizou Serviços");
        rbtFuncionarioMaisRealizouServicos.setToolTipText("");
        rbtFuncionarioMaisRealizouServicos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        rbtRelatórios.add(rbtFaturamentoPorFuncionario);
        rbtFaturamentoPorFuncionario.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rbtFaturamentoPorFuncionario.setText("Faturamento por Funcionário");
        rbtFaturamentoPorFuncionario.setToolTipText("");
        rbtFaturamentoPorFuncionario.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        rbtRelatórios.add(rbtServicosPorDia);
        rbtServicosPorDia.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rbtServicosPorDia.setText("Serviços por Dia");
        rbtServicosPorDia.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        rbtRelatórios.add(rbtServicosPorMes);
        rbtServicosPorMes.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rbtServicosPorMes.setText("Serviços por Mês");
        rbtServicosPorMes.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        rbtRelatórios.add(rbtFaturamentoPorMes);
        rbtFaturamentoPorMes.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rbtFaturamentoPorMes.setText("Faturamento por Mês");
        rbtFaturamentoPorMes.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        rbtRelatórios.add(rbtTicketMedioPorServico);
        rbtTicketMedioPorServico.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rbtTicketMedioPorServico.setText("Ticket Médio por Serviço");
        rbtTicketMedioPorServico.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        rbtRelatórios.add(rbtClientesMaisUtilizamLavacao);
        rbtClientesMaisUtilizamLavacao.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rbtClientesMaisUtilizamLavacao.setText("Clientes que Mais Utilizam a Lavação");
        rbtClientesMaisUtilizamLavacao.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addContainerGap(909, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(rbtFaturamentoPorFuncionario)
                                    .addComponent(rbtEstoqueProdutos)
                                    .addComponent(rbtFaturamentoDia)
                                    .addComponent(rbtOrdensAndamento)
                                    .addComponent(rbtServicosRealizadosDia)
                                    .addComponent(rbtContasParaPagar)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(rbtServicosPorDia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(rbtFuncionarioMaisRealizouServicos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(rbtServicosPorMes, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 691, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(rbtFaturamentoPorMes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(691, 691, 691))
                            .addComponent(rbtClientesMaisUtilizamLavacao, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(rbtTicketMedioPorServico, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnImprimirRelatório)
                        .addGap(87, 87, 87))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addComponent(jLabel1)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnImprimirRelatório)
                        .addGap(46, 46, 46))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(rbtFaturamentoDia)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rbtOrdensAndamento)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rbtServicosRealizadosDia)
                        .addGap(18, 18, 18)
                        .addComponent(rbtContasParaPagar)
                        .addGap(18, 18, 18)
                        .addComponent(rbtEstoqueProdutos)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rbtFuncionarioMaisRealizouServicos)
                        .addGap(18, 18, 18)
                        .addComponent(rbtFaturamentoPorFuncionario)
                        .addGap(18, 18, 18)
                        .addComponent(rbtServicosPorDia)
                        .addGap(18, 18, 18)
                        .addComponent(rbtServicosPorMes)
                        .addGap(18, 18, 18)
                        .addComponent(rbtFaturamentoPorMes)
                        .addGap(18, 18, 18)
                        .addComponent(rbtClientesMaisUtilizamLavacao)
                        .addGap(18, 18, 18)
                        .addComponent(rbtTicketMedioPorServico)
                        .addContainerGap(33, Short.MAX_VALUE))))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnImprimirRelatórioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirRelatórioActionPerformed
        // Chamando o metodo de imprimir OS

        imprimir_relatorio();
    }//GEN-LAST:event_btnImprimirRelatórioActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new TelaRelatórios().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnImprimirRelatório;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JRadioButton rbtClientesMaisUtilizamLavacao;
    private javax.swing.JRadioButton rbtContasParaPagar;
    private javax.swing.JRadioButton rbtEstoqueProdutos;
    private javax.swing.JRadioButton rbtFaturamentoDia;
    private javax.swing.JRadioButton rbtFaturamentoPorFuncionario;
    private javax.swing.JRadioButton rbtFaturamentoPorMes;
    private javax.swing.JRadioButton rbtFuncionarioMaisRealizouServicos;
    private javax.swing.JRadioButton rbtOrdensAndamento;
    private javax.swing.ButtonGroup rbtRelatórios;
    private javax.swing.JRadioButton rbtServicosPorDia;
    private javax.swing.JRadioButton rbtServicosPorMes;
    private javax.swing.JRadioButton rbtServicosRealizadosDia;
    private javax.swing.JRadioButton rbtTicketMedioPorServico;
    // End of variables declaration//GEN-END:variables
}
