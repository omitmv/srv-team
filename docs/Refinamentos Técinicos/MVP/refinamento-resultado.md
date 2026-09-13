# Refinamento técnico — Resultado

Status: modelagem técnica consolidada para o MVP; implementação e migração pendentes.

## Objetivo

Modelar `Resultado` como o desfecho esportivo obtido por um atleta em uma combinação de categoria e classe dentro de um ciclo específico de inscrição em campeonato, com validação obrigatória pelo atleta antes de produzir efeitos esportivos.

O resultado pode representar colocação classificatória ou situações esportivas específicas como desclassificação e ausência.

## Relação com Inscrição

`Resultado` referencia `cdInscricao`.

A inscrição identifica de forma autoritativa atleta, campeonato e ciclo de participação. Não duplicar `cdAtleta` e `cdCompeticao` em `Resultado` como fontes autoritativas.

## Multiplicidade e granularidade

Uma inscrição pode possuir vários resultados.

Todo `Resultado`, independentemente de sua situação esportiva, pertence obrigatoriamente a:

```text
Inscricao + Categoria + Classe
```

`CLASSIFICADO`, `DESCLASSIFICADO` e `AUSENTE` possuem a mesma granularidade. Não existe resultado global do campeonato sem categoria/classe e não existe ausência/desclassificação global na `Inscricao`.

Dentro da mesma inscrição, deve existir no máximo um resultado ativo para:

```text
(cdInscricao, cdCategoria, cdClasse)
```

Resultados históricos anteriores podem permanecer armazenados desde que não sejam simultaneamente ativos.

## Entidade conceitual

Campos recomendados:

- `cdResultado`
- `cdInscricao`
- `cdCategoria`
- `cdClasse`
- `situacaoResultado`
- `colocacao`
- `status`
- `nrVersao`
- `lockVersion`
- `cdResponsavelLancamento`
- `dtLancamento`
- `cdResponsavelDecisao`
- `dtDecisao`
- `motivoReprovacao`
- `dtCancelamento`
- `cdResponsavelCancelamento`
- `motivoCancelamento`
- campos de auditoria

`nrVersao` representa a versão de negócio submetida ao atleta. `lockVersion` representa exclusivamente concorrência otimista.

## Situação esportiva

Valores mínimos:

- `CLASSIFICADO`
- `DESCLASSIFICADO`
- `AUSENTE`

### CLASSIFICADO

- exige `colocacao > 0`;
- qualquer inteiro positivo é válido;
- não existe limite máximo funcional;
- validade independe de haver regra de pontuação;
- regra ausente na temporada produz impacto zero.

### DESCLASSIFICADO

- categoria e classe obrigatórias;
- `colocacao = null`;
- pode gerar penalidade específica da temporada.

### AUSENTE

- categoria e classe obrigatórias;
- `colocacao = null`;
- pode gerar penalidade específica da temporada;
- não é inferido automaticamente.

## Autorização contextual para lançamento e edição

A autorização ordinária do profissional sobre `Resultado` é derivada das temporadas que contêm o campeonato da inscrição.

Um profissional pode lançar ou editar um resultado pendente quando existir ao menos uma temporada `T` tal que:

```text
Campeonato da Inscricao pertence a T
E atleta pertence à composição de T
E (
     profissional = T.cdCriador
     OU profissional possui ADMINISTRACAO em T
   )
```

Consequências:

- o criador da temporada é autorizado por definição;
- `ADMINISTRACAO` permite operar resultados dos atletas pertencentes à temporada mesmo sem vínculo profissional-atleta próprio;
- `CONSULTA` não autoriza lançamento nem edição de resultado;
- vínculo profissional-atleta próprio, isoladamente, não concede autorização para lançar/editar resultado;
- a autorização contextual não cria vínculo profissional-atleta e não concede poder fora da temporada que a fundamentou;
- a autorização deve ser revalidada em cada operação.

Como `Resultado` e `Inscricao` não pertencem a uma temporada específica, uma operação é permitida se existir ao menos uma temporada associada ao campeonato que satisfaça integralmente o critério acima.

Temporada `ENCERRADA` continua autorizando lançamento e processamento de resultados tardios de campeonatos já associados. Não é necessário reabrir a temporada apenas para isso.

Temporada `CANCELADA` não deve ser usada como fundamento ordinário para lançamento/edição enquanto permanecer cancelada, pois está com efeito esportivo suspenso.

## Lançamento manual

Toda situação esportiva é informada explicitamente por profissional autorizado.

O sistema não infere `CLASSIFICADO`, `DESCLASSIFICADO` ou `AUSENTE` a partir de inscrição, ausência de colocação ou qualquer dado indireto.

```text
Profissional autorizado informa CLASSIFICADO + categoria + classe + colocacao
        -> PENDENTE_APROVACAO

Profissional autorizado informa DESCLASSIFICADO + categoria + classe
        -> PENDENTE_APROVACAO

Profissional autorizado informa AUSENTE + categoria + classe
        -> PENDENTE_APROVACAO
```

Nenhuma situação produz efeito esportivo antes da aprovação do atleta.

## Unicidade da colocação no campeonato

No resultado oficial não existe empate de colocação dentro de campeonato/categoria/classe.

Para `CLASSIFICADO`, a colocação deve ser única em:

```text
(cdCampeonato, cdCategoria, cdClasse, colocacao)
```

A unicidade é aplicada desde o lançamento do resultado, e não somente após a aprovação do atleta.

Para essa restrição, são considerados resultados `CLASSIFICADO` nos estados:

- `PENDENTE_APROVACAO`;
- `APROVADO`.

Portanto, um resultado `PENDENTE_APROVACAO` reserva temporariamente sua colocação no campeonato/categoria/classe. Enquanto ele permanecer ativo, outro atleta não pode receber a mesma colocação nesse conjunto esportivo.

Exemplo:

```text
Atleta A -> 1º / PENDENTE_APROVACAO
Atleta B tenta lançar -> 1º
                         X conflito: colocação já reservada
```

Se o atleta reprovar o resultado ou se o resultado for cancelado administrativamente, o resultado passa para `CANCELADO` e a colocação é liberada para novo lançamento.

A edição de resultado pendente também deve validar a colocação de destino antes de efetivar a alteração. Se o profissional alterar `2º` para `1º` e já existir outro `PENDENTE_APROVACAO` ou `APROVADO` ocupando `1º`, a edição deve ser rejeitada.

A proteção deve ser transacional e resistente a concorrência. Duas requisições simultâneas tentando reservar a mesma colocação não podem produzir dois resultados ativos com a mesma chave esportiva.

Conceitualmente, a restrição é:

```text
UNIQUE enquanto status IN (PENDENTE_APROVACAO, APROVADO)
(
  cdCampeonato,
  cdCategoria,
  cdClasse,
  colocacao
)
PARA situacaoResultado = CLASSIFICADO
```

A implementação física pode usar a estratégia compatível com o banco utilizado; a regra de domínio é obrigatória independentemente da solução de persistência.

Não confundir com empate no ranking da temporada, que continua permitido.

### Colocações não informadas

Posições superiores ao limite de pontuação da temporada podem simplesmente não ser lançadas.

Ausência de lançamento não equivale a `AUSENTE`, não gera penalidade e não exige preenchimento de lacunas. Qualquer colocação positiva pode ser lançada mesmo sem regra de pontuação correspondente.

## Estados do resultado

- `PENDENTE_APROVACAO`
- `APROVADO`
- `CANCELADO`

A reprovação pelo atleta transforma o lançamento em `CANCELADO`, preservando motivo, responsável e data.

## Fluxo único de aprovação

Todas as situações seguem:

```text
Profissional lança Resultado
        |
        v
PENDENTE_APROVACAO
        |
        +-- atleta aprova --> APROVADO
        |
        +-- atleta reprova -> CANCELADO
```

Somente `APROVADO` produz efeito esportivo. O atleta aprova categoria, classe, situação e colocação quando aplicável; não aprova a regra de pontuação da temporada.

A transição `PENDENTE_APROVACAO -> APROVADO` não precisa reservar novamente a colocação, pois ela já foi reservada no lançamento. Ainda assim, as invariantes devem ser revalidadas na transação de aprovação para proteção contra inconsistências ou alterações concorrentes.

## Validade esportiva

Um resultado produz efeito quando estiver `APROVADO` e for elegível para a temporada analisada conforme as regras de `Temporada`.

- `PENDENTE_APROVACAO` não pontua nem penaliza;
- `CANCELADO` não pontua nem penaliza;
- `APROVADO + CLASSIFICADO` usa pontuação atual da temporada;
- `APROVADO + DESCLASSIFICADO` usa penalidade atual da temporada;
- `APROVADO + AUSENTE` usa penalidade atual da temporada.

Pontuação/penalidade não é propriedade universal do resultado.

## Edição enquanto pendente

Enquanto `PENDENTE_APROVACAO`, o resultado pode ser editado por qualquer profissional que esteja contextualmente autorizado conforme a regra de temporada definida neste documento.

A edição deve:

- manter `cdResultado`;
- atualizar dados esportivos permitidos;
- validar novamente unicidade de colocação quando `CLASSIFICADO` ou quando a edição passar a ser `CLASSIFICADO`;
- liberar a colocação anterior quando ela deixar de pertencer ao resultado ativo após alteração válida;
- incrementar `nrVersao`;
- registrar responsável/data;
- invalidar aprovação vinculada a versão anterior;
- manter `PENDENTE_APROVACAO`;
- exigir aprovação do atleta sobre a nova versão.

Perder vínculo direto próprio com o atleta não retira autorização de um administrador que continua autorizado contextualmente por uma temporada válida.

Perder a administração/criação da única temporada que fundamentava a autorização, retirar o campeonato dessa temporada, ou o atleta deixar de pertencer à sua composição remove a autorização para operações futuras.

Após `APROVADO`, profissionais não editam pelo fluxo ordinário.

## Resultado tardio e temporada encerrada

Resultado pode ser lançado e aprovado depois do encerramento da temporada quando o campeonato já estava associado a ela e as demais regras de autorização/elegibilidade forem satisfeitas.

A aprovação de resultado tardio recalcula automaticamente o ranking das temporadas `ATIVO` ou `ENCERRADA` nas quais ele seja elegível.

Não é necessário reabrir temporada `ENCERRADA` para lançar, editar enquanto pendente, aprovar ou reprovar resultado tardio.

## Intervenção do proprietário sobre aprovado

O proprietário pode corrigir ou cancelar diretamente `APROVADO`.

Toda intervenção exige justificativa obrigatória, auditoria integral e não gera notificações.

### Correção direta

Pode alterar categoria, classe, situação e colocação, respeitando invariantes, inclusive a unicidade da colocação. Mantém `cdResultado`, incrementa `nrVersao`, mantém `APROVADO` e recalcula projeções afetadas.

### Cancelamento administrativo

```text
APROVADO -> CANCELADO
```

Preserva histórico, remove efeito esportivo e libera combinação para novo lançamento. Quando `CLASSIFICADO`, também libera a colocação anteriormente reservada no campeonato/categoria/classe.

## Reprovação pelo atleta

Ao reprovar:

1. `status = CANCELADO`;
2. registrar atleta, data e motivo;
3. preservar registro;
4. liberar `(cdInscricao, cdCategoria, cdClasse)` para novo lançamento;
5. se `CLASSIFICADO`, liberar também a colocação no campeonato/categoria/classe.

## Controle de versão e concorrência

Novo resultado nasce `nrVersao = 1`.

Alteração de categoria, classe, situação ou colocação incrementa versão. Aprovação/reprovação deve informar a versão visualizada e falhar se desatualizada.

Usar controle otimista, preferencialmente JPA `@Version`, para concorrência sobre o mesmo resultado. A unicidade de colocação exige adicionalmente proteção transacional no conjunto campeonato/categoria/classe, pois `@Version` isoladamente não impede dois resultados diferentes de reservarem simultaneamente a mesma colocação.

```text
nrVersao    = versão funcional
lockVersion = versão técnica
```

Não realizar merge implícito. Conflito recomendado: `409 Conflict`.

## Invariante de resultado ativo

Para `(cdInscricao, cdCategoria, cdClasse)`, no máximo um resultado em `PENDENTE_APROVACAO` ou `APROVADO`.

`CANCELADO` libera a combinação. A regra independe de `situacaoResultado`.

Para `CLASSIFICADO`, existe adicionalmente no máximo um resultado `PENDENTE_APROVACAO` ou `APROVADO` para `(cdCampeonato, cdCategoria, cdClasse, colocacao)`.

## Histórico funcional

Criar `ResultadoHistorico` ou equivalente, imutável e não autoritativo operacionalmente, preservando no mínimo resultado, versão, inscrição, categoria, classe, situação, colocação, status, responsável, data e justificativa.

## Migração da `tbPontuacaoHist`

A estrutura legada não representa adequadamente categoria, classe, situação, workflow, versões e autoria.

Somente converter quando atributos obrigatórios puderem ser reconstruídos sem inferência arbitrária. Não inferir `DESCLASSIFICADO` ou `AUSENTE` por valor nulo/zero.

## Relação com pontuação da temporada

`Resultado` não persiste pontos/penalidade como verdade autoritativa.

```text
CLASSIFICADO -> (colocacao, tipoClasse) -> TemporadaPontuacao atual
DESCLASSIFICADO -> TemporadaPenalidade.DESCLASSIFICACAO atual
AUSENTE -> TemporadaPenalidade.AUSENCIA atual
```

## Decisões consolidadas

- resultado pertence a um ciclo de `Inscricao`;
- todo resultado possui categoria/classe;
- `CLASSIFICADO`, `DESCLASSIFICADO`, `AUSENTE` têm granularidade `(Inscricao, Categoria, Classe)`;
- uma inscrição pode possuir vários resultados;
- no máximo um resultado ativo por combinação inscrição/categoria/classe;
- `CLASSIFICADO` exige colocação inteira positiva, sem máximo funcional;
- colocação sem regra produz impacto zero;
- `DESCLASSIFICADO` e `AUSENTE` têm colocação nula;
- situação é sempre lançada manualmente;
- não inferir ausência/desclassificação;
- não existe empate de colocação no campeonato/categoria/classe;
- unicidade da colocação vale para `PENDENTE_APROVACAO` e `APROVADO`;
- resultado pendente `CLASSIFICADO` reserva sua colocação até aprovação, reprovação/cancelamento ou alteração válida;
- conflito de colocação é bloqueado já no lançamento/edição, não postergado para aprovação;
- proteção de unicidade deve ser transacional e resistente a concorrência;
- posições não utilizadas podem não ser lançadas;
- todo lançamento nasce `PENDENTE_APROVACAO`;
- todas as situações usam o mesmo fluxo de aprovação do atleta;
- somente `APROVADO` produz efeito esportivo;
- criador/`ADMINISTRACAO` de temporada associada podem lançar/editar resultados dos atletas pertencentes à temporada sem vínculo próprio com o atleta;
- `CONSULTA` não autoriza lançamento/edição;
- vínculo profissional-atleta isolado não autoriza resultado;
- autorização é contextual e revalidada a cada operação;
- `ENCERRADA` permite resultado tardio sem reabertura;
- `CANCELADA` não fundamenta operação ordinária enquanto cancelada;
- edição pendente mantém resultado e incrementa `nrVersao`;
- `nrVersao` e `lockVersion` permanecem separados;
- após `APROVADO`, profissional não edita pelo fluxo ordinário;
- proprietário pode corrigir/cancelar aprovado com justificativa;
- resultado cancelado permanece histórico;
- histórico funcional é explícito e imutável;
- `tbPontuacaoHist` não é semanticamente equivalente a `Resultado`.
