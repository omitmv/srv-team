# Refinamento técnico — Modelo físico Fase 4: Inscrição

Status: modelagem técnica consolidada para implementação do MVP.

## Objetivo

Traduzir o refinamento funcional de `Inscricao` para um modelo físico/JPA seguro em MySQL, incluindo:

- ciclo de vida;
- autorização contextual;
- concorrência;
- unicidades condicionais;
- cancelamento;
- inscrição tardia;
- integração com `Resultado`;
- migração de `Competidores`.

## Responsabilidade do agregado

`Inscricao` representa a participação do atleta em um campeonato.

Não pertence a uma temporada específica.

A mesma inscrição pode ser interpretada por múltiplas temporadas associadas ao campeonato.

`Inscricao` não deve armazenar categoria, classe, colocação, situação de resultado ou pontuação.

## Entidade `Inscricao`

Tabela sugerida:

```text
tbInscricao
```

Campos propostos:

```text
cdInscricao                INT PK AUTO_INCREMENT
cdAtleta                   INT NOT NULL
cdCompeticao               INT NOT NULL
status                     VARCHAR(30) NOT NULL
origem                     VARCHAR(40) NOT NULL
cdSolicitante              INT NULL
dtSolicitacao              DATETIME(6) NOT NULL
cdResponsavelDecisao       INT NULL
dtDecisao                  DATETIME(6) NULL
motivoReprovacao           VARCHAR(1000) NULL
cdResponsavelCancelamento  INT NULL
dtCancelamento             DATETIME(6) NULL
motivoCancelamento         VARCHAR(1000) NULL
lockVersion                BIGINT NOT NULL DEFAULT 0
dtCadastro                 DATETIME(6) NOT NULL
dtAtualizacao              DATETIME(6) NOT NULL
```

FKs:

```text
cdAtleta      -> tbUsuario.cdUsuario
cdCompeticao  -> tbCompeticao.cdCompeticao
cdSolicitante -> tbUsuario.cdUsuario
cdResponsavelDecisao -> tbUsuario.cdUsuario
cdResponsavelCancelamento -> tbUsuario.cdUsuario
```

JPA:

```text
Inscricao
- @Entity
- @Table(name = "tbInscricao")
- @Version em lockVersion
- EnumType.STRING para status/origem
- relações ManyToOne LAZY
```

Não expor entidade diretamente na API.

## Status

Enum:

```text
InscricaoStatus
- PENDENTE
- CONFIRMADA
- REPROVADA
- CANCELADA
```

Transições ordinárias:

```text
PENDENTE -> CONFIRMADA
PENDENTE -> REPROVADA
CONFIRMADA -> CANCELADA
```

Uma inscrição cancelada não volta para `CONFIRMADA`.

Nova tentativa futura gera nova `Inscricao`.

## Origem

Enum mínimo:

```text
InscricaoOrigem
- SOLICITACAO_ATLETA
- CADASTRO_DIRETO_PROFISSIONAL
- ADMINISTRATIVO
- MIGRACAO_LEGADO
```

Não inferir origem por ator/status.

## Semântica temporal

`dtSolicitacao` representa criação da inscrição.

`dtDecisao` representa confirmação ou reprovação.

`dtCancelamento` representa o momento do cancelamento.

Não usar datas da temporada para limitar tecnicamente a existência da inscrição.

Inscrição tardia é válida quando autorizada pelo contexto de temporada já consolidado.

## Unicidade lógica

O domínio permite múltiplos ciclos históricos de inscrição para o mesmo atleta/campeonato, mas não deve existir mais de uma inscrição operacional concorrente.

Regra:

```text
para o mesmo (cdAtleta, cdCompeticao):
- no máximo uma PENDENTE
- no máximo uma CONFIRMADA
```

`REPROVADA` e `CANCELADA` são históricas e não bloqueiam nova tentativa.

Além disso, uma `CONFIRMADA` existente bloqueia nova `PENDENTE` ordinária.

## Proteção física no MySQL

MySQL não oferece partial unique index no mesmo modelo do PostgreSQL.

A estratégia recomendada é usar colunas geradas para chaves condicionais.

Exemplo conceitual:

```text
ukPendente = CASE WHEN status = 'PENDENTE'
                  THEN CONCAT(cdAtleta, ':', cdCompeticao)
             END

ukConfirmada = CASE WHEN status = 'CONFIRMADA'
                    THEN CONCAT(cdAtleta, ':', cdCompeticao)
               END
```

Criar índices UNIQUE sobre essas colunas.

Como múltiplos `NULL` são permitidos em índice UNIQUE no MySQL, históricos `REPROVADA`/`CANCELADA` continuam coexistindo.

A implementação concreta deve preferir colunas geradas STORED/VIRTUAL compatíveis com a versão real do MySQL em produção.

### Regra adicional: CONFIRMADA bloqueia PENDENTE

As duas unicidades isoladas não impedem uma `PENDENTE` coexistindo com uma `CONFIRMADA`.

Portanto, criação de inscrição deve ocorrer em transação com locking consistente no par atleta/campeonato.

Estratégia recomendada:

1. buscar inscrições operacionais do par com lock pessimista;
2. se existir `CONFIRMADA`, bloquear nova solicitação;
3. se existir `PENDENTE`, aplicar regra de reaproveitamento/idempotência quando cabível;
4. inserir somente após validação;
5. manter constraints físicas como última linha de defesa contra corrida.

Alternativa futura: criar tabela de chave operacional por atleta/campeonato, mas não é necessária no MVP se o locking estiver bem definido.

## Repositório

`InscricaoRepository` deve expor consultas orientadas a invariantes, não apenas CRUD genérico.

Exemplos conceituais:

```text
findByIdForUpdate(cdInscricao)
findOperationalByAtletaAndCampeonatoForUpdate(cdAtleta, cdCompeticao)
existsHistoricoResultadoByInscricao(cdInscricao)
findConfirmadasByCampeonato(...)
findByAtletaAndCampeonato(...)
```

Para locking concorrente, usar `@Lock(PESSIMISTIC_WRITE)` quando adequado.

## Serviço de aplicação

Criar `InscricaoService` ou `InscricaoApplicationService` com casos de uso explícitos:

```text
solicitarInscricao(...)
cadastrarDiretamente(...)
aprovar(...)
reprovar(...)
cancelar(...)
consultar(...)
```

Evitar método genérico `updateInscricao` capaz de alterar `status` arbitrariamente.

## Autorização contextual

A autorização não deriva apenas do vínculo direto profissional-atleta.

Para ação profissional ordinária em inscrição:

```text
existe Temporada T tal que:
  T.status IN (ATIVO, ENCERRADA)
  AND Campeonato pertence a T
  AND atleta pertence à composição de T
  AND (
       ator = T.cdCriador
       OR ator possui TemporadaProfissional ADMINISTRACAO vigente em T
      )
```

`CONSULTA` não autoriza alteração de inscrição.

Vínculo profissional-atleta direto isolado não autoriza operação.

Se o campeonato estiver associado a várias temporadas, basta um contexto válido.

Temporada `CANCELADA` não serve como fundamento ordinário de autorização.

## Composição do atleta na temporada

Para fins de autorização contextual, atleta pertence à temporada quando existe vínculo atual `ATIVO` entre o atleta e `Temporada.cdCriador`.

Essa regra é diferente da elegibilidade histórica de resultado, que usa vigência na data `Campeonato.dtInicio`.

## Solicitação pelo atleta

Fluxo:

```text
atleta solicita
-> status PENDENTE
-> origem SOLICITACAO_ATLETA
-> cdSolicitante = atleta
-> dtSolicitacao = agora
```

Validações:

- atleta da inscrição deve ser o usuário autenticado, salvo proprietário;
- campeonato não pode estar `CANCELADO` para fluxo ordinário;
- deve existir pelo menos um contexto de temporada `ATIVO` ou `ENCERRADA` no qual o atleta pertença à composição;
- não pode haver inscrição `CONFIRMADA` operacional;
- pendência existente pode ser retornada como operação idempotente conforme contrato da API.

## Cadastro direto por profissional

Fluxo:

```text
profissional autorizado
-> cria inscrição diretamente CONFIRMADA
-> origem CADASTRO_DIRETO_PROFISSIONAL
-> cdSolicitante = profissional
-> cdResponsavelDecisao = profissional
-> dtSolicitacao = agora
-> dtDecisao = agora
```

Requer autorização contextual `ADMINISTRACAO`/criador.

Não exige aprovação posterior do atleta no MVP.

## Aprovação

Somente inscrição `PENDENTE` pode ser aprovada.

Ao aprovar:

```text
status = CONFIRMADA
cdResponsavelDecisao = ator
dtDecisao = agora
```

Revalidar no momento da decisão:

- inscrição ainda está `PENDENTE`;
- não existe outra `CONFIRMADA` para atleta/campeonato;
- campeonato continua em situação válida para operação ordinária;
- existe contexto de temporada autorizador `ATIVO` ou `ENCERRADA`;
- ator continua autorizado;
- atleta continua pertencendo à composição de pelo menos um contexto autorizador.

Conflito concorrente -> `409 Conflict`.

## Reprovação

Somente `PENDENTE` pode ser reprovada.

Persistir:

```text
status = REPROVADA
motivoReprovacao = obrigatório
cdResponsavelDecisao
dtDecisao
```

Justificativa deve ter limite de tamanho e não pode ser vazia/blank.

## Cancelamento

Regra depende da existência histórica de `Resultado` associado à inscrição.

### Sem nenhum Resultado histórico

Criador/contextual `ADMINISTRACAO` pode cancelar diretamente inscrição `CONFIRMADA`.

Persistir:

```text
status = CANCELADA
cdResponsavelCancelamento
motivoCancelamento
dtCancelamento
```

Justificativa obrigatória.

### Com qualquer Resultado histórico

Se existir qualquer `Resultado` ligado à inscrição, inclusive cancelado, a atuação profissional direta não é suficiente.

Nesse caso, cancelamento exige proprietário, conforme regra funcional consolidada.

Isso impede apagar semanticamente uma participação que já produziu fato esportivo/auditoria.

`ResultadoRepository.existsByInscricao(...)` deve considerar histórico completo, não apenas resultados ativos.

## Efeito do cancelamento

Cancelar inscrição:

- não exclui Resultados;
- não reativa nem cancela automaticamente Resultado individual;
- torna a inscrição inelegível para contribuição esportiva enquanto `CANCELADA`;
- provoca atualização/invalidação das projeções das temporadas afetadas;
- preserva histórico integral.

Reinscrição futura cria nova `Inscricao`.

Resultados da inscrição antiga nunca migram automaticamente para a nova.

## Integração com Resultado

Relação:

```text
Inscricao 1 ---- N Resultado
```

`Resultado` referencia `cdInscricao`.

A partir da inscrição é possível derivar:

```text
atleta
campeonato
```

Portanto `Resultado` não deve duplicar `cdAtleta` nem `cdCompeticao` como fonte autoritativa.

Índice obrigatório futuro em `Resultado.cdInscricao`.

## Inscrição tardia em Temporada ENCERRADA

`ENCERRADA` continua podendo servir como contexto autorizador para inscrição tardia em campeonato já associado.

Isso é permitido porque inscrição é fato do campeonato, não alteração estrutural da temporada.

Reabertura da temporada não é necessária.

Ao confirmar inscrição tardia:

- o fato fica disponível para todas as temporadas que compartilham o campeonato;
- cada temporada reavalia sua própria elegibilidade;
- nenhuma inscrição é duplicada por temporada.

## Campeonato cancelado

Campeonato `CANCELADO` bloqueia novas operações ordinárias de inscrição.

Dados existentes permanecem preservados.

Reativação do campeonato pode voltar a permitir operações, desde que demais invariantes sejam satisfeitas.

## Concorrência

### Mesmo registro

`@Version` protege decisões concorrentes sobre a mesma `Inscricao`.

Exemplo:

```text
A aprova
B reprova simultaneamente
```

Uma operação deve vencer; a outra retorna conflito.

### Registros diferentes

`@Version` não impede duas novas linhas concorrentes para o mesmo atleta/campeonato.

Por isso são obrigatórios:

- lock transacional por chave lógica;
- índices únicos condicionais;
- tratamento de `DataIntegrityViolationException`/constraint violation como `409 Conflict` quando representar invariante funcional.

## HTTP sugerido

```text
POST /campeonatos/{cdCampeonato}/inscricoes
POST /campeonatos/{cdCampeonato}/inscricoes/direta
POST /inscricoes/{id}/aprovar
POST /inscricoes/{id}/reprovar
POST /inscricoes/{id}/cancelar
GET  /inscricoes/{id}
GET  /campeonatos/{cdCampeonato}/inscricoes
```

Os nomes finais podem ser alinhados às convenções da API, mas ações de workflow não devem ser expostas como `PUT` genérico.

## DTOs sugeridos

```text
SolicitarInscricaoRequest
CadastrarInscricaoDiretaRequest
AprovarInscricaoRequest
ReprovarInscricaoRequest
CancelarInscricaoRequest
InscricaoResponse
```

Campos de auditoria não devem ser aceitos do cliente.

## Erros

Exemplos:

```text
404 -> inscrição/campeonato/atleta inexistente
400 -> payload inválido, motivo obrigatório ausente
403 -> ator sem autorização contextual
409 -> estado inválido, inscrição operacional duplicada, corrida concorrente
```

## Auditoria

Mesmo que não exista tabela `InscricaoHistorico` no primeiro corte, o agregado deve preservar atores/datas de solicitação, decisão e cancelamento.

Se for necessário rastrear múltiplas alterações administrativas futuras, adicionar histórico append-only sem substituir os campos operacionais atuais.

No MVP, como o workflow é monotônico e cada ciclo gera nova inscrição, os campos atuais são suficientes para reconstruir a trajetória principal.

## Migração de `Competidores`

`Competidores` legado representa associação simplificada atleta/competição e deve ser convertido para inscrição confirmada quando o vínculo puder ser identificado com segurança.

Mapeamento recomendado:

```text
Competidores
-> Inscricao
status = CONFIRMADA
origem = MIGRACAO_LEGADO
dtSolicitacao = data histórica disponível ou timestamp de migração
dtDecisao = mesma referência definida para migração
```

### Identidade

Cada associação legado atleta/competição deve gerar no máximo uma inscrição migrada.

A migration deve ser idempotente.

Recomendação: criar tabela/coluna técnica de rastreabilidade de legado somente se necessária, por exemplo:

```text
cdOrigemLegado
```

ou tabela de controle da migration.

Não depender de inferência por nome.

## Validação pré-migração

Antes de migrar:

1. verificar duplicidades em `Competidores` para o mesmo atleta/competição;
2. verificar usuários inexistentes/inativos/inconsistentes;
3. verificar competições inexistentes;
4. identificar dados com chaves compostas/data que possam representar múltiplos registros históricos;
5. decidir deterministicamente quais registros equivalem a uma participação confirmada única.

Registros ambíguos não devem ser silenciosamente descartados nem duplicados; devem gerar relatório de inconsistência para tratamento controlado.

## Estratégia de rollout

1. criar `tbInscricao` e constraints;
2. publicar código capaz de ler/escrever `Inscricao` sem remover `Competidores`;
3. migrar dados elegíveis do legado;
4. validar contagens/duplicidades;
5. adaptar consumidores novos para `Inscricao`;
6. manter compatibilidade legada temporária se necessária;
7. remover escrita em `Competidores`;
8. somente em etapa posterior remover tabela/código legado.

Evitar dual-write prolongado. Se temporariamente necessário, definir fonte autoritativa explicitamente; após ativação do novo domínio, `Inscricao` deve ser a fonte de verdade.

## Testes obrigatórios

### Unitários

- transições válidas/inválidas;
- motivo obrigatório;
- regras de cancelamento;
- autorização contextual.

### Integração MySQL/Testcontainers

- duas solicitações concorrentes para mesmo atleta/campeonato;
- cadastro direto concorrente com solicitação;
- duas aprovações concorrentes;
- aprovação concorrente com reprovação;
- cancelamento com/sem resultado histórico;
- constraint de pendência única;
- constraint de confirmação única;
- inscrição tardia com temporada ENCERRADA;
- temporada CANCELADA não autorizando operação ordinária.

### Migração

- base sem dados;
- base com `Competidores` válidos;
- duplicidades legadas;
- registros inválidos;
- execução idempotente quando aplicável;
- contagem antes/depois.

## Invariantes finais da Fase 4

- `Inscricao` pertence ao campeonato, não à temporada;
- múltiplas temporadas podem interpretar a mesma inscrição;
- workflow é `PENDENTE -> CONFIRMADA|REPROVADA` e `CONFIRMADA -> CANCELADA`;
- inscrição cancelada não é reativada; nova tentativa gera novo registro;
- no máximo uma `PENDENTE` e uma `CONFIRMADA` por atleta/campeonato, com regra adicional impedindo coexistência operacional inadequada;
- unicidades críticas precisam de proteção física e locking;
- `@Version` protege concorrência sobre a mesma linha, não concorrência entre linhas distintas;
- profissional opera por autorização contextual de temporada, não por vínculo direto isolado;
- `ENCERRADA` pode autorizar inscrição tardia;
- `CANCELADA` não fundamenta operação ordinária;
- cancelamento sem Resultado histórico pode ser feito por criador/ADMINISTRACAO;
- qualquer Resultado histórico exige proprietário para cancelamento;
- cancelamento não apaga Resultado nem histórico;
- `Resultado` deriva atleta/campeonato pela inscrição;
- `Competidores` migra para `Inscricao CONFIRMADA` somente quando a semântica for reconstruível com segurança;
- após rollout, `Inscricao` passa a ser a fonte autoritativa de participação.
