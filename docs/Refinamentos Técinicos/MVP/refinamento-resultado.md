# Refinamento técnico — Resultado

Status: modelagem técnica consolidada para o MVP; implementação e migração pendentes.

## Objetivo

Modelar `Resultado` como a colocação obtida por um atleta em uma combinação de categoria e classe dentro de um ciclo específico de inscrição em campeonato, com validação obrigatória pelo atleta antes de produzir efeitos esportivos.

## Relação com Inscrição

`Resultado` referencia `cdInscricao`.

A inscrição identifica de forma autoritativa:

- atleta;
- campeonato;
- ciclo de participação.

Não duplicar `cdAtleta` e `cdCompeticao` em `Resultado` como fontes autoritativas.

## Multiplicidade

Uma inscrição pode possuir vários resultados.

Dentro da mesma inscrição, deve existir no máximo um resultado ativo para cada combinação:

```text
(cdInscricao, cdCategoria, cdClasse)
```

Resultados históricos anteriores da mesma combinação podem permanecer armazenados desde que não sejam simultaneamente ativos.

## Entidade conceitual

Campos recomendados:

- `cdResultado`
- `cdInscricao`
- `cdCategoria`
- `cdClasse`
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

`nrVersao` representa a versão de negócio submetida ao atleta. `lockVersion` representa exclusivamente o controle técnico de concorrência otimista.

## Estados do resultado

Estados mínimos para o MVP:

- `PENDENTE_APROVACAO`
- `APROVADO`
- `CANCELADO`

Não é necessário manter `REPROVADO` como estado ativo separado.

A reprovação é uma ação do atleta que encerra aquele lançamento e o transforma em `CANCELADO`, preservando no histórico o motivo, o responsável pela decisão e a data.

Fluxo principal:

```text
Novo lançamento
      |
      v
PENDENTE_APROVACAO
      |
      +-- atleta aprova --> APROVADO
      |
      +-- atleta reprova -> CANCELADO
```

## Validade esportiva

Um resultado somente é válido para relatórios esportivos, pontuação e rankings quando estiver em `APROVADO`.

Consequentemente:

- `PENDENTE_APROVACAO` não gera pontos;
- `PENDENTE_APROVACAO` não participa dos rankings;
- `CANCELADO` não gera pontos;
- `CANCELADO` não participa dos rankings;
- apenas `APROVADO` participa dos cálculos, desde que as demais regras de elegibilidade da temporada também sejam satisfeitas.

## Edição enquanto pendente

Enquanto o resultado estiver em `PENDENTE_APROVACAO`, ele pode ser editado por qualquer profissional que possua vínculo ativo com o atleta da inscrição.

A autorização não depende de o profissional ter sido o responsável pelo lançamento original.

Regra de autorização mínima:

```text
Resultado.status == PENDENTE_APROVACAO
AND
existe VinculoProfissionalAtleta ATIVO
para (profissional, atleta da Inscricao)
```

A edição deve:

- manter o mesmo `cdResultado`;
- atualizar os dados corrigidos do resultado;
- incrementar `nrVersao`;
- registrar quem realizou a alteração e quando;
- invalidar qualquer solicitação de aprovação vinculada a uma versão anterior;
- manter o resultado em `PENDENTE_APROVACAO`;
- exigir aprovação do atleta sobre a versão atualizada antes de qualquer efeito esportivo.

Após `APROVADO`, a permissão ordinária de edição por profissionais deixa de existir.

Se o vínculo do profissional com o atleta deixar de estar ativo antes da edição, ele não pode mais modificar o resultado, mesmo que tenha sido seu responsável pelo lançamento original.

## Intervenção do proprietário sobre resultado aprovado

O proprietário pode intervir diretamente em um resultado `APROVADO`.

São permitidas duas operações administrativas:

1. correção direta do resultado aprovado;
2. cancelamento do resultado aprovado.

Essas operações possuem efeito imediato e não exigem nova aprovação do atleta.

Toda intervenção administrativa sobre resultado aprovado exige justificativa obrigatória e deve ser integralmente auditada.

Nenhuma notificação deve ser enviada ao atleta, profissionais ou demais usuários em decorrência da correção ou do cancelamento administrativo realizado pelo proprietário.

### Correção direta

Ao corrigir um resultado `APROVADO`, o proprietário pode alterar os dados esportivos do registro, incluindo categoria, classe e colocação, respeitando as invariantes estruturais do domínio.

A correção administrativa deve:

- exigir justificativa obrigatória;
- manter o mesmo `cdResultado`;
- incrementar `nrVersao`;
- manter `status = APROVADO`;
- registrar proprietário responsável pela alteração;
- registrar data/hora;
- registrar a justificativa da intervenção;
- preservar os valores anteriores em histórico/auditoria;
- recalcular pontuação, rankings e relatórios afetados imediatamente;
- não gerar notificações.

Não há retorno para `PENDENTE_APROVACAO` e não há nova solicitação de aprovação ao atleta.

Se a correção alterar categoria ou classe, a combinação resultante deve continuar respeitando a invariante de no máximo um resultado ativo por `(cdInscricao, cdCategoria, cdClasse)`.

### Cancelamento administrativo

O proprietário também pode cancelar diretamente um resultado `APROVADO`.

Nesse caso:

```text
APROVADO -> CANCELADO
```

O cancelamento deve:

- exigir justificativa obrigatória;
- registrar `cdResponsavelCancelamento`;
- registrar `dtCancelamento`;
- registrar `motivoCancelamento`;
- preservar o resultado integralmente no histórico;
- remover imediatamente o resultado de pontuação, rankings e relatórios esportivos;
- recalcular todas as temporadas e projeções afetadas;
- liberar a combinação `(cdInscricao, cdCategoria, cdClasse)` para um novo lançamento, caso necessário;
- não gerar notificações.

O cancelamento administrativo não exclui fisicamente o resultado.

Após cancelado, qualquer novo lançamento para a mesma combinação nasce novamente como `PENDENTE_APROVACAO` e segue o fluxo normal de aprovação do atleta, salvo nova intervenção direta do proprietário.

## Reprovação pelo atleta

Quando o atleta reprova um resultado:

1. o lançamento atual é encerrado;
2. `status` passa para `CANCELADO`;
3. registrar o atleta como responsável pela decisão;
4. registrar data/hora da decisão;
5. registrar o motivo da reprovação conforme regra do MVP;
6. preservar integralmente o registro para histórico e auditoria;
7. liberar a combinação `(cdInscricao, cdCategoria, cdClasse)` para um novo lançamento.

A reprovação não altera o registro anterior para corrigir seus dados.

O profissional deve criar um novo `Resultado` com os dados corretos.

## Relançamento após reprovação

Após o cancelamento decorrente da reprovação, o profissional autorizado pode lançar novamente um resultado para a mesma inscrição, categoria e classe.

Esse novo lançamento:

- recebe nova identidade (`cdResultado`);
- nasce novamente como `PENDENTE_APROVACAO`;
- não herda aprovação do registro anterior;
- somente passa a produzir efeitos esportivos após nova aprovação do atleta.

## Controle de versão e concorrência

O MVP deve distinguir dois conceitos:

### 1. Versão de negócio — `nrVersao`

`nrVersao` identifica exatamente qual conteúdo do resultado foi apresentado ao atleta para aprovação.

Regras:

- novo resultado nasce com `nrVersao = 1`;
- toda alteração em dados esportivos relevantes incrementa `nrVersao`;
- no fluxo ordinário, alterações relevantes incluem pelo menos categoria, classe e colocação;
- correção administrativa do proprietário também incrementa `nrVersao`;
- aprovação ou reprovação do atleta deve informar o `nrVersao` que ele visualizou;
- o backend deve comparar a versão recebida com a versão atual antes de aplicar a decisão.

Uma decisão referente a versão antiga nunca deve ser convertida automaticamente em decisão sobre a versão atual.

### 2. Concorrência técnica — `lockVersion`

Além de `nrVersao`, a entidade deve possuir controle de concorrência otimista técnico, preferencialmente com JPA `@Version`.

Exemplo conceitual:

```java
@Version
private Long lockVersion;
```

Esse campo não possui significado esportivo e não deve ser usado como versão apresentada ao atleta.

Sua função é impedir lost update quando duas requisições concorrentes tentarem alterar o mesmo `Resultado`.

### Por que separar os dois campos

Não usar `@Version` como substituto de `nrVersao`.

```text
nrVersao    = versão funcional aprovada/reprovada pelo atleta
lockVersion = versão técnica para optimistic locking
```

Essa separação mantém o contrato de negócio estável e evita acoplamento da regra de aprovação ao mecanismo de persistência.

## Aprovação do atleta com controle de versão

A solicitação de aprovação deve transportar, de forma confiável:

- `cdResultado`;
- `nrVersao` esperado.

Ao receber aprovação, o backend deve validar na mesma transação:

1. resultado existe;
2. pertence ao atleta autenticado;
3. está `PENDENTE_APROVACAO`;
4. `nrVersao` informado corresponde ao atual;
5. nenhuma alteração concorrente venceu a transação;
6. somente então alterar para `APROVADO`.

Se a versão estiver desatualizada, a operação deve falhar sem alterar o estado.

Resposta de API recomendada para conflito de versão/estado concorrente: `409 Conflict`.

O mesmo princípio vale para reprovação pelo atleta.

## Edições concorrentes por profissionais

Como mais de um profissional vinculado pode editar um resultado pendente, duas alterações podem ocorrer praticamente ao mesmo tempo.

O backend não deve realizar merge implícito de alterações concorrentes no MVP.

A segunda requisição deve receber conflito e recarregar o estado atual antes de tentar novamente.

## Atomicidade da decisão

A verificação de versão e a mudança de estado devem ocorrer dentro da mesma transação.

A persistência deve garantir que nenhuma edição concorrente possa ocorrer silenciosamente entre a validação da versão e a mudança para `APROVADO` ou `CANCELADO`.

## Invariante de resultado ativo

Para a combinação:

```text
(cdInscricao, cdCategoria, cdClasse)
```

deve existir no máximo um resultado que esteja em estado ativo para o fluxo corrente.

Para o MVP, considerar como estados que ocupam a combinação:

- `PENDENTE_APROVACAO`
- `APROVADO`

`CANCELADO` libera a combinação para novo lançamento.

Essa regra deve ser protegida transacionalmente para evitar dois lançamentos concorrentes para a mesma combinação.

## Histórico funcional

`@Version` não preserva histórico e não deve ser usado para esse propósito.

Criar um mecanismo explícito de histórico funcional. A recomendação para o MVP é uma entidade `ResultadoHistorico`, vinculada ao `Resultado` atual.

### Entidade conceitual `ResultadoHistorico`

Campos recomendados:

- `cdResultadoHistorico`
- `cdResultado`
- `nrVersao`
- `tipoEvento`
- `cdInscricao`
- `cdCategoria`
- `cdClasse`
- `colocacao`
- `status`
- `cdResponsavelEvento`
- `dtEvento`
- `justificativa`
- campos de auditoria

O histórico deve armazenar um snapshot suficiente para reconstruir o estado funcional do resultado naquele momento.

### Tipos de evento

Enum conceitual recomendado:

- `CRIACAO`
- `EDICAO_PENDENTE`
- `APROVACAO_ATLETA`
- `REPROVACAO_ATLETA`
- `CORRECAO_ADMINISTRATIVA`
- `CANCELAMENTO_ADMINISTRATIVO`

Não usar o histórico como fonte de verdade do estado atual. A fonte de verdade operacional permanece `Resultado`; `ResultadoHistorico` é trilha imutável de auditoria funcional.

### Quando registrar histórico

Registrar evento, no mínimo, em toda operação que altere estado funcional ou dados esportivos do resultado:

- criação;
- edição pendente;
- aprovação;
- reprovação;
- correção administrativa;
- cancelamento administrativo.

Cada operação deve atualizar `Resultado` e inserir seu `ResultadoHistorico` na mesma transação.

Se a gravação do histórico falhar, a alteração funcional também deve falhar.

### Imutabilidade

Registros de `ResultadoHistorico` não podem ser alterados nem excluídos pelo fluxo ordinário da aplicação.

Correções posteriores geram novos eventos; nunca reescrevem eventos anteriores.

### Snapshot e referência histórica

Mesmo que categoria, classe ou inscrição sofram alterações cadastrais futuras, o histórico deve preservar as chaves utilizadas em cada versão.

Quando necessário para requisitos de auditoria mais fortes, descrições exibíveis podem ser reconstruídas por referência ao catálogo; não duplicar nomes na tabela histórica sem necessidade concreta.

## Migração da `tbPontuacaoHist`

A atual `tbPontuacaoHist` possui semântica incompatível com o novo domínio de `Resultado`.

No modelo atual ela relaciona:

- competidor;
- competição;
- registro de pontuação;
- data de cadastro.

O registro de pontuação legado fornece colocação por meio de `Pontuacao`, mas não representa adequadamente:

- ciclo de inscrição com identidade própria;
- categoria;
- classe;
- tipo de classe;
- aprovação do atleta;
- status do resultado;
- versões;
- cancelamento/reprovação;
- autoria das decisões.

Também não foi identificada no código atual uma associação complementar que permita reconstruir de forma confiável `Categoria + Classe` a partir de `PontuacaoHist`.

Portanto, não é seguro transformar automaticamente todo registro de `tbPontuacaoHist` em um `Resultado` do novo domínio atribuindo categoria/classe artificialmente.

### Estratégia de migração recomendada

A migração deve ser dividida em duas partes.

#### 1. Inscrições legadas

Cada registro de `tbCompetidores` deve originar uma `Inscricao` `CONFIRMADA`, conforme refinamento específico de inscrição.

Preservar, quando disponível:

- atleta;
- campeonato;
- data histórica de cadastro.

Essas inscrições servirão como raiz para qualquer resultado legado que possa ser convertido com segurança.

#### 2. Pontuações/resultados legados

Para cada `tbPontuacaoHist`:

1. localizar a inscrição migrada correspondente ao atleta/campeonato;
2. resolver a colocação por meio de `cdPontuacao` quando possível;
3. verificar se existem dados confiáveis para determinar categoria e classe;
4. somente criar `Resultado` se todos os atributos obrigatórios puderem ser reconstruídos sem inferência arbitrária.

Se categoria e classe não puderem ser determinadas de forma confiável, o registro não deve ser convertido em `Resultado` ativo do novo domínio.

### Preservação do legado não conversível

Registros que não puderem ser convertidos integralmente devem permanecer preservados para consulta/auditoria histórica durante a transição.

Opções técnicas aceitáveis:

- manter as tabelas legadas em modo somente leitura durante período de transição; ou
- copiar os registros para uma estrutura explicitamente marcada como legado bruto antes da remoção das tabelas antigas.

Para o MVP, a recomendação é **manter `tbPontuacaoHist` e as dependências legadas em modo somente leitura até a validação completa da migração**, evitando perda de informação.

Não utilizar esses registros incompletos em novos rankings ou pontuações do novo domínio.

### Não inventar aprovação histórica

Mesmo quando um registro legado puder ser convertido em `Resultado`, não há evidência de que tenha passado pelo novo fluxo de aprovação do atleta.

Por isso, registros migrados devem ser explicitamente identificáveis como originados de migração.

Recomendação:

- adicionar `origem` ao `Resultado` ou à auditoria correspondente;
- valores mínimos: `FLUXO_NORMAL`, `MIGRACAO_LEGADO`, `ADMINISTRATIVO` quando necessário.

Para registros convertidos a partir do legado, o proprietário poderá tratá-los como `APROVADO` por migração somente se essa decisão fizer parte do processo de cutover e ficar auditada como `MIGRACAO_LEGADO`.

Não registrar falsamente o atleta como responsável pela aprovação histórica.

### Cutover

Após a migração:

1. bloquear novas gravações em `tbCompetidores`, `tbPontuacaoHist` e estruturas de pontuação legadas substituídas;
2. direcionar toda nova operação para `Inscricao`, `Resultado` e `TemporadaPontuacao`;
3. executar reconciliação quantitativa dos dados migrados;
4. validar amostras funcionais com atleta/campeonato/colocação;
5. somente depois considerar remoção física das tabelas legadas em versão posterior.

A remoção das tabelas antigas não faz parte do primeiro passo da migração.

## Relação com a pontuação

`Resultado` não deve persistir um valor de pontos como verdade autoritativa.

A pontuação é derivada dinamicamente a partir de:

- resultado `APROVADO`;
- colocação;
- tipo da classe (`COMUM` ou `OVERALL`);
- tabela de pontos da temporada elegível.

Portanto, o mesmo resultado aprovado pode gerar pontuação diferente em temporadas distintas.

Qualquer correção ou cancelamento administrativo de um resultado aprovado deve provocar a atualização dos cálculos derivados afetados.

## Decisões consolidadas

- Resultado pertence a um ciclo específico de `Inscricao`.
- Uma inscrição pode possuir vários resultados.
- Há no máximo um resultado ativo por `(Inscricao, Categoria, Classe)`.
- Todo novo lançamento nasce `PENDENTE_APROVACAO`.
- Resultado só é esportivamente válido após aprovação do atleta.
- Enquanto `PENDENTE_APROVACAO`, qualquer profissional com vínculo ativo com o atleta pode editar o resultado.
- O profissional editor não precisa ser o responsável pelo lançamento original.
- Edição de resultado pendente mantém a mesma identidade e incrementa `nrVersao`.
- A aprovação do atleta deve corresponder à `nrVersao` atual do resultado.
- Uma edição invalida qualquer aprovação/solicitação referente à versão anterior.
- `nrVersao` é versão de negócio; `lockVersion` é controle técnico de concorrência.
- Resultado deve usar optimistic locking técnico, preferencialmente via JPA `@Version`.
- Conflitos de edição/aprovação concorrente devem falhar; não realizar merge automático.
- `409 Conflict` é a resposta recomendada para conflito de versão/estado concorrente.
- Após `APROVADO`, profissionais não podem editar pelo fluxo ordinário.
- Proprietário pode corrigir diretamente resultado `APROVADO`.
- Correção administrativa mantém o resultado `APROVADO`, incrementa `nrVersao` e tem efeito imediato.
- Correção administrativa não exige nova aprovação do atleta.
- Correção administrativa exige justificativa obrigatória.
- Proprietário pode cancelar diretamente resultado `APROVADO`.
- Cancelamento administrativo transforma o resultado em `CANCELADO` e tem efeito imediato.
- Cancelamento administrativo exige justificativa obrigatória.
- Intervenções administrativas do proprietário não geram notificações.
- Toda intervenção do proprietário deve ser auditada e preservar o estado anterior.
- Reprovação pelo atleta transforma o lançamento em `CANCELADO`.
- Resultado cancelado permanece no histórico.
- Após reprovação, o profissional cria um novo resultado; não corrige o registro cancelado como se fosse o mesmo lançamento.
- O novo resultado precisa de nova aprovação do atleta.
- Apenas resultados `APROVADO` participam de pontuação, rankings e relatórios esportivos.
- Histórico funcional deve ser explícito e imutável via `ResultadoHistorico` ou mecanismo equivalente.
- Alteração funcional e gravação de histórico devem ocorrer na mesma transação.
- `tbPontuacaoHist` não é semanticamente equivalente a `Resultado`.
- Migração automática só ocorre quando inscrição, categoria, classe e colocação puderem ser reconstruídas com segurança.
- Dados legados incompletos não devem receber categoria/classe inventadas.
- Legado não conversível permanece preservado em modo somente leitura durante a transição.
- Registros migrados devem ter origem identificável e não devem simular aprovação histórica inexistente.

## Próximo refinamento

Com `Resultado` fechado conceitualmente, o próximo refinamento deve tratar a composição dos rankings e critérios de desempate, incluindo ranking geral, ranking por categoria e comportamento de atletas com zero pontos.
