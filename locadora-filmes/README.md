# 🎬 CineMax Locadora — Sistema de Gerenciamento de Locadora de Filmes

Projeto Livre de Programação Orientada a Objetos em Java: um sistema de terminal para gerenciar o acervo, os clientes, os aluguéis, as devoluções e a cobrança de uma locadora de filmes.

## 👥 Integrantes

| Integrante | Função / Parte responsável |
|---|---|
| [Nome do integrante 1] | Modelagem das classes de domínio (`modelo`) e regras de negócio de `Aluguel` e `Pagamento` |
| [Nome do integrante 2] | Classe `Locadora` (cadastros, buscas, ordenação) e `Relatorios` |
| [Nome do integrante 3] | Menu interativo (`Main`), leitura de dados (`Entrada`) e dados de exemplo |
| [Nome do integrante 4] | Importação/exportação CSV, testes JUnit e README |

> Ajustem os nomes e as funções de acordo com o que cada um realmente fez.

## 📌 Descrição do problema

Pequenas locadoras ainda controlam os aluguéis em caderno ou planilhas. Isso causa problemas como:

- não saber quantas cópias de um filme estão livres;
- esquecer quem está com um filme atrasado;
- errar o cálculo de multas e descontos;
- alugar filme impróprio para a idade do cliente;
- não ter ideia de quais filmes e clientes dão mais retorno.

Este sistema resolve esses problemas controlando o **acervo (filmes e cópias)**, os **clientes**, o ciclo completo **alugar → devolver → cobrar → pagar** e gerando **relatórios e indicadores** para o dono da locadora.

**Possíveis usuários:** atendentes (registram aluguéis e devoluções), gerente/dono da locadora (consulta relatórios, comissões e indicadores) e administradores do acervo (cadastram filmes e cópias).

## ✅ Funcionalidades

| Operação obrigatória | Onde está no sistema |
|---|---|
| Cadastrar | Clientes, Filmes (com cópias), Funcionários |
| Consultar | Cliente por ID, Filme por ID |
| Editar | Dados do cliente (nome/telefone) e do filme (título/gênero/categoria) |
| Listar | Clientes, filmes, filmes disponíveis, aluguéis, pagamentos pendentes |
| Remover/desativar | Desativar cliente e desativar filme (exclusão lógica) |
| Associar objetos | Alugar = cliente + cópia + funcionário; adicionar cópias a um filme |
| Pesquisar por atributo | Cliente por nome; filme por título e por gênero; histórico por cliente |
| Ordenar | Clientes por nome e por pontos; filmes por título e por ano |
| Executar regras de negócio | Alugar, renovar, devolver, pagar (ver regras abaixo) |
| Calcular indicadores | Taxa de atraso, taxa de ocupação do acervo, comissões |
| Gerar relatórios | Menu "Relatórios e indicadores" |

### Processo completo (funcionalidade principal)

```
Cliente + Filme + Funcionário  →  ALUGUEL  →  (renovação)  →  DEVOLUÇÃO  →  PAGAMENTO
                                   ATIVO      ATIVO/ATRASADO    DEVOLVIDO     gera cobrança → paga
```

Envolve as classes `Cliente`, `Filme`/`Copia`, `Funcionario`, `Aluguel` e `Pagamento`.

### Relatórios e indicadores

| Relatório | Tipo exigido |
|---|---|
| Valor médio por aluguel | **Média** |
| Total arrecadado, total pendente e total de multas | **Total** |
| Filme mais e menos alugado; maior pagamento | **Maior / menor valor** |
| Ranking de clientes, de filmes e de funcionários (comissão) | **Ranking** |
| Taxa de atraso nas devoluções e taxa de ocupação do acervo | **Indicador da equipe** |

## 🧱 Principais classes

```
src/main/java/com/locadora
├── app        → Main (menu interativo no terminal)
├── modelo     → Cliente, Filme, Copia, Funcionario, Aluguel, Pagamento
│                + enums Categoria, StatusAluguel, FormaPagamento
├── servico    → Locadora (cadastros e processos), Relatorios (indicadores)
├── util       → Entrada, Formatador, ArquivoCsv, DadosExemplo
└── excecao    → RegraNegocioException
```

Relacionamentos (composição / associação entre objetos):

```
Locadora ──possui──► ArrayList<Cliente>      Cliente ──possui──► ArrayList<Aluguel>
         ──possui──► ArrayList<Filme>        Filme   ──possui──► ArrayList<Copia>
         ──possui──► ArrayList<Funcionario>  Funcionario ──registrou──► ArrayList<Aluguel>
         ──possui──► ArrayList<Aluguel>

Aluguel ──► Cliente, Copia (→ Filme), Funcionario     Pagamento ──► Aluguel (→ Cliente e Filme)
```

- **Cliente**: dados pessoais, pontos de fidelidade e lista de aluguéis; sabe validar se pode alugar.
- **Filme**: título, gênero, ano, classificação indicativa, categoria e lista de cópias.
- **Copia**: exemplar físico de um filme (disponível ou alugado).
- **Funcionario**: atende os clientes e calcula a própria comissão.
- **Aluguel**: liga cliente, cópia e funcionário; calcula valor, atraso e multa; renova, devolve e gera o pagamento.
- **Pagamento**: valor do aluguel, desconto, multa, total e situação (pendente/pago).
- **Locadora**: concentra as listas e os processos (alugar, devolver, pagar, buscar, ordenar).
- **Relatorios**: cálculos de total, média, maior/menor, ranking e indicadores.

## 📏 Regras de negócio

| # | Regra |
|---|---|
| 1 | **Estoque:** só é possível alugar se o filme tiver uma cópia disponível. |
| 2 | **Classificação indicativa:** o cliente precisa ter a idade mínima do filme (0, 10, 12, 14, 16 ou 18). |
| 3 | **Limite:** cada cliente pode ter no máximo **3 aluguéis ativos** ao mesmo tempo. |
| 4 | **Bloqueio:** cliente com filme **atrasado** ou com **pagamento pendente** não pode alugar. |
| 5 | **Valor por categoria:** Lançamento R$ 8,00/dia (2 dias) · Catálogo R$ 5,00/dia (3 dias) · Clássico R$ 3,00/dia (5 dias). Valor = diária × dias contratados. |
| 6 | **Multa:** cada dia de atraso custa **50% da diária** do filme. |
| 7 | **Fidelidade:** devolução no prazo dá **+10 pontos**. Com 50 pontos o cliente tem **10% de desconto**; com 100 pontos, **20%**. |
| 8 | **Status automático:** o aluguel muda de `ATIVO` para `ATRASADO` quando passa da data prevista. |
| 9 | **Renovação:** só **uma vez** por aluguel e **nunca** se ele já estiver atrasado (soma o prazo da categoria). |
| 10 | **Comissão:** o funcionário recebe **5%** do valor pago nos aluguéis que registrou. |
| 11 | **Desativação:** não é possível desativar cliente com filme para devolver ou pagamento pendente, nem filme com cópia alugada. |

Tudo isso está implementado nas classes de domínio (`Cliente.validarPodeAlugar`, `Aluguel.calcularMulta`, `Aluguel.renovar`, `Cliente.getPercentualDesconto`, `Funcionario.calcularComissao`...) e **não** no `Main`.

## 🛠️ Tecnologias utilizadas

- **Java 17** (Programação Orientada a Objetos, `ArrayList`, `LocalDate`)
- **Maven** (gerenciamento de dependências e build)
- **CSV** — importação e exportação de dados *(tecnologia externa 1)*
- **JUnit 5** — testes automatizados das regras de negócio *(tecnologia externa 2)*

## ⚙️ Instruções para configuração

Pré-requisitos: **JDK 17 ou superior** e **Maven 3.8+**.

```bash
java -version
mvn -version
```

Clone o repositório e entre na pasta:

```bash
git clone <url-do-repositorio>
cd locadora-filmes
```

## ▶️ Instruções para execução

Rodar os testes automatizados:

```bash
mvn clean test
```

Executar o sistema (menu no terminal):

```bash
mvn compile exec:java
```

Ou gerar o `.jar` e executar:

```bash
mvn clean package
java -jar target/locadora-filmes-1.0.0.jar
```

> **Windows:** se os acentos aparecerem errados no terminal, rode `chcp 65001` antes de executar.

Ao iniciar, responda `s` em *"Carregar dados de exemplo?"* para já ter filmes, clientes, aluguéis atrasados e pagamentos pendentes para testar.

## 💡 Exemplos de utilização

**1. Regra de bloqueio por pagamento pendente** (João devolveu um filme e ainda não pagou):

```
Menu Aluguéis > 1 - Alugar filme
ID do cliente: 2
ID do filme: 1
ID do funcionário: 1
Data do aluguel (Enter = hoje):
ERRO: Cliente possui pagamento pendente. Quite antes de alugar.
```

**2. Regra de classificação indicativa** (Pedro tem 15 anos e tenta alugar um filme 18+):

```
ID do cliente: 4
ID do filme: 4
ERRO: Cliente não tem idade para este filme (classificação 18 anos).
```

**3. Devolução com atraso gerando multa:**

```
Menu Aluguéis > 3 - Devolver filme
ID do aluguel: 4
Data da devolução (Enter = hoje):
Filme devolvido! Pagamento #4 | Aluguel #4 | Lucas Rocha | Coringa | Aluguel: R$ 15,00 | Desconto: R$ 0,00 | Multa: R$ 5,00 | TOTAL: R$ 20,00 | PENDENTE
Registrar o pagamento agora? (s/n):
```

**4. Relatório de indicadores:**

```
===== INDICADORES =====
Taxa de atraso nas devoluções: 50,0%
Taxa de ocupação do acervo: 22,2%
Aluguéis em aberto: 2
Aluguéis atrasados: 0
```

**5. Importar/exportar CSV:** em `Arquivos CSV` use `dados/filmes.csv` e `dados/clientes.csv` (já incluídos) para importar. As exportações são gravadas na pasta `exportados/`.

Formato dos arquivos de importação (separador `;`, com cabeçalho na primeira linha):

```
titulo;genero;ano;classificacao;categoria;copias
nome;cpf;dataNascimento;telefone
```

Dica para a demonstração: nas datas de aluguel e devolução, informe uma data passada (`dd/MM/aaaa`) para simular atrasos.

## 🤖 Utilização de IA

| Ferramenta | Objetivo | Resumo do uso | Revisão pelo Dev |
|---|---|---|---|
| Claude Sonnet 5.5 | Código Base e Regras de Negócio | Apoiou a definição da arquitetura inicial do código base (pacotes, classes de domínio e relacionamentos), a modelagem da lógica das regras de negócio e a geração dos testes JUnit. | Testes unitários/manuais das regras, validação dos casos de borda (edge cases) e refatoração do código para os padrões do projeto. |
| Claude Sonnet 5.5 | Documentação e README | Auxiliou na estruturação, formatação e redação inicial do arquivo README.md e dos guias de instalação e execução. | Validação dos comandos de instalação/execução, correção de links e ajustes na clareza do texto. |
