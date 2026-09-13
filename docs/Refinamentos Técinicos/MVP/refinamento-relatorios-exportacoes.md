# Refinamento técnico — Relatórios e exportações

Status: regras funcionais consolidadas a partir do domínio já fechado; decisões de apresentação ainda pendentes.

## Objetivo

Definir relatórios e exportações do MVP sem misturar fatos esportivos do campeonato com a interpretação específica de cada temporada.

Princípio central:

```text
Campeonato = fato esportivo
Temporada = interpretação esportiva derivada
```

Consequentemente:

- relatório de campeonato exibe inscrições/resultados e suas situações esportivas;
- relatório de temporada exibe elegibilidade, contribuições derivadas e ranking;
- pontos e penalidades nunca são apresentados como propriedades universais do campeonato;
- a mesma ocorrência esportiva pode ter impacto diferente em temporadas distintas.

## Escopos do MVP

O MVP possui dois escopos principais de relatório:

1. `RelatorioCampeonato`
2. `RelatorioTemporada`

Ambos devem existir em:

- consulta em tela;
- exportação PDF;
- exportação Excel `.xlsx`.

Tela e exportação devem aplicar as mesmas regras de autorização, elegibilidade e visibilidade.

## Relatório de campeonato

O relatório de campeonato representa fatos esportivos armazenados em `Inscricao` e `Resultado`.

### Não depende de pontuação da temporada

O relatório de campeonato não exibe pontos ou penalidades como atributo do resultado.

Um mesmo campeonato pode pertencer a múltiplas temporadas, portanto qualquer coluna de pontos sem contexto de temporada seria semanticamente incorreta.

### Resultados exibidos

Na visão esportiva ordinária, considerar somente `Resultado APROVADO` e não cancelado.

Resultados `PENDENTE_APROVACAO` e `CANCELADO` não fazem parte do resultado esportivo publicado.

Histórico administrativo é consulta distinta e não deve ser misturado ao relatório esportivo comum.

### Informações mínimas por resultado

- atleta;
- categoria;
- classe;
- `situacaoResultado`;
- colocação, quando `CLASSIFICADO`;
- indicação de `DESCLASSIFICADO` ou `AUSENTE` quando aplicável.

`DESCLASSIFICADO` e `AUSENTE` não devem ser exibidos como colocação numérica fictícia.

### Ordenação esportiva

Dentro de cada combinação categoria/classe:

1. `CLASSIFICADO` por colocação crescente;
2. situações sem colocação podem ser exibidas após os classificados;
3. ordenação secundária de apresentação pode ser por nome do atleta apenas para estabilidade visual.

A ordenação visual não cria nova regra esportiva.

### Inscritos sem resultado

A inscrição confirmada sem resultado aprovado pode ser exibida em visão analítica de participação como `Sem resultado`, sem inferir `AUSENTE`.

A ausência de resultado nunca gera situação esportiva automaticamente.

## Relatório de temporada

O relatório de temporada representa a projeção da temporada no instante da consulta/exportação.

Utiliza as mesmas regras de `refinamento-ranking.md`.

### Universo

O universo é composto pelos atletas atualmente elegíveis para a temporada.

A posição esportiva é calculada sobre todo o conjunto elegível antes de qualquer filtro de visualização do observador.

### Ranking sintético

Informações mínimas:

- posição;
- atleta;
- total de pontos;
- identificação da temporada.

Empates seguem ranking de competição:

```text
1, 1, 3, 4...
```

Nenhum critério adicional de desempate no MVP.

### Relatório analítico da temporada

Deve permitir decompor o total do atleta em contribuições individuais derivadas de cada `Resultado APROVADO` válido e elegível.

Informações mínimas por contribuição:

- campeonato;
- categoria;
- classe;
- situação esportiva;
- colocação, quando existente;
- tipo da contribuição: `PONTUACAO`, `PENALIDADE_DESCLASSIFICACAO` ou `PENALIDADE_AUSENCIA`;
- valor da contribuição na configuração atual da temporada.

Exemplo:

```text
Atleta A

Campeonato X / Classic / Sênior / 1º
-> PONTUACAO = 10

Campeonato Y / Classic / Overall / 2º
-> PONTUACAO = 6

Campeonato Z / Classic / Master / AUSENTE
-> PENALIDADE_AUSENCIA = -2

TOTAL = 14
```

## Configuração atual e recálculo

Relatórios de temporada sempre utilizam a configuração atual de `TemporadaPontuacao` e `TemporadaPenalidade`.

O MVP não reconstrói a pontuação histórica segundo a regra vigente na data em que o resultado foi aprovado.

Se a pontuação de `1º COMUM` mudar de `10` para `12`, uma nova consulta ou exportação da temporada passa a apresentar `12` para todos os resultados válidos/elegíveis correspondentes.

O resultado esportivo permanece inalterado; muda sua interpretação na temporada.

## Temporada ENCERRADA

Temporada `ENCERRADA` continua permitindo consulta e exportação de ranking e relatórios.

O relatório continua dinâmico e pode mudar por:

- resultado tardio aprovado;
- correção/cancelamento válido de resultado;
- alteração de inscrição;
- alteração de vínculo que afete elegibilidade;
- reativação/cancelamento de conta do atleta;
- demais eventos de domínio já definidos.

`ENCERRADA` não representa snapshot imutável.

## Temporada CANCELADA

Temporada `CANCELADA` preserva dados e histórico, porém sua projeção esportiva fica suspensa.

Consultas administrativas podem exibir a temporada cancelada e seus dados preservados, mas o relatório esportivo não deve apresentá-la como ranking vigente.

Na reativação, a projeção é recalculada com dados e regras atuais.

## Visibilidade do observador

A autorização de consulta não altera o universo esportivo nem a posição calculada.

Fluxo:

```text
1. calcular universo elegível completo
2. calcular impactos
3. calcular totais
4. atribuir posições reais
5. aplicar filtros de visibilidade do observador
```

Exemplo:

```text
Ranking real:
1º Atleta A
2º Atleta B
3º Atleta C

Observador só pode ver A e C:
1º Atleta A
3º Atleta C
```

Não renumerar para `1º` e `2º`.

## CONSULTA e ADMINISTRACAO

Na temporada:

- criador possui acesso administrativo por definição;
- `ADMINISTRACAO` pode consultar integralmente os dados necessários à operação da temporada;
- `CONSULTA` visualiza dados individualizados apenas dos atletas da temporada com os quais também possui vínculo ativo;
- proprietário mantém acesso administrativo global conforme as regras do MVP.

Exportações devem aplicar a mesma regra de visibilidade da consulta em tela.

## Conta de atleta cancelada

Atleta com conta cancelada não integra ranking vigente.

Para usuários comuns, seus dados ficam ocultos/suspensos conforme regra transversal do MVP.

O proprietário pode consultar/exportar administrativamente os dados preservados, sem que isso reinsira o atleta no ranking.

Reativação recalcula os rankings aplicáveis.

## Exportações

Formatos do MVP:

- PDF;
- Excel `.xlsx`.

### Regra de equivalência

Uma exportação representa o mesmo conjunto de dados que o usuário teria autorização para consultar naquele momento, respeitando o mesmo escopo e filtros solicitados.

Não criar uma autorização mais ampla apenas porque o formato é arquivo.

### Data/hora de geração

Toda exportação deve registrar data/hora de geração, pois rankings e interpretações são dinâmicos.

### Arquivo gerado não é fonte autoritativa

PDF/Excel é fotografia dos dados no momento da geração.

Alterações posteriores no domínio não modificam retroativamente arquivos já baixados.

Se o sistema armazenar arquivos ou links de download, o acesso futuro ao arquivo armazenado deve revalidar autorização vigente; possuir uma URL antiga não concede acesso permanente.

### Auditoria

Registrar, no mínimo:

- usuário solicitante;
- tipo de relatório;
- escopo (`Campeonato` ou `Temporada`);
- identificador do agregado consultado;
- formato;
- data/hora da geração.

Não é necessário transformar a exportação em entidade de domínio esportivo.

## Filtros

Filtros não alteram regra esportiva.

Filtros possíveis para evolução da interface incluem:

- campeonato;
- atleta;
- categoria;
- classe;
- situação esportiva.

Entretanto, ranking por categoria permanece fora do MVP.

Filtrar linhas analíticas por categoria não significa calcular uma nova classificação por categoria.

## Consistência entre tela, PDF e Excel

Para os mesmos parâmetros e instante lógico de leitura, tela, PDF e Excel devem refletir:

- mesmo universo autorizado;
- mesmas regras de elegibilidade;
- mesmos totais;
- mesmas posições;
- mesmos valores de contribuição.

Diferenças de layout são permitidas; diferenças semânticas não.

## Precisão numérica

Pontos, penalidades, subtotais e totais devem usar `BigDecimal` e respeitar a precisão definida no domínio.

Não recalcular ou arredondar de forma diferente entre tela, PDF e Excel.

## Concorrência durante geração

A exportação deve representar uma leitura logicamente consistente.

Não é aceitável gerar um arquivo parcialmente com configuração antiga e parcialmente com configuração nova em uma mesma execução.

O mecanismo técnico pode ser definido na implementação, mas o requisito funcional é consistência da fotografia gerada.

## Relatórios fora do MVP

Não fazem parte do MVP atual:

- ranking por categoria;
- ranking histórico congelado por data;
- comparação entre temporadas;
- dashboards analíticos avançados;
- estatísticas evolutivas do atleta;
- distribuição de posições;
- gráficos históricos;
- BI externo.

Podem ser adicionados posteriormente sem alterar o modelo esportivo central.

## Invariantes consolidadas

- relatório de campeonato representa fato esportivo e não exibe pontuação universal;
- relatório de temporada representa interpretação derivada da temporada;
- somente resultados aprovados/não cancelados aparecem como resultado esportivo publicado;
- inscrição sem resultado pode aparecer como `Sem resultado`, nunca como ausência inferida;
- `DESCLASSIFICADO`/`AUSENTE` não viram colocação fictícia;
- ranking de temporada usa configuração atual de pontuação/penalidade;
- relatório de `ENCERRADA` permanece dinâmico;
- `CANCELADA` não produz ranking vigente enquanto cancelada;
- filtros de visibilidade são aplicados depois da posição esportiva;
- exportação aplica a mesma autorização da tela;
- PDF/Excel são fotografias do momento da geração, não fontes de verdade;
- arquivos armazenados exigem revalidação de autorização no download;
- data/hora de geração deve ser identificável;
- tela/PDF/Excel devem ser semanticamente equivalentes;
- cálculo usa `BigDecimal` de forma consistente;
- ranking por categoria permanece fora do MVP.

## Decisões de apresentação ainda abertas

As regras de domínio acima estão fechadas. Ainda precisam ser definidos no refinamento de UX/contrato:

1. colunas finais de cada visão sintética e analítica;
2. agrupamentos padrão no Excel;
3. orientação/layout do PDF;
4. filtros expostos na primeira versão da interface;
5. paginação/limite de linhas em tela;
6. estratégia técnica para exportações de grande volume.

Esses itens não alteram a semântica esportiva consolidada neste documento.
