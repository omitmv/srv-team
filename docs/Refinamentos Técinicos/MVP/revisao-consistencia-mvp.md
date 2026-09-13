# Revisão de consistência — MVP de pontuação

Referência: [mvp-pontuacao.md](mvp-pontuacao.md).

Status: revisão reconsolidada após o refinamento dos principais agregados do domínio.

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

Sem reabrir, continuam permitidos lançamento, edição pendente, aprovação/reprovação e processamento de resultado tardio referente a campeonato já associado.

Enquanto encerrada, ficam bloqueadas alterações de pontuação, penalidades e associação/desassociação de campeonatos. Alteração estrutural exige `ENCERRADA -> ATIVO` com auditoria.

Gestão de acesso em `TemporadaProfissional` permanece permitida em `ENCERRADA`, pois não altera interpretação esportiva.

### Temporada cancelada

`CANCELADA` preserva dados e suspende a projeção esportiva da temporada. Reativação restaura o estado imediatamente anterior (`ATIVO` ou `ENCERRADA`) e recalcula a projeção.

Como `Inscricao` e `Resultado` pertencem ao campeonato compartilhado e não à temporada, o cancelamento de uma temporada não apaga nem cancela esses fatos.

### Campeonato compartilhado

`Campeonato` é compartilhado N:N com temporadas. `Inscricao` e `Resultado` são únicos no contexto do campeonato e não são duplicados por temporada.

O mesmo resultado pode produzir impactos diferentes em temporadas distintas porque pontuação e penalidade pertencem à temporada.

### Inscrição

`Inscricao` representa um ciclo de participação do atleta no campeonato. Reinscrição cria novo ciclo; resultados do ciclo anterior nunca são automaticamente reativados.

Criador ou `ADMINISTRACAO` de temporada válida associada ao campeonato podem operar a inscrição dos atletas pertencentes àquela temporada sem vínculo profissional-atleta próprio.

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

Resultado tardio aprovado em temporada `ENCERRADA` recalcula o ranking sem reabertura.

Empate no ranking é permitido e utiliza padrão de competição:

```text
1, 1, 3, 4...
```

Não existe critério esportivo adicional de desempate no MVP. Ranking por categoria permanece fora do MVP.

## Contradições históricas resolvidas

| Regra histórica | Regra consolidada vigente |
|---|---|
| Profissional precisava de vínculo próprio com atleta para operar inscrição/resultado | Criador ou `ADMINISTRACAO` pode operar contextualmente atletas da temporada sem vínculo próprio |
| Vínculo atual determinava também todo o histórico | Atual usa `ATIVO`; histórico usa intervalo temporal e `Campeonato.dtInicio` |
| Novo vínculo podia fazer resultados anteriores entrarem retroativamente | Não entra resultado de campeonato ocorrido em intervalo sem vínculo |
| Temporada encerrada poderia congelar ranking | `ENCERRADA` congela estrutura, mas ranking permanece dinâmico e aceita resultado tardio |
| Alteração de tabela poderia depender da regra vigente na data do resultado | Ranking usa configuração atual; alteração recalcula retroativamente |
| Ranking por categoria fazia parte do MVP | Fora do MVP |
| Empate do ranking estava pendente | Permitido com `1,1,3` |
| Empate de colocação do campeonato estava pendente | Proibido; unicidade vale inclusive para resultado pendente |
| Resultado representava apenas colocação | Pode ser `CLASSIFICADO`, `DESCLASSIFICADO` ou `AUSENTE` |
| Ausência/desclassificação poderiam ser inferidas | Sempre lançamento manual em categoria/classe |
| Campeonato pertencia operacionalmente a uma temporada | Campeonato é compartilhado; temporadas interpretam o mesmo resultado independentemente |
| Profissional não editava diretamente campeonato existente | Campeonato sem uso pode ser editado diretamente por profissional autorizado; com uso, alteração cadastral segue fluxo do proprietário |
| Desvincular campeonato com lançamentos dependia do proprietário | `ADMINISTRACAO` pode desvincular da temporada estruturalmente editável sem apagar inscrição/resultado |

## Pontos ainda abertos de negócio

O núcleo de pontuação, resultado, temporada, ranking, vínculo, inscrição, categoria/classe e campeonato está suficientemente consolidado para desenho técnico.

Ainda merecem refinamento específico antes do aceite funcional completo:

1. relatórios: colunas, filtros, agrupamentos, layouts PDF/Excel e apresentação de lacunas de visibilidade;
2. notificações/e-mails: conteúdo final, reenvio, entrega e eventos ainda não explicitamente definidos;
3. contas/perfis: campos obrigatórios e detalhes dos perfis autenticados fora do núcleo atleta/profissional/proprietário;
4. infraestrutura, migração, implantação, observabilidade e operação.

Esses itens não reabrem as decisões esportivas já consolidadas.

## Consistência para implementação

Antes de implementar cada agregado, o Copilot deve consultar o refinamento específico correspondente e usar `mvp-pontuacao.md` para contexto transversal.

A implementação deve preservar especialmente:

- autorização no backend;
- separação entre vínculo real e permissão administrativa contextual;
- separação entre fato esportivo (`Resultado`) e interpretação da temporada;
- histórico e auditoria de transições;
- concorrência/atomicidade das decisões;
- unicidades funcionais de inscrição, resultado e colocação;
- cálculo decimal com `BigDecimal`;
- ranking reconstruível a partir das fontes autoritativas.

## Resultado da revisão

As inconsistências funcionais mais relevantes do brainstorm foram absorvidas pelos refinamentos específicos. O próximo trabalho de domínio deve se concentrar nas áreas ainda não detalhadas, principalmente relatórios e notificações, em vez de reabrir as regras centrais de pontuação já fechadas.
