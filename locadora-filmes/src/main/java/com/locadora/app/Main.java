package com.locadora.app;

import com.locadora.excecao.RegraNegocioException;
import com.locadora.modelo.Aluguel;
import com.locadora.modelo.Categoria;
import com.locadora.modelo.Cliente;
import com.locadora.modelo.Filme;
import com.locadora.modelo.FormaPagamento;
import com.locadora.modelo.Funcionario;
import com.locadora.modelo.Pagamento;
import com.locadora.servico.Locadora;
import com.locadora.servico.Relatorios;
import com.locadora.util.ArquivoCsv;
import com.locadora.util.DadosExemplo;
import com.locadora.util.Entrada;
import com.locadora.util.Formatador;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Classe principal: apresenta os menus no terminal.
 * Toda a lógica de negócio fica nas classes do pacote modelo e servico.
 */
public class Main {

    private static Locadora locadora = new Locadora("CineMax Locadora");
    private static Relatorios relatorios = new Relatorios(locadora);

    public static void main(String[] args) {
        Locale.setDefault(Locale.forLanguageTag("pt-BR"));

        System.out.println("=== " + locadora.getNome().toUpperCase() + " ===");
        String resposta = Entrada.lerTexto("Carregar dados de exemplo? (s/n): ");
        if (resposta.equalsIgnoreCase("s")) {
            DadosExemplo.carregar(locadora);
            System.out.println("Dados de exemplo carregados!");
        }

        int opcao;
        do {
            // status automático: aluguéis vencidos passam para ATRASADO
            locadora.atualizarStatusAlugueis(LocalDate.now());

            System.out.println("\n===== MENU PRINCIPAL =====");
            System.out.println("1 - Clientes");
            System.out.println("2 - Filmes");
            System.out.println("3 - Funcionários");
            System.out.println("4 - Aluguéis (alugar, renovar, devolver, pagar)");
            System.out.println("5 - Relatórios e indicadores");
            System.out.println("6 - Arquivos CSV (exportar / importar)");
            System.out.println("0 - Sair");
            opcao = Entrada.lerInt("Escolha: ");

            switch (opcao) {
                case 1: menuClientes(); break;
                case 2: menuFilmes(); break;
                case 3: menuFuncionarios(); break;
                case 4: menuAlugueis(); break;
                case 5: menuRelatorios(); break;
                case 6: menuArquivos(); break;
                case 0: System.out.println("Até logo!"); break;
                default: System.out.println("Opção inválida.");
            }
        } while (opcao != 0);
    }

    // =====================================================
    // MENU CLIENTES
    // =====================================================

    private static void menuClientes() {
        int opcao;
        do {
            System.out.println("\n--- CLIENTES ---");
            System.out.println("1 - Cadastrar");
            System.out.println("2 - Listar todos");
            System.out.println("3 - Consultar por ID");
            System.out.println("4 - Pesquisar por nome");
            System.out.println("5 - Editar");
            System.out.println("6 - Desativar");
            System.out.println("7 - Listar ordenado por nome");
            System.out.println("8 - Listar ordenado por pontos de fidelidade");
            System.out.println("0 - Voltar");
            opcao = Entrada.lerInt("Escolha: ");

            try {
                switch (opcao) {
                    case 1: cadastrarCliente(); break;
                    case 2: imprimirLista(locadora.listarClientes()); break;
                    case 3: consultarCliente(); break;
                    case 4: imprimirLista(locadora.pesquisarClientesPorNome(Entrada.lerTexto("Nome (ou parte): "))); break;
                    case 5: editarCliente(); break;
                    case 6:
                        locadora.desativarCliente(Entrada.lerInt("ID do cliente: "));
                        System.out.println("Cliente desativado.");
                        break;
                    case 7: imprimirLista(locadora.listarClientesOrdenadosPorNome()); break;
                    case 8: imprimirLista(locadora.listarClientesOrdenadosPorPontos()); break;
                    case 0: break;
                    default: System.out.println("Opção inválida.");
                }
            } catch (RegraNegocioException e) {
                System.out.println("ERRO: " + e.getMessage());
            }
        } while (opcao != 0);
    }

    private static void cadastrarCliente() {
        String nome = Entrada.lerTexto("Nome: ");
        String cpf = Entrada.lerTexto("CPF (11 números): ");
        LocalDate nascimento = Entrada.lerData("Data de nascimento (dd/MM/aaaa): ");
        String telefone = Entrada.lerTexto("Telefone: ");
        Cliente c = locadora.cadastrarCliente(nome, cpf, nascimento, telefone);
        System.out.println("Cliente cadastrado: " + c);
    }

    private static void consultarCliente() {
        Cliente c = locadora.buscarClientePorId(Entrada.lerInt("ID do cliente: "));
        if (c == null) {
            System.out.println("Cliente não encontrado.");
            return;
        }
        System.out.println(c);
        System.out.println("Idade: " + c.getIdade() + " anos");
        System.out.println("Desconto de fidelidade: " + c.getPercentualDesconto() + "%");
        System.out.println("Aluguéis ativos: " + c.contarAlugueisAtivos() + "/" + Cliente.LIMITE_ALUGUEIS_ATIVOS);
        System.out.println("Pagamento pendente: " + (c.temPagamentoPendente() ? "SIM" : "não"));
    }

    private static void editarCliente() {
        int id = Entrada.lerInt("ID do cliente: ");
        String nome = Entrada.lerTexto("Novo nome (Enter para manter): ");
        String telefone = Entrada.lerTexto("Novo telefone (Enter para manter): ");
        locadora.editarCliente(id, nome, telefone);
        System.out.println("Cliente atualizado.");
    }

    // =====================================================
    // MENU FILMES
    // =====================================================

    private static void menuFilmes() {
        int opcao;
        do {
            System.out.println("\n--- FILMES ---");
            System.out.println("1 - Cadastrar");
            System.out.println("2 - Listar todos");
            System.out.println("3 - Consultar por ID");
            System.out.println("4 - Pesquisar por título");
            System.out.println("5 - Pesquisar por gênero");
            System.out.println("6 - Editar");
            System.out.println("7 - Desativar");
            System.out.println("8 - Adicionar cópias");
            System.out.println("9 - Listar ordenado por título");
            System.out.println("10 - Listar ordenado por ano (mais novos primeiro)");
            System.out.println("11 - Listar apenas disponíveis para aluguel");
            System.out.println("0 - Voltar");
            opcao = Entrada.lerInt("Escolha: ");

            try {
                switch (opcao) {
                    case 1: cadastrarFilme(); break;
                    case 2: imprimirLista(locadora.listarFilmes()); break;
                    case 3: consultarFilme(); break;
                    case 4: imprimirLista(locadora.pesquisarFilmesPorTitulo(Entrada.lerTexto("Título (ou parte): "))); break;
                    case 5: imprimirLista(locadora.pesquisarFilmesPorGenero(Entrada.lerTexto("Gênero: "))); break;
                    case 6: editarFilme(); break;
                    case 7:
                        locadora.desativarFilme(Entrada.lerInt("ID do filme: "));
                        System.out.println("Filme desativado.");
                        break;
                    case 8:
                        int id = Entrada.lerInt("ID do filme: ");
                        int qtd = Entrada.lerInt("Quantidade de cópias a adicionar: ");
                        locadora.adicionarCopias(id, qtd);
                        System.out.println("Cópias adicionadas.");
                        break;
                    case 9: imprimirLista(locadora.listarFilmesOrdenadosPorTitulo()); break;
                    case 10: imprimirLista(locadora.listarFilmesOrdenadosPorAno()); break;
                    case 11: imprimirLista(locadora.listarFilmesDisponiveis()); break;
                    case 0: break;
                    default: System.out.println("Opção inválida.");
                }
            } catch (RegraNegocioException e) {
                System.out.println("ERRO: " + e.getMessage());
            }
        } while (opcao != 0);
    }

    private static Categoria escolherCategoria() {
        System.out.println("Categorias:");
        Categoria[] todas = Categoria.values();
        for (int i = 0; i < todas.length; i++) {
            System.out.println((i + 1) + " - " + todas[i] + " (diária "
                    + String.format("R$ %.2f", todas[i].getValorDiaria()) + ", prazo " + todas[i].getPrazoDias() + " dias)");
        }
        int escolha = Entrada.lerInt("Categoria: ");
        if (escolha < 1 || escolha > todas.length) {
            throw new RegraNegocioException("Categoria inválida.");
        }
        return todas[escolha - 1];
    }

    private static void cadastrarFilme() {
        String titulo = Entrada.lerTexto("Título: ");
        String genero = Entrada.lerTexto("Gênero: ");
        int ano = Entrada.lerInt("Ano: ");
        int classificacao = Entrada.lerInt("Classificação indicativa (0, 10, 12, 14, 16 ou 18): ");
        Categoria categoria = escolherCategoria();
        int copias = Entrada.lerInt("Quantidade de cópias: ");
        Filme f = locadora.cadastrarFilme(titulo, genero, ano, classificacao, categoria, copias);
        System.out.println("Filme cadastrado: " + f);
    }

    private static void consultarFilme() {
        Filme f = locadora.buscarFilmePorId(Entrada.lerInt("ID do filme: "));
        if (f == null) {
            System.out.println("Filme não encontrado.");
            return;
        }
        System.out.println(f);
        System.out.println("Prazo de aluguel: " + f.getPrazoDias() + " dias");
        System.out.println("Vezes alugado: " + f.getVezesAlugado());
    }

    private static void editarFilme() {
        int id = Entrada.lerInt("ID do filme: ");
        String titulo = Entrada.lerTexto("Novo título (Enter para manter): ");
        String genero = Entrada.lerTexto("Novo gênero (Enter para manter): ");
        String trocar = Entrada.lerTexto("Trocar categoria? (s/n): ");
        Categoria categoria = null;
        if (trocar.equalsIgnoreCase("s")) {
            categoria = escolherCategoria();
        }
        locadora.editarFilme(id, titulo, genero, categoria);
        System.out.println("Filme atualizado.");
    }

    // =====================================================
    // MENU FUNCIONÁRIOS
    // =====================================================

    private static void menuFuncionarios() {
        int opcao;
        do {
            System.out.println("\n--- FUNCIONÁRIOS ---");
            System.out.println("1 - Cadastrar");
            System.out.println("2 - Listar");
            System.out.println("0 - Voltar");
            opcao = Entrada.lerInt("Escolha: ");

            try {
                switch (opcao) {
                    case 1:
                        String nome = Entrada.lerTexto("Nome: ");
                        String cargo = Entrada.lerTexto("Cargo: ");
                        Funcionario f = locadora.cadastrarFuncionario(nome, cargo);
                        System.out.println("Funcionário cadastrado: " + f);
                        break;
                    case 2: imprimirLista(locadora.listarFuncionarios()); break;
                    case 0: break;
                    default: System.out.println("Opção inválida.");
                }
            } catch (RegraNegocioException e) {
                System.out.println("ERRO: " + e.getMessage());
            }
        } while (opcao != 0);
    }

    // =====================================================
    // MENU ALUGUÉIS (processo principal)
    // =====================================================

    private static void menuAlugueis() {
        int opcao;
        do {
            locadora.atualizarStatusAlugueis(LocalDate.now());

            System.out.println("\n--- ALUGUÉIS ---");
            System.out.println("1 - Alugar filme");
            System.out.println("2 - Renovar aluguel");
            System.out.println("3 - Devolver filme (gera cobrança)");
            System.out.println("4 - Registrar pagamento");
            System.out.println("5 - Listar todos os aluguéis");
            System.out.println("6 - Listar aluguéis em aberto");
            System.out.println("7 - Listar aluguéis atrasados");
            System.out.println("8 - Histórico de um cliente");
            System.out.println("9 - Listar pagamentos pendentes");
            System.out.println("0 - Voltar");
            opcao = Entrada.lerInt("Escolha: ");

            try {
                switch (opcao) {
                    case 1: alugarFilme(); break;
                    case 2: renovarAluguel(); break;
                    case 3: devolverFilme(); break;
                    case 4: registrarPagamento(); break;
                    case 5: imprimirLista(locadora.listarAlugueis()); break;
                    case 6: imprimirLista(locadora.listarAlugueisEmAberto()); break;
                    case 7: imprimirLista(locadora.listarAlugueisAtrasados()); break;
                    case 8: historicoCliente(); break;
                    case 9: imprimirLista(locadora.listarPagamentosPendentes()); break;
                    case 0: break;
                    default: System.out.println("Opção inválida.");
                }
            } catch (RegraNegocioException e) {
                System.out.println("ERRO: " + e.getMessage());
            }
        } while (opcao != 0);
    }

    private static void alugarFilme() {
        int idCliente = Entrada.lerInt("ID do cliente: ");
        int idFilme = Entrada.lerInt("ID do filme: ");
        int idFuncionario = Entrada.lerInt("ID do funcionário: ");
        LocalDate data = Entrada.lerDataOuHoje("Data do aluguel");
        Aluguel a = locadora.alugarFilme(idCliente, idFilme, idFuncionario, data);
        System.out.println("Aluguel realizado! " + a);
        System.out.println("Valor base: " + String.format("R$ %.2f", a.calcularValorBase())
                + " (cobrado na devolução)");
    }

    private static void renovarAluguel() {
        int id = Entrada.lerInt("ID do aluguel: ");
        locadora.renovarAluguel(id, LocalDate.now());
        System.out.println("Aluguel renovado. Nova data prevista: "
                + locadora.buscarAluguelPorId(id).getDataPrevistaDevolucao().format(
                Formatador.FORMATO_DATA));
    }

    private static void devolverFilme() {
        int id = Entrada.lerInt("ID do aluguel: ");
        LocalDate data = Entrada.lerDataOuHoje("Data da devolução");
        Pagamento p = locadora.devolverFilme(id, data);
        System.out.println("Filme devolvido! " + p);

        String pagarAgora = Entrada.lerTexto("Registrar o pagamento agora? (s/n): ");
        if (pagarAgora.equalsIgnoreCase("s")) {
            FormaPagamento forma = escolherFormaPagamento();
            locadora.registrarPagamento(id, forma, data);
            System.out.println("Pagamento registrado. Obrigado!");
        }
    }

    private static void registrarPagamento() {
        int id = Entrada.lerInt("ID do aluguel: ");
        FormaPagamento forma = escolherFormaPagamento();
        locadora.registrarPagamento(id, forma, LocalDate.now());
        System.out.println("Pagamento registrado.");
    }

    private static FormaPagamento escolherFormaPagamento() {
        System.out.println("1 - Dinheiro | 2 - Pix | 3 - Cartão");
        int escolha = Entrada.lerInt("Forma de pagamento: ");
        switch (escolha) {
            case 1: return FormaPagamento.DINHEIRO;
            case 2: return FormaPagamento.PIX;
            case 3: return FormaPagamento.CARTAO;
            default: throw new RegraNegocioException("Forma de pagamento inválida.");
        }
    }

    private static void historicoCliente() {
        Cliente c = locadora.buscarClientePorId(Entrada.lerInt("ID do cliente: "));
        if (c == null) {
            System.out.println("Cliente não encontrado.");
            return;
        }
        System.out.println("Histórico de " + c.getNome() + ":");
        imprimirLista(c.getAlugueis());
    }

    // =====================================================
    // MENU RELATÓRIOS
    // =====================================================

    private static void menuRelatorios() {
        int opcao;
        do {
            locadora.atualizarStatusAlugueis(LocalDate.now());

            System.out.println("\n--- RELATÓRIOS E INDICADORES ---");
            System.out.println("1 - Financeiro (total, média e maior pagamento)");
            System.out.println("2 - Filme mais e menos alugado");
            System.out.println("3 - Rankings (clientes e filmes)");
            System.out.println("4 - Indicadores (taxa de atraso e ocupação)");
            System.out.println("5 - Comissão dos funcionários");
            System.out.println("6 - Relatório completo");
            System.out.println("0 - Voltar");
            opcao = Entrada.lerInt("Escolha: ");

            System.out.println();
            switch (opcao) {
                case 1: System.out.println(relatorios.relatorioFinanceiro()); break;
                case 2: System.out.println(relatorios.relatorioFilmes()); break;
                case 3: System.out.println(relatorios.relatorioRankings()); break;
                case 4: System.out.println(relatorios.relatorioIndicadores()); break;
                case 5: System.out.println(relatorios.relatorioComissoes()); break;
                case 6: System.out.println(relatorios.gerarRelatorioCompleto()); break;
                case 0: break;
                default: System.out.println("Opção inválida.");
            }
        } while (opcao != 0);
    }

    // =====================================================
    // MENU ARQUIVOS CSV
    // =====================================================

    private static void menuArquivos() {
        int opcao;
        do {
            System.out.println("\n--- ARQUIVOS CSV ---");
            System.out.println("1 - Exportar filmes");
            System.out.println("2 - Exportar clientes");
            System.out.println("3 - Exportar aluguéis");
            System.out.println("4 - Importar filmes (ex.: dados/filmes.csv)");
            System.out.println("5 - Importar clientes (ex.: dados/clientes.csv)");
            System.out.println("0 - Voltar");
            opcao = Entrada.lerInt("Escolha: ");

            try {
                switch (opcao) {
                    case 1:
                        ArquivoCsv.exportarFilmes(locadora, caminhoExportacao("filmes.csv"));
                        System.out.println("Arquivo exportado em exportados/filmes.csv");
                        break;
                    case 2:
                        ArquivoCsv.exportarClientes(locadora, caminhoExportacao("clientes.csv"));
                        System.out.println("Arquivo exportado em exportados/clientes.csv");
                        break;
                    case 3:
                        ArquivoCsv.exportarAlugueis(locadora, caminhoExportacao("alugueis.csv"));
                        System.out.println("Arquivo exportado em exportados/alugueis.csv");
                        break;
                    case 4:
                        String arquivoFilmes = Entrada.lerTexto("Caminho do arquivo: ");
                        int filmes = ArquivoCsv.importarFilmes(locadora, arquivoFilmes);
                        System.out.println(filmes + " filme(s) importado(s).");
                        break;
                    case 5:
                        String arquivoClientes = Entrada.lerTexto("Caminho do arquivo: ");
                        int clientes = ArquivoCsv.importarClientes(locadora, arquivoClientes);
                        System.out.println(clientes + " cliente(s) importado(s).");
                        break;
                    case 0: break;
                    default: System.out.println("Opção inválida.");
                }
            } catch (IOException e) {
                System.out.println("ERRO de arquivo: " + e.getMessage());
            } catch (RegraNegocioException e) {
                System.out.println("ERRO: " + e.getMessage());
            }
        } while (opcao != 0);
    }

    private static String caminhoExportacao(String nomeArquivo) {
        File pasta = new File("exportados");
        if (!pasta.exists()) {
            pasta.mkdir();
        }
        return "exportados/" + nomeArquivo;
    }

    // =====================================================
    // AUXILIAR
    // =====================================================

    private static void imprimirLista(ArrayList<?> lista) {
        if (lista.isEmpty()) {
            System.out.println("Nenhum registro encontrado.");
            return;
        }
        for (Object item : lista) {
            System.out.println(item);
        }
    }
}
