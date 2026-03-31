# AutoWash Manager 🚗✨

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)

![SQLite](https://img.shields.io/badge/sqlite-%2307405e.svg?style=for-the-badge&logo=sqlite&logoColor=white)

![Maven](https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)

![Status](https://img.shields.io/badge/Status-Em_Desenvolvimento-green?style=for-the-badge)

```text
    _         _        __        __         _
   / \  _   _| |_ ___  \ \      / /_ _  ___| |__
  / _ \| | | | __/ _ \  \ \ /\ / / _` |/ __| '_ \
 / ___ \ |_| | || (_) |  \ V  V / (_| | (__| | | |
/_/   \_\__,_|\__\___/    \_/\_/ \__,_|\___|_| |_|

   M  A  N  A  G  E  R   -   V E R S Ã O  1 . 0

`````

> A solução digital inteligente para a gestão completa de lavações automotivas.

`--`

## 📖 Sobre o projeto

O **AutoWash Manager** foi desenvolvido com um propósito real: modernizar a gestão de uma lavação de automóveis familiar. O objetivo principal é digitalizar processos que antes dependiam de cadernos de anotações, oferecendo uma plataforma organizada, rápida e acessível.

Para garantir que o sistema fosse fácil de implantar e utilizar, optou-se pelo uso do **SQLite**. Isso elimina a necessidade de instalar servidores de banco de dados pesados (como MySQL ou PostgreSQL), tornando o sistema totalmente portátil e pronto para uso imediato após a execução.

`--`

## 🚀 Funcionalidades Principais

- 🌤️ **Clima em tempo real na tela principal** — Integração com API meteorológica para exibir temperatura e condições do dia, permitindo planejar a demanda conforme o tempo.
- 📋 **Cadastro de serviços digital** — Fim dos papéis perdidos. Registro completo de cada lavagem, polimento ou higienização realizada.
- 👥 **Cadastro de clientes e veículos** — Histórico detalhado para fidelização, permitindo saber exatamente quando o cliente veio e qual veículo foi atendido.
- 📊 **Relatórios de produtividade** — Visão clara do faturamento e serviços, com dados gerados via JasperReports para análise de desempenho.
- 💰 **Controle financeiro** — Módulo para gestão de contas a pagar e controle de estoque de insumos.
- 🏠 **Dashboard principal** — Uma central de comando com visão rápida das métricas do dia logo após o login.
- 📍 **Busca automatizada de CEP** — Integração com ViaCEP para preenchimento automático de endereços no cadastro.
- 🗄️ **Banco de dados SQLite** — Portabilidade total e zero configuração de servidor externa.


## 🛠️ Tecnologias Utilizadas

| Componente         | Tecnologia               |
|--------------------|--------------------------|
| Linguagem          | Java (JDK 25)            |
| Interface          | Java Swing               |
| Banco de Dados     | SQLite                   |
| Build/Dependências | Maven                    |
| Relatórios         | JasperReports            |
| Integrações        | OpenWeather API & ViaCEP |

`--`

## 📥 Pré-requisitos e Instalação

### Pré-requisitos

- Java JDK 25 ou superior instalado.
- Maven instalado e configurado no seu sistema.

## 📥 Download e Instalação (Executável)

Para facilitar o uso, você não precisa compilar o código-fonte. O projeto já está empacotado com todas as dependências e pronto para rodar:

1. **Baixe o pacote do sistema:** [[Download via Google Drive]](https://drive.google.com/file/d/1a55JWXZIRYGrQClX_tPLO8671yMVL1-V/view?usp=sharing)

2. **Extraia o arquivo `.zip`:** Certifique-se de extrair todos os arquivos para uma pasta de sua preferência.

3. **Pré-requisito:** É necessário ter o **Java (JRE) 25** ou superior instalado no computador.

`--`

## 🚀 Como Executar

Dentro da pasta extraída, execute o arquivo `.exe`. Você pode fazer isso clicando duas vezes no arquivo:

> ⚠️ **Atenção:** Como os relatórios são carregados externamente para facilitar a manutenção, a pasta `reports/` deve permanecer no mesmo diretório que o arquivo `.exe`. Se ela for movida ou deletada, o sistema não conseguirá imprimir os relatórios.

`--`

## 📂 Estrutura de Distribuição

Para o funcionamento correto, a pasta do programa deve estar organizada assim:

````plaintext

AutoWash-Manager/

├── autowashmanager.exe      # O programa principal

├── config.xml

├── launch4j.log

├── LICENSE.txt

├── jre

├── AutoWashManager-1.0-SNAPSHOT.jar` 

├── reports/               # Pasta com os arquivos .jasper (obrigatória)

└── database/              # Onde o banco SQLite será armazenado

`````

`--`

## 🕹️ Como usar

1. **Acesso:** Utilize suas credenciais na tela de login (user=admin and password=admin).

2. **Cadastros:** Inicie registrando os **Clientes** e **Veículos**. O sistema buscará o endereço automaticamente ao digitar o CEP.

3. **Ordens de Serviço:** Na aba de **Emissão de OS**, selecione o veículo, o tipo de serviço e o funcionário.

4. **Financeiro:** Use o módulo de **Gestão de Contas** para registrar despesas e entradas.

5. **Relatórios:** Gere PDFs de faturamento por periodo ou estoque através do menu de **Relatórios**.

`--`

## 📂 Estrutura do Projeto

````plaintext

autowash-manager/

├── database/               # Scripts SQL e banco de dados SQLite (.db)

├── src/main/java/br/com/autowashmanager/

│   ├── dal/                # Conexão com o banco (ModuloConexao)

│   ├── model/              # Modelos de dados (Clima, Endereco, etc)

│   ├── service/            # Lógica de negócio e validadores de OS/Produtos

│   ├── telas/              # Telas em Swing (GUI)

│   └── util/               # Classes utilitárias (DbUtils)

├── src/main/resources/

│   ├── icones/             # Ativos visuais do sistema

│   └── reports/            # Arquivos .jasper para relatórios

└── pom.xml                 # Gerenciador de dependências Maven

`````

`--`

## ❤️ Motivação / História do projeto

Este sistema não é apenas um exercício de programação, mas uma **ferramenta de trabalho real** desenvolvida para a lavação da minha mãe. O projeto foi guiado pela vontade de resolver problemas práticos de um pequeno negócio familiar, aplicando tecnologia para trazer eficiência, controle e profissionalismo ao atendimento diário.

`--`

## ⚖️ Contribuição e Licença`

Sinta-se à vontade para contribuir com melhorias ou sugerir novas funcionalidades abrindo uma *issue* ou *pull request*.

Este projeto está sob a licença **MIT**. Veja o arquivo `LICENSE` para mais detalhes.

`--`

<p align="center">Desenvolvido com ❤️ por <strong>Henrique Baz</strong> 🚀</p>
