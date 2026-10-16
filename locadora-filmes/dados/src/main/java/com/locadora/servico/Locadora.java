package com.locadora.servico;

import com.locadora.excecao.RegraNegocioException;
import com.locadora.modelo.Aluguel;
import com.locadora.modelo.Categoria;
import com.locadora.modelo.Cliente;
import com.locadora.modelo.Copia;
import com.locadora.modelo.Filme;
import com.locadora.modelo.FormaPagamento;
import com.locadora.modelo.Funcionario;
import com.locadora.modelo.Pagamento;
import com.locadora.modelo.StatusAluguel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Classe principal do domínio: guarda as listas de clientes, filmes,
 * funcionários e aluguéis e coordena as operações do sistema.
 */
public class Locadora {

    private String nome;
    private ArrayList<Cliente> clientes;
    private ArrayList<Filme> filmes;
    private ArrayList<Funcionario> funcionarios;
    private ArrayList<Aluguel> alugueis;

    private int proximoIdCliente = 1;
    private int proximoIdFilme = 1;
    private int proximoIdFuncionario = 1;
    private int proximoIdAluguel = 1;
    private int proximoIdPagamento = 1;

    public Locadora(String nome) {
        this.nome = nome;
        this.clientes = new ArrayList<>();
        this.filmes = new ArrayList<>();
        this.funcionarios = new ArrayList<>();
        this.alugueis = new ArrayList<>();
    }

    // =====================================================
    // CLIENTES
    // =====================================================

    public Cliente cadastrarCliente(String nome, String cpf, LocalDate dataNascimento, String telefone) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraNegocioException("O nome do cliente é obrigatório.");
        }
        String cpfLimpo = (cpf == null) ? "" : cpf.replaceAll("[^0-9]", "");
        if (cpfLimpo.length() != 11) {
            throw new RegraNegocioException("CPF inválido: informe 11 números.");
        }
        if (buscarClientePorCpf(cpfLimpo) != null) {
            throw new RegraNegocioException("Já existe um cliente com este CPF.");
        }
        if (dataNascimento == null || dataNascimento.isAfter(LocalDate.now())) {
            throw new RegraNegocioException("Data de nascimento inválida.");
        }
        Cliente cliente = new Cliente(proximoIdCliente++, nome.trim(), cpfLimpo, dataNascimento, telefone);
        clientes.add(cliente);
        return cliente;
    }

    public Cliente buscarClientePorId(int id) {
        for (Cliente c : clientes) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    public Cliente buscarClientePorCpf(String cpf) {
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");
        for (Cliente c : clientes) {
            if (c.getCpf().equals(cpfLimpo)) {
                return c;
            }
        }
        return null;
    }

    public ArrayList<Cliente> pesquisarClientesPorNome(String trecho) {
        ArrayList<Cliente> resultado = new ArrayList<>();
        for (Cliente c : clientes) {
            if (c.getNome().toLowerCase().contains(trecho.toLowerCase())) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    public void editarCliente(int id, String novoNome, String novoTelefone) {
        Cliente cliente = exigirCliente(id);
        if (novoNome != null && !novoNome.trim().isEmpty()) {
            cliente.setNome(novoNome.trim());
        }
        if (novoTelefone != null && !novoTelefone.trim().isEmpty()) {
            cliente.setTelefone(novoTelefone.trim());
        }
    }

    /** Regra: não é possível desativar cliente com aluguel ativo ou pagamento pendente. */
    public void desativarCliente(int id) {
        Cliente cliente = exigirCliente(id);
        if (cliente.contarAlugueisAtivos() > 0) {
            throw new RegraNegocioException("Cliente ainda possui filmes para devolver.");
        }
        if (cliente.temPagamentoPendente()) {
            throw new RegraNegocioException("Cliente possui pagamento pendente.");
        }
        cliente.setAtivo(false);
    }

    public ArrayList<Cliente> listarClientes() {
        return new ArrayList<>(clientes);
    }

    public ArrayList<Cliente> listarClientesOrdenadosPorNome() {
        ArrayList<Cliente> lista = new ArrayList<>(clientes);
        lista.sort(Comparator.comparing(Cliente::getNome, String.CASE_INSENSITIVE_ORDER));
        return lista;
    }

    public ArrayList<Cliente> listarClientesOrdenadosPorPontos() {
        ArrayList<Cliente> lista = new ArrayList<>(clientes);
        lista.sort((a, b) -> Integer.compare(b.getPontos(), a.getPontos()));
        return lista;
    }

    // =====================================================
    // FILMES
    // =====================================================

    public Filme cadastrarFilme(String titulo, String genero, int ano, int classificacao,
                                Categoria categoria, int quantidadeCopias) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new RegraNegocioException("O título do filme é obrigatório.");
        }
        if (ano < 1888 || ano > LocalDate.now().getYear() + 1) {
            throw new RegraNegocioException("Ano do filme inválido.");
        }
        if (classificacao != 0 && classificacao != 10 && classificacao != 12
                && classificacao != 14 && classificacao != 16 && classificacao != 18) {
            throw new RegraNegocioException("Classificação deve ser 0 (livre), 10, 12, 14, 16 ou 18.");
        }
        if (categoria == null) {
            throw new RegraNegocioException("A categoria do filme é obrigatória.");
        }
        if (quantidadeCopias < 1) {
            throw new RegraNegocioException("O filme precisa ter pelo menos 1 cópia.");
        }
        Filme filme = new Filme(proximoIdFilme++, titulo.trim(), genero, ano, classificacao, categoria);
        filme.adicionarCopias(quantidadeCopias);
        filmes.add(filme);
        return filme;
    }

    public Filme buscarFilmePorId(int id) {
        for (Filme f : filmes) {
            if (f.getId() == id) {
                return f;
            }
        }
        return null;
    }

    public ArrayList<Filme> pesquisarFilmesPorTitulo(String trecho) {
        ArrayList<Filme> resultado = new ArrayList<>();
        for (Filme f : filmes) {
            if (f.getTitulo().toLowerCase().contains(trecho.toLowerCase())) {
                resultado.add(f);
            }
        }
        return resultado;
    }

    public ArrayList<Filme> pesquisarFilmesPorGenero(String genero) {
        ArrayList<Filme> resultado = new ArrayList<>();
        for (Filme f : filmes) {
            if (f.getGenero().toLowerCase().contains(genero.toLowerCase())) {
                resultado.add(f);
            }
        }
        return resultado;
    }

    public void editarFilme(int id, String novoTitulo, String novoGenero, Categoria novaCategoria) {
        Filme filme = exigirFilme(id);
        if (novoTitulo != null && !novoTitulo.trim().isEmpty()) {
            filme.setTitulo(novoTitulo.trim());
        }
        if (novoGenero != null && !novoGenero.trim().isEmpty()) {
            filme.setGenero(novoGenero.trim());
        }
        if (novaCategoria != null) {
            filme.setCategoria(novaCategoria);
        }
    }

    public void adicionarCopias(int idFilme, int quantidade) {
        if (quantidade < 1) {
            throw new RegraNegocioException("A quantidade deve ser pelo menos 1.");
        }
        exigirFilme(idFilme).adicionarCopias(quantidade);
    }

    /** Regra: não é possível desativar um filme que está com alguma cópia alugada. */
    public void desativarFilme(int id) {
        Filme filme = exigirFilme(id);
        if (filme.temCopiaAlugada()) {
            throw new RegraNegocioException("O filme possui cópias alugadas no momento.");
        }
        filme.setAtivo(false);
    }

    public ArrayList<Filme> listarFilmes() {
        return new ArrayList<>(filmes);
    }

    public ArrayList<Filme> listarFilmesDisponiveis() {
        ArrayList<Filme> resultado = new ArrayList<>();
        for (Filme f : filmes) {
            if (f.isAtivo() && f.getQuantidadeDisponivel() > 0) {
                resultado.add(f);
            }
        }
        return resultado;
    }

    public ArrayList<Filme> listarFilmesOrdenadosPorTitulo() {
        ArrayList<Filme> lista = new ArrayList<>(filmes);
        lista.sort(Comparator.comparing(Filme::getTitulo, String.CASE_INSENSITIVE_ORDER));
        return lista;
    }

    public ArrayList<Filme> listarFilmesOrdenadosPorAno() {
        ArrayList<Filme> lista = new ArrayList<>(filmes);
        lista.sort((a, b) -> Integer.compare(b.getAno(), a.getAno()));
        return lista;
    }

    // =====================================================
    // FUNCIONÁRIOS
    // =====================================================

    public Funcionario cadastrarFuncionario(String nome, String cargo) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraNegocioException("O nome do funcionário é obrigatório.");
        }
        Funcionario f = new Funcionario(proximoIdFuncionario++, nome.trim(), cargo);
        funcionarios.add(f);
        return f;
    }

    public Funcionario buscarFuncionarioPorId(int id) {
        for (Funcionario f : funcionarios) {
            if (f.getId() == id) {
                return f;
            }
        }
        return null;
    }

    public ArrayList<Funcionario> listarFuncionarios() {
        return new ArrayList<>(funcionarios);
    }

    // =====================================================
    // PROCESSO PRINCIPAL: ALUGAR -> (RENOVAR) -> DEVOLVER -> PAGAR
    // =====================================================

    /**
     * Associa cliente + cópia do filme + funcionário em um novo aluguel.
     * Aplica as regras: cliente apto, filme ativo e cópia disponível.
     */
    public Aluguel alugarFilme(int idCliente, int idFilme, int idFuncionario, LocalDate data) {
        Cliente cliente = exigirCliente(idCliente);
        Filme filme = exigirFilme(idFilme);
        Funcionario funcionario = exigirFuncionario(idFuncionario);

        if (!filme.isAtivo()) {
            throw new RegraNegocioException("Este filme está inativo.");
        }
        cliente.validarPodeAlugar(filme, data);

        Copia copia = filme.buscarCopiaDisponivel();
        if (copia == null) {
            throw new RegraNegocioException("Não há cópias disponíveis de \"" + filme.getTitulo() + "\".");
        }

        Aluguel aluguel = new Aluguel(proximoIdAluguel++, cliente, copia, funcionario, data);
        copia.marcarComoAlugada();
        cliente.adicionarAluguel(aluguel);
        funcionario.registrarAluguel(aluguel);
        alugueis.add(aluguel);
        return aluguel;
    }

    public void renovarAluguel(int idAluguel, LocalDate hoje) {
        exigirAluguel(idAluguel).renovar(hoje);
    }

    /** Devolve o filme, gera a cobrança e pontua o cliente se devolveu no prazo. */
    public Pagamento devolverFilme(int idAluguel, LocalDate dataDevolucao) {
        Aluguel aluguel = exigirAluguel(idAluguel);
        aluguel.devolver(dataDevolucao);
        Pagamento pagamento = aluguel.gerarPagamento(proximoIdPagamento++);
        aluguel.getCliente().pontuarDevolucao(!aluguel.devolvidoComAtraso());
        return pagamento;
    }

    public void registrarPagamento(int idAluguel, FormaPagamento forma, LocalDate data) {
        Aluguel aluguel = exigirAluguel(idAluguel);
        if (aluguel.getPagamento() == null) {
            throw new RegraNegocioException("Este aluguel ainda não foi devolvido, não há cobrança.");
        }
        aluguel.getPagamento().pagar(forma, data);
    }

    /** Atualiza o status dos aluguéis vencidos. Retorna quantos ficaram ATRASADOS. */
    public int atualizarStatusAlugueis(LocalDate hoje) {
        int atrasados = 0;
        for (Aluguel a : alugueis) {
            a.atualizarStatus(hoje);
            if (a.getStatus() == StatusAluguel.ATRASADO) {
                atrasados++;
            }
        }
        return atrasados;
    }

    // ---------- consultas de aluguéis e pagamentos ----------

    public Aluguel buscarAluguelPorId(int id) {
        for (Aluguel a : alugueis) {
            if (a.getId() == id) {
                return a;
            }
        }
        return null;
    }

    public ArrayList<Aluguel> listarAlugueis() {
        return new ArrayList<>(alugueis);
    }

    public ArrayList<Aluguel> listarAlugueisEmAberto() {
        ArrayList<Aluguel> resultado = new ArrayList<>();
        for (Aluguel a : alugueis) {
            if (a.getStatus() != StatusAluguel.DEVOLVIDO) {
                resultado.add(a);
            }
        }
        return resultado;
    }

    public ArrayList<Aluguel> listarAlugueisAtrasados() {
        ArrayList<Aluguel> resultado = new ArrayList<>();
        for (Aluguel a : alugueis) {
            if (a.getStatus() == StatusAluguel.ATRASADO) {
                resultado.add(a);
            }
        }
        return resultado;
    }

    public ArrayList<Pagamento> listarPagamentos() {
        ArrayList<Pagamento> resultado = new ArrayList<>();
        for (Aluguel a : alugueis) {
            if (a.getPagamento() != null) {
                resultado.add(a.getPagamento());
            }
        }
        return resultado;
    }

    public ArrayList<Pagamento> listarPagamentosPendentes() {
        ArrayList<Pagamento> resultado = new ArrayList<>();
        for (Pagamento p : listarPagamentos()) {
            if (!p.isPago()) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    // =====================================================
    // MÉTODOS AUXILIARES (lançam erro se não encontrar)
    // =====================================================

    private Cliente exigirCliente(int id) {
        Cliente c = buscarClientePorId(id);
        if (c == null) {
            throw new RegraNegocioException("Cliente " + id + " não encontrado.");
        }
        return c;
    }

    private Filme exigirFilme(int id) {
        Filme f = buscarFilmePorId(id);
        if (f == null) {
            throw new RegraNegocioException("Filme " + id + " não encontrado.");
        }
        return f;
    }

    private Funcionario exigirFuncionario(int id) {
        Funcionario f = buscarFuncionarioPorId(id);
        if (f == null) {
            throw new RegraNegocioException("Funcionário " + id + " não encontrado.");
        }
        return f;
    }

    private Aluguel exigirAluguel(int id) {
        Aluguel a = buscarAluguelPorId(id);
        if (a == null) {
            throw new RegraNegocioException("Aluguel " + id + " não encontrado.");
        }
        return a;
    }

    public String getNome() {
        return nome;
    }
}
