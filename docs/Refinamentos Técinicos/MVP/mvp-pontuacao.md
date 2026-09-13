# Especificação do MVP — Pontuação de atletas

Versão: 0.3 — consolidação pós-refinamento  
Status: núcleo funcional de pontuação consolidado; relatórios, notificações e implantação ainda em refinamento.

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
- dependências externas em tempo de execução para catálogo geográfico.

## 2. Precedência transversal

1. Conta de atleta cancelada fica fora dos rankings e oculta/suspensa para usuários comuns; o proprietário mantém acesso administrativo.
2. Conta de profissional inativa perde acesso, mas preserva vínculos e pendências. Conta cancelada sai de seletores/notificações e suas demandas podem ser tratadas pelo proprietário.
3. Conta, vínculo real e permissão contextual são conceitos independentes.
4. Resultado só produz efeito esportivo quando `APROVADO`.
5. Temporada interpreta resultados; não altera o fato esportivo do campeonato.
6. Posições do ranking são calculadas sobre o universo elegível antes de filtros de visualização.

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

`VinculoProfissionalAtleta` possui identidade própria e estados:

- `PENDENTE`;
- `ATIVO`;
- `REPROVADO`;
- `ENCERRADO`.

Cadastro direto de novo atleta pelo profissional e liberação de pré-cadastro pelo profissional selecionado podem criar vínculo diretamente `ATIVO`. Solicitações envolvendo atleta já cadastrado dependem de aprovação do destinatário.

Há no máximo uma pendência e um vínculo ativo por par atleta/profissional. Reprovação ou encerramento não impede nova tentativa futura; nova tentativa cria novo registro.

Atleta e profissional podem encerrar unilateralmente vínculo ativo, com justificativa obrigatória e auditoria. Encerramento não cancela conta, inscrição ou resultado.

### Vigência temporal

Para operação atual que dependa da relação, considera-se vínculo `ATIVO`.

Para fato histórico:

```text
vinculoVigenteNaData(atleta, profissional, dataReferencia)
= dtInicio <= dataReferencia
  E (dtEncerramento IS NULL OU dtEncerramento >= dataReferencia)
```

Novo vínculo não preenche retroativamente intervalo sem relacionamento.

## 5. Temporada

Estados exclusivos:

- `ATIVO`;
- `ENCERRADA`;
- `CANCELADA`.

A temporada nasce `ATIVO`. `cdCriador` é permanente no MVP; não existe transferência de titularidade.

Um profissional pode criar/administrar múltiplas temporadas `ATIVO` simultâneas, inclusive com datas sobrepostas.

### Permissões

O criador possui `ADMINISTRACAO` por definição. Outros profissionais são associados via `TemporadaProfissional` com:

- `ADMINISTRACAO`;
- `CONSULTA`.

`ADMINISTRACAO` permite operar, estritamente no contexto da temporada, os atletas pertencentes à sua composição sem exigir vínculo profissional-atleta próprio.

`CONSULTA` não autoriza alterações, inscrições ou resultados e limita consulta individual aos atletas da temporada com os quais o consultor possua vínculo ativo.

Vínculo profissional-atleta isolado não concede administração.

Gestão de `TemporadaProfissional` permanece permitida em temporada `ENCERRADA`.

### Composição de atletas

A composição é determinada exclusivamente pelos vínculos do criador. Não existe `TemporadaAtleta` no MVP.

Para presença atual no ranking, exige-se vínculo atual ativo com o criador. Para contribuição de resultado histórico, verifica-se vínculo do atleta com o criador em `Campeonato.dtInicio`.

### ENCERRADA

`ENCERRADA` congela configuração estrutural, não fatos esportivos históricos.

Continua permitindo:

- consulta e ranking dinâmico;
- lançamento de resultado tardio de campeonato já associado;
- edição de resultado pendente;
- aprovação/reprovação pelo atleta;
- correção/cancelamento administrativo conforme regras de `Resultado`;
- gestão de acessos da temporada.

Bloqueia, até reabertura para `ATIVO`:

- alteração de pontuação;
- alteração de penalidades;
- associação/desassociação de campeonatos;
- demais alterações estruturais que modifiquem interpretação esportiva.

Reabertura exige justificativa, responsável, data/hora e histórico auditável.

### CANCELADA

Cancelamento é lógico. Preserva dados e suspende o efeito esportivo da temporada. Reativação restaura o estado imediatamente anterior (`ATIVO` ou `ENCERRADA`) e recalcula a projeção.

## 6. Campeonato e organizador

Organizador é catálogo central administrado pelo proprietário. Campeonato exige organizador ativo.

Campeonato é compartilhado e possui relação N:N com temporada via `TemporadaCampeonato`.

Campos essenciais incluem nome, organizador, país, subdivisão quando aplicável, local descritivo opcional, `dtInicio`, `dtFim`, status e auditoria. Datas são obrigatórias e `dtFim >= dtInicio`; igualdade representa campeonato de um dia.

País/subdivisão usam catálogo interno. Identidade semântica considera nome normalizado, organizador, país, subdivisão e data inicial.

Profissional pode criar campeonato. Em campeonato existente, autorização profissional deriva de `ADMINISTRACAO` em pelo menos uma temporada associada.

Campeonato sem uso pode ser editado diretamente por profissional autorizado. Alteração cadastral de campeonato com uso segue solicitação/decisão do proprietário.

Somente `ADMINISTRACAO` vincula/desvincula campeonato de temporada estruturalmente editável. Desvincular não cancela campeonato, inscrição ou resultado.

Status: `ATIVO` ou `CANCELADO`. Alteração exige justificativa/auditoria. Existindo qualquer `Resultado` histórico, profissional solicita mudança de status ao proprietário; sem resultado histórico, profissional autorizado pode alterar diretamente.

## 7. Categoria, classe e Overall

Categoria e classe são catálogos centrais do proprietário.

Classe pertence exatamente a uma categoria e possui tipo:

- `COMUM`;
- `OVERALL`.

Cada categoria possui no máximo um `OVERALL` ativo. Itens utilizados preservam identidade histórica; alterações estruturais incompatíveis devem criar novo item e inativar o anterior.

Categoria/classe são selecionadas no `Resultado`, não previamente no campeonato.

## 8. Inscrição

`Inscricao` representa um ciclo de participação do atleta em um campeonato.

Estados:

- `PENDENTE`;
- `CONFIRMADA`;
- `REPROVADA`;
- `CANCELADA`.

No máximo uma pendência e uma inscrição confirmada por atleta/campeonato. Reinscrição após reprovação/cancelamento cria novo ciclo.

Criador ou `ADMINISTRACAO` de temporada associada ao campeonato podem operar inscrição de atleta pertencente àquela temporada sem vínculo profissional-atleta próprio.

Aprovação/reprovação revalida autorização no momento da decisão. Uma decisão resolve a pendência compartilhada.

Reprovação exige justificativa e preserva histórico.

Cancelamento direto por profissional autorizado só é permitido quando nunca existiu resultado naquela inscrição. Se qualquer `Resultado` histórico já existiu, inclusive cancelado, intervenção/aprovação do proprietário é necessária.

Cancelar inscrição não apaga resultados. Reinscrição cria nova `Inscricao`; resultados do ciclo anterior nunca são automaticamente reativados.

## 9. Resultado

`Resultado` referencia `cdInscricao` e possui granularidade:

```text
Inscricao + Categoria + Classe
```

Situações:

- `CLASSIFICADO`;
- `DESCLASSIFICADO`;
- `AUSENTE`.

`CLASSIFICADO` exige `colocacao` inteira positiva, sem limite máximo funcional. `DESCLASSIFICADO` e `AUSENTE` exigem `colocacao = null`.

Toda situação é lançada manualmente. Ausência de lançamento não significa `AUSENTE`; ausência de colocação não significa `DESCLASSIFICADO`.

Estados de workflow:

- `PENDENTE_APROVACAO`;
- `APROVADO`;
- `CANCELADO`.

Todo novo resultado nasce pendente e depende de aprovação do atleta. Reprovação transforma o lançamento em `CANCELADO`.

Criador ou `ADMINISTRACAO` de temporada válida associada ao campeonato podem lançar/editar resultado dos atletas pertencentes à temporada sem vínculo próprio. `CONSULTA` e vínculo isolado não autorizam.

Resultado pendente pode ser editado, mantendo `cdResultado`, incrementando `nrVersao` e exigindo nova aprovação sobre a versão atual. Usar também controle técnico de concorrência otimista (`lockVersion`).

Após `APROVADO`, profissional não edita pelo fluxo ordinário. Proprietário pode corrigir/cancelar diretamente mediante justificativa e auditoria.

### Unicidade

Para `(Inscricao, Categoria, Classe)`, no máximo um resultado `PENDENTE_APROVACAO` ou `APROVADO`.

Para `CLASSIFICADO`, colocação é única em:

```text
(Campeonato, Categoria, Classe, Colocacao)
```

A unicidade vale para `PENDENTE_APROVACAO` e `APROVADO`. Resultado pendente reserva a colocação; reprovação/cancelamento libera. Proteção deve ser transacional e resistente a concorrência.

Não existe empate de colocação no campeonato.

## 10. Pontuação e penalidades

`TemporadaPontuacao` pertence à temporada e diferencia `COMUM`/`OVERALL` por colocação.

Não é obrigatório configurar todas as posições. Colocação válida sem regra produz impacto zero.

`TemporadaPenalidade` define valores para:

- `DESCLASSIFICACAO`;
- `AUSENCIA`.

Regra ausente produz impacto zero. Cada resultado aprovado contribui independentemente; não existe agregação de penalidades por campeonato/categoria/situação.

Valores usam `BigDecimal(10,3)` e podem ser positivos, zero ou negativos conforme o tipo de regra permitido. Ranking pode ficar negativo.

### Regra atual é autoritativa

`Resultado` não armazena pontuação/penalidade como verdade histórica.

```text
CLASSIFICADO -> colocacao + tipoClasse -> TemporadaPontuacao atual
DESCLASSIFICADO -> TemporadaPenalidade.DESCLASSIFICACAO atual
AUSENTE -> TemporadaPenalidade.AUSENCIA atual
```

Enquanto `ATIVO`, alteração de pontuação ou penalidade recalcula retroativamente todos os resultados aprovados, válidos e elegíveis afetados.

O MVP não versiona regra de pontuação/penalidade por resultado. Alterações administrativas da configuração devem permanecer auditáveis.

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

A data de lançamento/aprovação do resultado pode ser posterior ao encerramento da temporada.

```text
TOTAL = soma(impacto atual de cada Resultado APROVADO válido e elegível)
```

Resultado tardio aprovado em `ENCERRADA` recalcula automaticamente o ranking. `CANCELADA` suspende a projeção; reativação recalcula.

O mesmo `Resultado` pode produzir pontuações diferentes em temporadas diferentes.

### Empates

Empate é permitido no ranking e usa ranking de competição:

```text
1, 1, 3, 4...
```

Formalmente:

```text
posicao = 1 + quantidade de atletas com total estritamente maior
```

Não há desempate esportivo adicional no MVP.

Filtros de visibilidade são aplicados depois do cálculo e não renumeram posições.

Ranking por categoria está fora do MVP.

## 12. Resultado tardio e campeonatos compartilhados

`Inscricao` e `Resultado` pertencem ao campeonato, não à temporada.

Se o mesmo campeonato estiver associado a várias temporadas, existe apenas um resultado compartilhado. Cada temporada decide independentemente se ele é elegível e qual impacto produz.

Exemplo:

```text
Campeonato X
  -> Temporada A / ATIVO
  -> Temporada B / ENCERRADA
  -> Temporada C / CANCELADA

Resultado tardio APROVADO:
  A -> recalcula se elegível
  B -> recalcula se elegível
  C -> preserva o fato, mas não produz efeito enquanto cancelada
```

## 13. Contas e precedência administrativa

Todo atleta precisa de conta. Cadastro direto por profissional e pré-cadastro/liberação seguem os fluxos já definidos no produto.

Atleta ou proprietário podem cancelar conta de atleta. Enquanto cancelada, ela fica fora dos rankings e oculta/suspensa para usuários comuns; vínculos, inscrições, resultados e pendências são preservados. Proprietário mantém acesso administrativo. Atleta ou proprietário podem reativar conforme os fluxos definidos; reativar conta não reativa vínculo encerrado, inscrição cancelada ou resultado cancelado.

Somente proprietário cadastra, inativa, cancela e reativa contas de profissionais. Inatividade e cancelamento são estados distintos: inatividade bloqueia acesso mas preserva seleção/notificações; cancelamento remove de seletores/notificações e encaminha demandas ao proprietário, sem encerrar vínculos ou transferir temporadas.

Permissões administrativas devem ser verificadas no backend.

## 14. Relatórios

Relatório de campeonato apresenta fatos esportivos — inscrições, categoria, classe, situação e colocação — sem atribuir pontuação universal.

Relatório de temporada usa a projeção da própria temporada e deve conseguir detalhar contribuição por campeonato/resultado, distinguindo:

- pontos de colocação;
- penalidade por desclassificação;
- penalidade por ausência;
- categoria/classe de origem;
- total.

Ranking/relatório por categoria fica fora do MVP.

Tela, PDF e Excel devem aplicar o mesmo universo e autorização. Layouts, filtros, agrupamentos e colunas finais permanecem em refinamento.

## 15. Auditoria e concorrência

Preservar histórico das decisões e transições relevantes, incluindo:

- vínculos;
- inscrições;
- versões e decisões de resultado;
- correções/cancelamentos administrativos;
- alterações de configuração de temporada;
- transições de temporada/campeonato;
- alterações de acesso.

Decisões concorrentes devem ser atômicas. Usar proteção de persistência além de validação em memória para invariantes que envolvam registros diferentes, especialmente unicidade de colocação.

Conflitos de versão/unicidade podem ser representados por `409 Conflict`.

## 16. Fontes detalhadas para implementação

- [Vínculo profissional-atleta](refinamento-vinculo-profissional-atleta.md)
- [Temporada](refinamento-temporada.md)
- [Categoria, classe e Overall](refinamento-categoria-classe-overall.md)
- [Inscrição](refinamento-inscricao.md)
- [Resultado](refinamento-resultado.md)
- [Ranking](refinamento-ranking.md)
- [Campeonato e organizador](refinamento-campeonato-organizador.md)
- [Revisão de consistência](revisao-consistencia-mvp.md)

## 17. Próximos refinamentos

O núcleo esportivo está consolidado. As próximas frentes funcionais são:

1. relatórios e exportações;
2. notificações/e-mails e pendências;
3. detalhes restantes de contas/perfis fora do núcleo esportivo;
4. modelo físico, contratos de API e migração;
5. implantação, observabilidade e automação para o Copilot.

Essas frentes devem respeitar as invariantes consolidadas neste documento e nos refinamentos específicos, sem reabrir decisões esportivas já fechadas salvo nova decisão explícita do proprietário do produto.
