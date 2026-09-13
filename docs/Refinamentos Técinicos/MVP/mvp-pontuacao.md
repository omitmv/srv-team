# Especificação do MVP — Pontuação de atletas

Versão: 0.4 — consolidação pós-refinamento  
Status: núcleo funcional esportivo, campeonato, relatórios e exportações consolidados; notificações, contas periféricas e implantação ainda em refinamento.

Este documento apresenta a visão funcional consolidada do MVP. Os documentos `refinamento-*.md` contêm as regras detalhadas de cada agregado e prevalecem em caso de detalhe técnico não reproduzido aqui.

O histórico de harmonização está em [revisao-consistencia-mvp.md](revisao-consistencia-mvp.md).

## 1. Objetivo e escopo

Controlar participação e resultados de atletas em campeonatos compartilhados e interpretar esses fatos em temporadas independentes, com pontuação, penalidades, ranking geral, aprovação de resultados e relatórios.

### Dentro do MVP

- contas e perfis;
- vínculo profissional-atleta;
- equipes técnicas existentes no domínio;
- organizadores, campeonatos, categorias e classes;
- temporadas e permissões de administração/consulta;
- inscrições em campeonatos;
- resultados por categoria/classe;
- pontuação e penalidades específicas por temporada;
- ranking geral de temporada;
- pendências, auditoria e notificações já definidas;
- relatórios em tela, PDF e Excel.

### Fora do MVP

- ranking por categoria;
- agenda/Google Calendar e autoagendamento;
- funcionalidades de treino/nutrição/atendimento;
- regras esportivas adicionais de desempate;
- dependências externas em tempo de execução para catálogo geográfico;
- ranking histórico congelado por data;
- comparação entre temporadas e dashboards analíticos avançados.

## 2. Precedência transversal

1. Conta de atleta cancelada fica fora dos rankings e oculta/suspensa para usuários comuns; o proprietário mantém acesso administrativo.
2. Conta de profissional inativa perde acesso, mas preserva vínculos e pendências. Conta cancelada sai de seletores/notificações e suas demandas podem ser tratadas pelo proprietário.
3. Conta, vínculo real e permissão contextual são conceitos independentes.
4. Resultado só produz efeito esportivo quando `APROVADO`.
5. Temporada interpreta resultados; não altera o fato esportivo do campeonato.
6. Posições do ranking são calculadas sobre o universo elegível antes de filtros de visualização.
7. `Inscricao` e `Resultado` pertencem ao campeonato compartilhado; temporada apenas fornece contexto de autorização/elegibilidade e interpretação esportiva.

## 3. Conceitos do domínio

| Conceito | Regra consolidada |
|---|---|
| Proprietário | Autoridade administrativa global. |
| Profissional | Papel de negócio elegível para atendimento: `NUTRITIONISTA`, `TREINADOR` ou `COACH`. |
| Vínculo | Relação temporal real entre atleta e profissional. |
| Organizador | Catálogo central obrigatório para campeonato. |
| Campeonato | Evento compartilhado entre profissionais e temporadas. |
| Categoria | Catálogo central usado no resultado. |
| Classe | Pertence a uma categoria; tipo `COMUM` ou `OVERALL`. |
| Temporada | Contexto independente de campeonatos, regras, permissões e ranking. |
| Inscrição | Ciclo de participação de um atleta em um campeonato. |
| Resultado | Fato esportivo de uma inscrição em categoria/classe. |
| Pontuação/Penalidade | Interpretação específica da temporada sobre resultado aprovado. |
| Ranking | Projeção dinâmica derivada da temporada. |

## 4. Vínculo profissional-atleta

`VinculoProfissionalAtleta` possui identidade própria e estados `PENDENTE`, `ATIVO`, `REPROVADO` e `ENCERRADO`.

Há no máximo uma pendência e um vínculo ativo por par atleta/profissional. Reprovação ou encerramento não impede nova tentativa futura; nova tentativa cria novo registro.

Atleta e profissional podem encerrar unilateralmente vínculo ativo, com justificativa obrigatória e auditoria. Encerramento não cancela conta, inscrição ou resultado.

Para fato histórico:

```text
vinculoVigenteNaData(atleta, profissional, dataReferencia)
= dtInicio <= dataReferencia
  E (dtEncerramento IS NULL OU dtEncerramento >= dataReferencia)
```

Novo vínculo não preenche retroativamente intervalo sem relacionamento.

## 5. Temporada

Estados exclusivos: `ATIVO`, `ENCERRADA`, `CANCELADA`. A temporada nasce `ATIVO`. `cdCriador` é permanente no MVP; não existe transferência de titularidade.

Um profissional pode criar/administrar múltiplas temporadas `ATIVO` simultâneas, inclusive com datas sobrepostas.

### Permissões

O criador possui `ADMINISTRACAO` por definição. Outros profissionais são associados via `TemporadaProfissional` como `ADMINISTRACAO` ou `CONSULTA`.

`ADMINISTRACAO` permite operar, estritamente no contexto da temporada, os atletas pertencentes à sua composição sem exigir vínculo profissional-atleta próprio. `CONSULTA` não autoriza alterações, inscrições ou resultados e limita consulta individual aos atletas da temporada com os quais o consultor possua vínculo ativo.

Gestão de `TemporadaProfissional` permanece permitida em temporada `ENCERRADA`.

### Composição de atletas

A composição é determinada exclusivamente pelos vínculos do criador. Não existe `TemporadaAtleta` no MVP.

Para presença atual no ranking, exige-se vínculo atual ativo com o criador. Para contribuição de resultado histórico, verifica-se vínculo do atleta com o criador em `Campeonato.dtInicio`.

### ENCERRADA

`ENCERRADA` congela configuração estrutural, não fatos esportivos históricos.

Continua permitindo, para campeonatos já associados e conforme autorização de cada agregado:

- consulta e ranking dinâmico;
- solicitação, aprovação/reprovação, cadastro direto e cancelamento permitido de inscrição tardia;
- lançamento/edição de resultado pendente e aprovação/reprovação de resultado tardio;
- correção/cancelamento administrativo conforme regras de `Resultado`;
- gestão de acessos da temporada.

Bloqueia, até reabertura para `ATIVO`, alteração de pontuação, penalidades, associação/desassociação de campeonatos e demais mudanças estruturais esportivas.

### CANCELADA

Cancelamento é lógico. Preserva dados e suspende o efeito esportivo da temporada. Reativação restaura o estado imediatamente anterior (`ATIVO` ou `ENCERRADA`) e recalcula a projeção.

Enquanto cancelada, não serve como fundamento ordinário para operações de inscrição ou resultado.

## 6. Campeonato e organizador

Organizador é catálogo central administrado pelo proprietário. Campeonato exige organizador ativo.

Campeonato é compartilhado e possui relação N:N com temporada via `TemporadaCampeonato`.

Campos essenciais incluem nome, organizador, país, subdivisão quando aplicável, local descritivo opcional, `dtInicio`, `dtFim`, status e auditoria. Datas são obrigatórias e `dtFim >= dtInicio`; igualdade representa campeonato de um dia.

País/subdivisão usam catálogo interno. Identidade semântica considera:

```text
(nomeNormalizado, cdOrganizador, cdPais, cdSubdivisao, dtInicio)
```

Profissional pode criar campeonato. Em campeonato existente, autorização profissional deriva de `ADMINISTRACAO` em pelo menos uma temporada associada.

Somente `ADMINISTRACAO` vincula/desvincula campeonato e somente em temporada `ATIVO`. `ENCERRADA` precisa ser reaberta para alteração estrutural e `CANCELADA` bloqueia alteração ordinária.

Campeonato sem uso pode ser editado diretamente por profissional autorizado. Alteração cadastral de campeonato com uso segue solicitação/decisão do proprietário.

Status: `ATIVO` ou `CANCELADO`. Toda transição exige justificativa e histórico imutável. Existindo qualquer `Resultado` histórico, profissional solicita mudança de status ao proprietário; sem resultado histórico, profissional autorizado pode alterar diretamente.

Cada transição efetiva gera `CampeonatoStatusHistorico` append-only na mesma transação da mudança de `Campeonato.status`.

Reativação sempre revalida equivalência. Se existir outro campeonato `ATIVO` semanticamente equivalente, `CANCELADO -> ATIVO` é bloqueado. Não há merge ou transferência automática de inscrições/resultados entre identidades.

## 7. Categoria, classe e Overall

Categoria e classe são catálogos centrais do proprietário. Classe pertence exatamente a uma categoria e possui tipo `COMUM` ou `OVERALL`.

Cada categoria possui no máximo um `OVERALL` ativo. Itens utilizados preservam identidade histórica; alterações estruturais incompatíveis devem criar novo item e inativar o anterior.

Categoria/classe são selecionadas no `Resultado`, não previamente no campeonato.

## 8. Inscrição

`Inscricao` representa um ciclo de participação do atleta em um campeonato.

Estados: `PENDENTE`, `CONFIRMADA`, `REPROVADA`, `CANCELADA`.

No máximo uma pendência e uma inscrição confirmada por atleta/campeonato. Reinscrição após reprovação/cancelamento cria novo ciclo.

Criador ou `ADMINISTRACAO` de temporada `ATIVO` ou `ENCERRADA`, associada ao campeonato e cuja composição contenha o atleta, podem operar inscrição sem vínculo profissional-atleta próprio.

Temporada `ENCERRADA` pode fundamentar solicitação, aprovação/reprovação, cadastro direto, conclusão de pendência e cancelamento permitido de inscrição tardia sem reabertura. `CANCELADA` não fundamenta operação ordinária enquanto permanecer cancelada.

Aprovação/reprovação revalida autorização no momento da decisão. Uma decisão resolve a pendência compartilhada.

Cancelamento direto por profissional autorizado só é permitido quando nunca existiu resultado naquela inscrição. Se qualquer `Resultado` histórico já existiu, inclusive cancelado, intervenção/aprovação do proprietário é necessária.

Cancelar inscrição não apaga resultados. Reinscrição cria nova `Inscricao`; resultados do ciclo anterior nunca são automaticamente reativados.

## 9. Resultado

`Resultado` referencia `cdInscricao` e possui granularidade:

```text
Inscricao + Categoria + Classe
```

Situações: `CLASSIFICADO`, `DESCLASSIFICADO`, `AUSENTE`.

`CLASSIFICADO` exige `colocacao` inteira positiva, sem limite máximo funcional. `DESCLASSIFICADO` e `AUSENTE` exigem `colocacao = null`.

Toda situação é lançada manualmente. Ausência de lançamento não significa `AUSENTE`; ausência de colocação não significa `DESCLASSIFICADO`.

Estados de workflow: `PENDENTE_APROVACAO`, `APROVADO`, `CANCELADO`.

Todo novo resultado nasce pendente e depende de aprovação do atleta. Reprovação transforma o lançamento em `CANCELADO`.

Criador ou `ADMINISTRACAO` de temporada válida `ATIVO` ou `ENCERRADA` associada ao campeonato podem lançar/editar resultado dos atletas pertencentes à temporada sem vínculo próprio. `CONSULTA`, vínculo isolado e temporada `CANCELADA` não autorizam ordinariamente.

Após `APROVADO`, profissional não edita pelo fluxo ordinário. Proprietário pode corrigir/cancelar diretamente mediante justificativa e auditoria.

### Unicidade

Para `(Inscricao, Categoria, Classe)`, no máximo um resultado `PENDENTE_APROVACAO` ou `APROVADO`.

Para `CLASSIFICADO`, colocação é única em `(Campeonato, Categoria, Classe, Colocacao)` para `PENDENTE_APROVACAO` e `APROVADO`. Resultado pendente reserva a colocação; reprovação/cancelamento libera. Proteção deve ser transacional e resistente a concorrência.

Não existe empate de colocação no campeonato.

## 10. Pontuação e penalidades

`TemporadaPontuacao` pertence à temporada e diferencia `COMUM`/`OVERALL` por colocação. Não é obrigatório configurar todas as posições; colocação válida sem regra produz impacto zero.

`TemporadaPenalidade` define `DESCLASSIFICACAO` e `AUSENCIA`. Regra ausente produz impacto zero. Cada resultado aprovado contribui independentemente; não existe agregação de penalidades.

Valores usam `BigDecimal(10,3)` e ranking pode ficar negativo.

A configuração atual é autoritativa:

```text
CLASSIFICADO -> colocacao + tipoClasse -> TemporadaPontuacao atual
DESCLASSIFICADO -> TemporadaPenalidade.DESCLASSIFICACAO atual
AUSENTE -> TemporadaPenalidade.AUSENCIA atual
```

Enquanto `ATIVO`, alteração de pontuação ou penalidade recalcula retroativamente todos os resultados aprovados, válidos e elegíveis afetados. O MVP não versiona regra de pontuação/penalidade por resultado.

## 11. Ranking geral

Ranking é projeção dinâmica e reconstruível; não é fonte autoritativa.

Atleta integra atualmente o ranking quando:

1. temporada está `ATIVO` ou `ENCERRADA`;
2. possui vínculo atual ativo com o criador;
3. possui ao menos uma inscrição `CONFIRMADA` e não cancelada em campeonato associado;
4. conta não está cancelada.

Não precisa possuir resultado aprovado para aparecer; pode iniciar com zero.

Para cada resultado contribuir:

```text
Temporada não CANCELADA
E Campeonato associado
E Campeonato esportivamente válido
E Resultado APROVADO
E Inscricao CONFIRMADA e não CANCELADA
E conta do atleta não cancelada
E vinculoVigenteNaData(atleta, Temporada.cdCriador, Campeonato.dtInicio)
```

A data de criação/confirmação da inscrição e a data de lançamento/aprovação do resultado podem ser posteriores ao encerramento.

```text
TOTAL = soma(impacto atual de cada Resultado APROVADO válido e elegível)
```

Inscrição tardia que altera elegibilidade e resultado tardio aprovado em `ENCERRADA` recalculam automaticamente a projeção. `CANCELADA` suspende a projeção; reativação recalcula.

Empate é permitido no ranking e usa ranking de competição `1, 1, 3, 4...`. Não há desempate esportivo adicional no MVP. Filtros de visibilidade são aplicados depois do cálculo e não renumeram posições.

Ranking por categoria está fora do MVP.

## 12. Fatos tardios e campeonatos compartilhados

`Inscricao` e `Resultado` pertencem ao campeonato, não à temporada.

Se o mesmo campeonato estiver associado a várias temporadas, existe apenas uma inscrição/ciclo e um conjunto compartilhado de resultados por ciclo. Cada temporada decide independentemente se o fato é elegível e qual impacto produz.

Temporadas `ATIVO` e `ENCERRADA` podem servir de fundamento para operações tardias conforme autorização. `CANCELADA` preserva os fatos, mas não serve de fundamento ordinário e não produz efeito esportivo enquanto cancelada.

## 13. Contas e precedência administrativa

Todo atleta precisa de conta. Atleta ou proprietário podem cancelar conta de atleta. Enquanto cancelada, ela fica fora dos rankings e oculta/suspensa para usuários comuns; vínculos, inscrições, resultados e pendências são preservados. Proprietário mantém acesso administrativo.

Somente proprietário cadastra, inativa, cancela e reativa contas de profissionais. Inatividade e cancelamento são estados distintos. Permissões administrativas devem ser verificadas no backend.

## 14. Relatórios e exportações

Existem dois escopos principais:

- `RelatorioCampeonato`: fato esportivo de inscrições/resultados, sem pontuação universal;
- `RelatorioTemporada`: interpretação derivada, com ranking e contribuições calculadas pelas regras atuais da temporada.

Relatório esportivo de campeonato exibe somente resultados `APROVADO` e não cancelados; inscrição confirmada sem resultado pode aparecer em visão analítica como `Sem resultado`, nunca como `AUSENTE` inferido.

Relatório de temporada usa o mesmo universo e regras de `refinamento-ranking.md`. Ranking e posições são calculados antes dos filtros de visibilidade.

Não existe consulta esportiva global irrestrita de campeonato para profissionais. O profissional acessa os resultados do campeonato por contexto de temporada associada:

- `ADMINISTRACAO`: visão necessária à operação integral daquela temporada;
- `CONSULTA`: somente dados individualizados dos atletas da temporada com os quais possua vínculo ativo;
- proprietário: acesso administrativo global.

Ser criador do campeonato, pertencer ao mesmo time ou possuir vínculo isolado com atleta não concede consulta global dos resultados.

Tela, PDF e Excel aplicam as mesmas regras de autorização, elegibilidade e visibilidade. Exportação é fotografia do momento da geração, deve registrar data/hora e, se armazenada, exige revalidação de autorização no download.

PDF/Excel não são fontes autoritativas. A geração deve representar leitura logicamente consistente e usar `BigDecimal` com a mesma semântica da tela.

Ranking/relatório por categoria permanece fora do MVP.

## 15. Auditoria e concorrência

Preservar histórico das decisões e transições relevantes, incluindo vínculos, inscrições, versões/decisões de resultado, correções/cancelamentos administrativos, configurações de temporada, acessos e associações.

Transições `ATIVO <-> CANCELADO` de campeonato possuem histórico imutável `CampeonatoStatusHistorico`, append-only e atômico com a alteração do estado corrente.

Decisões concorrentes devem ser atômicas. Usar proteção de persistência além de validação em memória para invariantes que envolvam registros diferentes, especialmente unicidade de colocação e equivalência de campeonato.

Conflitos de versão/unicidade podem ser representados por `409 Conflict`.

## 16. Fontes detalhadas para implementação

- [Vínculo profissional-atleta](refinamento-vinculo-profissional-atleta.md)
- [Temporada](refinamento-temporada.md)
- [Categoria, classe e Overall](refinamento-categoria-classe-overall.md)
- [Inscrição](refinamento-inscricao.md)
- [Resultado](refinamento-resultado.md)
- [Ranking](refinamento-ranking.md)
- [Campeonato e organizador](refinamento-campeonato-organizador.md)
- [Relatórios e exportações](refinamento-relatorios-exportacoes.md)
- [Revisão de consistência](revisao-consistencia-mvp.md)

## 17. Próximos refinamentos

O núcleo esportivo, campeonato e regras funcionais de relatórios/exportações estão consolidados. As próximas frentes são:

1. notificações/e-mails e pendências;
2. detalhes restantes de contas/perfis fora do núcleo esportivo;
3. desenho técnico de implementação: modelo físico, entidades, migrations, casos de uso/services, contratos de API e autorização;
4. implantação, observabilidade e automação para o Copilot;
5. detalhes de apresentação de relatórios/exportações (colunas finais, layout, agrupamentos, paginação e estratégia de grande volume), sem alterar a semântica já consolidada.

Essas frentes devem respeitar as invariantes consolidadas neste documento e nos refinamentos específicos, sem reabrir decisões esportivas já fechadas salvo nova decisão explícita do proprietário do produto.
