# Refinamento técnico — Inscrição em campeonato

Status: modelagem técnica consolidada para o MVP; implementação e migração pendentes.

## Objetivo

Substituir a associação simplificada atual `Competidores` por uma entidade de domínio capaz de representar solicitação, aprovação, reprovação, cadastro direto, remoção, reinscrição e histórico de participação do atleta em um campeonato.

## Situação atual

A entidade atual `Competidores` usa chave composta `(cdCompetidor, cdCompeticao)` e armazena apenas `dtCadastro`.

Essa estrutura não suporta:

- solicitação pendente;
- aprovação/reprovação;
- justificativa;
- responsável pela decisão;
- remoção;
- ciclos de reinscrição;
- histórico;
- auditoria;
- distinção entre inscrição atual e inscrições anteriores.

Portanto, ela não deve permanecer como fonte de verdade no novo domínio.

## Entidade proposta

Criar `Inscricao` com identidade própria.

Campos conceituais:

- `cdInscricao`
- `cdAtleta`
- `cdCompeticao`
- `status`
- `origem`
- `cdSolicitante`
- `dtSolicitacao`
- `cdResponsavelDecisao`
- `dtDecisao`
- `motivoReprovacao`
- `dtCancelamento`
- `cdResponsavelCancelamento`
- `motivoCancelamento`
- campos de auditoria

A identidade própria é necessária porque o mesmo atleta pode ter múltiplos ciclos históricos de inscrição no mesmo campeonato.

## Status

Estados mínimos:

- `PENDENTE`
- `CONFIRMADA`
- `REPROVADA`
- `CANCELADA`

Fluxo principal:

```text
PENDENTE -> CONFIRMADA
PENDENTE -> REPROVADA
CONFIRMADA -> CANCELADA
```

Uma reinscrição após reprovação ou cancelamento gera novo registro, preservando o ciclo anterior.

## Origem

Enum recomendado:

- `SOLICITACAO_ATLETA`
- `CADASTRO_DIRETO_PROFISSIONAL`
- `ADMINISTRATIVO`

### SOLICITACAO_ATLETA

Cria inscrição `PENDENTE` e segue o fluxo de aprovação.

### CADASTRO_DIRETO_PROFISSIONAL

Profissional autorizado pode cadastrar diretamente o atleta no campeonato. A inscrição nasce `CONFIRMADA`, sem aprovação adicional do atleta.

A aprovação de resultados continua sendo fluxo separado.

### ADMINISTRATIVO

Permite intervenção do proprietário com auditoria explícita.

## Unicidade

Deve existir no máximo:

- uma inscrição `PENDENTE` por atleta/campeonato;
- uma inscrição `CONFIRMADA` por atleta/campeonato.

Registros `REPROVADA` e `CANCELADA` permanecem históricos e não impedem uma nova tentativa.

A regra precisa ser protegida transacionalmente para impedir solicitações concorrentes.

## Solicitação pelo atleta

Antes de criar uma pendência:

1. verificar se já existe inscrição `CONFIRMADA`;
2. verificar se já existe `PENDENTE`;
3. localizar temporadas contendo o campeonato;
4. identificar criadores/administradores dessas temporadas;
5. filtrar profissionais que possuem vínculo ativo com o atleta;
6. remover destinatários duplicados;
7. se não houver aprovador elegível, não criar a solicitação.

Se já houver pendência, o reenvio deve reutilizar o mesmo registro e apenas reenviar a notificação aos aprovadores elegíveis atuais.

## Aprovadores elegíveis

Um profissional pode decidir sobre a solicitação quando, no momento da decisão:

- possui vínculo ativo com o atleta; e
- cria ou administra ao menos uma temporada que contém o campeonato.

Permissão somente de consulta não autoriza aprovação.

A autorização deve ser revalidada no momento da decisão; não basta ter sido elegível quando a pendência foi criada.

## Aprovação

A decisão de qualquer aprovador elegível resolve a pendência para todos.

Na aprovação:

- `status = CONFIRMADA`;
- registrar responsável e data;
- encerrar a pendência para todos os demais aprovadores;
- notificar o atleta;
- recalcular elegibilidade das temporadas relacionadas.

A inscrição pode ser confirmada mesmo que, após a decisão, nenhuma temporada seja esportivamente elegível para o atleta.

Isso ocorre porque aprovação da inscrição e elegibilidade de temporada são conceitos distintos.

## Reprovação

- justificativa obrigatória;
- `status = REPROVADA`;
- registrar responsável e data;
- decisão resolve a pendência para todos;
- atleta recebe notificação de reprovação;
- justificativa não é exposta ao atleta;
- histórico interno preserva a justificativa conforme as permissões definidas no MVP.

Após reprovação, uma nova solicitação futura cria novo ciclo de inscrição.

## Perda de autorização durante pendência

A lista de aprovadores é dinâmica.

Se um profissional perder vínculo ou administração:

- deixa de poder decidir;
- reavaliar os demais aprovadores elegíveis;
- manter a mesma `Inscricao` pendente;
- não criar uma nova pendência.

Se não restar aprovador elegível para uma pendência já existente, encaminhar a demanda ao proprietário.

Conta profissional inativa segue as regras específicas do MVP e não equivale automaticamente à perda de vínculo/administração.

## Cancelamento / remoção

A remoção de inscrição deve ser representada como `CANCELADA`, não exclusão física.

### Sem qualquer resultado no histórico da inscrição

Profissional autorizado pode cancelar diretamente.

### Com qualquer resultado no histórico da inscrição

Mesmo que todos os resultados estejam atualmente cancelados, a remoção da inscrição exige aprovação/intervenção do proprietário.

Essa regra depende da existência histórica de resultados, não apenas de resultados atualmente ativos.

## Efeitos do cancelamento

Ao cancelar inscrição:

- ela deixa de satisfazer elegibilidade da temporada;
- resultados vinculados ao ciclo devem permanecer preservados;
- resultados relacionados ao ciclo cancelado deixam de participar de relatórios esportivos e cálculos conforme as regras do MVP;
- rankings afetados devem ser recalculados;
- não excluir fisicamente inscrição nem resultados.

## Reinscrição

Após cancelamento ou reprovação, o atleta pode participar de novo ciclo.

A reinscrição cria uma nova `Inscricao`.

Resultados de ciclos anteriores nunca são reativados automaticamente.

Novos resultados devem referenciar a nova inscrição.

Portanto:

```text
Atleta A + Campeonato X

Inscrição #1 -> CONFIRMADA -> CANCELADA
  └── resultados históricos

Inscrição #2 -> CONFIRMADA
  └── novos resultados
```

Isso elimina ambiguidade sobre a qual participação pertence cada resultado.

## Relação com Resultado

`Resultado` deve referenciar `cdInscricao`, não apenas atleta + campeonato.

Essa decisão é essencial para suportar reinscrição corretamente.

A partir da inscrição, o resultado consegue obter:

- atleta;
- campeonato;
- ciclo de participação.

Assim, não é necessário duplicar `cdAtleta` e `cdCompeticao` como fonte autoritativa dentro de `Resultado`.

## Relação com Temporada

A inscrição não pertence a uma temporada.

Ela pertence ao par atleta/campeonato e pode tornar o atleta elegível simultaneamente em várias temporadas.

```text
Inscricao CONFIRMADA
        │
        └── Campeonato
              │
              ├── Temporada A
              ├── Temporada B
              └── Temporada C
```

Cada temporada aplica independentemente suas regras de vínculo com o criador e situação da conta do atleta.

## Índices recomendados

- índice por `cdAtleta`;
- índice por `cdCompeticao`;
- índice composto `(cdAtleta, cdCompeticao, status)`;
- índice por `dtSolicitacao` para filas/pendências quando necessário;
- proteção lógica para uma única pendência e uma única confirmada por atleta/campeonato.

## Auditoria

Preservar pelo menos:

- origem;
- solicitante;
- responsável pela decisão;
- data da solicitação;
- data da decisão;
- justificativa de reprovação;
- responsável pelo cancelamento;
- data do cancelamento;
- justificativa quando aplicável.

Eventos de notificação não precisam ser parte da entidade principal; podem ser tratados pela infraestrutura/auditoria correspondente.

## Migração conceitual de `Competidores`

A atual `tbCompetidores` representa implicitamente uma participação confirmada.

Na migração, cada registro legado deverá ser convertido em uma `Inscricao` histórica/atual `CONFIRMADA`, preservando atleta, campeonato e `dtCadastro` como referência temporal quando possível.

A estratégia concreta de migration será definida após o modelo de `Resultado` ser fechado, porque `PontuacaoHist` também depende atualmente de `cdCompetidor` e `cdCompeticao`.

## Invariantes consolidadas

- Inscrição possui identidade própria.
- Um atleta pode ter múltiplos ciclos históricos no mesmo campeonato.
- Há no máximo uma `PENDENTE` por atleta/campeonato.
- Há no máximo uma `CONFIRMADA` por atleta/campeonato.
- Aprovação por qualquer elegível resolve a pendência para todos.
- Reprovação exige justificativa.
- Reenvio reutiliza a pendência existente.
- Cadastro direto autorizado nasce `CONFIRMADA`.
- Inscrição não pertence à temporada.
- Cancelamento é lógico e preserva histórico.
- Existência histórica de qualquer resultado exige intervenção do proprietário para cancelar a inscrição.
- Reinscrição cria novo registro.
- Resultados antigos não são reativados em reinscrição.
- `Resultado` deve referenciar `cdInscricao`.

## Próximo refinamento

Refinar `Resultado`, incluindo:

- identidade;
- categoria/classe/colocação;
- estados;
- aprovação/reprovação pelo atleta;
- versão concorrente;
- cancelamento;
- intervenção do proprietário;
- relacionamento com a atual `PontuacaoHist`.
