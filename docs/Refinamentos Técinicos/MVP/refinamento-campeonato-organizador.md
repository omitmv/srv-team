# Refinamento técnico — Campeonato e Organizador

Status: modelagem técnica em andamento; regras já confirmadas e decisões abertas identificadas abaixo.

## Objetivo

Modelar `Campeonato` como evento compartilhado entre profissionais e temporadas, independente de propriedade exclusiva de uma temporada, e substituir o uso de organizador/federação como texto livre por referência a um catálogo central de `Organizador` administrado pelo proprietário.

## Regras já confirmadas pelo MVP

- `Organizador` pertence a catálogo central administrado exclusivamente pelo proprietário.
- O profissional pode criar campeonato.
- Todo campeonato deve possuir organizador selecionado do catálogo central.
- Campeonato criado por um profissional passa a integrar o catálogo compartilhado e pode ser usado por outros profissionais.
- Campeonato não pertence exclusivamente a uma temporada.
- A associação campeonato-temporada é N:N e já está modelada conceitualmente por `TemporadaCampeonato`.
- Profissionais não editam diretamente campeonato existente.
- Alterações em campeonato existente são solicitadas ao proprietário.
- O proprietário aprova ou rejeita a solicitação.
- Quando aprovada, a alteração modifica o cadastro compartilhado e, portanto, é refletida para todas as temporadas que utilizam aquele campeonato.
- Categoria e classe não fazem parte da composição prévia do campeonato. São informadas no lançamento de `Resultado`.

## Situação atual do código

A entidade atual `Competicao` possui:

- `cdCompeticao`;
- `nmCompeticao`;
- `dtInicio`;
- `dtFim`;
- `local`;
- `federacao` como texto livre.

Esse desenho não atende integralmente ao domínio consolidado porque o MVP exige um `Organizador` pertencente a catálogo central e selecionado obrigatoriamente no campeonato.

A evolução recomendada é manter a identidade técnica atual (`cdCompeticao`) caso isso reduza impacto de migração, mas adotar conceitualmente o nome de domínio `Campeonato` na arquitetura, ou planejar renomeação posterior se o custo for aceitável.

## Organizador

### Entidade conceitual

```text
Organizador
 ├── cdOrganizador
 ├── dsNome
 ├── flAtivo
 └── auditoria
```

Campos mínimos recomendados para o MVP:

- `cdOrganizador`;
- `dsNome` obrigatório;
- `flAtivo`;
- campos de auditoria.

Não incluir dados fiscais, contatos ou endereço no MVP sem requisito explícito.

### Administração

Somente o proprietário pode:

- criar organizador;
- editar organizador;
- inativar organizador;
- reativar organizador.

Profissionais apenas selecionam organizadores ativos ao criar campeonato.

### Inativação

Organizador já utilizado por campeonato não deve ser fisicamente excluído.

Ao ser inativado:

- permanece associado aos campeonatos históricos existentes;
- não aparece para criação de novos campeonatos;
- não invalida campeonatos já cadastrados;
- não altera inscrições, resultados, temporadas ou rankings existentes.

### Unicidade

Recomendação: impedir dois organizadores ativos semanticamente equivalentes pelo nome normalizado.

A normalização deve ignorar pelo menos:

- espaços externos irrelevantes;
- diferenças de caixa.

A estratégia exata de unicidade deve respeitar o comportamento de collation do MySQL.

## Campeonato

### Entidade conceitual

```text
Campeonato
 ├── cdCampeonato
 ├── dsNome
 ├── cdOrganizador
 ├── dtInicio
 ├── dtFim
 ├── dsLocal
 ├── status/flAtivo
 ├── cdCriador
 └── auditoria
```

### Identidade e compatibilidade

No código atual, a identidade é `cdCompeticao`.

Para minimizar impacto no MVP, é aceitável manter o nome físico/técnico `cdCompeticao` e `tbCompeticao`, desde que o domínio e os novos serviços tratem a entidade semanticamente como `Campeonato`.

Renomeação física de tabela/PK não é requisito funcional e pode ser adiada para evitar migração cosmética.

### Campos recomendados

- identidade própria;
- nome obrigatório;
- organizador obrigatório;
- data inicial obrigatória;
- data final obrigatória;
- local opcional;
- criador do cadastro;
- indicador de ativo/cancelado conforme decisão a fechar;
- auditoria.

### Organizador obrigatório

`Campeonato` deve referenciar `Organizador` por identidade:

```text
Campeonato N ---- 1 Organizador
```

Não manter `federacao` como fonte autoritativa de texto livre.

Em eventual migração, os valores distintos existentes em `Competicao.federacao` devem ser analisados para criação/deduplicação de registros de `Organizador`, quando houver dados legados reais.

## Criação por profissional

Um profissional autorizado pode criar campeonato diretamente, sem aprovação prévia do proprietário.

Na criação:

1. informa os dados do campeonato;
2. seleciona um organizador ativo do catálogo;
3. o sistema valida os campos e possível duplicidade;
4. cria o campeonato no catálogo compartilhado;
5. o campeonato fica disponível para associação a temporadas por outros usuários autorizados.

O usuário criador do campeonato não se torna proprietário exclusivo do registro.

`cdCriador` deve ser preservado para auditoria e rastreabilidade, não para restringir reutilização por outros profissionais.

## Catálogo compartilhado

Todos os campeonatos válidos pertencem ao mesmo catálogo compartilhado.

Consequências:

- um campeonato pode participar de várias temporadas;
- profissionais diferentes podem associar o mesmo campeonato às suas temporadas;
- inscrição é feita no campeonato, não na temporada;
- resultado pertence à inscrição do campeonato;
- pontuação do mesmo resultado varia conforme cada temporada elegível.

Não duplicar campeonato para cada temporada.

## Associação com temporada

A associação permanece responsabilidade de `TemporadaCampeonato`.

```text
Temporada N ---- N Campeonato
```

O campeonato não deve possuir `cdTemporada` diretamente.

Adicionar/remover associação com temporada não altera o cadastro compartilhado do campeonato.

## Alteração de campeonato

Profissionais não alteram diretamente um campeonato já existente.

Deve existir uma solicitação explícita de alteração encaminhada ao proprietário.

### Entidade conceitual recomendada

```text
SolicitacaoAlteracaoCampeonato
 ├── cdSolicitacao
 ├── cdCampeonato
 ├── cdSolicitante
 ├── status
 ├── dadosPropostos
 ├── justificativa
 ├── dtSolicitacao
 ├── cdResponsavelDecisao
 ├── dtDecisao
 └── motivoReprovacao
```

Status mínimos:

- `PENDENTE`;
- `APROVADA`;
- `REPROVADA`.

### Dados propostos

Evitar persistir apenas uma descrição textual da mudança.

A solicitação deve permitir reconstruir claramente:

- estado atual do campeonato no momento da solicitação;
- valores propostos;
- campos alterados.

Implementações possíveis:

- snapshot estruturado JSON do antes/depois; ou
- entidade com campos propostos explicitamente.

Para o MVP, JSON estruturado pode reduzir complexidade, desde que validado e auditável. A camada de domínio não deve aplicar diretamente conteúdo arbitrário sem validação campo a campo.

### Aprovação

Ao aprovar:

1. revalidar o estado atual do campeonato;
2. detectar se o cadastro foi alterado desde a solicitação;
3. aplicar os valores aprovados;
4. registrar responsável e data;
5. preservar histórico do antes/depois;
6. marcar solicitação como `APROVADA`.

Como o cadastro é compartilhado, a alteração aprovada passa a valer para todas as temporadas que referenciam o campeonato.

### Reprovação

Ao reprovar:

- manter campeonato inalterado;
- marcar solicitação como `REPROVADA`;
- registrar responsável e data;
- registrar justificativa/motivo da reprovação;
- preservar histórico.

## Concorrência nas solicitações

Recomendação para o MVP: permitir no máximo uma solicitação `PENDENTE` por campeonato por vez.

Motivo: múltiplas propostas paralelas sobre o mesmo cadastro criariam conflitos de merge e decisões baseadas em estados diferentes.

Se já houver solicitação pendente, uma nova tentativa deve ser bloqueada ou direcionar o usuário à pendência existente.

A aprovação deve ainda validar se a versão do campeonato usada como base continua atual.

Recomendação técnica: `@Version` em `Campeonato` para optimistic locking e registro da versão base na solicitação.

## Auditoria

Histórico do campeonato deve permitir identificar:

- quem criou;
- quando criou;
- valores originais;
- solicitações de alteração;
- quem solicitou;
- valores propostos;
- quem aprovou/reprovou;
- justificativas;
- alterações administrativas diretas do proprietário, se permitidas;
- valores antes/depois.

## Categorias e classes

Campeonato não mantém coleção configurada de categorias/classes participantes.

A combinação utilizada é registrada em `Resultado`.

Portanto, não criar no MVP entidades como:

- `CampeonatoCategoria`;
- `CampeonatoClasse`.

Essa decisão evita configuração antecipada obrigatória e segue a regra já consolidada de que categoria/classe são selecionadas no lançamento do resultado.

## Efeitos sobre inscrições e resultados

Alterar dados cadastrais do campeonato não cria novo campeonato nem altera sua identidade.

Inscrições e resultados continuam vinculados ao mesmo `cdCampeonato`/`cdCompeticao`.

Mudanças em nome, local ou organizador não devem reatribuir inscrições nem resultados.

Mudança de datas pode afetar apresentação e regras futuras, mas não deve apagar ou recriar inscrições/resultados existentes.

Os efeitos exatos de alterações sensíveis após já existirem inscrições/resultados permanecem como decisão de produto a fechar.

## Duplicidade de campeonatos

O catálogo compartilhado precisa de proteção contra duplicação acidental, mas nome isolado não é suficiente para definir identidade esportiva.

Exemplos possíveis:

```text
Mr Rio 2026 — 27/06/2026 — Organizador X
Mr Rio 2027 — 26/06/2027 — Organizador X
```

São campeonatos distintos apesar do mesmo nome.

Recomendação inicial de chave semântica para detecção de possível duplicidade:

```text
(nome normalizado, cdOrganizador, dtInicio)
```

Essa combinação deve inicialmente gerar validação/alerta forte. A decisão entre bloqueio absoluto ou confirmação administrativa permanece aberta.

## Remoção / cancelamento de campeonato

Não excluir fisicamente campeonato que já possua qualquer uma destas referências:

- associação com temporada;
- inscrição;
- resultado;
- histórico de solicitação.

O MVP deve preferir estado lógico.

A semântica exata entre `INATIVO` e `CANCELADO`, bem como os efeitos sobre ranking e inscrições, ainda deve ser consolidada antes da implementação.

## Invariantes já consolidadas

- Organizador é catálogo central administrado pelo proprietário.
- Campeonato exige organizador.
- `federacao` texto livre não deve permanecer como fonte autoritativa.
- Profissional pode criar campeonato diretamente.
- Campeonato criado integra catálogo compartilhado.
- Criador do cadastro não possui exclusividade sobre o campeonato.
- Campeonato pode integrar várias temporadas.
- Campeonato não referencia temporada diretamente.
- Associação N:N é realizada via `TemporadaCampeonato`.
- Profissionais não editam diretamente campeonato existente.
- Alterações são solicitadas ao proprietário.
- Alteração aprovada afeta o cadastro compartilhado.
- Categoria/classe não são pré-configuradas no campeonato.
- Inscrição pertence ao campeonato, não à temporada.
- Não excluir fisicamente campeonato já utilizado.

## Decisões abertas

### D1 — Prevenção de duplicidade

Definir se possível duplicidade por `(nome normalizado, organizador, data inicial)`:

- deve ser bloqueada; ou
- gera apenas alerta/necessidade de confirmação.

**Recomendação:** bloquear a criação ordinária de duplicidade exata dessa chave semântica. Se houver caso legítimo excepcional, somente o proprietário poderá autorizar/corrigir o cadastro.

### D2 — Alterações após uso

Definir quais campos podem ser alterados após existir inscrição ou resultado.

**Recomendação:**

- nome: permitir;
- local: permitir;
- organizador: permitir com justificativa/auditoria;
- datas: permitir com justificativa/auditoria;
- identidade do campeonato: nunca substituir; se for outro evento, criar novo campeonato.

### D3 — Cancelamento lógico do campeonato

Definir se o campeonato terá estado explícito `CANCELADO` além de ativo/inativo e os efeitos sobre inscrições/resultados/ranking.

**Recomendação:** usar `ATIVO` e `CANCELADO` como estados de negócio, evitando um `flAtivo` ambíguo para evento esportivo. Cancelamento deve preservar todo histórico e impedir novas inscrições/associações, mas seus efeitos sobre resultados já aprovados e rankings precisam de decisão explícita do produto.
