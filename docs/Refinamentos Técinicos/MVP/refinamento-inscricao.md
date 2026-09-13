# Refinamento técnico — Inscrição em campeonato

Status: modelagem técnica consolidada para o MVP; implementação e migração pendentes.

## Objetivo

Substituir a associação simplificada atual `Competidores` por uma entidade de domínio capaz de representar solicitação, aprovação, reprovação, cadastro direto, remoção, reinscrição e histórico de participação do atleta em um campeonato.

## Situação atual

A entidade atual `Competidores` usa chave composta `(cdCompetidor, cdCompeticao)` e armazena apenas `dtCadastro`.

Essa estrutura não suporta solicitação pendente, aprovação/reprovação, justificativa, responsável pela decisão, remoção, ciclos de reinscrição, histórico e auditoria. Portanto, não deve permanecer como fonte de verdade no novo domínio.

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

Cria inscrição `PENDENTE` e segue fluxo de aprovação.

### CADASTRO_DIRETO_PROFISSIONAL

Profissional autorizado pode cadastrar diretamente o atleta no campeonato. A inscrição nasce `CONFIRMADA`, sem aprovação adicional do atleta.

A aprovação de resultados continua sendo fluxo separado.

### ADMINISTRATIVO

Permite intervenção do proprietário com auditoria explícita.

## Unicidade

Deve existir no máximo:

- uma inscrição `PENDENTE` por atleta/campeonato;
- uma inscrição `CONFIRMADA` por atleta/campeonato.

Registros `REPROVADA` e `CANCELADA` permanecem históricos e não impedem nova tentativa.

A regra precisa ser protegida transacionalmente para impedir solicitações concorrentes.

## Autorização contextual por temporada

A autorização operacional sobre uma inscrição é derivada das temporadas que contêm o campeonato.

Para uma temporada específica:

- o criador da temporada possui autoridade administrativa por definição;
- profissional com `TemporadaProfissional.ADMINISTRACAO` pode operar inscrições dos atletas pertencentes àquela temporada mesmo sem `VinculoProfissionalAtleta` próprio com o atleta;
- `CONSULTA` não autoriza cadastro direto, aprovação, reprovação ou cancelamento de inscrição;
- vínculo profissional-atleta próprio não é exigido do administrador da temporada para agir no contexto dessa temporada;
- vínculo profissional-atleta, isoladamente, não concede autoridade sobre inscrições se o profissional não cria/administra uma temporada que contenha o campeonato.

A autorização contextual não cria vínculo profissional-atleta e não concede poderes fora da temporada que fundamentou a autorização.

Como a inscrição não pertence a uma temporada, uma mesma inscrição pode ser alcançada por mais de uma temporada. Basta que o profissional possua autoridade administrativa válida em ao menos uma temporada associada ao campeonato e na qual o atleta pertença à composição daquela temporada.

A composição do atleta na temporada continua sendo definida pelo vínculo do atleta com o criador da temporada.

## Estado da temporada e inscrição tardia

Temporada `ATIVO` e `ENCERRADA` podem servir de fundamento contextual para operações ordinárias de `Inscricao`, desde que as demais regras de autorização sejam satisfeitas.

Isso significa que, em uma temporada `ENCERRADA`, ainda é permitido:

- o atleta solicitar inscrição em campeonato já associado à temporada;
- profissional contextualmente autorizado aprovar ou reprovar a solicitação;
- profissional contextualmente autorizado realizar `CADASTRO_DIRETO_PROFISSIONAL`;
- concluir pendências de inscrição já existentes;
- cancelar inscrição quando a regra de cancelamento permitir a atuação direta do profissional.

A finalidade é permitir registro tardio de um fato histórico real, da mesma forma que resultados tardios podem ser processados após o encerramento da temporada.

Não é necessário executar `ENCERRADA -> ATIVO` apenas para registrar, aprovar ou corrigir o ciclo de participação no campeonato.

Essa permissão não reabre a configuração estrutural da temporada. Enquanto `ENCERRADA`, continuam bloqueadas alterações estruturais que exijam reabertura conforme `refinamento-temporada.md`, como mudanças de pontuação, penalidades e composição de campeonatos.

Temporada `CANCELADA` não deve servir como fundamento ordinário para criação, aprovação, reprovação, cadastro direto ou cancelamento de inscrição enquanto permanecer cancelada.

Se um campeonato estiver associado a múltiplas temporadas, uma operação sobre `Inscricao` permanece permitida quando existir ao menos uma temporada `ATIVO` ou `ENCERRADA` que satisfaça integralmente os critérios de autorização contextual.

A eventual existência de outras temporadas `CANCELADA` associadas ao mesmo campeonato não invalida a operação autorizada por uma temporada válida.

## Solicitação pelo atleta

Antes de criar uma pendência:

1. verificar se já existe inscrição `CONFIRMADA`;
2. verificar se já existe `PENDENTE`;
3. localizar temporadas `ATIVO` ou `ENCERRADA` contendo o campeonato;
4. entre essas temporadas, identificar aquelas nas quais o atleta pertence à composição da temporada;
5. identificar criadores e profissionais com `ADMINISTRACAO` nessas temporadas;
6. remover destinatários duplicados;
7. se não houver aprovador elegível, não criar a solicitação.

Não é necessário que cada administrador possua vínculo profissional-atleta próprio com o atleta. A autorização decorre da administração da temporada e da pertença do atleta àquela temporada.

Se já houver pendência, o reenvio reutiliza o mesmo registro e apenas reenvia a notificação aos aprovadores elegíveis atuais.

## Aprovadores elegíveis

Um profissional pode decidir sobre a solicitação quando, no momento da decisão:

```text
existe Temporada T tal que:
  T.status IN (ATIVO, ENCERRADA)
  E Campeonato pertence a T
  E atleta pertence à composição de T
  E (
       profissional = T.cdCriador
       OU profissional possui ADMINISTRACAO em T
     )
```

Não é exigido vínculo direto entre o profissional decisor e o atleta quando a autorização decorre de `ADMINISTRACAO` da temporada.

`CONSULTA` não autoriza decisão.

A autorização deve ser revalidada no momento da decisão; não basta ter sido elegível quando a pendência foi criada.

## Cadastro direto pelo profissional

O mesmo critério de autorização contextual vale para `CADASTRO_DIRETO_PROFISSIONAL`.

Um criador ou administrador pode confirmar diretamente a inscrição de um atleta quando existir ao menos uma temporada `ATIVO` ou `ENCERRADA` administrada por ele que:

- contenha o campeonato; e
- tenha o atleta em sua composição.

Não é necessário vínculo próprio administrador-atleta.

## Aprovação

A decisão de qualquer aprovador elegível resolve a pendência para todos.

Na aprovação:

- `status = CONFIRMADA`;
- registrar responsável e data;
- encerrar a pendência para os demais aprovadores;
- notificar atleta;
- recalcular elegibilidade das temporadas relacionadas.

A inscrição pode ser confirmada mesmo que, após a decisão, nenhuma temporada seja esportivamente elegível para o atleta, porque aprovação de inscrição e elegibilidade esportiva são conceitos distintos.

Quando a confirmação tardia tornar o atleta elegível em temporada `ATIVO` ou `ENCERRADA`, sua projeção/ranking deve ser recalculada conforme as regras vigentes.

## Reprovação

- justificativa obrigatória;
- `status = REPROVADA`;
- registrar responsável e data;
- decisão resolve a pendência para todos;
- atleta recebe notificação;
- justificativa não é exposta ao atleta;
- histórico interno preserva justificativa conforme permissões do MVP.

Nova solicitação futura cria novo ciclo.

## Perda de autorização durante pendência

A lista de aprovadores é dinâmica.

O profissional deixa de poder decidir se deixar de satisfazer o critério contextual, por exemplo:

- perder `ADMINISTRACAO` da temporada que sustentava a autorização;
- a temporada deixar de conter o campeonato;
- a temporada utilizada como único fundamento passar para `CANCELADA`;
- o atleta deixar de pertencer à composição daquela temporada;
- perder sua condição operacional de conta conforme as regras gerais do MVP.

A perda de vínculo profissional-atleta próprio do administrador não retira sua autorização quando ele continua autorizado contextualmente por temporada.

Ao perder autorização:

- reavaliar os demais aprovadores elegíveis;
- manter a mesma `Inscricao` pendente;
- não criar nova pendência.

Se não restar aprovador elegível, encaminhar a demanda ao proprietário.

## Cancelamento / remoção

A remoção é `CANCELADA`, nunca exclusão física.

### Sem qualquer resultado no histórico da inscrição

Criador ou profissional com `ADMINISTRACAO` contextualmente autorizado por temporada `ATIVO` ou `ENCERRADA` pode cancelar diretamente.

### Com qualquer resultado no histórico da inscrição

Mesmo que todos os resultados estejam atualmente cancelados, o cancelamento da inscrição exige aprovação/intervenção do proprietário.

A regra depende da existência histórica de resultados, não apenas dos atualmente ativos.

## Efeitos do cancelamento

Ao cancelar inscrição:

- deixa de satisfazer elegibilidade da temporada;
- resultados vinculados ao ciclo permanecem preservados;
- resultados do ciclo cancelado deixam de participar de relatórios esportivos e cálculos conforme regras do MVP;
- rankings afetados são recalculados;
- não excluir fisicamente inscrição ou resultados.

## Reinscrição

Após cancelamento ou reprovação, novo ciclo cria nova `Inscricao`.

Resultados de ciclos anteriores nunca são reativados automaticamente e novos resultados referenciam a nova inscrição.

Reinscrição tardia também pode ocorrer com fundamento em temporada `ENCERRADA`, desde que o campeonato continue associado e as demais regras contextuais sejam satisfeitas.

## Relação com Resultado

`Resultado` referencia `cdInscricao`, não atleta + campeonato.

A partir da inscrição, resultado obtém atleta, campeonato e ciclo de participação.

Uma inscrição pode possuir vários resultados em diferentes combinações categoria/classe, mas no máximo um resultado ativo por:

```text
(cdInscricao, cdCategoria, cdClasse)
```

Registros anteriores podem permanecer somente como histórico/versionamento/cancelamento conforme o modelo de `Resultado`.

## Relação com Temporada

A inscrição não pertence à temporada.

Ela pertence ao atleta/campeonato e pode tornar o atleta elegível em várias temporadas simultaneamente.

Cada temporada avalia de forma independente sua elegibilidade esportiva e suas próprias regras.

A autorização operacional do profissional, entretanto, pode ser derivada de uma temporada específica `ATIVO` ou `ENCERRADA` que contenha o campeonato e tenha o atleta em sua composição.

Isso não transforma `Inscricao` em entidade filha da temporada.

## Índices recomendados

- índice por `cdAtleta`;
- índice por `cdCompeticao`;
- índice composto `(cdAtleta, cdCompeticao, status)`;
- índice por `dtSolicitacao` quando necessário;
- proteção lógica para única pendência e única confirmada por atleta/campeonato.

## Auditoria

Preservar pelo menos origem, solicitante, responsável pela decisão, datas, justificativa de reprovação, responsável/data/motivo de cancelamento.

Eventos de notificação não precisam fazer parte da entidade principal.

## Migração conceitual de `Competidores`

Cada registro legado de `tbCompetidores` deverá ser convertido conceitualmente em `Inscricao CONFIRMADA`, preservando atleta, campeonato e `dtCadastro` quando possível.

A estratégia concreta de migration será definida em conjunto com a migração de `Resultado`/`PontuacaoHist`.

## Invariantes consolidadas

- inscrição possui identidade própria;
- atleta pode ter múltiplos ciclos históricos no mesmo campeonato;
- no máximo uma `PENDENTE` e uma `CONFIRMADA` por atleta/campeonato;
- inscrição não pertence à temporada;
- `ATIVO` e `ENCERRADA` podem fundamentar operações tardias de inscrição;
- `ENCERRADA` não precisa ser reaberta apenas para solicitar, aprovar, reprovar, cadastrar diretamente ou cancelar inscrição quando permitido;
- `CANCELADA` não fundamenta operação ordinária de inscrição enquanto cancelada;
- aprovação por qualquer elegível resolve pendência para todos;
- reprovação exige justificativa;
- reenvio reutiliza pendência existente;
- cadastro direto autorizado nasce `CONFIRMADA`;
- criador da temporada é administrador por definição;
- `ADMINISTRACAO` da temporada autoriza operar inscrições dos atletas daquela temporada sem vínculo próprio administrador-atleta;
- `CONSULTA` não autoriza operações de inscrição;
- vínculo profissional-atleta isolado não substitui administração de temporada;
- autorização exige campeonato associado à temporada e atleta pertencente à composição dela;
- autorização é revalidada no momento da ação;
- cancelamento é lógico e preserva histórico;
- existência histórica de qualquer resultado exige proprietário para cancelar inscrição;
- reinscrição cria novo registro;
- resultados antigos não são reativados;
- `Resultado` referencia `cdInscricao`;
- uma inscrição pode possuir vários resultados;
- no máximo um resultado ativo por combinação categoria/classe dentro da inscrição.
