# Refinamento técnico — Modelo físico da Fase 1

Status: desenho técnico consolidado para implementação após modernização da plataforma.

## Objetivo

Definir o modelo físico/JPA inicial para:

- `Organizador`;
- `Pais`;
- `Subdivisao`;
- `Categoria`;
- `Classe`;
- `Campeonato`;
- histórico e solicitações administrativas relacionadas a campeonato.

O desenho preserva `tbCompeticao.cdCompeticao` como identidade física do campeonato durante o MVP, enquanto o domínio Java e a API nova utilizam `Campeonato`.

## Convenções gerais

### IDs

Manter `Integer` + `AUTO_INCREMENT` para compatibilidade com o legado.

### Datas

Usar:

- `LocalDate` para datas esportivas (`dtInicio`, `dtFim`);
- `LocalDateTime` para auditoria e transições administrativas.

### Enums

Persistir enums novos como `VARCHAR` usando `EnumType.STRING`.

### Auditoria

Entidades mutáveis de catálogo/domínio devem possuir, no mínimo:

```text
dtCadastro
cdUsuarioCadastro
dtAlteracao
cdUsuarioAlteracao
```

Quando a autoria for derivada exclusivamente do usuário autenticado, evitar receber esses campos do cliente.

Históricos append-only registram a autoria própria do evento e não devem ser atualizados depois da inserção.

### Lock otimista

Adicionar `@Version` em `Campeonato` e nas solicitações administrativas mutáveis.

Catálogos centrais simples não exigem `@Version` inicialmente, salvo necessidade observada na implementação.

### Exclusão

Não usar exclusão física para registros de catálogo já referenciados.

`Organizador`, `Pais`, `Subdivisao`, `Categoria` e `Classe` usam inativação lógica.

`Campeonato` usa estado `ATIVO` / `CANCELADO`.

## 1. Organizador

### Tabela

```text
tbOrganizador
```

Campos:

```text
cdOrganizador          INT PK AUTO_INCREMENT
nmOrganizador          VARCHAR(250) NOT NULL
nmOrganizadorNormalizado VARCHAR(250) NOT NULL
flAtivo                BOOLEAN NOT NULL DEFAULT TRUE
dtCadastro             DATETIME NOT NULL
cdUsuarioCadastro      INT NOT NULL
dtAlteracao            DATETIME NULL
cdUsuarioAlteracao     INT NULL
```

### Constraints e índices

- PK `cdOrganizador`;
- índice em `flAtivo`;
- índice em `nmOrganizadorNormalizado`;
- unicidade lógica do nome normalizado ativo.

Como MySQL não possui partial unique index tradicional, a unicidade entre ativos deve ser implementada por estratégia compatível com MySQL.

Recomendação preferencial: coluna gerada de chave ativa ou coluna funcional equivalente controlada por migration.

Exemplo conceitual:

```text
ukOrganizadorAtivo = IF(flAtivo = 1, nmOrganizadorNormalizado, NULL)
UNIQUE(ukOrganizadorAtivo)
```

A sintaxe concreta depende da versão do MySQL validada no ambiente.

### JPA

Classe:

```text
Organizador
```

Não expor setter público irrestrito para `flAtivo`; ativação/inativação deve passar por caso de uso próprio.

## 2. País

### Tabela

```text
tbPais
```

Campos:

```text
cdPais             INT PK AUTO_INCREMENT
codigoIso2         CHAR(2) NOT NULL
codigoIso3         CHAR(3) NOT NULL
nmPais             VARCHAR(150) NOT NULL
flAtivo            BOOLEAN NOT NULL DEFAULT TRUE
dtCadastro         DATETIME NOT NULL
```

### Constraints

```text
UNIQUE(codigoIso2)
UNIQUE(codigoIso3)
```

Os códigos ISO são a identidade externa estável do catálogo.

### Gestão

No MVP, o catálogo é interno e pré-carregado. Não criar CRUD profissional.

Se existir endpoint administrativo, somente proprietário pode operar.

## 3. Subdivisão

### Tabela

```text
tbSubdivisao
```

Campos:

```text
cdSubdivisao       INT PK AUTO_INCREMENT
cdPais             INT NOT NULL
codigoIso          VARCHAR(10) NOT NULL
nmSubdivisao       VARCHAR(150) NOT NULL
flAtivo            BOOLEAN NOT NULL DEFAULT TRUE
dtCadastro         DATETIME NOT NULL
```

### FKs

```text
cdPais -> tbPais.cdPais
```

### Constraints

```text
UNIQUE(cdPais, codigoIso)
```

Adicionar índice de `cdPais` para consulta por país.

A aplicação deve validar que a subdivisão informada no campeonato pertence ao país selecionado. A FK isoladamente não garante a coerência entre `Campeonato.cdPais` e `Campeonato.cdSubdivisao`.

## 4. Categoria

### Tabela

```text
tbCategoria
```

Campos:

```text
cdCategoria          INT PK AUTO_INCREMENT
nmCategoria          VARCHAR(150) NOT NULL
nmCategoriaNormalizado VARCHAR(150) NOT NULL
flAtivo              BOOLEAN NOT NULL DEFAULT TRUE
dtCadastro           DATETIME NOT NULL
cdUsuarioCadastro    INT NOT NULL
dtAlteracao          DATETIME NULL
cdUsuarioAlteracao   INT NULL
```

### Unicidade

No máximo uma categoria ativa com o mesmo nome normalizado.

Usar a mesma estratégia de chave ativa condicional recomendada para `Organizador`.

### Normalização

A aplicação deve produzir uma representação estável para unicidade:

- `trim`;
- espaços internos repetidos reduzidos;
- caixa normalizada;
- política de acentos/pontuação definida uma única vez e coberta por teste.

Não depender implicitamente da collation do banco como única regra de domínio.

## 5. Classe

### Tabela

```text
tbClasse
```

Campos:

```text
cdClasse             INT PK AUTO_INCREMENT
cdCategoria          INT NOT NULL
nmClasse             VARCHAR(150) NOT NULL
nmClasseNormalizado  VARCHAR(150) NOT NULL
tipoClasse           VARCHAR(20) NOT NULL
flAtivo              BOOLEAN NOT NULL DEFAULT TRUE
dtCadastro           DATETIME NOT NULL
cdUsuarioCadastro    INT NOT NULL
dtAlteracao          DATETIME NULL
cdUsuarioAlteracao   INT NULL
```

Enum:

```text
TipoClasse
- COMUM
- OVERALL
```

### FKs

```text
cdCategoria -> tbCategoria.cdCategoria
```

### Unicidades

Regra 1:

```text
no máximo uma classe ativa semanticamente igual por categoria
```

Chave lógica:

```text
(cdCategoria, nmClasseNormalizado) entre ativos
```

Regra 2:

```text
no máximo uma classe ativa OVERALL por categoria
```

Para MySQL, implementar ambas com estratégia de coluna/chave condicional ou mecanismo equivalente validado por migration.

Exemplo conceitual para Overall:

```text
ukOverallAtivoCategoria = IF(flAtivo = 1 AND tipoClasse = 'OVERALL', cdCategoria, NULL)
UNIQUE(ukOverallAtivoCategoria)
```

### Imutabilidade estrutural após uso

Se `Classe` já estiver referenciada por algum `Resultado`:

- pode alterar apenas nome, se permitido pelo catálogo;
- não pode alterar `cdCategoria`;
- não pode alterar `tipoClasse`.

Essa regra pertence ao service/application layer e deve consultar existência histórica de resultado.

## 6. Campeonato sobre `tbCompeticao`

### Decisão de compatibilidade

Domínio Java:

```text
Campeonato
```

Tabela física:

```text
tbCompeticao
```

PK preservada:

```text
cdCompeticao
```

A entidade JPA nova `Campeonato` será a única entidade autoritativa apontando para `tbCompeticao`.

A antiga classe `Competicao` deve ser removida/renomeada na etapa de implementação. Não manter duas entidades JPA mapeadas simultaneamente para a mesma tabela.

## 7. Evolução de `tbCompeticao`

Campos legados atuais conhecidos:

```text
cdCompeticao
nmCompeticao
dtInicio
dtFim
local
federacao
```

Modelo físico alvo:

```text
cdCompeticao             INT PK AUTO_INCREMENT
nmCompeticao             VARCHAR(250) NOT NULL
nmCompeticaoNormalizado  VARCHAR(250) NOT NULL
cdOrganizador            INT NOT NULL
cdPais                   INT NOT NULL
cdSubdivisao             INT NULL
local                     VARCHAR(1000) NULL
dtInicio                 DATE NOT NULL
dtFim                    DATE NOT NULL
status                   VARCHAR(20) NOT NULL DEFAULT 'ATIVO'
cdCriador                INT NOT NULL
lockVersion              BIGINT NOT NULL DEFAULT 0
dtCadastro               DATETIME NOT NULL
cdUsuarioCadastro        INT NOT NULL
dtAlteracao              DATETIME NULL
cdUsuarioAlteracao       INT NULL
federacao                VARCHAR(250) NULL -- legado, não autoritativo durante transição
```

### FKs

```text
cdOrganizador -> tbOrganizador.cdOrganizador
cdPais        -> tbPais.cdPais
cdSubdivisao  -> tbSubdivisao.cdSubdivisao
cdCriador     -> tbUsuario.cdUsuario
```

Se `cdUsuarioCadastro` e `cdUsuarioAlteracao` forem usados como FK de auditoria, referenciar `tbUsuario`.

### Enum

```text
CampeonatoStatus
- ATIVO
- CANCELADO
```

### Check de datas

Regra obrigatória:

```text
dtFim >= dtInicio
```

Garantir em duas camadas:

1. validação de aplicação;
2. `CHECK` no MySQL quando suportado/validado pela versão efetivamente usada.

## 8. Identidade semântica do campeonato

Regra:

```text
(nmCompeticaoNormalizado,
 cdOrganizador,
 cdPais,
 cdSubdivisao,
 dtInicio)
```

Somente campeonatos `ATIVO` participam da unicidade ativa.

### Proteção no banco

A invariante não pode depender apenas de `SELECT exists` seguido de `INSERT`, pois há race condition.

Desenhar uma chave de unicidade ativa materializada/gerada que represente a identidade semântica.

Como `cdSubdivisao` pode ser nulo, a estratégia deve normalizar `NULL` de forma determinística na chave.

Exemplo conceitual:

```text
semanticKeyAtiva =
  IF(status = 'ATIVO',
     HASH/CONCAT(
       nmCompeticaoNormalizado,
       cdOrganizador,
       cdPais,
       COALESCE(cdSubdivisao, 0),
       dtInicio
     ),
     NULL)

UNIQUE(semanticKeyAtiva)
```

A implementação concreta deve evitar truncamento/colisão. Preferir chave composta gerada por colunas auxiliares determinísticas quando possível em vez de hash curto.

### Service

O service ainda deve consultar equivalentes para produzir erro funcional amigável e indicar o campeonato existente.

O banco é a última barreira para concorrência.

## 9. Organizador/localização válidos

Para criar/editar campeonato:

- `Organizador.flAtivo = true`;
- `Pais.flAtivo = true`;
- se `cdSubdivisao != null`, `Subdivisao.flAtivo = true` e `Subdivisao.cdPais = Campeonato.cdPais`;
- `dtFim >= dtInicio`;
- nome normalizado válido;
- não produzir equivalência ativa.

A inativação posterior de organizador/país/subdivisão não invalida campeonato histórico existente.

## 10. `CampeonatoStatusHistorico`

### Tabela

```text
tbCampeonatoStatusHistorico
```

Campos:

```text
cdHistorico          BIGINT PK AUTO_INCREMENT
cdCompeticao         INT NOT NULL
statusOrigem         VARCHAR(20) NOT NULL
statusDestino        VARCHAR(20) NOT NULL
justificativa        VARCHAR(1000) NOT NULL
cdResponsavel        INT NOT NULL
dtTransicao          DATETIME NOT NULL
origemOperacao       VARCHAR(40) NOT NULL
cdSolicitacaoStatus  BIGINT NULL
```

Enum de origem:

```text
DIRETA_PROFISSIONAL
APROVACAO_PROPRIETARIO
DIRETA_PROPRIETARIO
```

### Invariantes

- append-only;
- nenhuma atualização/deleção pelo fluxo comum;
- inserção ocorre na mesma transação da alteração de `Campeonato.status`;
- tentativa bloqueada/rejeitada não cria registro de transição efetiva.

### Índices

```text
INDEX(cdCompeticao, dtTransicao)
INDEX(cdResponsavel, dtTransicao)
```

## 11. `SolicitacaoAlteracaoStatusCampeonato`

### Tabela

```text
tbSolicitacaoAlteracaoStatusCompeticao
```

Campos mínimos:

```text
cdSolicitacao        BIGINT PK AUTO_INCREMENT
cdCompeticao         INT NOT NULL
statusOrigem         VARCHAR(20) NOT NULL
statusDestino        VARCHAR(20) NOT NULL
justificativa        VARCHAR(1000) NOT NULL
statusSolicitacao    VARCHAR(30) NOT NULL
cdSolicitante        INT NOT NULL
dtSolicitacao        DATETIME NOT NULL
cdResponsavelDecisao INT NULL
dtDecisao            DATETIME NULL
motivoDecisao        VARCHAR(1000) NULL
lockVersion          BIGINT NOT NULL DEFAULT 0
```

Enum recomendado:

```text
PENDENTE
APROVADA
REPROVADA
CANCELADA
```

No máximo uma solicitação `PENDENTE` por campeonato.

Essa unicidade condicional deve receber proteção de banco equivalente às demais regras de estado ativo.

Na aprovação:

1. lock/revalidar solicitação;
2. revalidar status atual do campeonato;
3. revalidar autorização;
4. em reativação, revalidar equivalência ativa;
5. alterar `Campeonato.status`;
6. inserir `CampeonatoStatusHistorico`;
7. concluir solicitação;
8. commit atômico.

## 12. `SolicitacaoAlteracaoCampeonato`

Usada para alteração cadastral de campeonato já utilizado.

### Tabela

```text
tbSolicitacaoAlteracaoCompeticao
```

Campos mínimos:

```text
cdSolicitacao        BIGINT PK AUTO_INCREMENT
cdCompeticao         INT NOT NULL
statusSolicitacao    VARCHAR(30) NOT NULL
justificativa        VARCHAR(1000) NOT NULL
snapshotAnterior     JSON NOT NULL
snapshotProposto     JSON NOT NULL
versaoBase           BIGINT NOT NULL
cdSolicitante        INT NOT NULL
dtSolicitacao        DATETIME NOT NULL
cdResponsavelDecisao INT NULL
dtDecisao            DATETIME NULL
motivoDecisao        VARCHAR(1000) NULL
lockVersion          BIGINT NOT NULL DEFAULT 0
```

### Snapshot

Usar JSON apenas como trilha administrativa de antes/depois e proposta, não como fonte autoritativa do campeonato.

Os campos efetivos continuam normalizados em `tbCompeticao`.

### Concorrência

`versaoBase` referencia o `Campeonato.lockVersion` observado ao criar a solicitação.

Na aprovação:

```text
if campeonato.lockVersion != versaoBase
    -> 409 Conflict / solicitação desatualizada
```

Também revalidar equivalência, datas, organizador e localização.

## 13. JPA de `Campeonato`

Modelo conceitual:

```text
@Entity
@Table(name = "tbCompeticao")
class Campeonato {
    Integer id;
    String nome;
    String nomeNormalizado;
    Organizador organizador;
    Pais pais;
    Subdivisao subdivisao;
    String local;
    LocalDate dataInicio;
    LocalDate dataFim;
    CampeonatoStatus status;
    Usuario criador;
    Long lockVersion;
    ...auditoria
}
```

### Relacionamentos

Preferir:

```text
@ManyToOne(fetch = LAZY)
```

para Organizador, Pais, Subdivisao, Criador.

Não mapear inicialmente coleções bidirecionais `Organizador -> Campeonatos`, `Categoria -> Resultados` etc. quando não forem necessárias para o caso de uso.

Isso reduz acoplamento e consultas acidentais.

## 14. DTOs da API nova

### Campeonato

Separar comandos por operação.

```text
CreateCampeonatoRequest
UpdateCampeonatoRequest
AlterarStatusCampeonatoRequest
SolicitarAlteracaoCampeonatoRequest
DecidirSolicitacaoCampeonatoRequest
CampeonatoResponse
```

### Catálogos

```text
OrganizadorRequest/Response
CategoriaRequest/Response
ClasseRequest/Response
PaisResponse
SubdivisaoResponse
```

Não permitir que cliente envie:

- `status` livre em create/update genérico;
- `cdCriador` arbitrário;
- auditoria;
- `lockVersion` como campo mutável de negócio.

Quando necessário para concorrência otimista de API, a versão pode ser enviada como precondition/versão esperada, mas nunca como valor a persistir diretamente.

## 15. Endpoints iniciais

### Campeonato

```text
POST   /campeonatos
GET    /campeonatos/{id}
GET    /campeonatos
PUT    /campeonatos/{id}                 -- apenas edição cadastral direta permitida
POST   /campeonatos/{id}/cancelamento
POST   /campeonatos/{id}/reativacao
POST   /campeonatos/{id}/solicitacoes-alteracao
GET    /campeonatos/{id}/historico-status
```

Quando campeonato utilizado exigir aprovação, o `PUT` direto deve retornar conflito/regra de negócio e orientar para o fluxo de solicitação; não deve criar solicitação implicitamente.

### Catálogos

Endpoints administrativos separados e protegidos por autoridade do proprietário.

Para país/subdivisão, priorizar leitura.

## 16. Compatibilidade REST legada

Endpoints atuais de `CompeticaoController` podem continuar temporariamente.

Estratégia:

```text
endpoint legado
    -> adapter/service de compatibilidade
        -> novo CampeonatoService
```

Não duplicar regra de negócio entre API antiga e nova.

O endpoint legado deve ser descontinuado gradualmente após migração do `fed-team`.

## 17. Ordem de migrations da Fase 1

Após baseline Flyway da base atual:

1. criar `tbOrganizador`;
2. criar `tbPais`;
3. criar `tbSubdivisao`;
4. criar `tbCategoria`;
5. criar `tbClasse`;
6. popular país/subdivisão;
7. evoluir `tbCompeticao` com novas colunas inicialmente nullable quando necessário para migração;
8. migrar/associar organizador e localização dos registros legados quando possível;
9. preencher normalizações/status/criador/auditoria segundo regra de migração definida;
10. somente após dados consistentes, promover `NOT NULL` obrigatórios;
11. criar constraints/índices de equivalência;
12. criar tabelas de solicitações;
13. criar `tbCampeonatoStatusHistorico`.

Evitar uma única migration gigantesca. Separar DDL, carga de catálogo, backfill e endurecimento de constraints.

## 18. Migração do campo legado `federacao`

`federacao` não deve continuar como fonte autoritativa.

Estratégia de migração:

- extrair valores distintos existentes;
- normalizar nomes;
- revisar colisões/variações textuais;
- criar `Organizador` correspondente somente quando a equivalência puder ser estabelecida com segurança;
- preencher `cdOrganizador` nos campeonatos migrados;
- preservar `federacao` temporariamente para auditoria/rollback;
- remover o campo apenas em etapa posterior e após consumidores legados deixarem de usá-lo.

Não criar organizadores duplicados automaticamente apenas por diferenças de caixa/espaços.

## 19. Migração de localização legada

O campo `local` atual é livre e não permite inferir com segurança país/subdivisão em todos os casos.

Portanto:

- não inferir país/estado por parsing arbitrário;
- criar processo de backfill determinístico quando houver fonte confiável;
- registros não reconstruíveis devem ser tratados explicitamente na estratégia de migração antes de tornar `cdPais` obrigatório para dados legados;
- `local` permanece como descrição textual complementar.

## 20. Testes obrigatórios da Fase 1

### Catálogos

- duplicidade normalizada de organizador ativo;
- duplicidade normalizada de categoria ativa;
- duplicidade de classe ativa por categoria;
- segundo Overall ativo na mesma categoria;
- item inativo não usado em novo lançamento futuro.

### Campeonato

- `dtFim < dtInicio` rejeitado;
- subdivisão de outro país rejeitada;
- organizador inativo rejeitado em novo campeonato;
- duplicidade semântica ativa rejeitada;
- equivalência concorrente: duas criações simultâneas, somente uma vence;
- criação de equivalente cancelado direciona para reativação;
- reativação bloqueada se outro equivalente ativo surgiu;
- reativação concorrente protegida;
- alteração de campeonato utilizado exige solicitação;
- solicitação desatualizada por `lockVersion` falha com 409;
- transição de status e histórico são atômicos;
- histórico não é mutável pelo fluxo comum.

Esses testes de persistência/concorrência devem executar contra MySQL via Testcontainers.

## 21. Decisões técnicas consolidadas

- Java usa `Campeonato`; banco mantém `tbCompeticao/cdCompeticao` durante o MVP;
- somente uma entidade JPA autoritativa mapeia `tbCompeticao`;
- normalização textual é explícita e testável;
- catálogos usam inativação lógica;
- enums novos persistidos como texto;
- relacionamentos JPA `ManyToOne` preferencialmente LAZY;
- não criar coleções bidirecionais sem necessidade;
- `Campeonato` possui `@Version`;
- equivalência ativa deve ser protegida no banco, não apenas no service;
- `CampeonatoStatusHistorico` é append-only e transacional com a mudança de status;
- solicitações administrativas possuem controle de concorrência;
- MySQL/Testcontainers é referência dos testes de constraints/locking;
- migrations são aditivas e endurecem constraints apenas após backfill consistente;
- `federacao` e `local` legados não devem gerar inferências arbitrárias durante migração.
