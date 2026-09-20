# Refinamento técnico — Implementação do MVP

Status: arquitetura de implementação em refinamento; domínio funcional consolidado.

## Objetivo

Traduzir as regras consolidadas do MVP para uma implementação incremental no `srv-team`, preservando compatibilidade com o legado sempre que isso reduzir risco sem comprometer as invariantes do novo domínio.

Este documento não substitui os refinamentos funcionais. Ele define como o domínio deve ser organizado tecnicamente em Spring Boot/JPA.

## Diagnóstico do projeto atual

O projeto atual utiliza:

- Spring Boot 3.2.2;
- Java 17;
- Spring Web;
- Spring Data JPA;
- Spring Validation;
- Spring Security;
- JWT com JJWT;
- MySQL em produção;
- H2 como dependência de desenvolvimento;
- estrutura em camadas por tipo: `controller`, `service`, `repository`, `model`, `dto`, `mapper`.

O domínio legado de pontuação é representado principalmente por:

- `Competicao` / `tbCompeticao`;
- `Competidores`;
- `Pontuacao`;
- `PontuacaoHist`.

Esse modelo não suporta adequadamente temporada, inscrição com workflow, resultado por categoria/classe, aprovação, penalidades específicas por temporada, autorização contextual e histórico de transições.

## Estratégia arquitetural

### Não reescrever o projeto inteiro

O MVP não deve provocar uma migração ampla e simultânea de todos os módulos existentes para arquitetura hexagonal.

A recomendação é evolução incremental para organização **por feature/domínio** somente nos novos módulos e nos módulos profundamente alterados pelo MVP.

Estrutura alvo conceitual:

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

Não é necessário mover imediatamente módulos antigos de treino, exercícios, menu etc. A migração de pacotes deve ser orientada por necessidade funcional, evitando refatoração sem valor para o MVP.

## Camadas dentro de cada feature

Manter separação clara:

```text
Controller
   -> Application/Service
      -> Repository
         -> JPA/DB
```

Regras:

- controller não contém regra de negócio;
- repository não decide autorização;
- mapper não consulta banco;
- service/application service coordena transação, autorização e invariantes;
- entidades não devem depender de DTOs;
- DTOs externos não devem ser reutilizados como entidades JPA.

Não criar abstrações adicionais apenas para simular arquitetura hexagonal quando houver somente uma implementação concreta.

## Transações

As operações de domínio com múltiplas invariantes devem possuir fronteira transacional explícita no service/use case.

Exemplos obrigatórios:

- criação/aprovação/cancelamento de `Inscricao`;
- lançamento/edição/aprovação/cancelamento de `Resultado`;
- reserva/liberação de colocação;
- associação/desassociação `TemporadaCampeonato`;
- transições de temporada;
- transições de campeonato + `CampeonatoStatusHistorico`;
- alteração de pontuação/penalidade + invalidação/recalculo da projeção.

Preferir `org.springframework.transaction.annotation.Transactional` para deixar a semântica Spring explícita.

## Modelo JPA proposto

### Entidades centrais novas

Criar gradualmente:

- `VinculoProfissionalAtleta`;
- `Temporada`;
- `TemporadaProfissional`;
- `TemporadaCampeonato`;
- `TemporadaPontuacao`;
- `TemporadaPenalidade`;
- `Organizador`;
- `Pais`;
- `Subdivisao`;
- `Categoria`;
- `Classe`;
- `Inscricao`;
- `Resultado`;
- `ResultadoHistorico`;
- `SolicitacaoAlteracaoCampeonato`;
- `SolicitacaoAlteracaoStatusCampeonato`;
- `CampeonatoStatusHistorico`.

Entidades de histórico devem ser append-only no fluxo ordinário.

### Ranking

Não criar entidade `Ranking` como fonte de verdade.

Ranking permanece projeção derivada de:

- temporada;
- vínculo;
- campeonato associado;
- inscrição;
- resultado;
- pontuação;
- penalidade;
- estado da conta.

Pode existir DTO/projection/query model específico de leitura.

### Relatórios

Não criar entidades esportivas específicas para PDF/Excel.

Relatórios devem consumir query models/projections derivadas das mesmas regras usadas na tela.

## Substituição de `Competicao` por `Campeonato`

A tabela física `tbCompeticao` será evoluída no MVP em vez de criar uma nova
tabela `tbCampeonato`. A identidade física permanece `tbCompeticao.cdCompeticao`,
mas o domínio Java autoritativo passa a utilizar `Campeonato`.

Não manter simultaneamente duas entidades JPA autoritativas apontando para
`tbCompeticao`. O modelo legado `Competicao` será substituído pelo novo modelo
`Campeonato`. Serviços, controllers, mappers, DTOs e testes que ainda consumirem
`Competicao` devem ser adaptados ou removidos no mínimo necessário para preservar
compilação e consistência arquitetural. Não criar adapter apenas para preservar
uma abstração que não possua mais função no domínio.

Endpoints legados que ainda precisarem continuar funcionando podem ser adaptados
para o novo domínio, mas não devem duplicar regras de negócio nem justificar a
manutenção de duas entidades JPA para a mesma tabela.

## Substituição de `Competidores`

`Competidores` não deve receber novas responsabilidades.

Destino conceitual:

```text
Competidores legado
        -> migração
Inscricao CONFIRMADA
```

Após migração e estabilização:

- novos fluxos usam exclusivamente `Inscricao`;
- `CompetidoresService`/`Repository` deixam de ser usados por novas funcionalidades;
- remoção física do legado ocorre somente quando consumidores antigos tiverem sido migrados.

## Substituição de `Pontuacao` e `PontuacaoHist`

`Pontuacao` atual não deve ser adaptada para representar simultaneamente regra de temporada e resultado esportivo.

Separação obrigatória:

```text
Resultado
= fato esportivo

TemporadaPontuacao / TemporadaPenalidade
= regra de interpretação

Ranking
= projeção
```

`PontuacaoHist` legado só deve ser migrado para `Resultado` quando categoria, classe, situação e colocação puderem ser reconstruídas sem inferência arbitrária.

Dados que não possam ser reconstruídos com segurança devem permanecer como histórico legado consultável, sem inventar `AUSENTE`, `DESCLASSIFICADO`, categoria ou classe.

## Enums

Criar enums de domínio específicos, preferencialmente persistidos como `STRING` para estados novos:

- `TemporadaStatus`;
- `TemporadaPermissao`;
- `InscricaoStatus`;
- `InscricaoOrigem`;
- `ResultadoStatus`;
- `SituacaoResultado`;
- `TipoClasse`;
- `TipoPenalidade`;
- `CampeonatoStatus`;
- `SolicitacaoStatus`;
- `OrigemTransicaoCampeonato`.

Evitar códigos numéricos sem necessidade para novos estados do domínio, pois tornam migration, leitura e auditoria menos transparentes.

O `EnumTpAcesso` existente pode continuar com código numérico por compatibilidade do cadastro de usuário.

## IDs e relacionamentos

Manter `Integer` para IDs existentes e novas entidades relacionadas ao legado no MVP, evitando migração transversal para UUID sem benefício imediato.

Nos relacionamentos JPA:

- evitar `EAGER` em coleções;
- preferir `LAZY`;
- evitar serializar entidades diretamente nos controllers;
- usar DTOs para impedir ciclos e consultas acidentais;
- não depender de cascade amplo (`CascadeType.ALL`) entre agregados independentes.

## Concorrência

### Mesmo registro

Usar `@Version` nas entidades que sofrem edição concorrente relevante, especialmente:

- `Resultado`;
- `Campeonato`;
- `Temporada`;
- solicitações administrativas quando aplicável.

### Invariantes entre registros diferentes

`@Version` não resolve:

- duas inscrições confirmadas concorrentes;
- duas pendências de inscrição;
- dois resultados ativos na mesma inscrição/categoria/classe;
- duas reservas da mesma colocação;
- campeonatos ativos semanticamente equivalentes.

Essas regras precisam de proteção no banco e/ou locking transacional específico.

Conflitos funcionais de concorrência devem resultar em `409 Conflict`.

## Constraints e índices mínimos

A modelagem física deve prever, no mínimo:

- índices das FKs;
- unicidade lógica de inscrição ativa/pendente por atleta/campeonato;
- unicidade de resultado ativo por inscrição/categoria/classe;
- proteção de colocação ativa por campeonato/categoria/classe/colocação;
- unicidade de `TemporadaPontuacao` por temporada/posição/tipoClasse;
- unicidade de `TemporadaPenalidade` por temporada/tipoPenalidade;
- unicidade de associação ativa temporada/campeonato;
- unicidade coerente de vínculos ativos/pendentes atleta/profissional;
- índice temporal dos vínculos por atleta/profissional/início/fim;
- índices de consulta de ranking por temporada/campeonato/inscrição/resultado.

Como MySQL não oferece índice parcial da mesma forma que PostgreSQL, as unicidades condicionais devem ser desenhadas especificamente para MySQL. Não presumir sintaxe de partial index.

## Migration de banco

O diretório `src/main/resources/db/migration` existe, mas o projeto precisa adotar explicitamente uma ferramenta de migration antes do novo domínio.

Recomendação: **Flyway**.

A partir da adoção:

- adicionar dependência Flyway;
- migrations versionadas passam a ser fonte de evolução de schema;
- produção não deve depender de `spring.jpa.hibernate.ddl-auto=update`;
- após estabilização, usar `ddl-auto=validate` ou equivalente nos ambientes controlados;
- nenhuma alteração de schema do MVP deve depender somente da inicialização do Hibernate.

### Estratégia de rollout

Preferir migration aditiva em fases:

1. criar novas tabelas/colunas sem remover legado;
2. popular catálogos e dados reconstruíveis;
3. migrar `Competidores -> Inscricao`;
4. migrar resultados históricos reconstruíveis;
5. publicar código novo lendo/escrevendo o modelo novo;
6. adaptar consumidores legados;
7. somente depois remover tabelas/colunas antigas quando comprovadamente sem uso.

Evitar migration destrutiva no primeiro deploy.

## Segurança e configuração

Há configuração sensível versionada no `application.properties` atual.

Antes de qualquer implantação do novo MVP:

- remover credenciais de banco do repositório;
- remover segredo JWT do repositório;
- rotacionar os valores atualmente expostos;
- carregar segredos por variável de ambiente/secret manager;
- separar configuração local, teste e produção;
- evitar log TRACE de dados potencialmente sensíveis em produção.

Essa ação é pré-requisito técnico, não melhoria opcional.

## Tratamento de erros

Substituir `RuntimeException` genérica dos novos fluxos por exceções de aplicação/domínio mapeadas centralmente com `@RestControllerAdvice`.

Mapa mínimo recomendado:

- recurso inexistente -> `404 Not Found`;
- payload/regra de validação -> `400 Bad Request`;
- autenticado sem autorização -> `403 Forbidden`;
- estado/invariante/conflito concorrente -> `409 Conflict`;
- erro inesperado -> `500 Internal Server Error` sem expor stack trace ao cliente.

Não retornar mensagens de banco ou Hibernate diretamente na API.

## Autorização

Autorização funcional deve permanecer no backend mesmo que o frontend esconda ações.

Criar componente central para políticas contextuais, evitando duplicar consultas e condicionais em cada service.

Exemplo conceitual:

```text
TemporadaAuthorizationService
CampeonatoAuthorizationService
InscricaoAuthorizationService
ResultadoAuthorizationService
RelatorioAuthorizationService
```

Esses componentes podem reutilizar consultas comuns, mas cada operação continua responsável por validar seu caso de uso específico.

Não usar apenas `EnumTpAcesso` para decidir autorização contextual: papel global e permissão na temporada são dimensões distintas.

## DTOs e API

Os endpoints novos devem usar DTOs específicos de comando e resposta.

Evitar um DTO único de CRUD para entidades com workflows complexos.

Exemplo para Resultado:

```text
CreateResultadoRequest
UpdateResultadoPendenteRequest
AprovarResultadoRequest
ReprovarResultadoRequest
CorrigirResultadoRequest
CancelarResultadoRequest
ResultadoResponse
```

A API deve expressar ações do domínio, em vez de permitir `PUT` genérico alterando arbitrariamente qualquer status/campo.

## Ordem recomendada de implementação

### Fase 0 — fundação

- configuração externa de segredos;
- Flyway;
- profiles/configuração por ambiente;
- exception handler central;
- auditoria base;
- utilitários de usuário autenticado;
- convenções de enums e timestamps.

### Fase 1 — catálogos e campeonato

- Organizador;
- País/Subdivisão;
- Categoria/Classe;
- evolução `Competicao -> Campeonato`;
- equivalência;
- status/histórico;
- solicitações administrativas.

### Fase 2 — vínculo e temporada

- `VinculoProfissionalAtleta`;
- `Temporada`;
- `TemporadaProfissional`;
- `TemporadaCampeonato`;
- transições e autorização contextual.

### Fase 3 — regras esportivas

- `TemporadaPontuacao`;
- `TemporadaPenalidade`.

### Fase 4 — inscrição

- workflow completo;
- autorização;
- concorrência;
- migração de `Competidores`.

### Fase 5 — resultado

- workflow;
- `ResultadoHistorico`;
- reserva de colocação;
- aprovação/versionamento;
- intervenção do proprietário;
- migração reconstruível de `PontuacaoHist`.

### Fase 6 — ranking e relatórios

- query service de ranking;
- projections;
- visibilidade;
- relatório de campeonato;
- relatório de temporada;
- PDF/Excel.

### Fase 7 — compatibilidade e limpeza

- adaptar endpoints legados necessários;
- remover código legado sem consumidores;
- remover `ddl-auto=update` definitivamente;
- validar migration limpa e migration sobre base existente.

## Estratégia de testes

Prioridade:

1. testes unitários de regras puras;
2. testes de integração JPA/MySQL para constraints e locking;
3. testes de autorização contextual;
4. testes de API dos workflows;
5. testes de migration sobre snapshot realista do banco legado.

H2 não deve ser a única validação para regras de concorrência/constraint que dependem de MySQL.

Casos críticos obrigatórios incluem:

- reserva concorrente da mesma colocação;
- criação concorrente de inscrição pendente/confirmada;
- alteração de resultado com `nrVersao` desatualizada;
- reativação concorrente de campeonato equivalente;
- resultado compartilhado contribuindo diferentemente em duas temporadas;
- `ENCERRADA` aceitando inscrição/resultado tardio mas recusando alteração estrutural;
- `CANCELADA` não servindo de contexto ordinário de autorização;
- filtro `CONSULTA` sem renumerar ranking.

## Princípios de implementação

- domínio consolidado prevalece sobre CRUD legado;
- nenhuma regra crítica depende somente do frontend;
- nenhuma unicidade crítica depende somente de `exists()` antes do `save()`;
- histórico não é apagado para simplificar estado corrente;
- pontuação não é copiada para `Resultado`;
- ranking não vira fonte autoritativa;
- migrations devem ser reversíveis operacionalmente por rollout/backup, não por apagar histórico do domínio;
- priorizar implementação incremental e verificável em vez de refatoração transversal do projeto inteiro.

## Gate 3 / Slice 3.3 — núcleo de Campeonato e identidade semântica

### Decisões fechadas

- `tbCompeticao` não possui dados legados que precisem ser preservados ou
  migrados no contexto do MVP;
- não implementar backfill;
- não inferir Organizador a partir de `federacao`;
- não inferir País/Subdivisão a partir de `local`;
- `federacao` pode ser removida se a análise dos consumidores atuais confirmar
  que não há dependência;
- a migration será `V20260917__evolve_competicao_for_campeonato.sql`;
- a única entidade JPA autoritativa para `tbCompeticao` será `Campeonato`;
- `Competicao` será substituída, com adaptação ou remoção mínima de seus
  consumidores;
- o slice contém somente o núcleo persistente, a identidade semântica e as
  invariantes estruturais de Campeonato.

### Escopo do slice

Inclui:

- `Campeonato`;
- `CampeonatoStatus` com `ATIVO` e `CANCELADO`;
- repository;
- consultas de equivalência semântica independentes do status e específicas
  para `ATIVO`;
- nome normalizado persistido com `NomeCatalogoNormalizer`;
- Organizador, País, Subdivisão opcional e usuário criador;
- auditoria necessária;
- datas, status corrente e `@Version`;
- identidade semântica e unicidade concorrente;
- validação de catálogos ativos e coerência País/Subdivisão;
- testes unitários e MySQL/Testcontainers.

Não inclui:

- `CampeonatoStatusHistorico`;
- `SolicitacaoAlteracaoStatusCampeonato`;
- `SolicitacaoAlteracaoCampeonato`;
- workflow administrativo completo de cancelamento/reativação;
- Temporada, TemporadaCampeonato, Vínculo, Inscrição, Resultado, Ranking,
  pontuação, relatórios ou APIs novas completas.

### Migration e integração

A migration pode estabelecer diretamente as novas invariantes obrigatórias,
pois não existe dado legado a ser preservado no contexto aprovado. Migrations
anteriores são imutáveis. A tabela e a PK físicas permanecem `tbCompeticao` e
`cdCompeticao`.

O limite do slice está fechado. Histórico, solicitações e autorização
contextual serão refinados em slices posteriores, sem antecipar dependências de
Temporada, Inscrição ou Resultado.

A `UNIQUE` de identidade ativa continua sendo a garantia concorrente do banco
somente para registros `ATIVO`. Ela não substitui a consulta do repository
independente de status, necessária para que o futuro caso de uso bloqueie
recadastro de equivalente `CANCELADO` e revalide reativação sem antecipar o
workflow administrativo neste slice.
