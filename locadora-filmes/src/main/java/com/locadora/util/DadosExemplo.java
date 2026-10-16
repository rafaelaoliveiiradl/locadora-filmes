package com.locadora.util;

import com.locadora.modelo.Aluguel;
import com.locadora.modelo.Categoria;
import com.locadora.modelo.Cliente;
import com.locadora.modelo.Filme;
import com.locadora.modelo.FormaPagamento;
import com.locadora.modelo.Funcionario;
import com.locadora.servico.Locadora;

import java.time.LocalDate;

/**
 * Carrega alguns dados de exemplo para facilitar os testes e a apresentação.
 * As datas são calculadas a partir de hoje para sempre gerar casos de atraso.
 */
public class DadosExemplo {

    public static void carregar(Locadora locadora) {
        LocalDate hoje = LocalDate.now();

        // Funcionários
        Funcionario ana = locadora.cadastrarFuncionario("Ana Paula", "Atendente");
        Funcionario carlos = locadora.cadastrarFuncionario("Carlos Mendes", "Gerente");

        // Filmes
        Filme interestelar = locadora.cadastrarFilme("Interestelar", "Ficção Científica", 2014, 10, Categoria.CATALOGO, 2);
        Filme divertida = locadora.cadastrarFilme("Divertida Mente 2", "Animação", 2024, 0, Categoria.LANCAMENTO, 2);
        Filme coringa = locadora.cadastrarFilme("Coringa", "Drama", 2019, 16, Categoria.CATALOGO, 1);
        Filme poderoso = locadora.cadastrarFilme("O Poderoso Chefão", "Crime", 1972, 18, Categoria.CLASSICO, 1);
        Filme casablanca = locadora.cadastrarFilme("Casablanca", "Romance", 1942, 12, Categoria.CLASSICO, 1);
        Filme vingadores = locadora.cadastrarFilme("Vingadores: Ultimato", "Ação", 2019, 12, Categoria.CATALOGO, 2);

        // Clientes
        Cliente maria = locadora.cadastrarCliente("Maria Silva", "11111111111", LocalDate.of(1990, 5, 12), "(91) 98888-1111");
        Cliente joao = locadora.cadastrarCliente("João Souza", "22222222222", LocalDate.of(1985, 8, 30), "(91) 98888-2222");
        Cliente lucas = locadora.cadastrarCliente("Lucas Rocha", "33333333333", LocalDate.of(2000, 1, 20), "(91) 98888-3333");
        Cliente pedro = locadora.cadastrarCliente("Pedro Lima", "44444444444", hoje.minusYears(15), "(91) 98888-4444");
        Cliente beatriz = locadora.cadastrarCliente("Beatriz Costa", "55555555555", LocalDate.of(1998, 11, 3), "(91) 98888-5555");

        // Maria já é cliente fiel (60 pontos = 10% de desconto)
        maria.adicionarPontos(60);

        // Maria: aluguel no prazo, devolvido e pago
        Aluguel a1 = locadora.alugarFilme(maria.getId(), interestelar.getId(), ana.getId(), hoje.minusDays(12));
        locadora.devolverFilme(a1.getId(), hoje.minusDays(10));
        locadora.registrarPagamento(a1.getId(), FormaPagamento.PIX, hoje.minusDays(10));

        // João: devolveu com 3 dias de atraso (multa) e pagou
        Aluguel a2 = locadora.alugarFilme(joao.getId(), divertida.getId(), ana.getId(), hoje.minusDays(9));
        locadora.devolverFilme(a2.getId(), hoje.minusDays(4));
        locadora.registrarPagamento(a2.getId(), FormaPagamento.CARTAO, hoje.minusDays(4));

        // João: devolveu no prazo, mas AINDA NÃO PAGOU (pagamento pendente bloqueia novos aluguéis)
        Aluguel a3 = locadora.alugarFilme(joao.getId(), casablanca.getId(), carlos.getId(), hoje.minusDays(3));
        locadora.devolverFilme(a3.getId(), hoje.minusDays(1));

        // Lucas: está com o filme ATRASADO (prazo de 3 dias, alugou há 5)
        locadora.alugarFilme(lucas.getId(), coringa.getId(), carlos.getId(), hoje.minusDays(5));

        // Maria: filme clássico ainda dentro do prazo
        locadora.alugarFilme(maria.getId(), poderoso.getId(), carlos.getId(), hoje.minusDays(4));

        // Beatriz: aluguel de ontem, dentro do prazo
        locadora.alugarFilme(beatriz.getId(), vingadores.getId(), ana.getId(), hoje.minusDays(1));

        // Pedro (15 anos) não tem aluguéis: serve para testar a regra de classificação indicativa

        locadora.atualizarStatusAlugueis(hoje);
    }
}
