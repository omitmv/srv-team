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

Após `APROVADO`, a permissão ordinária de edição por profissionais deixa de existir. Intervenções posteriores seguem o fluxo administrativo específico a ser refinado para o proprietário.

Se o vínculo do profissional com o atleta deixar de estar ativo antes da edição, ele não pode mais modificar o resultado, mesmo que tenha sido seu responsável pelo lançamento original.

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
- eventual cancelamento administrativo posterior.

## Relação com a pontuação

`Resultado` não deve persistir um valor de pontos como verdade autoritativa.

A pontuação é derivada dinamicamente a partir de:

- resultado `APROVADO`;
- colocação;
- tipo da classe (`COMUM` ou `OVERALL`);
- tabela de pontos da temporada elegível.

Portanto, o mesmo resultado aprovado pode gerar pontuação diferente em temporadas distintas.

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
- Reprovação pelo atleta transforma o lançamento em `CANCELADO`.
- Resultado cancelado permanece no histórico.
- Após reprovação, o profissional cria um novo resultado; não corrige o registro cancelado como se fosse o mesmo lançamento.
- O novo resultado precisa de nova aprovação do atleta.
- Apenas resultados `APROVADO` participam de pontuação, rankings e relatórios esportivos.

## Próximos pontos de refinamento

1. Definir intervenção do proprietário sobre resultado `APROVADO`.
2. Definir cancelamento administrativo de resultado aprovado.
3. Fechar estratégia técnica de controle de versão/concor­rência para impedir aprovação de uma versão desatualizada.
4. Refinar migração/aposentadoria de `tbPontuacaoHist`.
