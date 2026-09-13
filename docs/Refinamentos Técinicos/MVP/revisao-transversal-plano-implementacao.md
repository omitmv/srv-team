# Revisão transversal final — plano executável de implementação do MVP

Versão: 1.0  
Status: consolidação técnica das Fases 1–7 antes da implementação

## 1. Objetivo

Este documento consolida os refinamentos funcionais e técnicos do MVP em uma ordem única de implementação. Ele não substitui os documentos específicos de cada fase; funciona como mapa de execução, dependências, critérios de aceite e pontos de controle.

A implementação deve preservar três separações fundamentais:

```text
Resultado = fato esportivo
TemporadaPontuacao / TemporadaPenalidade = interpretação vigente
Ranking = projeção derivada
```

```text
Campeonato = fato esportivo compartilhado
Temporada = contexto administrativo/esportivo
Relatórios = read models derivados
```

```text
VinculoProfissionalAtleta = relacionamento real
TemporadaProfissional = autorização contextual adicional
```

## 2. Baseline técnico obrigatório

Antes dos novos aggregates:

- Java 21 LTS;
- Spring Boot 4.1.1;
- Maven Wrapper 3.9.16;
- MySQL como banco autoritativo;
- Flyway como única fonte de evolução de schema;
- Hibernate com `ddl-auto=validate`;
- Spring Data JPA;
- Spring Security;
- Testcontainers MySQL para testes de persistência, migrations e concorrência;
- Maven Enforcer para Java/Maven mínimos;
- DTOs separados de entidades;
- enums persistidos como STRING;
- `BigDecimal` para score/penalidade;
- `@Version` apenas como proteção de atualização da mesma linha, nunca como substituto de constraints concorrentes entre linhas distintas.

Segredos atualmente existentes em arquivos versionados devem ser externalizados e rotacionados antes de qualquer rollout produtivo.

## 3. Arquitetura de pacotes

Manter package-by-feature, sem reescrever o legado inteiro para arquitetura hexagonal.

```text
com.example.srvteam
 ├── campeonato
 │    ├── controller
 │    ├── dto
 │    ├── model
 │    ├── repository
 │    ├── service
 │    └── mapper
 ├── temporada
 ├── inscricao
 ├── resultado
 ├── ranking
 ├── vinculo
 ├── catalogo
 ├── relatorio
 └── shared
      ├── exception
      ├── security
      ├── audit
      └── persistence
```

Fluxo esperado:

```text
Controller -> Application/Service -> Repository -> JPA/MySQL
```

Não colocar regra de negócio em controller, mapper ou repository.

## 4. Ordem global de implementação

A ordem recomendada é:

1. modernização da plataforma e build;
2. Flyway + baseline do schema existente;
3. catálogos e Campeonato;
4. VinculoProfissionalAtleta;
5. Temporada + histórico de status;
6. TemporadaProfissional;
7. TemporadaCampeonato;
8. TemporadaPontuacao e TemporadaPenalidade + auditoria;
9. Inscricao;
10. Resultado + histórico + reserva de colocação;
11. Ranking;
12. Relatórios e exportações;
13. adapters de compatibilidade para endpoints legados;
14. remoção controlada de estruturas legadas somente após validação de migração.

A implementação deve ser incremental, mantendo a aplicação executável e testável após cada bloco.

## 5. Ordem recomendada das migrations Flyway

Os números exatos devem respeitar a sequência já existente no repositório. A ordem lógica deve ser preservada.

### Bloco A — baseline e catálogos

```text
Vxxx__baseline_existing_schema.sql
Vxxx__create_organizador.sql
Vxxx__create_pais.sql
Vxxx__create_subdivisao.sql
Vxxx__create_categoria.sql
Vxxx__create_classe.sql
Vxxx__alter_competicao_for_campeonato_model.sql
Vxxx__create_campeonato_status_historico.sql
Vxxx__create_campeonato_administrative_requests.sql
Vxxx__create_phase1_indexes_and_constraints.sql
```

### Bloco B — vínculo e temporada

```text
Vxxx__create_vinculo_profissional_atleta.sql
Vxxx__create_temporada.sql
Vxxx__create_temporada_status_historico.sql
Vxxx__create_temporada_profissional.sql
Vxxx__create_temporada_campeonato.sql
Vxxx__create_phase2_indexes_and_constraints.sql
```

### Bloco C — configuração esportiva da temporada

```text
Vxxx__create_temporada_pontuacao.sql
Vxxx__create_temporada_pontuacao_historico.sql
Vxxx__create_temporada_penalidade.sql
Vxxx__create_temporada_penalidade_historico.sql
Vxxx__create_phase3_indexes_and_constraints.sql
```

### Bloco D — inscrição

```text
Vxxx__create_inscricao.sql
Vxxx__create_inscricao_historico.sql
Vxxx__create_phase4_indexes_and_constraints.sql
Vxxx__migrate_competidores_to_inscricao.sql
```

A migration de dados deve ser idempotente do ponto de vista lógico e produzir relatório de divergências quando não for possível converter um registro com segurança.

### Bloco E — resultado

```text
Vxxx__create_resultado.sql
Vxxx__create_resultado_historico.sql
Vxxx__create_resultado_colocacao_reserva.sql
Vxxx__create_phase5_indexes_and_constraints.sql
Vxxx__migrate_pontuacao_hist_to_resultado.sql
```

A migração de `PontuacaoHist` não deve inventar Categoria, Classe, Inscricao, colocação ou situação quando os dados legados não permitirem reconstrução inequívoca.

### Bloco F — read side

Ranking e relatórios não exigem tabela autoritativa no MVP. Criar somente índices adicionais comprovadamente necessários após análise de plano de execução.

## 6. Aggregates e responsabilidades

### Campeonato

Aggregate compartilhado e independente de Temporada.

Responsabilidades:

- identidade semântica;
- organizador;
- localização normalizada;
- datas;
- status;
- histórico de transição;
- regras de edição conforme existência de Inscricao/Resultado.

Não colocar score de temporada em Campeonato.

### VinculoProfissionalAtleta

Fonte de verdade do relacionamento real entre atleta e profissional.

Responsabilidades:

- solicitação;
- aprovação/reprovação;
- encerramento;
- vigência histórica;
- unicidade de vínculo pendente/ativo.

### Temporada

Aggregate administrativo central.

Responsabilidades:

- status;
- criador imutável;
- transições;
- composição derivada;
- contexto de autorização;
- configuração esportiva.

### Inscricao

Aggregate de participação do atleta no Campeonato.

Responsabilidades:

- workflow PENDENTE/CONFIRMADA/REPROVADA/CANCELADA;
- reentrada por nova inscrição;
- associação atleta + campeonato;
- base de Resultado.

### Resultado

Aggregate do fato esportivo por Inscricao + Categoria + Classe.

Responsabilidades:

- situação;
- colocação quando aplicável;
- aprovação do atleta;
- correção administrativa;
- cancelamento lógico;
- histórico;
- reserva concorrente de colocação.

Resultado não persiste pontuação de temporada.

## 7. Matriz de autorização consolidada

### Proprietário global

Pode:

- administrar catálogos globais;
- corrigir/cancelar Resultado aprovado;
- aprovar operações administrativas sensíveis de Campeonato quando exigidas;
- consultar globalmente conforme regras administrativas.

### Criador da Temporada

Possui autoridade implícita e permanente sobre sua Temporada.

Não deve existir linha redundante em `TemporadaProfissional` para o criador.

### TemporadaProfissional — ADMINISTRACAO

Pode administrar a temporada no mesmo nível contextual do criador, exceto poderes explicitamente reservados ao proprietário global.

### TemporadaProfissional — CONSULTA

É leitura contextual restrita.

Para dados individuais de atleta, exige vínculo profissional-atleta ativo próprio quando a regra funcional assim determinar.

### Regra central

```text
isSeasonAdmin = actor == temporada.cdCriador
             OR current ADMINISTRACAO access exists
```

### Inscricao e Resultado

A autorização contextual exige uma Temporada que:

```text
status IN (ATIVO, ENCERRADA)
AND campeonato está atualmente associado à temporada
AND atleta integra a composição da temporada
AND ator é criador ou ADMINISTRACAO
```

`CONSULTA` não cria/edita Inscricao ou Resultado.

Vínculo profissional-atleta isolado não concede permissão de lançamento.

## 8. Composição atual versus elegibilidade histórica

Essa distinção deve aparecer explicitamente no código.

### Composição atual

Atleta integra a Temporada hoje quando possui vínculo ATIVO com o criador da temporada.

### Elegibilidade histórica de um Resultado

O vínculo com o criador deve estar vigente na data de referência do Campeonato:

```text
dtInicioVinculo <= campeonato.dtInicio
AND (
  dtEncerramentoVinculo IS NULL
  OR dtEncerramentoVinculo >= campeonato.dtInicio
)
```

Não usar apenas `status = ATIVO` para decidir contribuição histórica.

## 9. Estratégia de concorrência e locking

### Princípio

`@Version` protege conflito de edição da mesma linha.

Ele não protege:

- duas novas inscrições concorrentes;
- dois vínculos ativos criados em paralelo;
- duas associações atuais iguais;
- duas regras de pontuação para a mesma chave;
- duas reservas da mesma colocação.

### Vínculo

Usar constraints condicionais MySQL para no máximo um PENDENTE e um ATIVO por par atleta/profissional.

Erro residual de unique constraint deve ser convertido em `409 CONFLICT`.

### Temporada

Transições de status:

- lock pessimista ou equivalente na Temporada;
- validar transição;
- atualizar status;
- inserir histórico;
- commit único.

### TemporadaProfissional

Para promoção, rebaixamento ou remoção:

- bloquear acesso corrente;
- fechar `dtFim`;
- criar nova linha quando aplicável;
- garantir uma única linha corrente por temporada/profissional.

### TemporadaCampeonato

Garantir apenas uma associação corrente para o par temporada/campeonato.

### Pontuação e penalidade

Mutação somente com temporada ATIVO.

Lock da temporada deve serializar alterações estruturais/configuracionais relevantes.

### Inscricao

Usar lock lógico/transacional sobre atleta+campeonato, combinado com constraints de banco que impeçam estados correntes incompatíveis.

### Resultado

A colocação deve possuir reserva física própria:

```text
UNIQUE (
  cdCampeonato,
  cdCategoria,
  cdClasse,
  colocacao
)
```

`PENDENTE_APROVACAO` já reserva posição.

Aprovação mantém reserva.

Reprovação/cancelamento libera reserva.

Conflitos concorrentes retornam 409.

## 10. Invariantes que devem existir no banco

Preferir DB constraint para invariantes de integridade estrutural que independem de contexto complexo.

Obrigatórias:

- FKs;
- uma associação atual Temporada-Campeonato;
- um acesso atual Temporada-Profissional;
- unicidade de regras de pontuação por `(temporada, posicao, tipoClasse)`;
- unicidade de penalidade por `(temporada, tipoPenalidade)`;
- unicidade de reserva de colocação;
- no máximo um vínculo PENDENTE e um ATIVO por par;
- `posicao > 0` onde aplicável;
- checks de enum/status suportados pela versão alvo do MySQL.

Regras contextuais de autorização e workflow continuam na aplicação.

## 11. Histórico e auditoria

Históricos administrativos são append-only.

Não atualizar registros históricos.

Históricos mínimos:

- CampeonatoStatusHistorico;
- TemporadaStatusHistorico;
- TemporadaPontuacaoHistorico;
- TemporadaPenalidadeHistorico;
- InscricaoHistorico;
- ResultadoHistorico.

Todo histórico deve registrar, conforme contexto:

- entidade alvo;
- ação/transição;
- valor anterior relevante;
- valor posterior relevante;
- responsável;
- justificativa quando obrigatória;
- timestamp.

A alteração e a gravação do histórico devem ocorrer na mesma transação.

## 12. Configuração esportiva e retroatividade

A configuração atual da temporada é autoritativa.

Não versionar regra esportiva por Resultado no MVP.

### CLASSIFICADO

```text
Resultado.colocacao
+ Classe.tipoClasse
-> TemporadaPontuacao atual
```

Ausência de regra = impacto zero.

### DESCLASSIFICADO

Usa `TemporadaPenalidade.DESCLASSIFICACAO` atual.

Ausência de regra = zero.

### AUSENTE

Usa `TemporadaPenalidade.AUSENCIA` atual.

Ausência de regra = zero.

Cada Resultado aprovado contribui independentemente.

Uma alteração de regra afeta retroativamente todos os Resultados elegíveis da temporada, sem alterar o fato esportivo armazenado.

## 13. Ranking

Ranking é read model dinâmico.

Pipeline conceitual:

```text
1. validar temporada e autorização
2. obter atletas da composição corrente
3. obter campeonatos associados relevantes
4. obter inscrições confirmadas/não canceladas
5. obter resultados aprovados/não cancelados
6. validar contribuição histórica pelo vínculo na data do campeonato
7. aplicar configuração atual de pontuação/penalidade
8. somar por atleta
9. calcular posição sobre o universo completo
10. aplicar filtro de visibilidade do observador
```

Empate usa ranking de competição:

```text
100, 100, 80, 70, 70, 50
  1,   1,  3,  4,  4,  6
```

Usar semântica equivalente a `RANK()`, não `DENSE_RANK()`.

Ranking pode ficar negativo.

Temporada CANCELADA não produz ranking ativo.

Temporada ENCERRADA continua produzindo ranking dinâmico.

## 14. Relatórios e exportações

Não criar lógica distinta por formato.

Fluxo:

```text
Authorization
  -> Query Service
  -> Read Model
      -> API/tela
      -> PDF exporter
      -> XLSX exporter
```

Exporters não acessam repositories.

### Relatório de Campeonato

Mostra fatos esportivos do Campeonato.

Não atribuir score universal.

Uma Inscricao CONFIRMADA sem Resultado aprovado pode aparecer como `Sem resultado`.

Nunca inferir AUSENTE.

### Relatório de Temporada

Reutiliza exatamente o mesmo motor/projeção do ranking.

Não duplicar lógica de cálculo.

### Visibilidade

A posição deve ser calculada antes do filtro final do observador.

Exemplo: se o usuário pode ver somente atletas que estão em 2º e 4º, retornar 2º e 4º, nunca renumerar para 1º e 2º.

### Exportação

Revalidar autorização imediatamente antes de obter o snapshot de dados.

A transação de leitura deve terminar antes da renderização pesada do PDF/XLSX.

## 15. Serviços transversais recomendados

### `TemporadaAuthorizationService`

```text
canAdministerSeason
canViewSeason
canManageSeasonAccess
canChangeSeasonStructure
canOperateAthleteInSeason
```

Não transformar esse serviço em um "god service" contendo regras internas completas de Inscricao/Resultado.

### `ResultadoImpactCalculator`

Componente puro e determinístico.

Entrada:

- Resultado elegível;
- tipo da Classe;
- configuração atual da Temporada.

Saída:

- impacto `BigDecimal`.

Não persiste nada.

### `RankingQueryService`

Responsável pela orquestração da projeção do ranking.

### `RelatorioCampeonatoQueryService`

Responsável pelo read model factual de Campeonato.

### `RelatorioTemporadaQueryService`

Deve reutilizar o cálculo de Ranking, não reimplementá-lo.

## 16. Tratamento de erros HTTP

Padronizar em `RestControllerAdvice`.

Diretriz:

```text
400 BAD_REQUEST
- payload inválido
- transição semanticamente inválida quando o recurso existe e não há conflito de estado concorrente

401 UNAUTHORIZED
- ausência/falha de autenticação

403 FORBIDDEN
- ator autenticado sem autorização contextual

404 NOT_FOUND
- recurso inexistente ou ocultado deliberadamente pela política de segurança

409 CONFLICT
- optimistic locking
- unique constraint concorrente
- colocação já reservada
- vínculo corrente conflitante
- associação corrente duplicada
- estado corrente incompatível com operação concorrente

422 UNPROCESSABLE_ENTITY (opcional, se padronizado no projeto)
- regra de domínio semanticamente rejeitada quando houver valor em distinguir de 400
```

Não depender da mensagem textual do MySQL para classificar conflitos; mapear por constraint conhecida.

## 17. Plano de rollout

### Etapa 1 — infraestrutura

- atualizar Java/Boot/Maven Wrapper;
- introduzir Flyway;
- configurar Testcontainers;
- externalizar secrets;
- estabilizar CI.

### Etapa 2 — schema aditivo

Criar novas tabelas/colunas sem remover imediatamente estruturas legadas.

### Etapa 3 — implementação nova

Adicionar novos serviços/endpoints por feature.

### Etapa 4 — migração dos dados

Executar migrations de Competidores e PontuacaoHist com validações antes/depois.

### Etapa 5 — compatibilidade

Endpoints legados devem delegar para a nova camada quando necessário.

Evitar dual-write prolongado.

Se um período transitório de dual-read/compatibilidade for inevitável, definir explicitamente qual fonte é autoritativa.

### Etapa 6 — validação

Comparar dados legados x novos read models em ambiente controlado.

### Etapa 7 — corte

Desabilitar escrita legada.

### Etapa 8 — limpeza

Remover estruturas legadas somente após ciclo de estabilização e confirmação de rollback seguro.

## 18. Testes obrigatórios

### Unitários

- transições de status;
- autorização contextual;
- elegibilidade histórica;
- ResultadoImpactCalculator;
- score ausente = zero;
- penalidade ausente = zero;
- ranking negativo;
- empate `1,1,3`;
- filtros de CONSULTA sem renumeração.

### Persistência com MySQL/Testcontainers

- todas as migrations do zero;
- baseline sobre cópia representativa de schema legado;
- constraints condicionais;
- generated columns usadas nas unicidades;
- FKs;
- precisão DECIMAL(10,3);
- queries temporais de vínculo;
- índices críticos.

### Concorrência

Obrigatórios testes reais com transações simultâneas para:

- dois vínculos ATIVO do mesmo par;
- dois vínculos PENDENTE;
- dois acessos atuais à mesma temporada/profissional;
- duas associações atuais temporada/campeonato;
- duas regras da mesma chave;
- duas inscrições conflitantes;
- duas reservas da mesma colocação;
- duas correções concorrentes do mesmo Resultado;
- duas transições concorrentes de Temporada.

### Integração

Cobrir fluxos completos:

```text
vínculo -> temporada -> associação campeonato -> inscrição -> resultado -> aprovação -> ranking -> relatório
```

### Autorização

Criar matriz de testes por papel:

- proprietário;
- criador;
- ADMINISTRACAO;
- CONSULTA com vínculo;
- CONSULTA sem vínculo;
- profissional apenas com vínculo;
- usuário sem contexto.

## 19. Divergências esperadas entre legado e novo modelo

Durante a implementação, não adaptar o novo domínio para reproduzir automaticamente inconsistências do legado.

Pontos prováveis de divergência:

- `Competidores` não representa corretamente o ciclo completo de Inscricao;
- `PontuacaoHist` pode misturar fato esportivo e score derivado;
- Campeonato legado pode possuir campos denormalizados de federação/localização;
- ausência de Categoria/Classe estruturadas em dados históricos;
- possíveis registros sem informação suficiente para reconstrução;
- permissões antigas podem não possuir equivalência direta com autorização contextual da Temporada.

Para cada divergência, classificar:

```text
MIGRAVEL_AUTOMATICAMENTE
MIGRAVEL_COM_REGRA_EXPLICITA
REQUER_REVISAO_MANUAL
NAO_MIGRAVEL_SEM_INVENTAR_DADO
```

Nunca preencher lacunas históricas por suposição silenciosa.

## 20. Critérios de aceite antes de iniciar endpoints novos em produção

O bloco é considerado pronto para rollout quando:

1. todas as migrations executam em MySQL real via Testcontainers;
2. `ddl-auto=validate` passa;
3. não há secret produtivo versionado;
4. constraints concorrentes possuem testes reproduzíveis;
5. autorização possui testes por matriz de papéis;
6. não existe score autoritativo duplicado em Resultado;
7. ranking e relatório de temporada usam a mesma regra de impacto;
8. exportação usa o mesmo read model da tela;
9. migrations de legado possuem contagem antes/depois e relatório de exceções;
10. endpoints legados que permanecerem ativos têm estratégia explícita de compatibilidade.

## 21. Sequência sugerida de PRs/commits de implementação

Evitar um único PR gigantesco.

```text
PR 1  - platform modernization + wrapper + CI
PR 2  - Flyway baseline + Testcontainers
PR 3  - catalogs + Campeonato physical model
PR 4  - VinculoProfissionalAtleta
PR 5  - Temporada + status history
PR 6  - TemporadaProfissional + authorization service
PR 7  - TemporadaCampeonato
PR 8  - scoring/penalty configuration + audit
PR 9  - Inscricao + Competidores migration
PR 10 - Resultado + placement reservation + history
PR 11 - PontuacaoHist migration
PR 12 - Ranking query projection
PR 13 - reports/read models
PR 14 - PDF/XLSX exporters
PR 15 - legacy adapters + cutover preparation
PR 16 - cleanup after stabilization
```

Cada PR deve incluir suas migrations, testes de integração e documentação necessária para operar a mudança.

## 22. Decisões deliberadamente não adotadas no MVP

Para evitar overengineering:

- não reescrever o projeto inteiro para arquitetura hexagonal;
- não usar event sourcing;
- não versionar regra esportiva por Resultado;
- não persistir Ranking como fonte de verdade;
- não introduzir Kafka apenas para invalidação de ranking;
- não criar `TemporadaAtleta`;
- não duplicar criador em `TemporadaProfissional`;
- não armazenar score calculado como verdade no Resultado;
- não usar H2 como prova de integridade das constraints MySQL;
- não usar Maven 4 RC como baseline produtivo;
- não criar abstrações genéricas de workflow sem necessidade concreta.

## 23. Próximo passo técnico

Com esta revisão transversal, o refinamento arquitetural do MVP está suficientemente fechado para iniciar implementação.

A primeira execução deve ser a modernização técnica do projeto, seguida da introdução de Flyway/Testcontainers. Somente depois deve começar a criação das novas entidades do domínio.

A implementação deve seguir o plano acima mantendo cada etapa compilável, migrável e coberta por testes antes de avançar para a próxima dependência.
