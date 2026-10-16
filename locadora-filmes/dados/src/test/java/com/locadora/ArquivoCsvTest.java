package com.locadora;

import com.locadora.modelo.Categoria;
import com.locadora.servico.Locadora;
import com.locadora.util.ArquivoCsv;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes de exportação e importação de CSV.
 */
public class ArquivoCsvTest {

    @Test
    public void exportaEImportaFilmes() throws IOException {
        Locadora origem = new Locadora("Origem");
        origem.cadastrarFilme("Matrix", "Ficção", 1999, 14, Categoria.CLASSICO, 2);
        origem.cadastrarFilme("Moana 2", "Animação", 2024, 0, Categoria.LANCAMENTO, 3);

        File arquivo = File.createTempFile("filmes", ".csv");
        arquivo.deleteOnExit();
        ArquivoCsv.exportarFilmes(origem, arquivo.getAbsolutePath());

        Locadora destino = new Locadora("Destino");
        int importados = ArquivoCsv.importarFilmes(destino, arquivo.getAbsolutePath());

        assertEquals(2, importados);
        assertEquals(1, destino.pesquisarFilmesPorTitulo("matrix").size());
        assertEquals(3, destino.pesquisarFilmesPorTitulo("moana").get(0).getQuantidadeCopias());
    }

    @Test
    public void importacaoIgnoraLinhasInvalidas() throws IOException {
        File arquivo = File.createTempFile("clientes", ".csv");
        arquivo.deleteOnExit();
        String conteudo = "nome;cpf;dataNascimento;telefone\n"
                + "Fernanda;66666666666;14/03/1995;9999\n"
                + "CPF Curto;123;14/03/1995;9999\n"
                + "Data Ruim;77777777777;31-12-1990;9999\n"
                + "Rafael;88888888888;22/07/1988;8888\n";
        Files.write(arquivo.toPath(), conteudo.getBytes(StandardCharsets.UTF_8));

        Locadora locadora = new Locadora("Teste");
        int importados = ArquivoCsv.importarClientes(locadora, arquivo.getAbsolutePath());

        assertEquals(2, importados);
        assertNotNull(locadora.buscarClientePorCpf("66666666666"));
        assertEquals(2, locadora.listarClientes().size());
    }

    @Test
    public void exportaClientesComCabecalho() throws IOException {
        Locadora locadora = new Locadora("Teste");
        locadora.cadastrarCliente("Maria", "11111111111", LocalDate.of(1990, 1, 1), "9999");

        File arquivo = File.createTempFile("export", ".csv");
        arquivo.deleteOnExit();
        ArquivoCsv.exportarClientes(locadora, arquivo.getAbsolutePath());

        java.util.List<String> linhas = Files.readAllLines(arquivo.toPath(), StandardCharsets.UTF_8);
        assertEquals(2, linhas.size());
        assertTrue(linhas.get(0).startsWith("nome;cpf"));
        assertTrue(linhas.get(1).startsWith("Maria;11111111111;01/01/1990"));
    }
}
