# Refinamento técnico — Categoria, Classe e Overall

Status: modelagem técnica consolidada para o MVP; detalhes de migração e implementação pendentes.

## Objetivo

Modelar os catálogos de `Categoria` e `Classe` usados no lançamento dos resultados e definir de forma explícita como o sistema distingue classes comuns de `Overall` para aplicação da tabela de pontuação da temporada.

## Regras de negócio já consolidadas

- Categorias e classes pertencem a catálogos centrais administrados pelo proprietário.
- Categorias/classes não são previamente selecionadas para compor um campeonato.
- No lançamento do resultado, o usuário informa categoria e classe.
- Um campeonato pode possuir resultados em várias categorias e classes.
- Há várias classes comuns dentro de uma categoria.
- Existe um único `Overall` por categoria por campeonato.
- A tabela da temporada diferencia apenas `COMUM` e `OVERALL`; a categoria não altera o valor da pontuação.

## Estrutura conceitual

```text
Categoria
 ├── cdCategoria
 ├── dsNome
 ├── flAtivo
 └── auditoria

Classe
 ├── cdClasse
 ├── cdCategoria
 ├── dsNome
 ├── tipoClasse = COMUM | OVERALL
 ├── flAtivo
 └── auditoria
```

A relação é:

```text
Categoria 1 ---- N Classe
```

Uma `Classe` pertence exatamente a uma `Categoria`.

## Categoria

Entidade conceitual `Categoria`.

Campos recomendados:

- `cdCategoria`
- `dsNome`
- `flAtivo`
- `dtCadastro`
- campos de auditoria

Regras:

- `dsNome` obrigatório;
- nome deve possuir limite de tamanho;
- não permitir duplicidade lógica de categoria ativa com o mesmo nome normalizado;
- categoria utilizada historicamente não deve ser excluída fisicamente;
- inativação impede novos lançamentos comuns, mas preserva resultados existentes.

A normalização canônica para unicidade deve:

1. remover espaços nas extremidades;
2. aplicar normalização Unicode `NFKD`;
3. remover marcas Unicode combinantes/diacríticos;
4. normalizar caixa usando `Locale.ROOT`;
5. reduzir qualquer sequência de whitespace para um único espaço ASCII;
6. aplicar `trim` novamente;
7. preservar pontuação e símbolos.

Assim, `SÃO   PAULO` e `Sao Paulo` produzem `sao paulo`, enquanto `Classe-A` e `Classe A` permanecem diferentes.

## Classe

Entidade conceitual `Classe`.

Campos recomendados:

- `cdClasse`
- `cdCategoria`
- `dsNome`
- `tipoClasse`
- `flAtivo`
- `dtCadastro`
- campos de auditoria

Regras:

- toda classe pertence exatamente a uma categoria;
- `dsNome` obrigatório;
- nome deve possuir limite de tamanho;
- `tipoClasse` obrigatório;
- classe utilizada historicamente não deve ser excluída fisicamente;
- inativação impede seleção em novos resultados, preservando referências anteriores;
- não permitir duas classes ativas semanticamente iguais dentro da mesma categoria.

## Tipo de classe

Criar enum explícito:

```text
TipoClasse
- COMUM
- OVERALL
```

Não identificar `Overall` por:

- nome da classe;
- prefixo/sufixo textual;
- comparação case-insensitive com a palavra `Overall`;
- flag inferida no serviço;
- convenção de código numérico sem semântica explícita.

`tipoClasse` é parte do domínio e deve estar persistido/modelado explicitamente.

## Overall

O `Overall` é tratado como uma classe especial da categoria.

Portanto:

```text
Categoria
 ├── Classe A [COMUM]
 ├── Classe B [COMUM]
 ├── Classe C [COMUM]
 └── Overall [OVERALL]
```

### Invariante estrutural

Cada categoria pode possuir no máximo uma classe ativa do tipo `OVERALL`.

Essa regra evita ambiguidade no lançamento e permite que o sistema determine diretamente qual tabela de pontuação utilizar.

A existência de uma classe `OVERALL` no catálogo não significa que todo campeonato obrigatoriamente terá resultado Overall naquela categoria.

## Significado de “um único Overall por categoria por campeonato”

A regra não significa que exista apenas um registro de resultado Overall.

O `Overall` representa uma única disputa/classificação daquela categoria no campeonato, dentro da qual vários atletas podem possuir colocações diferentes.

Exemplo:

```text
Campeonato X
Categoria Bodybuilding
Classe Overall

Atleta A -> 1º
Atleta B -> 2º
Atleta C -> 3º
```

O que não pode existir é uma segunda classe/disputa Overall distinta para a mesma categoria no mesmo campeonato.

Como a categoria possui uma única classe de catálogo `OVERALL`, essa ambiguidade já é eliminada estruturalmente no modelo.

## Resultado

O futuro `Resultado` deverá referenciar:

- atleta/inscrição;
- campeonato;
- categoria;
- classe;
- colocação.

A classe selecionada deve obrigatoriamente pertencer à categoria informada.

Essa consistência deve ser validada no backend mesmo que a interface filtre corretamente as opções.

Não persistir `tipoClasse` novamente em `Resultado`, pois ele é derivado de `Classe.tipoClasse`. Duplicá-lo criaria risco de inconsistência.

## Aplicação da pontuação

A determinação da pontuação acontece assim:

```text
Resultado
 ├── categoria
 ├── classe
 │    └── tipoClasse
 └── colocacao

TemporadaPontuacao
 ├── temporada
 ├── posicao
 ├── tipoClasse
 └── pontuacao
```

Chave de busca lógica:

```text
(cdTemporada, resultado.colocacao, resultado.classe.tipoClasse)
```

A categoria não participa da busca do valor da tabela.

Se não existir regra para a colocação/tipo de classe, o resultado vale zero ponto naquela temporada.

## Ativação e inativação

Categorias e classes usadas por resultados devem preservar identidade histórica.

Portanto:

- não realizar exclusão física de item utilizado;
- `flAtivo = false` retira o item dos seletores de novos lançamentos;
- resultados existentes continuam exibindo o item inativo;
- rankings continuam considerando resultados aprovados já existentes, salvo outra regra de cancelamento/elegibilidade;
- o proprietário pode reativar item de catálogo quando necessário.

### Inativação de categoria

Ao inativar uma categoria, suas classes deixam de ser elegíveis para novos lançamentos enquanto a categoria permanecer inativa, mesmo que individualmente estejam com `flAtivo = true`.

### Inativação de classe

Inativar uma classe afeta somente aquela classe.

## Alterações de catálogo após uso

Alterar o nome de uma categoria ou classe não deve alterar a identidade histórica do registro, pois resultados apontam por identificador.

Porém, alterações estruturais exigem restrição maior:

- não permitir mover uma classe já utilizada para outra categoria;
- não permitir alterar `tipoClasse` de uma classe já utilizada em resultado;
- mudanças desse tipo devem ser realizadas por criação de novo item e inativação do anterior.

Isso evita reinterpretar retroativamente resultados históricos como `COMUM` ou `OVERALL` de maneira diferente.

## Unicidade recomendada

### Categoria

Unicidade lógica por nome normalizado entre registros ativos.

### Classe comum

Unicidade lógica por:

```text
(cdCategoria, nome normalizado)
```

entre classes ativas.

### Overall

Além da regra anterior, deve existir no máximo uma classe ativa `OVERALL` por categoria.

Se o SGBD não suportar índice parcial/condicional de forma conveniente, garantir no serviço dentro de transação e complementar com mecanismo de banco adequado ao MySQL utilizado.

## Invariantes consolidadas

- Categoria possui várias classes.
- Classe pertence a exatamente uma categoria.
- `tipoClasse` é explícito e possui somente `COMUM` ou `OVERALL` no MVP.
- Categoria possui no máximo uma classe ativa `OVERALL`.
- `Overall` não é inferido pelo nome.
- Categoria/classe não precisam ser previamente associadas ao campeonato.
- Resultado deve usar classe pertencente à categoria informada.
- Categoria e classe utilizadas não são excluídas fisicamente.
- Itens inativos não podem ser usados em novos resultados comuns.
- Inativação não altera resultados históricos existentes.
- Classe utilizada não pode trocar de categoria nem de `tipoClasse`.
- A pontuação usa `Classe.tipoClasse` para decidir entre tabela `COMUM` e `OVERALL`.

## Próximo refinamento

Com Categoria/Classe/Overall definidos, a próxima peça deve ser `Inscricao` e seu ciclo de vida, seguida por `Resultado` e aprovação do atleta.

Essas duas entidades substituirão a semântica simplificada hoje concentrada em `Competidores` e `PontuacaoHist` e completarão as fontes autoritativas necessárias para o cálculo dos rankings.
