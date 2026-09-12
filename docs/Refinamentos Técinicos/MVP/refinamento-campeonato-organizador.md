# Refinamento técnico — Campeonato e Organizador

Status: modelagem técnica em andamento; regras de criação, alteração e autorização de cancelamento consolidadas; efeitos esportivos do cancelamento ainda pendentes.

## Objetivo

Modelar `Campeonato` como evento compartilhado entre profissionais e temporadas, independente de propriedade exclusiva de uma temporada, e substituir organizador/federação como texto livre por referência a catálogo central de `Organizador` administrado pelo proprietário.

## Regras confirmadas

- `Organizador` pertence a catálogo central administrado exclusivamente pelo proprietário.
- O profissional pode criar campeonato.
- Todo campeonato deve possuir organizador selecionado do catálogo central.
- Campeonato criado por profissional integra o catálogo compartilhado.
- Campeonato não pertence exclusivamente a uma temporada.
- Associação campeonato-temporada é N:N via `TemporadaCampeonato`.
- Profissionais não editam diretamente campeonato existente.
- Alterações são solicitadas ao proprietário.
- Somente o proprietário pode efetivamente cancelar um campeonato.
- Um profissional pode solicitar o cancelamento, obrigatoriamente mediante justificativa.
- Categoria e classe são informadas no `Resultado`, não pré-configuradas no campeonato.

## Organizador

Entidade conceitual:

```text
Organizador
 ├── cdOrganizador
 ├── dsNome
 ├── flAtivo
 └── auditoria
```

Somente o proprietário pode criar, editar, inativar ou reativar organizador.

Organizador já utilizado não deve ser fisicamente excluído. Inativação preserva campeonatos históricos e impede seleção em novos campeonatos.

Recomendação de unicidade: impedir dois organizadores ativos semanticamente equivalentes pelo nome normalizado, respeitando collation do MySQL.

## Campeonato

Entidade conceitual:

```text
Campeonato
 ├── cdCampeonato/cdCompeticao
 ├── dsNome
 ├── cdOrganizador
 ├── dtInicio
 ├── dtFim
 ├── dsLocal
 ├── status
 ├── cdCriador
 └── auditoria
```

Para reduzir impacto de migração, pode ser mantida a identidade física atual `cdCompeticao`/`tbCompeticao`, tratando semanticamente a entidade como Campeonato.

O campo legado `federacao` não deve permanecer como fonte autoritativa. `Campeonato` referencia `Organizador` por identidade.

## Criação

Profissional autorizado pode criar campeonato diretamente, sem aprovação prévia do proprietário.

Na criação:

1. informa dados;
2. seleciona organizador ativo;
3. sistema valida campos e duplicidade;
4. campeonato é criado no catálogo compartilhado;
5. fica disponível para associação a temporadas.

`cdCriador` é informação de auditoria, não propriedade exclusiva do registro.

## Alteração

Profissionais não alteram diretamente campeonato existente.

Deve existir solicitação encaminhada ao proprietário.

Estrutura conceitual:

```text
SolicitacaoAlteracaoCampeonato
 ├── cdSolicitacao
 ├── cdCampeonato
 ├── cdSolicitante
 ├── status
 ├── dadosPropostos
 ├── justificativa
 ├── dtSolicitacao
 ├── cdResponsavelDecisao
 ├── dtDecisao
 └── motivoReprovacao
```

Status mínimos:

- `PENDENTE`
- `APROVADA`
- `REPROVADA`

Ao aprovar, revalidar estado/versionamento, aplicar valores aprovados e preservar antes/depois. Ao reprovar, manter campeonato inalterado e registrar decisão.

Recomendação: no máximo uma solicitação de alteração `PENDENTE` por campeonato.

## Cancelamento de campeonato

### Autoridade

O cancelamento efetivo é uma operação exclusiva do **proprietário do sistema**.

Nenhum profissional, inclusive:

- criador do campeonato;
- criador de temporada que o utiliza;
- administrador de temporada;

pode executar diretamente a transição do campeonato para `CANCELADO`.

O proprietário pode realizar o cancelamento administrativo diretamente, sujeito à auditoria.

### Solicitação por profissional

Um profissional pode solicitar o cancelamento de um campeonato.

A solicitação exige **justificativa obrigatória**.

Fluxo:

```text
Profissional
    |
    | solicita cancelamento + justificativa
    v
PENDENTE
    |
    v
Proprietário analisa
    |
    +-- aprova  -> Campeonato CANCELADO
    |
    +-- reprova -> Campeonato permanece inalterado
```

### Entidade conceitual

Recomenda-se separar a solicitação de cancelamento da solicitação de alteração para preservar semântica explícita:

```text
SolicitacaoCancelamentoCampeonato
 ├── cdSolicitacao
 ├── cdCampeonato
 ├── cdSolicitante
 ├── justificativa
 ├── status
 ├── dtSolicitacao
 ├── cdResponsavelDecisao
 ├── dtDecisao
 └── motivoReprovacao
```

Status:

- `PENDENTE`
- `APROVADA`
- `REPROVADA`

`justificativa` é obrigatória na criação da solicitação.

Quando aprovada:

1. registrar proprietário responsável pela decisão;
2. registrar data/hora;
3. marcar solicitação `APROVADA`;
4. realizar cancelamento lógico do campeonato;
5. preservar campeonato e todas as referências históricas.

Quando reprovada:

1. campeonato permanece inalterado;
2. solicitação fica `REPROVADA`;
3. responsável e data são registrados;
4. registrar motivo da reprovação para auditoria.

### Concorrência e duplicidade de solicitação

Recomendação para o MVP: permitir no máximo uma `SolicitacaoCancelamentoCampeonato` `PENDENTE` por campeonato.

Se já existir uma pendência, nova solicitação não deve criar outro registro concorrente. O usuário deve ser direcionado à solicitação existente.

A existência de solicitação pendente não altera o estado do campeonato antes da decisão do proprietário.

### Cancelamento lógico

Campeonato utilizado não deve ser fisicamente excluído.

O cancelamento preserva:

- identidade do campeonato;
- associações históricas com temporadas;
- inscrições;
- resultados;
- solicitações de alteração/cancelamento;
- auditoria.

O estado `CANCELADO` deve ser explícito, em vez de depender de `flAtivo` ambíguo.

Após cancelamento, o campeonato não deve aceitar novas associações ordinárias com temporadas nem novas inscrições.

Os efeitos sobre inscrições já existentes, resultados aprovados e rankings permanecem decisão de produto específica a fechar.

## Auditoria

Histórico deve permitir identificar criação, alterações solicitadas, cancelamentos solicitados, justificativas, decisões, responsáveis, datas e valores antes/depois quando aplicável.

## Categorias e classes

Campeonato não mantém coleção pré-configurada de categorias/classes. A combinação é registrada em `Resultado`.

Não criar no MVP `CampeonatoCategoria` ou `CampeonatoClasse`.

## Duplicidade

Recomendação de chave semântica para criação:

```text
(nome normalizado, cdOrganizador, dtInicio)
```

Recomendação: bloquear duplicidade exata na criação ordinária; exceções somente por intervenção do proprietário.

## Invariantes consolidadas

- Organizador é catálogo central administrado pelo proprietário.
- Campeonato exige organizador.
- `federacao` texto livre deixa de ser fonte autoritativa.
- Profissional pode criar campeonato diretamente.
- Campeonato integra catálogo compartilhado.
- Criador não possui exclusividade sobre o campeonato.
- Campeonato pode integrar várias temporadas.
- Associação é N:N via `TemporadaCampeonato`.
- Profissionais não editam diretamente campeonato existente.
- Alterações são solicitadas ao proprietário.
- Somente proprietário cancela campeonato.
- Profissional apenas solicita cancelamento.
- Solicitação de cancelamento exige justificativa.
- Cancelamento é lógico.
- Campeonato cancelado não é fisicamente excluído.
- Categoria/classe não são pré-configuradas no campeonato.
- Inscrição pertence ao campeonato, não à temporada.

## Decisões ainda abertas

### D1 — Prevenção de duplicidade

Recomendação: bloquear criação ordinária de `(nome normalizado, organizador, data inicial)` duplicado. Exceção somente por proprietário.

### D2 — Alterações após uso

Recomendação:

- nome: permitir;
- local: permitir;
- organizador: permitir com justificativa/auditoria;
- datas: permitir com justificativa/auditoria;
- identidade: nunca substituir; outro evento exige novo campeonato.

### D3 — Efeito esportivo do cancelamento

Ainda é necessário definir o que ocorre, após o proprietário cancelar um campeonato, com:

- inscrições já confirmadas;
- resultados `PENDENTE_APROVACAO`;
- resultados `APROVADO`;
- pontos/rankings de temporadas que possuem o campeonato associado.

O cancelamento lógico, a autoridade exclusiva do proprietário e o fluxo de solicitação profissional já estão confirmados; somente esses efeitos esportivos permanecem abertos.
