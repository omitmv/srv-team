# Refinamento técnico — Ranking

Status: regras funcionais consolidadas; critérios de desempate e numeração após empate permanecem pendentes de decisão do produto.

## Objetivo

Modelar o ranking de temporada como uma projeção dinâmica derivada de elegibilidade, inscrições, resultados aprovados e tabela de pontuação da própria temporada.

O ranking não deve ser persistido inicialmente como fonte autoritativa. Ele é resultado de cálculo.

## Princípio geral

O ranking pertence à `Temporada` e é calculado sobre o conjunto completo de atletas elegíveis daquela temporada.

Filtros de visibilidade do usuário que consulta são aplicados somente depois da posição esportiva ter sido calculada.

Portanto, o sistema não deve recalcular posições diferentes para observadores diferentes.

## Elegibilidade para ranking geral

Um atleta integra o ranking geral da temporada quando satisfaz simultaneamente as regras de elegibilidade já definidas para a temporada:

1. possui vínculo ativo com o criador da temporada;
2. possui ao menos uma `Inscricao` `CONFIRMADA` e não cancelada em campeonato associado à temporada;
3. sua conta de atleta não está cancelada.

Não é necessário possuir resultado aprovado para aparecer no ranking geral.

Assim que a primeira inscrição elegível existir, o atleta entra no ranking geral com zero pontos.

## Exclusão dinâmica

O atleta deixa de integrar temporariamente ou definitivamente o ranking quando deixa de satisfazer alguma regra de elegibilidade.

Exemplos:

- conta do atleta cancelada;
- encerramento do vínculo com o criador da temporada;
- cancelamento da única inscrição que o tornava elegível;
- remoção do campeonato da temporada quando não restar outro campeonato associado com inscrição confirmada.

A exclusão não apaga resultados, inscrições nem histórico. Apenas altera a projeção atual do ranking.

Se a elegibilidade voltar a existir, o atleta retorna ao ranking com os resultados aprovados ainda válidos.

## Resultados considerados

Para pontuação do ranking, considerar apenas `Resultado` que:

- pertence a uma inscrição elegível da temporada;
- está `APROVADO`;
- não está `CANCELADO`;
- pertence a campeonato associado à temporada.

Resultados `PENDENTE_APROVACAO` e `CANCELADO` não contribuem.

A pontuação não é lida do resultado. É calculada pela tabela da própria temporada usando:

```text
(colocacao, tipoClasse)
```

onde `tipoClasse` é derivado da `Classe` (`COMUM` ou `OVERALL`).

Se a temporada não possuir pontuação configurada para aquela colocação/tipo de classe, o resultado contribui com zero pontos.

## Ranking geral

O ranking geral soma os pontos de todos os resultados válidos do atleta na temporada, sem restringir categoria.

Inclui:

- classes `COMUM`;
- classes `OVERALL`;
- todas as categorias;
- todos os campeonatos associados à temporada.

Exemplo conceitual:

```text
Atleta A

Campeonato 1
  Classic / Sênior  -> 10
  Classic / Overall -> 15

Campeonato 2
  Bodybuilding / Master -> 8

Total geral = 33
```

O Overall participa do total geral como qualquer outro resultado, aplicando a tabela específica de `tipoClasse = OVERALL`.

## Ranking por categoria

O ranking por categoria é uma projeção independente para cada `Categoria`.

O total do atleta na categoria soma apenas resultados aprovados daquela categoria, incluindo a classe `OVERALL` pertencente à categoria.

Um atleta só passa a integrar o ranking daquela categoria após possuir ao menos um resultado `APROVADO` nela.

Essa regra vale mesmo quando o resultado gera zero pontos.

Consequentemente:

- inscrição confirmada sem resultado aprovado -> aparece no ranking geral com zero;
- não aparece em nenhum ranking de categoria ainda;
- primeiro resultado aprovado em uma categoria -> passa a aparecer naquela categoria;
- se esse resultado não possuir pontuação configurada -> aparece com zero.

## Resultado Overall

O Overall pertence à categoria através da `Classe` com `tipoClasse = OVERALL`.

Portanto:

- seus pontos entram no ranking geral;
- seus pontos também entram no ranking da categoria correspondente;
- não existe um ranking separado obrigatório de Overall no MVP.

## Pontuação zero

Zero ponto não significa ausência do ranking.

### Ranking geral

Atleta elegível aparece com zero mesmo sem qualquer resultado aprovado.

### Ranking por categoria

Atleta aparece com zero se já possui resultado aprovado na categoria, ainda que:

- a colocação esteja fora da tabela;
- a tabela atribua explicitamente zero;
- o somatório dos resultados daquela categoria seja zero.

## Cálculo da posição

O universo para cálculo da posição deve ser formado antes dos filtros de visualização do observador.

Sequência conceitual:

```text
1. obter atletas elegíveis da temporada
2. excluir contas de atleta canceladas
3. calcular pontuação de cada atleta
4. ordenar o conjunto esportivo
5. atribuir posições
6. somente depois aplicar filtros de visibilidade da consulta
```

Isso evita situações em que o mesmo atleta aparece em posições diferentes dependendo de quem consulta.

## Filtros de visibilidade

Permissões de consulta não alteram o universo esportivo usado para determinar a posição.

Exemplo:

```text
Ranking real:
1. Atleta A - 30
2. Atleta B - 20
3. Atleta C - 10

Usuário só pode visualizar A e C.

Consulta exibida:
1. Atleta A - 30
3. Atleta C - 10
```

Não renumerar C para posição 2 apenas porque B está oculto para aquele observador.

## Alterações que exigem recálculo

Como o ranking é derivado, qualquer mudança relevante deve refletir imediatamente no cálculo subsequente.

Exemplos:

- aprovação de resultado;
- cancelamento de resultado;
- correção administrativa de resultado aprovado;
- alteração da tabela de pontos da temporada;
- associação ou remoção de campeonato da temporada;
- confirmação/cancelamento de inscrição;
- criação/encerramento de vínculo com o criador;
- cancelamento/reativação da conta do atleta;
- inativação de resultado por cancelamento de inscrição conforme regras definidas.

Não é necessário manter uma entidade `Ranking` autoritativa para conseguir esse comportamento.

## Persistência e performance

Para o MVP, o ranking deve ser tratado conceitualmente como projeção calculada.

Se o volume justificar otimização futura, pode ser introduzida materialização/cache, desde que:

- não se torne fonte autoritativa independente;
- possa ser completamente reconstruída a partir das entidades de domínio;
- seja invalidada/recalculada quando ocorrer qualquer evento que altere elegibilidade ou pontuação.

Materialização prematura não é necessária para fechar o modelo de domínio.

## Precisão numérica

O somatório deve usar `BigDecimal`, seguindo a mesma precisão da pontuação da temporada.

Não converter para `double`/`float` para ordenação ou agregação.

Evitar arredondamentos intermediários desnecessários.

A comparação para ordenação deve usar o valor numérico real do `BigDecimal`.

## Empates

A regra funcional já confirmada estabelece que totais iguais compartilham posição.

Exemplo:

```text
Atleta A = 30
Atleta B = 30
Atleta C = 20
```

A e B devem aparecer empatados na mesma colocação.

Entretanto, ainda existem duas decisões de produto não fechadas:

1. critérios de desempate adicionais, caso futuramente seja desejado eliminar ou ordenar empates esportivos;
2. padrão de numeração após empate:
   - competição/dense gap: `1, 1, 3`;
   - ranking denso: `1, 1, 2`.

Até essa decisão, a implementação não deve inventar critérios como:

- maior número de vitórias;
- maior quantidade de Overall;
- melhor resultado recente;
- melhor colocação individual;
- data de cadastro;
- ordem alfabética.

Ordem alfabética pode eventualmente ser usada apenas como ordenação visual estável entre atletas empatados, sem alterar a posição esportiva, se necessário para a interface. Isso não constitui desempate esportivo.

## Ordenação recomendada da consulta

Enquanto não houver critério esportivo de desempate, a projeção pode ordenar por:

1. pontuação total decrescente;
2. posição esportiva;
3. nome do atleta apenas como estabilidade visual entre registros da mesma posição.

O terceiro item não altera a colocação.

## Ranking encerrado

Uma `Temporada` `ENCERRADA` continua consultável e seu ranking continua sendo derivado dos dados vigentes conforme as regras consolidadas.

Isso significa que alterações administrativas que afetem dados históricos válidos podem modificar a projeção do ranking mesmo após o encerramento, conforme já definido no refinamento de temporada.

Não criar snapshot imutável de ranking no MVP.

## Relatórios

Relatórios de temporada podem utilizar a mesma projeção de ranking usada na consulta.

Relatório de campeonato, por outro lado, exibe colocação esportiva do campeonato e não a pontuação da temporada como se fosse propriedade universal daquele resultado.

Quando um relatório de temporada detalhar por campeonato, pode demonstrar a pontuação que aquele resultado produziu naquela temporada específica.

## Invariantes consolidadas

- Ranking pertence à temporada e é derivado dinamicamente.
- Não existe entidade `Ranking` autoritativa no MVP.
- Elegibilidade é calculada antes da pontuação.
- Conta de atleta cancelada exclui o atleta do universo do ranking.
- Ranking geral inclui atleta elegível desde a primeira inscrição confirmada, mesmo com zero pontos.
- Ranking por categoria exige ao menos um resultado aprovado na categoria.
- Resultado aprovado que vale zero ainda inclui o atleta no ranking da categoria.
- Somente resultados aprovados e válidos participam da pontuação.
- Ranking geral soma todas as categorias e inclui Overall.
- Ranking por categoria inclui o Overall da própria categoria.
- Pontuação é calculada segundo a tabela da própria temporada.
- Um mesmo resultado pode produzir pontuações diferentes em temporadas diferentes.
- Filtros de visibilidade são aplicados depois do cálculo das posições.
- Filtros de visibilidade não renumeram posições.
- Totais iguais compartilham posição.
- Critério de desempate esportivo permanece indefinido.
- Numeração após empate (`1,1,3` versus `1,1,2`) permanece indefinida.
- Ranking usa `BigDecimal` para agregação e comparação.

## Decisões ainda abertas

### D1 — Numeração após empate

Escolher entre:

- **ranking de competição:** `1, 1, 3, 4...`;
- **ranking denso:** `1, 1, 2, 3...`.

Recomendação técnica/produto: usar **ranking de competição (`1, 1, 3`)**, por representar melhor a ideia de colocação esportiva: se dois atletas ocupam o primeiro lugar, duas posições foram ocupadas e o próximo é terceiro.

### D2 — Critérios adicionais de desempate

Recomendação para o MVP: **não criar desempate adicional**. Pontuações iguais permanecem empatadas.

Adicionar desempate somente quando existir regra esportiva explícita da organização, evitando uma política arbitrária embutida no software.
