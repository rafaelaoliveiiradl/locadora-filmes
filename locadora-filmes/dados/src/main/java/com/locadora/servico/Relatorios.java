package com.locadora.servico;

import com.locadora.modelo.Aluguel;
import com.locadora.modelo.Cliente;
import com.locadora.modelo.Filme;
import com.locadora.modelo.Funcionario;
import com.locadora.modelo.Pagamento;
import com.locadora.modelo.StatusAluguel;
import com.locadora.util.Formatador;

import java.util.ArrayList;

/**
 * Cálculo de indicadores e geração de relatórios da locadora.
 * Os métodos "calcular/encontrar/ranking" devolvem valores (e podem ser testados);
 * os métodos "relatorio..." montam o texto que aparece no menu.
 */
public class Relatorios {

    private Locadora locadora;

    public Relatorios(Locadora locadora) {
        this.locadora = locadora;
    }

    // =====================================================
    // TOTAIS
    // =====================================================

    /** TOTAL: soma de tudo que já foi pago. */
    public double calcularTotalArrecadado() {
        double total = 0;
        for (Pagamento p : locadora.listarPagamentos()) {
            if (p.isPago()) {
                total += p.getValorTotal();
            }
        }
        return total;
    }

    public double calcularTotalPendente() {
        double total = 0;
        for (Pagamento p : locadora.listarPagamentosPendentes()) {
            total += p.getValorTotal();
        }
        return total;
    }

    /** TOTAL: soma de todas as multas aplicadas por atraso. */
    public double calcularTotalMultas() {
        double total = 0;
        for (Pagamento p : locadora.listarPagamentos()) {
            total += p.getMulta();
        }
        return total;
    }

    // =====================================================
    // MÉDIA
    // =====================================================

    /** MÉDIA: valor médio cobrado por aluguel devolvido. */
    public double calcularValorMedioPorAluguel() {
        ArrayList<Pagamento> pagamentos = locadora.listarPagamentos();
        if (pagamentos.isEmpty()) {
            return 0;
        }
        double soma = 0;
        for (Pagamento p : pagamentos) {
            soma += p.getValorTotal();
        }
        return soma / pagamentos.size();
    }

    // =====================================================
    // MAIOR / MENOR
    // =====================================================

    /** MAIOR: filme com mais aluguéis. */
    public Filme encontrarFilmeMaisAlugado() {
        Filme maior = null;
        for (Filme f : locadora.listarFilmes()) {
            if (maior == null || f.getVezesAlugado() > maior.getVezesAlugado()) {
                maior = f;
            }
        }
        return maior;
    }

    /** MENOR: filme ativo com menos aluguéis. */
    public Filme encontrarFilmeMenosAlugado() {
        Filme menor = null;
        for (Filme f : locadora.listarFilmes()) {
            if (!f.isAtivo()) {
                continue;
            }
            if (menor == null || f.getVezesAlugado() < menor.getVezesAlugado()) {
                menor = f;
            }
        }
        return menor;
    }

    public Pagamento encontrarMaiorPagamento() {
        Pagamento maior = null;
        for (Pagamento p : locadora.listarPagamentos()) {
            if (maior == null || p.getValorTotal() > maior.getValorTotal()) {
                maior = p;
            }
        }
        return maior;
    }

    // =====================================================
    // RANKINGS
    // =====================================================

    /** RANKING: clientes que mais alugaram. */
    public ArrayList<Cliente> rankingClientes() {
        ArrayList<Cliente> lista = locadora.listarClientes();
        lista.sort((a, b) -> Integer.compare(b.getTotalAlugueis(), a.getTotalAlugueis()));
        return lista;
    }

    /** RANKING: filmes mais alugados. */
    public ArrayList<Filme> rankingFilmes() {
        ArrayList<Filme> lista = locadora.listarFilmes();
        lista.sort((a, b) -> Integer.compare(b.getVezesAlugado(), a.getVezesAlugado()));
        return lista;
    }

    /** RANKING: funcionários por comissão. */
    public ArrayList<Funcionario> rankingFuncionarios() {
        ArrayList<Funcionario> lista = locadora.listarFuncionarios();
        lista.sort((a, b) -> Double.compare(b.calcularComissao(), a.calcularComissao()));
        return lista;
    }

    // =====================================================
    // INDICADORES DEFINIDOS PELA EQUIPE
    // =====================================================

    /** INDICADOR: % dos aluguéis devolvidos que tiveram atraso. */
    public double calcularTaxaAtraso() {
        int devolvidos = 0;
        int comAtraso = 0;
        for (Aluguel a : locadora.listarAlugueis()) {
            if (a.getStatus() == StatusAluguel.DEVOLVIDO) {
                devolvidos++;
                if (a.devolvidoComAtraso()) {
                    comAtraso++;
                }
            }
        }
        if (devolvidos == 0) {
            return 0;
        }
        return comAtraso * 100.0 / devolvidos;
    }

    /** INDICADOR: % das cópias do acervo que estão alugadas agora. */
    public double calcularTaxaOcupacao() {
        int totalCopias = 0;
        int alugadas = 0;
        for (Filme f : locadora.listarFilmes()) {
            if (f.isAtivo()) {
                totalCopias += f.getQuantidadeCopias();
                alugadas += f.getQuantidadeCopias() - f.getQuantidadeDisponivel();
            }
        }
        if (totalCopias == 0) {
            return 0;
        }
        return alugadas * 100.0 / totalCopias;
    }

    // =====================================================
    // RELATÓRIOS EM TEXTO
    // =====================================================

    public String relatorioFinanceiro() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== RELATÓRIO FINANCEIRO =====\n");
        sb.append("Total arrecadado (pago): ").append(Formatador.moeda(calcularTotalArrecadado())).append("\n");
        sb.append("Total pendente de pagamento: ").append(Formatador.moeda(calcularTotalPendente())).append("\n");
        sb.append("Total de multas aplicadas: ").append(Formatador.moeda(calcularTotalMultas())).append("\n");
        sb.append("Valor médio por aluguel: ").append(Formatador.moeda(calcularValorMedioPorAluguel())).append("\n");
        Pagamento maior = encontrarMaiorPagamento();
        sb.append("Maior pagamento: ").append(maior == null ? "-" : maior.toString()).append("\n");
        return sb.toString();
    }

    public String relatorioFilmes() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== FILMES MAIS E MENOS ALUGADOS =====\n");
        Filme mais = encontrarFilmeMaisAlugado();
        Filme menos = encontrarFilmeMenosAlugado();
        if (mais == null) {
            sb.append("Nenhum filme cadastrado.\n");
            return sb.toString();
        }
        sb.append("Mais alugado: ").append(mais.getTitulo())
                .append(" (").append(mais.getVezesAlugado()).append(" aluguel(éis))\n");
        sb.append("Menos alugado: ").append(menos.getTitulo())
                .append(" (").append(menos.getVezesAlugado()).append(" aluguel(éis))\n");
        return sb.toString();
    }

    public String relatorioRankings() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== RANKING DE CLIENTES (Top 5) =====\n");
        ArrayList<Cliente> clientes = rankingClientes();
        for (int i = 0; i < clientes.size() && i < 5; i++) {
            Cliente c = clientes.get(i);
            sb.append(i + 1).append("º ").append(c.getNome())
                    .append(" - ").append(c.getTotalAlugueis()).append(" aluguel(éis)\n");
        }
        sb.append("\n===== RANKING DE FILMES (Top 5) =====\n");
        ArrayList<Filme> filmes = rankingFilmes();
        for (int i = 0; i < filmes.size() && i < 5; i++) {
            Filme f = filmes.get(i);
            sb.append(i + 1).append("º ").append(f.getTitulo())
                    .append(" - ").append(f.getVezesAlugado()).append(" aluguel(éis)\n");
        }
        return sb.toString();
    }

    public String relatorioIndicadores() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== INDICADORES =====\n");
        sb.append(String.format("Taxa de atraso nas devoluções: %.1f%%\n", calcularTaxaAtraso()));
        sb.append(String.format("Taxa de ocupação do acervo: %.1f%%\n", calcularTaxaOcupacao()));
        sb.append("Aluguéis em aberto: ").append(locadora.listarAlugueisEmAberto().size()).append("\n");
        sb.append("Aluguéis atrasados: ").append(locadora.listarAlugueisAtrasados().size()).append("\n");
        return sb.toString();
    }

    public String relatorioComissoes() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== COMISSÃO DOS FUNCIONÁRIOS (5% do valor pago) =====\n");
        int posicao = 1;
        for (Funcionario f : rankingFuncionarios()) {
            sb.append(posicao++).append("º ").append(f.getNome())
                    .append(" - ").append(f.getTotalAlugueis()).append(" aluguel(éis)")
                    .append(" - Comissão: ").append(Formatador.moeda(f.calcularComissao())).append("\n");
        }
        return sb.toString();
    }

    public String gerarRelatorioCompleto() {
        return relatorioFinanceiro() + "\n"
                + relatorioFilmes() + "\n"
                + relatorioRankings() + "\n"
                + relatorioIndicadores() + "\n"
                + relatorioComissoes();
    }
}
