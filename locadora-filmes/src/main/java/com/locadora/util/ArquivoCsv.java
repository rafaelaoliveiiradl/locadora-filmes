package com.locadora.util;

import com.locadora.excecao.RegraNegocioException;
import com.locadora.modelo.Aluguel;
import com.locadora.modelo.Categoria;
import com.locadora.modelo.Cliente;
import com.locadora.modelo.Filme;
import com.locadora.servico.Locadora;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Importação e exportação de dados em arquivos CSV (separador ponto e vírgula).
 *
 * Formato de importação de filmes:   titulo;genero;ano;classificacao;categoria;copias
 * Formato de importação de clientes: nome;cpf;dataNascimento;telefone
 */
public class ArquivoCsv {

    private static final String SEP = ";";

    // =====================================================
    // EXPORTAÇÃO
    // =====================================================

    public static void exportarFilmes(Locadora locadora, String caminho) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(caminho, StandardCharsets.UTF_8))) {
            out.println("titulo;genero;ano;classificacao;categoria;copias;disponiveis;ativo");
            for (Filme f : locadora.listarFilmes()) {
                out.println(f.getTitulo() + SEP + f.getGenero() + SEP + f.getAno() + SEP
                        + f.getClassificacao() + SEP + f.getCategoria().name() + SEP
                        + f.getQuantidadeCopias() + SEP + f.getQuantidadeDisponivel() + SEP + f.isAtivo());
            }
        }
    }

    public static void exportarClientes(Locadora locadora, String caminho) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(caminho, StandardCharsets.UTF_8))) {
            out.println("nome;cpf;dataNascimento;telefone;pontos;ativo");
            for (Cliente c : locadora.listarClientes()) {
                out.println(c.getNome() + SEP + c.getCpf() + SEP + Formatador.data(c.getDataNascimento()) + SEP
                        + c.getTelefone() + SEP + c.getPontos() + SEP + c.isAtivo());
            }
        }
    }

    public static void exportarAlugueis(Locadora locadora, String caminho) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(caminho, StandardCharsets.UTF_8))) {
            out.println("id;cliente;filme;funcionario;dataAluguel;dataPrevista;dataDevolucao;status;valorTotal;pago");
            for (Aluguel a : locadora.listarAlugueis()) {
                String valor = "";
                String pago = "";
                if (a.getPagamento() != null) {
                    valor = String.format("%.2f", a.getPagamento().getValorTotal());
                    pago = String.valueOf(a.getPagamento().isPago());
                }
                out.println(a.getId() + SEP + a.getCliente().getNome() + SEP + a.getFilme().getTitulo() + SEP
                        + a.getFuncionario().getNome() + SEP + Formatador.data(a.getDataAluguel()) + SEP
                        + Formatador.data(a.getDataPrevistaDevolucao()) + SEP
                        + (a.getDataDevolucao() == null ? "" : Formatador.data(a.getDataDevolucao())) + SEP
                        + a.getStatus() + SEP + valor + SEP + pago);
            }
        }
    }

    // =====================================================
    // IMPORTAÇÃO
    // =====================================================

    /** Importa filmes de um CSV. Retorna quantos foram cadastrados. */
    public static int importarFilmes(Locadora locadora, String caminho) throws IOException {
        int importados = 0;
        try (BufferedReader in = new BufferedReader(new FileReader(caminho, StandardCharsets.UTF_8))) {
            String linha = in.readLine(); // cabeçalho
            int numeroLinha = 1;
            while ((linha = in.readLine()) != null) {
                numeroLinha++;
                if (linha.trim().isEmpty()) {
                    continue;
                }
                try {
                    String[] c = linha.split(SEP);
                    locadora.cadastrarFilme(c[0].trim(), c[1].trim(), Integer.parseInt(c[2].trim()),
                            Integer.parseInt(c[3].trim()), Categoria.valueOf(c[4].trim().toUpperCase()),
                            Integer.parseInt(c[5].trim()));
                    importados++;
                } catch (RegraNegocioException | IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
                    System.out.println("Linha " + numeroLinha + " ignorada: " + e.getMessage());
                }
            }
        }
        return importados;
    }

    /** Importa clientes de um CSV. Retorna quantos foram cadastrados. */
    public static int importarClientes(Locadora locadora, String caminho) throws IOException {
        int importados = 0;
        try (BufferedReader in = new BufferedReader(new FileReader(caminho, StandardCharsets.UTF_8))) {
            String linha = in.readLine(); // cabeçalho
            int numeroLinha = 1;
            while ((linha = in.readLine()) != null) {
                numeroLinha++;
                if (linha.trim().isEmpty()) {
                    continue;
                }
                try {
                    String[] c = linha.split(SEP);
                    LocalDate nascimento = LocalDate.parse(c[2].trim(), Formatador.FORMATO_DATA);
                    String telefone = (c.length > 3) ? c[3].trim() : "";
                    locadora.cadastrarCliente(c[0].trim(), c[1].trim(), nascimento, telefone);
                    importados++;
                } catch (RegraNegocioException | DateTimeParseException | ArrayIndexOutOfBoundsException e) {
                    System.out.println("Linha " + numeroLinha + " ignorada: " + e.getMessage());
                }
            }
        }
        return importados;
    }
}
