# Refinamento técnico — Modelo físico da Fase 2

Status: desenho técnico consolidado para implementação; migrations e código pendentes.

## Objetivo

Detalhar o modelo físico/JPA da Fase 2 do MVP:

- `VinculoProfissionalAtleta`;
- `Temporada`;
- `TemporadaProfissional`;
- `TemporadaCampeonato`;
- histórico de transições de temporada;
- autorização contextual associada a esses agregados.

Este documento complementa os refinamentos funcionais e o `refinamento-implementacao.md`.

## Princípios estruturais

1. `VinculoProfissionalAtleta` representa relação real atleta-profissional.
2. `TemporadaProfissional` representa permissão contextual e nunca substitui vínculo real.
3. A composição de atletas da temporada é derivada exclusivamente dos vínculos do criador.
4. Não criar `TemporadaAtleta` no MVP.
5. `TemporadaCampeonato` é associação estrutural N:N.
6. Toda transição de status da temporada deve ser auditável por histórico append-only.
7. Estados e permissões novos devem ser persistidos como `STRING`.
8. Constraints críticas devem existir no banco, não somente no service.

---

# 1. `VinculoProfissionalAtleta`

## Tabela proposta

```text
tbVinculoProfissionalAtleta
```

Campos:

```text
cdVinculo                INT PK AUTO_INCREMENT
cdAtleta                 INT NOT NULL FK -> tbUsuario.cdUsuario
cdProfissional           INT NOT NULL FK -> tbUsuario.cdUsuario
status                    VARCHAR(30) NOT NULL
origem                    VARCHAR(50) NOT NULL
cdSolicitante             INT NULL FK -> tbUsuario.cdUsuario
dtSolicitacao             DATETIME(6) NULL
dtInicio                  DATETIME(6) NULL
dtEncerramento            DATETIME(6) NULL
motivoReprovacao          VARCHAR(1000) NULL
motivoEncerramento        VARCHAR(1000) NULL
cdResponsavelDecisao      INT NULL FK -> tbUsuario.cdUsuario
dtDecisao                 DATETIME(6) NULL
cdResponsavelEncerramento INT NULL FK -> tbUsuario.cdUsuario
lockVersion               BIGINT NOT NULL DEFAULT 0
dtCadastro                DATETIME(6) NOT NULL
dtAtualizacao             DATETIME(6) NOT NULL
```

Enums:

```text
VinculoStatus
- PENDENTE
- ATIVO
- REPROVADO
- ENCERRADO

VinculoOrigem
- CADASTRO_DIRETO_PROFISSIONAL
- PRE_CADASTRO_ATLETA
- SOLICITACAO_ATLETA
- SOLICITACAO_PROFISSIONAL
- ADMINISTRATIVO
```

## Regras de consistência

### PENDENTE

- `dtInicio = null`;
- `dtEncerramento = null`;
- `dtSolicitacao` obrigatória;
- `cdSolicitante` obrigatório.

### ATIVO

- `dtInicio` obrigatória;
- `dtEncerramento = null`.

### REPROVADO

- `motivoReprovacao` obrigatório;
- `cdResponsavelDecisao` obrigatório;
- `dtDecisao` obrigatória;
- `dtInicio = null`.

### ENCERRADO

- `dtInicio` obrigatória;
- `dtEncerramento` obrigatória;
- `motivoEncerramento` obrigatório;
- `cdResponsavelEncerramento` obrigatório.

## Unicidades condicionais

Regras:

```text
no máximo 1 PENDENTE por (cdAtleta, cdProfissional)
no máximo 1 ATIVO por (cdAtleta, cdProfissional)
```

Como MySQL não oferece partial unique index tradicional, usar coluna gerada ou chave funcional equivalente.

Estratégia recomendada:

```text
chavePendente =
  CASE WHEN status = 'PENDENTE'
       THEN CONCAT(cdAtleta, ':', cdProfissional)
       ELSE NULL
  END

chaveAtivo =
  CASE WHEN status = 'ATIVO'
       THEN CONCAT(cdAtleta, ':', cdProfissional)
       ELSE NULL
  END
```

Com índices únicos:

```text
UNIQUE(chavePendente)
UNIQUE(chaveAtivo)
```

`NULL` múltiplo permanece permitido para estados históricos.

Alternativamente, usar colunas geradas separadas por IDs, se a versão do MySQL e padrão adotado no projeto favorecerem isso.

## Índices

```text
INDEX(cdAtleta)
INDEX(cdProfissional)
INDEX(cdAtleta, cdProfissional, status)
INDEX(cdAtleta, cdProfissional, dtInicio, dtEncerramento)
```

## Concorrência

Fluxos de criação/aprovação devem:

1. validar estado atual;
2. executar dentro de transação;
3. confiar também na constraint física;
4. converter violação de unicidade funcional em `409 Conflict`.

`@Version` protege somente edição concorrente do mesmo vínculo, não criação concorrente de registros diferentes.

---

# 2. `Temporada`

## Tabela proposta

```text
tbTemporada
```

Campos:

```text
cdTemporada        INT PK AUTO_INCREMENT
dsNome             VARCHAR(250) NOT NULL
cdCriador          INT NOT NULL FK -> tbUsuario.cdUsuario
dtInicio           DATE NULL
dtEncerramento     DATE NULL
status              VARCHAR(30) NOT NULL
lockVersion         BIGINT NOT NULL DEFAULT 0
dtCadastro          DATETIME(6) NOT NULL
dtAtualizacao       DATETIME(6) NOT NULL
```

Enum:

```text
TemporadaStatus
- ATIVO
- ENCERRADA
- CANCELADA
```

## Observação sobre datas

As datas nominais da temporada são informativas e não delimitam automaticamente associação de campeonato ou validade de resultado.

Portanto:

- `dtInicio` pode ser opcional se o produto atual não exigir período fechado;
- `dtEncerramento` nominal não deve ser confundida com timestamp de transição `ATIVO -> ENCERRADA`;
- timestamps reais das transições ficam no histórico.

Se o frontend exigir datas sempre preenchidas, isso pode ser validado no contrato sem reutilizar esses campos para determinar elegibilidade histórica.

## Criador

`cdCriador`:

- obrigatório;
- imutável após criação;
- deve apontar para perfil profissional elegível;
- não existe transferência de titularidade no MVP.

Não representar o criador apenas por registro em `TemporadaProfissional`, porque ele possui semântica permanente própria.

## Índices

```text
INDEX(cdCriador)
INDEX(status)
INDEX(cdCriador, status)
```

Não criar unicidade de temporada ativa por profissional.

---

# 3. `TemporadaStatusHistorico`

A Fase 2 deve formalizar histórico de status de temporada da mesma forma que campeonato.

## Tabela proposta

```text
tbTemporadaStatusHistorico
```

Campos:

```text
cdHistorico      BIGINT PK AUTO_INCREMENT
cdTemporada      INT NOT NULL FK -> tbTemporada.cdTemporada
statusOrigem      VARCHAR(30) NOT NULL
statusDestino     VARCHAR(30) NOT NULL
justificativa     VARCHAR(1000) NULL
cdResponsavel     INT NOT NULL FK -> tbUsuario.cdUsuario
dtTransicao       DATETIME(6) NOT NULL
origemOperacao    VARCHAR(50) NOT NULL
dtCadastro        DATETIME(6) NOT NULL
```

Enum sugerido:

```text
OrigemTransicaoTemporada
- DIRETA_CRIADOR
- DIRETA_ADMINISTRADOR_TEMPORADA
- DIRETA_PROPRIETARIO
```

Se futuramente houver workflow de aprovação do proprietário, adicionar referência opcional à solicitação sem alterar a semântica do histórico.

## Justificativa por transição

```text
ATIVO -> ENCERRADA
justificativa opcional

ENCERRADA -> ATIVO
justificativa obrigatória

ATIVO -> CANCELADA
justificativa obrigatória

ENCERRADA -> CANCELADA
justificativa obrigatória

CANCELADA -> ATIVO
justificativa obrigatória

CANCELADA -> ENCERRADA
justificativa obrigatória
```

A origem de reativação depende do estado imediatamente anterior à última entrada em `CANCELADA`.

## Fonte autoritativa

`tbTemporada.status` representa estado atual.

`tbTemporadaStatusHistorico` representa a sequência autoritativa de transições.

Campos denormalizados como última data de encerramento/cancelamento podem ser adicionados somente se houver ganho de consulta, nunca como substitutos do histórico.

## Atomicidade

Toda mudança efetiva de status deve executar na mesma transação:

```text
1. validar transição
2. validar autorização
3. determinar statusOrigem
4. atualizar tbTemporada.status
5. inserir exatamente 1 histórico
6. commit
```

Se falhar qualquer passo, nenhum efeito parcial deve permanecer.

## Append-only

Histórico não recebe update/delete funcional ordinário.

---

# 4. `TemporadaProfissional`

## Tabela proposta

```text
tbTemporadaProfissional
```

Campos:

```text
cdTemporadaProfissional INT PK AUTO_INCREMENT
cdTemporada             INT NOT NULL FK -> tbTemporada.cdTemporada
cdProfissional          INT NOT NULL FK -> tbUsuario.cdUsuario
permissao                VARCHAR(30) NOT NULL
flAtivo                  BOOLEAN NOT NULL DEFAULT TRUE
cdResponsavelAlteracao  INT NOT NULL FK -> tbUsuario.cdUsuario
dtInicio                DATETIME(6) NOT NULL
dtFim                   DATETIME(6) NULL
lockVersion             BIGINT NOT NULL DEFAULT 0
dtCadastro              DATETIME(6) NOT NULL
dtAtualizacao           DATETIME(6) NOT NULL
```

Enum:

```text
TemporadaPermissao
- ADMINISTRACAO
- CONSULTA
```

## Decisão sobre histórico de acesso

Recomendação: **não sobrescrever silenciosamente permissão antiga**.

Há duas estratégias possíveis:

### Estratégia A — um registro atual + histórico separado

```text
TemporadaProfissional
TemporadaProfissionalHistorico
```

### Estratégia B — intervalos temporais na própria tabela

Cada alteração encerra o registro atual (`dtFim`) e cria novo registro.

Para o MVP, recomendo **Estratégia B**, porque:

- preserva histórico de acesso naturalmente;
- facilita auditoria temporal;
- evita tabela extra;
- mantém consulta atual simples com `dtFim IS NULL` e `flAtivo = true`.

Assim:

```text
inclusão -> novo registro ativo
promoção/rebaixamento -> encerra registro atual + cria novo
remoção -> encerra registro atual
```

`flAtivo` pode ser eliminado se `dtFim IS NULL` for suficiente. Se mantido, deve permanecer coerente com `dtFim`.

Recomendação final: **não usar `flAtivo`; usar intervalo temporal**.

Modelo final preferido:

```text
cdTemporadaProfissional
cdTemporada
cdProfissional
permissao
dtInicio
dtFim
cdResponsavelAlteracao
dtCadastro
```

## Unicidade atual

No máximo um acesso atual por:

```text
(cdTemporada, cdProfissional)
```

Proteger fisicamente com chave condicional baseada em `dtFim IS NULL`.

Exemplo conceitual:

```text
chaveAcessoAtual =
  CASE WHEN dtFim IS NULL
       THEN CONCAT(cdTemporada, ':', cdProfissional)
       ELSE NULL
  END
```

`UNIQUE(chaveAcessoAtual)`.

## Criador

Não é necessário criar registro do criador em `TemporadaProfissional`.

Recomendação:

```text
Temporada.cdCriador = autoridade administrativa implícita e permanente
TemporadaProfissional = somente profissionais adicionais
```

Vantagens:

- impede inconsistência criador removido/rebaixado;
- evita duplicar fonte de verdade;
- consulta de autorização fica explícita.

Política:

```text
isAdmin = usuario == temporada.cdCriador
       OR existe acesso atual ADMINISTRACAO
```

Tentativa de cadastrar o criador em `TemporadaProfissional` deve ser bloqueada.

## Gestão em `ENCERRADA`

Permitida sem reabertura:

- incluir profissional;
- remover acesso;
- promover CONSULTA -> ADMINISTRACAO;
- rebaixar ADMINISTRACAO -> CONSULTA.

Em `CANCELADA`, recomendação técnica: permitir somente leitura administrativa e operações explícitas do proprietário até reativação, salvo decisão funcional futura em contrário.

---

# 5. `TemporadaCampeonato`

## Tabela proposta

```text
tbTemporadaCampeonato
```

Campos:

```text
cdTemporadaCampeonato INT PK AUTO_INCREMENT
cdTemporada           INT NOT NULL FK -> tbTemporada.cdTemporada
cdCompeticao          INT NOT NULL FK -> tbCompeticao.cdCompeticao
cdResponsavelInclusao INT NOT NULL FK -> tbUsuario.cdUsuario
dtInicioAssociacao    DATETIME(6) NOT NULL
cdResponsavelRemocao  INT NULL FK -> tbUsuario.cdUsuario
dtFimAssociacao       DATETIME(6) NULL
dtCadastro            DATETIME(6) NOT NULL
```

## Associação lógica

A associação atual existe quando:

```text
dtFimAssociacao IS NULL
```

Não excluir fisicamente associação utilizada.

Mesmo que não existam resultados, preservar histórico de inclusão/remoção é útil para auditoria e entendimento da composição passada.

## Unicidade atual

No máximo uma associação ativa por:

```text
(cdTemporada, cdCompeticao)
```

Proteger com chave condicional no MySQL.

## Estado da temporada

Criar/remover associação apenas quando:

```text
Temporada.status = ATIVO
```

`ENCERRADA` exige reabertura.

`CANCELADA` bloqueia alteração estrutural ordinária.

## Efeito da remoção

Remover associação:

- não altera `Campeonato`;
- não altera `Inscricao`;
- não altera `Resultado`;
- apenas retira o campeonato da interpretação daquela temporada a partir do estado corrente;
- recalcula projeção/ranking afetado.

## Reassociação

Após remoção, nova associação futura cria **novo registro**, preservando o ciclo anterior.

Isso evita perder histórico de composição da temporada.

## Observação sobre elegibilidade histórica

A regra funcional consolidada utiliza campeonato atualmente associado para participação na projeção corrente.

O histórico de associação serve auditoria, mas não congela automaticamente a regra de pontuação por período.

Se o campeonato for removido da temporada, seus resultados deixam de contribuir na projeção corrente; se reassociado, voltam a ser interpretados conforme regras atuais e demais critérios de elegibilidade.

---

# 6. Autorização contextual

## Componente central

Criar:

```text
TemporadaAuthorizationService
```

Responsabilidades:

- identificar criador;
- verificar acesso atual em `TemporadaProfissional`;
- diferenciar `ADMINISTRACAO` e `CONSULTA`;
- validar estado da temporada quando a operação depender de lifecycle;
- expor métodos de política reutilizáveis.

Exemplos:

```text
canAdministerSeason(userId, temporadaId)
canViewSeason(userId, temporadaId)
canManageSeasonAccess(userId, temporadaId)
canChangeSeasonStructure(userId, temporadaId)
canOperateAthleteInSeason(userId, temporadaId, atletaId)
```

Não colocar regra de `Inscricao` ou `Resultado` inteira nesse componente; ele fornece somente a dimensão de autorização da temporada.

## Administração

```text
usuario == cdCriador
OU acesso atual ADMINISTRACAO
```

## Consulta

```text
usuario == cdCriador
OU acesso atual ADMINISTRACAO
OU acesso atual CONSULTA
```

Para dados individualizados sob `CONSULTA`, ainda verificar vínculo ativo entre consultor e atleta conforme regra funcional.

## Operação contextual sobre atleta

Para `ADMINISTRACAO`, vínculo próprio administrador-atleta não é exigido.

Mas o atleta precisa pertencer à composição da temporada:

```text
existe VinculoProfissionalAtleta ATIVO
entre atleta e Temporada.cdCriador
```

Para fatos históricos, a regra consumidora usa vigência na data do campeonato.

---

# 7. Consultas essenciais de repositório

## `VinculoProfissionalAtletaRepository`

Consultas principais:

```text
findCurrentActive(atleta, profissional)
findCurrentPending(atleta, profissional)
existsActive(atleta, profissional)
existsPending(atleta, profissional)
findValidAtDate(atleta, profissional, dataReferencia)
```

A consulta histórica deve usar intervalo, não somente status.

## `TemporadaRepository`

```text
findByIdForUpdate(id)      // quando transição estrutural exigir lock
findByCreator(idCriador)
```

## `TemporadaProfissionalRepository`

```text
findCurrentAccess(temporada, profissional)
existsCurrentAdmin(temporada, profissional)
findCurrentMembers(temporada)
findHistory(temporada, profissional)
```

## `TemporadaCampeonatoRepository`

```text
existsCurrentAssociation(temporada, campeonato)
findCurrentChampionships(temporada)
findCurrentSeasonsForChampionship(campeonato)
findHistory(temporada, campeonato)
```

---

# 8. Locking e transações

## Transição de temporada

Recomendação:

- carregar `Temporada` com lock pessimista de escrita ou estratégia transacional equivalente para mudança de status;
- usar `@Version` como proteção adicional contra update perdido;
- inserir histórico na mesma transação.

Isso evita duas transições concorrentes partirem do mesmo estado e gerarem histórico inconsistente.

## Gestão de acesso

Promoção/rebaixamento/remoção:

- travar acesso atual do par temporada/profissional;
- encerrar registro atual;
- criar novo quando necessário;
- constraint de acesso atual protege concorrência residual.

## Associação de campeonato

Inclusão:

- validar `Temporada.status = ATIVO`;
- validar autorização;
- verificar ausência de associação atual;
- inserir;
- constraint física impede duplicidade concorrente.

Remoção:

- localizar associação atual;
- marcar `dtFimAssociacao` e responsável;
- recalcular/inutilizar cache de ranking se existir.

---

# 9. APIs sugeridas

## Vínculo

```text
POST   /vinculos
POST   /vinculos/{id}/aprovar
POST   /vinculos/{id}/reprovar
POST   /vinculos/{id}/encerrar
GET    /vinculos/meus
GET    /vinculos/{id}
```

Não usar `PUT /vinculos/{id}` genérico para alterar status.

## Temporada

```text
POST   /temporadas
GET    /temporadas/{id}
GET    /temporadas
POST   /temporadas/{id}/encerrar
POST   /temporadas/{id}/reabrir
POST   /temporadas/{id}/cancelar
POST   /temporadas/{id}/reativar
```

## Acessos

```text
POST   /temporadas/{id}/profissionais
PATCH  /temporadas/{id}/profissionais/{profissionalId}/permissao
DELETE /temporadas/{id}/profissionais/{profissionalId}
GET    /temporadas/{id}/profissionais
```

`DELETE` representa remoção lógica/encerramento do acesso, não delete físico.

## Campeonatos da temporada

```text
POST   /temporadas/{id}/campeonatos/{campeonatoId}
DELETE /temporadas/{id}/campeonatos/{campeonatoId}
GET    /temporadas/{id}/campeonatos
```

---

# 10. DTOs mínimos

## Temporada

```text
CreateTemporadaRequest
TemporadaResponse
EncerrarTemporadaRequest
ReabrirTemporadaRequest
CancelarTemporadaRequest
ReativarTemporadaRequest
```

`EncerrarTemporadaRequest` não exige justificativa.

Os demais comandos de transição exigem justificativa.

## TemporadaProfissional

```text
AddTemporadaProfissionalRequest
ChangeTemporadaPermissaoRequest
TemporadaProfissionalResponse
```

## Vínculo

```text
CreateVinculoRequest
AprovarVinculoRequest
ReprovarVinculoRequest
EncerrarVinculoRequest
VinculoResponse
```

---

# 11. Migrations sugeridas

Após a Fase 1 e baseline do Flyway:

```text
Vxxx__create_vinculo_profissional_atleta.sql
Vxxx__create_temporada.sql
Vxxx__create_temporada_status_historico.sql
Vxxx__create_temporada_profissional.sql
Vxxx__create_temporada_campeonato.sql
Vxxx__create_phase2_indexes_and_constraints.sql
```

Separar tabelas de constraints mais complexas facilita rollback lógico/análise de falha, embora Flyway não execute rollback automático de versioned migrations.

---

# 12. Invariantes de banco versus domínio

## Banco deve garantir

- FKs válidas;
- uma pendência por atleta/profissional;
- um vínculo ativo por atleta/profissional;
- um acesso atual por temporada/profissional;
- uma associação atual temporada/campeonato;
- enum/string dentro do conjunto esperado quando possível via CHECK compatível;
- integridade de IDs e índices.

## Service/domínio deve garantir

- atleta realmente possui papel `ATLETA`;
- profissional possui papel `NUTRITIONISTA`, `TREINADOR` ou `COACH`;
- solicitante/destinatário corretos no vínculo;
- justificativas obrigatórias conforme transição;
- criador imutável;
- criador não cadastrado como acesso adicional;
- autorização de ADMINISTRACAO/CONSULTA;
- composição derivada de vínculo do criador;
- mudança estrutural somente com temporada `ATIVO`;
- reativação retorna ao estado imediatamente anterior ao cancelamento;
- histórico e atualização de status são atomicamente consistentes.

---

# 13. Decisão de desenho sobre estado anterior ao cancelamento

Para reativar `CANCELADA`, não criar campo `statusAnteriorCancelamento` como fonte autoritativa em `tbTemporada`.

Recomendação:

1. consultar a última transição efetiva para `CANCELADA` no histórico;
2. usar `statusOrigem` dessa transição;
3. validar que origem foi `ATIVO` ou `ENCERRADA`;
4. reativar para esse estado;
5. registrar nova transição no histórico.

Opcionalmente, um campo denormalizado pode existir para otimização, mas nunca deve substituir o histórico.

---

# 14. Ordem de implementação da Fase 2

1. enums da fase;
2. `VinculoProfissionalAtleta` + constraints;
3. `Temporada`;
4. `TemporadaStatusHistorico`;
5. transições de temporada;
6. `TemporadaProfissional` com histórico por intervalos;
7. `TemporadaAuthorizationService`;
8. `TemporadaCampeonato`;
9. endpoints/DTOs;
10. testes de concorrência e autorização;
11. integração com Fase 1 `Campeonato`.

## Critério de pronto

A Fase 2 estará pronta quando:

- vínculos atuais e históricos forem representados sem sobreposição indevida;
- composição da temporada puder ser derivada do criador sem `TemporadaAtleta`;
- criador e administradores possuírem autorização contextual correta;
- consultores respeitarem visibilidade restrita;
- acessos de temporada preservarem histórico temporal;
- campeonato puder ser associado/removido apenas em temporada `ATIVO`;
- transições de temporada forem atômicas e auditáveis;
- `CANCELADA` puder restaurar corretamente o estado anterior usando histórico;
- testes MySQL comprovarem unicidades e concorrência.
