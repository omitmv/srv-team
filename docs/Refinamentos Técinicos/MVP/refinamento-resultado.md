# Refinamento técnico — Resultado

Status: modelagem técnica em andamento; decisões já confirmadas abaixo.

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

Campos iniciais recomendados:

- `cdResultado`
- `cdInscricao`
- `cdCategoria`
- `cdClasse`
- `colocacao`
- `status`
- `versao`
- `cdResponsavelLancamento`
- `dtLancamento`
- `cdResponsavelDecisao`
- `dtDecisao`
- `motivoReprovacao`
- `dtCancelamento`
- `cdResponsavelCancelamento`
- `motivoCancelamento`
- campos de auditoria

A lista poderá ser refinada à medida que os fluxos restantes forem fechados.

## Estados do resultado

Estados mínimos recomendados para o MVP:

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

Isso permite que profissionais diferentes, desde que efetivamente vinculados ao atleta, corrijam o lançamento antes da decisão do atleta.

A edição deve:

- manter o mesmo `cdResultado`;
- atualizar os dados corrigidos do resultado;
- incrementar a versão do resultado;
- registrar quem realizou a alteração e quando;
- invalidar qualquer solicitação de aprovação vinculada a uma versão anterior;
- manter o resultado em `PENDENTE_APROVACAO`;
- exigir aprovação do atleta sobre a versão atualizada antes de qualquer efeito esportivo.

A aprovação do atleta deve sempre estar associada à versão corrente do resultado. Se o resultado for alterado após a emissão de uma solicitação de aprovação, uma tentativa de aprovar a versão anterior deve ser recusada pelo backend.

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
- incrementar a versão do resultado;
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

Exemplo:

```text
Inscrição #123

Resultado #10
  Categoria: Classic Physique
  Classe: Sênior
  Colocação: 2
  Status: CANCELADO
  Motivo: atleta reprovou o lançamento

Resultado #11
  Categoria: Classic Physique
  Classe: Sênior
  Colocação: 1
  Status: PENDENTE_APROVACAO

Atleta aprova

Resultado #11 -> APROVADO
```

Somente o `Resultado #11` aprovado entra em relatórios esportivos, pontuação e rankings.

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

## Histórico

Nunca sobrescrever um resultado cancelado por reprovação com os dados corrigidos do novo lançamento.

O histórico deve permitir reconstruir:

- quem lançou;
- quando lançou;
- quais dados foram informados;
- quais alterações ocorreram enquanto pendente;
- qual profissional realizou cada alteração;
- qual versão foi submetida ao atleta;
- quem aprovou ou reprovou;
- quando decidiu;
- motivo da reprovação;
- quais correções administrativas foram realizadas após aprovação;
- valores anteriores e novos de cada correção administrativa;
- justificativa de cada intervenção administrativa;
- eventual cancelamento administrativo posterior;
- responsável e data de cada intervenção do proprietário.

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
- Edição de resultado pendente mantém a mesma identidade e incrementa sua versão.
- A aprovação do atleta deve corresponder à versão atual do resultado.
- Uma edição invalida qualquer aprovação/solicitação referente a versão anterior.
- Após `APROVADO`, profissionais não podem editar pelo fluxo ordinário.
- Proprietário pode corrigir diretamente resultado `APROVADO`.
- Correção administrativa mantém o resultado `APROVADO`, incrementa versão e tem efeito imediato.
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

## Próximos pontos de refinamento

1. Fechar estratégia técnica de controle de versão/concor­rência para impedir aprovação de uma versão desatualizada.
2. Refinar migração/aposentadoria de `tbPontuacaoHist`.
