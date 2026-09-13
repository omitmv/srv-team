# Revisão de consistência — MVP de pontuação

Referência: [mvp-pontuacao.md](mvp-pontuacao.md).

Status: revisão reconsolidada após fechamento funcional dos principais agregados, campeonato e relatórios/exportações.

## Objetivo

Este documento registra a consistência entre a especificação geral do MVP e os refinamentos técnicos específicos. Os refinamentos específicos são a fonte detalhada para implementação; `mvp-pontuacao.md` deve permanecer como visão consolidada do produto.

## Documentos autoritativos por agregado

- [refinamento-vinculo-profissional-atleta.md](refinamento-vinculo-profissional-atleta.md)
- [refinamento-temporada.md](refinamento-temporada.md)
- [refinamento-categoria-classe-overall.md](refinamento-categoria-classe-overall.md)
- [refinamento-inscricao.md](refinamento-inscricao.md)
- [refinamento-resultado.md](refinamento-resultado.md)
- [refinamento-ranking.md](refinamento-ranking.md)
- [refinamento-campeonato-organizador.md](refinamento-campeonato-organizador.md)
- [refinamento-relatorios-exportacoes.md](refinamento-relatorios-exportacoes.md)

Em conflito entre texto histórico do brainstorm e um refinamento específico consolidado, prevalece o refinamento específico mais recente.

## Regras transversais consolidadas

### Vínculo profissional-atleta

`VinculoProfissionalAtleta` possui identidade e ciclo de vida próprios. Para autorização atual que realmente dependa da relação, considera-se vínculo `ATIVO`. Para fatos históricos, usa-se o intervalo `dtInicio`/`dtEncerramento`.

```text
vinculoVigenteNaData(atleta, profissional, dataReferencia)
= dtInicio <= dataReferencia
  E (dtEncerramento IS NULL OU dtEncerramento >= dataReferencia)
```

Novo vínculo não preenche retroativamente um intervalo sem relacionamento.

### Temporada e autorização contextual

A temporada possui somente `ATIVO`, `ENCERRADA` e `CANCELADA` e nasce `ATIVO`.

`TemporadaProfissional` separa permissão administrativa da relação profissional-atleta:

- criador possui administração por definição;
- `ADMINISTRACAO` pode operar, no contexto da temporada, os atletas pertencentes à sua composição sem vínculo próprio com eles;
- `CONSULTA` é leitura e não autoriza operações de inscrição/resultado;
- vínculo profissional-atleta isolado não concede administração de temporada, inscrição ou resultado.

A composição da temporada continua determinada pelos vínculos do criador; convidar administrador não adiciona atletas próprios do convidado.

### Temporada encerrada

`ENCERRADA` congela configuração estrutural, não fatos esportivos históricos.

Sem reabrir, continuam permitidos, para campeonatos já associados e conforme autorização específica:

- solicitação, aprovação/reprovação, cadastro direto e cancelamento permitido de `Inscricao` tardia;
- lançamento e edição pendente de `Resultado` tardio;
- aprovação/reprovação pelo atleta;
- correção/cancelamento administrativo de resultado;
- gestão de `TemporadaProfissional`.

Enquanto encerrada, ficam bloqueadas alterações de pontuação, penalidades e associação/desassociação de campeonatos. Alteração estrutural exige `ENCERRADA -> ATIVO` com auditoria.

### Temporada cancelada

`CANCELADA` preserva dados e suspende a projeção esportiva da temporada. Reativação restaura o estado imediatamente anterior (`ATIVO` ou `ENCERRADA`) e recalcula a projeção.

Como `Inscricao` e `Resultado` pertencem ao campeonato compartilhado e não à temporada, o cancelamento de uma temporada não apaga nem cancela esses fatos. Entretanto, a temporada cancelada não serve como fundamento ordinário para novas operações de inscrição/resultado enquanto permanecer cancelada.

### Campeonato compartilhado

`Campeonato` é compartilhado N:N com temporadas. `Inscricao` e `Resultado` são únicos no contexto do campeonato e não são duplicados por temporada.

Somente `ADMINISTRACAO` altera `TemporadaCampeonato`, e somente enquanto a temporada está `ATIVO`. `ENCERRADA` exige reabertura; `CANCELADA` bloqueia alteração estrutural ordinária.

O mesmo resultado pode produzir impactos diferentes em temporadas distintas porque pontuação e penalidade pertencem à temporada.

### Campeonato: estado e equivalência

Campeonato possui `ATIVO` e `CANCELADO`. Toda transição exige justificativa e gera registro imutável em `CampeonatoStatusHistorico`, append-only, na mesma transação da mudança do estado corrente.

Reativação deve revalidar a identidade semântica imediatamente antes de `CANCELADO -> ATIVO`. Se já existir outro campeonato `ATIVO` equivalente, a reativação é bloqueada. O MVP não faz merge automático nem transfere inscrições/resultados entre campeonatos.

### Inscrição

`Inscricao` representa um ciclo de participação do atleta no campeonato. Reinscrição cria novo ciclo; resultados do ciclo anterior nunca são automaticamente reativados.

Criador ou `ADMINISTRACAO` de temporada `ATIVO` ou `ENCERRADA`, associada ao campeonato e contendo o atleta em sua composição, podem operar a inscrição sem vínculo profissional-atleta próprio.

`ENCERRADA` pode fundamentar inscrição tardia; `CANCELADA` não fundamenta operação ordinária enquanto cancelada.

### Resultado

`Resultado` possui granularidade:

```text
Inscricao + Categoria + Classe
```

Situações mínimas:

- `CLASSIFICADO` — colocação inteira positiva;
- `DESCLASSIFICADO` — colocação nula;
- `AUSENTE` — colocação nula.

Todas são lançadas manualmente; o sistema não infere ausência ou desclassificação.

Todo lançamento nasce `PENDENTE_APROVACAO`. Somente `APROVADO` produz efeito esportivo.

Para `CLASSIFICADO`, não existe empate de colocação no mesmo campeonato/categoria/classe. A unicidade vale desde `PENDENTE_APROVACAO`; pendência reserva a colocação. `CANCELADO` libera a reserva.

### Pontuação e penalidade

Pontuação/penalidade não é armazenada no `Resultado` como verdade autoritativa. Cada temporada interpreta o resultado usando sua configuração atual.

Alteração de `TemporadaPontuacao` ou `TemporadaPenalidade` em temporada `ATIVO` recalcula retroativamente todos os resultados aprovados, válidos e elegíveis afetados. O MVP não versiona regra de pontuação por resultado.

### Ranking

Ranking é projeção dinâmica da temporada.

Para composição atual do ranking, o atleta precisa possuir vínculo atual ativo com o criador, inscrição confirmada/não cancelada em campeonato associado e conta não cancelada.

Para um resultado histórico contribuir, o vínculo com o criador deve ter sido vigente em `Campeonato.dtInicio`.

`ATIVO` e `ENCERRADA` produzem projeção. `CANCELADA` suspende.

Inscrição tardia pode alterar elegibilidade e resultado tardio aprovado em temporada `ENCERRADA` recalcula o ranking sem reabertura.

Empate no ranking é permitido e utiliza padrão de competição `1, 1, 3, 4...`. Não existe critério esportivo adicional de desempate no MVP. Ranking por categoria permanece fora do MVP.

### Relatórios e exportações

Existem dois escopos funcionais: relatório de campeonato e relatório de temporada.

Relatório de campeonato representa fatos esportivos e não atribui pontuação universal. Relatório de temporada representa a interpretação da temporada e usa as mesmas regras do ranking.

Profissional não possui consulta esportiva global irrestrita sobre o catálogo compartilhado de campeonatos. A consulta ocorre por contexto de temporada associada:

- `ADMINISTRACAO`: visão integral necessária à operação da temporada;
- `CONSULTA`: somente dados individualizados dos atletas da temporada com vínculo ativo com o observador;
- proprietário: acesso administrativo global.

Tela, PDF e Excel devem ser semanticamente equivalentes e respeitar a mesma autorização. Exportação é fotografia do momento da geração e não fonte autoritativa.

## Contradições históricas resolvidas

| Regra histórica | Regra consolidada vigente |
|---|---|
| Profissional precisava de vínculo próprio com atleta para operar inscrição/resultado | Criador ou `ADMINISTRACAO` pode operar contextualmente atletas da temporada sem vínculo próprio |
| Vínculo atual determinava também todo o histórico | Atual usa `ATIVO`; histórico usa intervalo temporal e `Campeonato.dtInicio` |
| Novo vínculo podia fazer resultados anteriores entrarem retroativamente | Não entra resultado de campeonato ocorrido em intervalo sem vínculo |
| Temporada encerrada poderia congelar ranking | `ENCERRADA` congela estrutura, mas ranking permanece dinâmico e aceita inscrição/resultado tardios |
| Alteração de tabela poderia depender da regra vigente na data do resultado | Ranking usa configuração atual; alteração recalcula retroativamente |
| Ranking por categoria fazia parte do MVP | Fora do MVP |
| Empate do ranking estava pendente | Permitido com `1,1,3` |
| Empate de colocação do campeonato estava pendente | Proibido; unicidade vale inclusive para resultado pendente |
| Resultado representava apenas colocação | Pode ser `CLASSIFICADO`, `DESCLASSIFICADO` ou `AUSENTE` |
| Ausência/desclassificação poderiam ser inferidas | Sempre lançamento manual em categoria/classe |
| Campeonato pertencia operacionalmente a uma temporada | Campeonato é compartilhado; temporadas interpretam o mesmo resultado independentemente |
| Profissional não editava diretamente campeonato existente | Campeonato sem uso pode ser editado diretamente por profissional autorizado; com uso, alteração cadastral segue fluxo do proprietário |
| Desvincular campeonato com lançamentos dependia do proprietário | `ADMINISTRACAO` pode desvincular de temporada `ATIVO` sem apagar inscrição/resultado |
| Temporada encerrada não poderia fundamentar nova inscrição tardia | `ATIVO` e `ENCERRADA` podem fundamentar operações tardias de inscrição; `CANCELADA` não |
| Histórico de status de campeonato podia depender apenas dos campos correntes | Toda transição efetiva possui `CampeonatoStatusHistorico` imutável e atômico com a mudança de estado |
| Reativação de campeonato cancelado poderia recriar colisão semântica | Reativação revalida equivalência e é bloqueada se houver outro `ATIVO` equivalente |
| Catálogo compartilhado de campeonatos poderia sugerir consulta global de resultados | Consulta profissional de resultados é sempre contextual à temporada; proprietário permanece global |

## Pontos ainda abertos de negócio/produto

O núcleo de pontuação, resultado, temporada, ranking, vínculo, inscrição, categoria/classe, campeonato e semântica funcional de relatórios/exportações está suficientemente consolidado para desenho técnico.

Ainda merecem refinamento específico:

1. notificações/e-mails: conteúdo final, reenvio, entrega e eventos ainda não explicitamente definidos;
2. contas/perfis: campos obrigatórios e detalhes dos perfis autenticados fora do núcleo atleta/profissional/proprietário;
3. detalhes de apresentação dos relatórios: colunas finais, filtros expostos, agrupamentos, layout PDF/Excel, paginação e grande volume;
4. infraestrutura, migração, implantação, observabilidade e operação.

Esses itens não reabrem as decisões esportivas já consolidadas.

## Consistência para implementação

Antes de implementar cada agregado, o Copilot deve consultar o refinamento específico correspondente e usar `mvp-pontuacao.md` para contexto transversal.

A implementação deve preservar especialmente:

- autorização no backend;
- separação entre vínculo real e permissão administrativa contextual;
- separação entre fato esportivo (`Inscricao`/`Resultado`) e interpretação da temporada;
- histórico e auditoria de transições;
- histórico imutável de status de campeonato;
- concorrência/atomicidade das decisões;
- unicidades funcionais de inscrição, resultado e colocação;
- proteção contra campeonatos `ATIVO` semanticamente equivalentes;
- cálculo decimal com `BigDecimal`;
- ranking reconstruível a partir das fontes autoritativas;
- equivalência semântica entre tela e exportações.

## Resultado da revisão

A varredura cruzada dos refinamentos não encontrou contradição estrutural remanescente entre vínculo, temporada, campeonato, inscrição, resultado, ranking e relatórios após as harmonizações acima.

Os desalinhamentos encontrados eram de propagação documental: inscrição tardia em `ENCERRADA`, ciclo estrutural de `TemporadaCampeonato`, revalidação de equivalência na reativação, histórico imutável de status de campeonato e autorização contextual de relatórios. Esses pontos foram consolidados nos documentos autoritativos e na especificação geral.

O domínio está apto a avançar para refinamento técnico de implementação, ressalvadas decisões de produto ainda explicitamente abertas.
