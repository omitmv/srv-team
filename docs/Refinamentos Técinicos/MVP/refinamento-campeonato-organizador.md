# Refinamento técnico — Campeonato e Organizador

Status: modelagem técnica em andamento; ciclo de criação, alteração, cancelamento e reativação consolidado para o MVP.

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
- Somente o proprietário pode cancelar ou reativar um campeonato.
- Profissional pode solicitar cancelamento ou reativação mediante justificativa obrigatória.
- Campeonato cancelado não pode ser recadastrado como novo campeonato equivalente.
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
 ├── dtCancelamento
 ├── cdResponsavelCancelamento
 ├── dtReativacao
 ├── cdResponsavelReativacao
 └── auditoria
```

Estados de negócio mínimos:

- `ATIVO`
- `CANCELADO`

Para reduzir impacto de migração, pode ser mantida a identidade física atual `cdCompeticao`/`tbCompeticao`, tratando semanticamente a entidade como Campeonato.

O campo legado `federacao` não deve permanecer como fonte autoritativa. `Campeonato` referencia `Organizador` por identidade.

## Criação

Profissional autorizado pode criar campeonato diretamente, sem aprovação prévia do proprietário.

Na criação:

1. informa dados;
2. seleciona organizador ativo;
3. sistema valida campos e duplicidade;
4. verifica também a existência de campeonato equivalente cancelado;
5. se não houver conflito, cria campeonato no catálogo compartilhado.

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

Ao aprovar, revalidar estado/versionamento, aplicar valores aprovados e preservar antes/depois.

## Cancelamento

### Autoridade

Somente o proprietário pode efetivamente executar:

```text
ATIVO -> CANCELADO
```

Nenhum profissional, inclusive criador do campeonato ou administrador de temporada, pode cancelar diretamente.

### Solicitação de cancelamento

Profissional pode solicitar cancelamento mediante justificativa obrigatória.

```text
Profissional
    |
    | justificativa
    v
Solicitação PENDENTE
    |
    v
Proprietário
 ├── aprova  -> Campeonato CANCELADO
 └── reprova -> Campeonato permanece ATIVO
```

Entidade conceitual:

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

Permitir no máximo uma solicitação de cancelamento `PENDENTE` por campeonato.

## Efeito do cancelamento sobre inscrições, resultados e ranking

O cancelamento é lógico e preserva todos os registros.

Ao cancelar o campeonato:

- campeonato passa a `CANCELADO`;
- não aceita novas inscrições ordinárias;
- não aceita novas associações ordinárias com temporadas;
- inscrições existentes são preservadas;
- resultados existentes são preservados;
- resultados do campeonato deixam de produzir efeito esportivo enquanto o campeonato estiver cancelado;
- resultados `APROVADO` deixam de contribuir com pontos e rankings;
- resultados `PENDENTE_APROVACAO` permanecem armazenados, porém ficam inativos para operação esportiva ordinária enquanto o campeonato estiver cancelado;
- nenhuma entidade é fisicamente excluída.

Importante: os resultados **não devem ter seu próprio status funcional sobrescrito para `CANCELADO` somente porque o campeonato foi cancelado**.

A inativação deve decorrer do estado do campeonato:

```text
Resultado.status = APROVADO
Campeonato.status = CANCELADO

=> resultado preservado
=> efeito esportivo = inativo
```

Isso permite restaurar corretamente o mesmo conjunto de dados caso o campeonato seja reativado.

## Reativação de campeonato cancelado

Diferentemente de uma `Temporada` cancelada, um campeonato cancelado **pode ser reativado**.

A reativação preserva a mesma identidade:

```text
Campeonato #15
ATIVO
  |
  v
CANCELADO
  |
  | reativação aprovada
  v
ATIVO
```

Não criar novo `cdCampeonato`/`cdCompeticao` para representar o mesmo evento.

Somente o proprietário pode executar:

```text
CANCELADO -> ATIVO
```

## Tentativa de recadastro de campeonato cancelado

Quando um profissional tentar cadastrar um campeonato que corresponda a um campeonato `CANCELADO`, o sistema **não deve permitir o recadastro como novo registro**.

O backend deve detectar a correspondência antes da criação.

A interface deve informar que já existe um campeonato cancelado correspondente e perguntar se o profissional deseja solicitar sua reativação.

Fluxo:

```text
Profissional tenta cadastrar campeonato
        |
        v
Sistema encontra equivalente CANCELADO
        |
        v
"Este campeonato já existe e está cancelado.
Deseja solicitar a reativação?"
        |
   +----+----+
   |         |
  não       sim
   |         |
encerra   exige justificativa
             |
             v
     Solicitação de reativação
             |
             v
         Proprietário
        /           \
   aprova          reprova
      |               |
   ATIVO        permanece CANCELADO
```

A detecção deve considerar a mesma estratégia de identidade semântica usada para prevenção de duplicidade. No MVP, a referência recomendada é:

```text
(nome normalizado, cdOrganizador, dtInicio)
```

A implementação pode complementar a comparação com outros dados para reduzir falso positivo, mas não deve permitir contornar a regra criando um novo ID para o mesmo campeonato cancelado.

## Solicitação de reativação

Entidade conceitual recomendada:

```text
SolicitacaoReativacaoCampeonato
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

Regras:

- somente campeonato `CANCELADO` pode receber solicitação de reativação;
- justificativa é obrigatória;
- profissional apenas solicita;
- somente proprietário decide e efetiva;
- no máximo uma solicitação `PENDENTE` por campeonato;
- solicitação pendente não reativa o campeonato automaticamente.

## Efeito da reativação sobre resultados

Ao reativar o campeonato:

- o mesmo campeonato volta a `ATIVO`;
- inscrições existentes permanecem vinculadas ao mesmo ID;
- resultados existentes permanecem vinculados ao mesmo ID de inscrição;
- resultados que já estavam `APROVADO` antes do cancelamento voltam a produzir efeito esportivo automaticamente, desde que continuem válidos segundo as demais regras do domínio;
- resultados `PENDENTE_APROVACAO` voltam a ficar disponíveis para o fluxo ordinário de aprovação, conforme permissões vigentes;
- resultados que já estavam `CANCELADO` por seu próprio fluxo continuam cancelados e **não** são reativados;
- rankings das temporadas aplicáveis são recalculados.

Portanto, a reativação não recria resultados nem inventa novos lançamentos. Ela restaura a validade contextual dos registros que já existiam.

## Auditoria

O histórico deve permitir identificar:

- criação;
- alterações;
- solicitações de cancelamento;
- justificativa de cancelamento;
- decisão do proprietário;
- cancelamento efetivo;
- solicitações de reativação;
- justificativa da reativação;
- decisão do proprietário;
- reativação efetiva;
- responsáveis e datas.

## Categorias e classes

Campeonato não mantém coleção pré-configurada de categorias/classes. A combinação é registrada em `Resultado`.

Não criar `CampeonatoCategoria` ou `CampeonatoClasse` no MVP.

## Duplicidade

A criação deve verificar campeonatos ativos **e cancelados**.

Chave semântica recomendada:

```text
(nome normalizado, cdOrganizador, dtInicio)
```

Comportamento:

- equivalente `ATIVO` -> bloquear duplicidade;
- equivalente `CANCELADO` -> bloquear recadastro e oferecer fluxo de solicitação de reativação;
- inexistente -> permitir criação ordinária.

## Invariantes consolidadas

- Organizador é catálogo central administrado pelo proprietário.
- Campeonato exige organizador.
- Profissional pode criar campeonato diretamente.
- Campeonato integra catálogo compartilhado.
- Campeonato pode integrar várias temporadas.
- Profissional não edita diretamente campeonato existente.
- Somente proprietário cancela campeonato.
- Profissional pode solicitar cancelamento com justificativa obrigatória.
- Cancelamento é lógico.
- Campeonato cancelado não é fisicamente excluído.
- Cancelamento preserva inscrições e resultados.
- Resultados de campeonato cancelado ficam esportivamente inativos sem alteração artificial de seu próprio status.
- Campeonato cancelado não pode ser recadastrado com novo ID.
- Tentativa de recadastro deve oferecer solicitação de reativação.
- Solicitação de reativação exige justificativa.
- Somente proprietário pode efetivar reativação.
- Reativação utiliza o mesmo `cdCampeonato`/`cdCompeticao`.
- Resultados previamente aprovados voltam a produzir efeito esportivo na reativação, se ainda forem válidos.
- Resultados cancelados por seu próprio fluxo não são reativados.
- Rankings são recalculados no cancelamento e na reativação.
- Categoria/classe não são pré-configuradas no campeonato.
- Inscrição pertence ao campeonato, não à temporada.

## Decisões ainda abertas

### D1 — Alterações após uso

Recomendação:

- nome: permitir;
- local: permitir;
- organizador: permitir com justificativa/auditoria;
- datas: permitir com justificativa/auditoria;
- identidade: nunca substituir; outro evento exige novo campeonato.

### D2 — Correspondência para impedir recadastro

A regra funcional está fechada: campeonato cancelado equivalente não pode ser recadastrado.

Ainda pode ser refinada tecnicamente a heurística de correspondência além de `(nome normalizado, organizador, data inicial)` para minimizar falsos positivos e falsos negativos sem enfraquecer a regra de domínio.
