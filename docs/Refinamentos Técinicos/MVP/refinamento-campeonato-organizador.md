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
- Profissionais não editam diretamente campeonato já utilizado por inscrição ou resultado.
- Quando campeonato já possui inscrição ou resultado, qualquer alteração cadastral exige solicitação ao proprietário.
- A solicitação de alteração deve conter os dados atuais, os novos dados propostos e justificativa obrigatória do profissional.
- O proprietário deve ser notificado para aprovar ou reprovar a alteração.
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

## Alteração de campeonato

### Campeonato ainda sem inscrição e sem resultado

Enquanto o campeonato ainda não possuir qualquer `Inscricao` ou `Resultado`, a política de edição pode permanecer mais simples conforme a autorização ordinária definida para cadastro.

A partir do momento em que existir ao menos uma inscrição ou resultado associado, o campeonato passa a ser considerado **utilizado** para efeito de alteração cadastral.

A verificação é histórica: basta já ter existido inscrição ou resultado, mesmo que posteriormente cancelado ou inativado.

### Campeonato utilizado

Quando o campeonato já possuir inscrição ou resultado, o profissional não pode alterar seus dados diretamente.

Qualquer alteração deve gerar uma solicitação para decisão do proprietário.

O fluxo é:

```text
Profissional informa alteração
        |
        | justificativa obrigatória
        v
Sistema captura:
- dados atuais
- novos dados propostos
- campos alterados
        |
        v
Solicitação PENDENTE
        |
        +--> notificação ao proprietário
        |
        v
Proprietário analisa antes/depois + justificativa
        |
   +----+----+
   |         |
aprova     reprova
   |         |
aplica    mantém cadastro atual
```

### Solicitação de alteração

Estrutura conceitual:

```text
SolicitacaoAlteracaoCampeonato
 ├── cdSolicitacao
 ├── cdCampeonato
 ├── cdSolicitante
 ├── status
 ├── dadosAtuais
 ├── dadosPropostos
 ├── camposAlterados
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

A justificativa do profissional é obrigatória.

A solicitação deve preservar um snapshot suficiente do estado do campeonato no momento da solicitação para que o proprietário consiga comparar claramente:

```text
DADO ATUAL         NOVO DADO
--------------------------------
Nome A          -> Nome B
Organizador X   -> Organizador Y
Data 10/10      -> Data 17/10
Local A         -> Local B
```

Persistir somente uma descrição textual genérica não é suficiente.

### Notificação ao proprietário

Ao criar uma solicitação de alteração de campeonato utilizado, o sistema deve gerar uma notificação destinada ao proprietário.

A notificação deve permitir identificar no mínimo:

- campeonato;
- profissional solicitante;
- data da solicitação;
- justificativa;
- dados atuais;
- dados propostos;
- campos modificados.

A notificação representa uma pendência de decisão e não altera o campeonato antes da aprovação.

### Aprovação

Somente o proprietário decide a solicitação.

Ao aprovar:

1. revalidar que a solicitação continua `PENDENTE`;
2. revalidar a versão atual do campeonato;
3. verificar se o estado atual ainda corresponde à base usada na solicitação;
4. aplicar os novos valores;
5. registrar antes/depois definitivo;
6. registrar proprietário responsável e data/hora;
7. marcar solicitação como `APROVADA`.

Se o campeonato tiver sido alterado desde a solicitação, a aprovação não deve sobrescrever silenciosamente dados mais recentes. Deve ocorrer conflito e nova análise.

Recomendação técnica: `@Version` em `Campeonato` e armazenamento da versão-base na solicitação.

### Reprovação

Ao reprovar:

- campeonato permanece sem alteração;
- solicitação passa a `REPROVADA`;
- responsável e data são registrados;
- motivo da reprovação deve ser preservado para auditoria.

### Concorrência

Para o MVP, permitir no máximo uma `SolicitacaoAlteracaoCampeonato` `PENDENTE` por campeonato.

Isso evita propostas concorrentes baseadas em estados diferentes.

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

Os resultados não devem ter seu próprio status funcional sobrescrito para `CANCELADO` somente porque o campeonato foi cancelado.

A inativação decorre do estado do campeonato.

## Reativação de campeonato cancelado

Diferentemente de uma `Temporada` cancelada, um campeonato cancelado pode ser reativado.

A reativação preserva a mesma identidade.

Somente o proprietário pode executar:

```text
CANCELADO -> ATIVO
```

## Tentativa de recadastro de campeonato cancelado

Quando um profissional tentar cadastrar um campeonato correspondente a um registro `CANCELADO`, o sistema não deve permitir novo cadastro.

A interface deve informar que já existe um campeonato cancelado correspondente e perguntar se deseja solicitar reativação.

A solicitação exige justificativa e somente o proprietário pode aprovar.

A detecção usa como referência inicial:

```text
(nome normalizado, cdOrganizador, dtInicio)
```

## Solicitação de reativação

Estrutura conceitual:

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

Regras:

- somente campeonato `CANCELADO` pode receber solicitação de reativação;
- justificativa obrigatória;
- profissional apenas solicita;
- somente proprietário decide;
- no máximo uma solicitação `PENDENTE` por campeonato.

## Efeito da reativação sobre resultados

Ao reativar:

- mesmo campeonato retorna a `ATIVO`;
- inscrições existentes continuam vinculadas ao mesmo ID;
- resultados existentes continuam vinculados às mesmas inscrições;
- resultados anteriormente `APROVADO` voltam a produzir efeito esportivo, se ainda válidos;
- resultados `PENDENTE_APROVACAO` voltam ao fluxo ordinário;
- resultados `CANCELADO` por seu próprio fluxo continuam cancelados;
- rankings aplicáveis são recalculados.

## Auditoria

O histórico deve permitir identificar:

- criação;
- alterações solicitadas;
- dados atuais e propostos;
- justificativa do profissional;
- notificações geradas;
- decisões do proprietário;
- valores antes/depois;
- cancelamentos;
- reativações;
- responsáveis e datas.

## Categorias e classes

Campeonato não mantém coleção pré-configurada de categorias/classes. A combinação é registrada em `Resultado`.

Não criar `CampeonatoCategoria` ou `CampeonatoClasse` no MVP.

## Duplicidade

A criação deve verificar campeonatos ativos e cancelados.

Chave semântica recomendada:

```text
(nome normalizado, cdOrganizador, dtInicio)
```

Comportamento:

- equivalente `ATIVO` -> bloquear duplicidade;
- equivalente `CANCELADO` -> bloquear recadastro e oferecer solicitação de reativação;
- inexistente -> permitir criação ordinária.

## Invariantes consolidadas

- Organizador é catálogo central administrado pelo proprietário.
- Campeonato exige organizador.
- Profissional pode criar campeonato diretamente.
- Campeonato integra catálogo compartilhado.
- Campeonato pode integrar várias temporadas.
- Campeonato que já teve inscrição ou resultado é considerado utilizado.
- Profissional não altera diretamente campeonato utilizado.
- Alteração de campeonato utilizado exige solicitação com justificativa.
- Solicitação de alteração deve conter dados atuais e novos dados.
- Proprietário deve ser notificado sobre solicitação de alteração.
- Somente proprietário aprova ou reprova alteração de campeonato utilizado.
- Alteração só é aplicada após aprovação.
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
- Resultados previamente aprovados voltam a produzir efeito esportivo na reativação, se ainda válidos.
- Resultados cancelados por seu próprio fluxo não são reativados.
- Rankings são recalculados no cancelamento e na reativação.
- Categoria/classe não são pré-configuradas no campeonato.
- Inscrição pertence ao campeonato, não à temporada.

## Decisão técnica ainda aberta

### Correspondência para impedir recadastro

A regra funcional está fechada: campeonato cancelado equivalente não pode ser recadastrado.

Ainda pode ser refinada tecnicamente a heurística de correspondência além de `(nome normalizado, organizador, data inicial)` para minimizar falsos positivos e falsos negativos sem enfraquecer a regra de domínio.
