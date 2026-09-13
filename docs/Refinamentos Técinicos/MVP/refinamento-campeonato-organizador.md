# Refinamento técnico — Campeonato e Organizador

Status: modelagem técnica consolidada para o MVP; implementação e migração pendentes.

## Objetivo

Modelar `Campeonato` como evento compartilhado entre profissionais e temporadas, sem propriedade exclusiva de uma temporada, com organizador centralizado, localização padronizada e regras explícitas de criação, alteração, cancelamento, reativação, equivalência, autorização e associação com temporadas.

## Regras confirmadas

- `Organizador` pertence a catálogo central administrado exclusivamente pelo proprietário.
- Profissional pode criar campeonato diretamente; `cdCriador` é auditoria e não confere propriedade exclusiva.
- Campeonato exige organizador ativo, país, subdivisão quando aplicável, `dtInicio` e `dtFim`.
- `dtFim >= dtInicio`; igualdade representa campeonato de um dia.
- Datas são informativas e não controlam automaticamente o ciclo de vida.
- Campeonato pode integrar várias temporadas via `TemporadaCampeonato`.
- Somente `ADMINISTRACAO` pode vincular/desvincular campeonato e apenas com temporada `ATIVO`.
- `ENCERRADA` exige reabertura para `ATIVO`; `CANCELADA` não admite alteração estrutural ordinária.
- Inscrições/resultados existentes não impedem desvinculação e não são alterados por ela.
- Categoria e classe pertencem ao `Resultado`.
- Manutenção profissional de campeonato existente deriva da administração de ao menos uma temporada vinculada.
- Campeonato já utilizado exige aprovação do proprietário para alteração cadastral.
- Toda alteração de status exige justificativa e histórico imutável da transição.
- Havendo qualquer `Resultado` histórico, alteração de status solicitada por profissional exige aprovação do proprietário.
- Campeonato cancelado equivalente não pode ser recadastrado.
- Reativação deve revalidar equivalência e é bloqueada se já existir outro campeonato `ATIVO` semanticamente equivalente.

## Organizador

```text
Organizador
 ├── cdOrganizador
 ├── dsNome
 ├── flAtivo
 └── auditoria
```

Somente o proprietário cria, edita, inativa ou reativa organizador. Organizador utilizado não é excluído fisicamente; inativação preserva histórico e impede novas seleções.

## Catálogo geográfico

```text
Pais
 ├── cdPais
 ├── codigoIso2
 ├── codigoIso3
 ├── dsNome
 ├── flAtivo
 └── auditoria técnica

Subdivisao
 ├── cdSubdivisao
 ├── cdPais
 ├── codigoIso
 ├── dsNome
 ├── flAtivo
 └── auditoria técnica
```

O catálogo é interno e pré-carregado, preferencialmente baseado em ISO 3166/3166-2. País/subdivisão inativos não podem ser selecionados em novos campeonatos, mas permanecem válidos historicamente. A subdivisão deve pertencer ao país informado.

## Campeonato

```text
Campeonato
 ├── cdCampeonato/cdCompeticao
 ├── dsNome
 ├── cdOrganizador
 ├── cdPais
 ├── cdSubdivisao
 ├── dtInicio
 ├── dtFim
 ├── dsLocal
 ├── status
 ├── cdCriador
 └── auditoria corrente
```

Estados: `ATIVO` e `CANCELADO`.

`status` representa o estado corrente. O histórico de cancelamentos e reativações não deve depender de campos sobrescrevíveis no próprio `Campeonato`; cada transição é preservada em histórico imutável específico.

`dtInicio` e `dtFim` são obrigatórias. `dtFim` nunca pode ser anterior a `dtInicio`; igualdade representa evento de um dia. O término da data não muda automaticamente o estado, não cancela inscrições/resultados e não controla pontuação.

Localização exige país, subdivisão quando aplicável e permite `dsLocal` livre/opcional. `dsLocal` não participa da identidade semântica.

Pode-se manter fisicamente `cdCompeticao`/`tbCompeticao` no MVP para reduzir impacto de migração, tratando semanticamente a entidade como `Campeonato`. O campo legado `federacao` deixa de ser autoritativo; campeonato referencia `Organizador`.

## Autorização profissional

Um profissional é autorizado a manter campeonato existente quando administra ao menos uma temporada que possua associação ativa com ele. `CONSULTA`, pertencer ao mesmo `Time`, possuir vínculo com atleta ou ser o criador do campeonato não concede isoladamente manutenção global.

O proprietário possui autoridade administrativa global.

A criação inicial é exceção: profissional pode criar campeonato antes de existir qualquer `TemporadaCampeonato`. Depois da criação, operações contextuais dependem das associações com temporadas.

## Associação Campeonato ↔ Temporada

`TemporadaCampeonato` é configuração estrutural da temporada.

Somente `ADMINISTRACAO` pode vincular/desvincular e o estado deve ser:

- `ATIVO`: alteração permitida;
- `ENCERRADA`: alteração bloqueada até reabertura para `ATIVO`;
- `CANCELADA`: alteração ordinária bloqueada enquanto cancelada.

Permitir `Inscricao` ou `Resultado` tardio em `ENCERRADA` não libera alteração estrutural.

Para vincular, campeonato deve estar apto segundo seu próprio estado, temporada deve estar `ATIVO`, profissional deve ter `ADMINISTRACAO` e associação ativa duplicada deve ser impedida.

Para desvincular, temporada também deve estar `ATIVO`. Inscrições/resultados não impedem a operação. Desvinculação não cancela, inativa ou modifica esses registros; apenas retira o campeonato da projeção da temporada e força recálculo do ranking. Revinculação faz resultados válidos voltarem a ser interpretados segundo as regras vigentes da temporada.

Vínculos/desvínculos devem manter histórico auditável com temporada, campeonato, responsável, data/hora e operação.

## Criação e equivalência

Identidade semântica aproximada:

```text
(nomeNormalizado, cdOrganizador, cdPais, cdSubdivisao, dtInicio)
```

Quando subdivisão não for aplicável, `cdSubdivisao` pode ser nulo e deve ser comparado consistentemente. Normalização considera trim, case-insensitive, espaços repetidos e normalização segura de acentuação/pontuação. `dtFim` e `dsLocal` não participam da equivalência.

Na criação:

- equivalente `ATIVO`: bloquear e direcionar ao existente;
- equivalente `CANCELADO`: bloquear recadastro e direcionar ao fluxo de reativação;
- inexistente: permitir criação.

A equivalência é invariante de domínio e precisa de proteção contra concorrência; não deve depender apenas de comparação textual na aplicação.

## Alteração cadastral

Campeonato é considerado **utilizado** a partir da existência histórica de qualquer `Inscricao` ou `Resultado`, mesmo posteriormente cancelado.

### Não utilizado

Profissional autorizado pode editar diretamente. A alteração deve ser auditada e revalidar organizador, localização, datas e equivalência.

### Utilizado

Profissional autorizado não altera diretamente. Deve criar `SolicitacaoAlteracaoCampeonato` com justificativa, snapshot antes/depois e versão-base. Somente proprietário decide. No máximo uma solicitação pendente por campeonato.

Na aprovação, revalidar estado, versão, organizador, localização, datas e equivalência. Alteração concorrente não pode ser sobrescrita silenciosamente.

## Cancelamento e reativação

Transições:

```text
ATIVO -> CANCELADO
CANCELADO -> ATIVO
```

Toda transição exige justificativa, responsável, data/hora e criação de registro imutável em `CampeonatoStatusHistorico` na mesma transação que altera o estado corrente.

### Histórico imutável de status

Modelo conceitual:

```text
CampeonatoStatusHistorico
 ├── cdHistorico
 ├── cdCampeonato
 ├── statusOrigem
 ├── statusDestino
 ├── justificativa
 ├── cdResponsavel
 ├── dtTransicao
 ├── origemOperacao
 ├── cdSolicitacaoStatus (opcional)
 └── auditoria técnica
```

`origemOperacao` deve permitir distinguir ao menos:

- `DIRETA_PROFISSIONAL`;
- `APROVACAO_PROPRIETARIO`;
- `DIRETA_PROPRIETARIO`.

Regras:

- cada transição efetivamente aplicada gera exatamente um registro histórico;
- o registro histórico é append-only: não pode ser editado nem excluído pelo fluxo ordinário;
- `statusOrigem` deve corresponder ao estado corrente imediatamente antes da transição;
- `statusDestino` deve corresponder ao estado corrente imediatamente após a transição;
- justificativa é obrigatória em todas as transições;
- responsável e instante efetivo da transição são obrigatórios;
- quando a transição decorrer de solicitação aprovada, o histórico referencia a solicitação que a originou;
- criação do histórico e alteração do `Campeonato.status` pertencem à mesma unidade transacional: não pode existir mudança de status sem histórico nem histórico de mudança que não tenha sido aplicada;
- tentativas rejeitadas ou bloqueadas não geram `CampeonatoStatusHistorico`, pois não houve transição; permanecem registradas no fluxo de solicitação/log técnico aplicável.

Campos como última data/responsável de cancelamento ou reativação podem existir apenas como projeção/otimização de leitura, mas não são fonte autoritativa do histórico.

### Existência histórica de lançamento

Campeonato possui lançamento quando já existiu qualquer `Resultado` associado a qualquer inscrição. `PENDENTE_APROVACAO`, `APROVADO` e `CANCELADO` contam historicamente. Essa condição nunca é apagada.

### Sem lançamento histórico

Profissional autorizado pode cancelar ou reativar diretamente, sempre com justificativa e histórico imutável. Proprietário também pode executar diretamente.

### Com lançamento histórico

Profissional autorizado solicita alteração de status e somente proprietário aprova/reprova. O proprietário pode alterar diretamente, sempre com justificativa e histórico equivalente.

Pode-se utilizar entidade única `SolicitacaoAlteracaoStatusCampeonato`, contendo campeonato, solicitante, status origem/destino, justificativa, status da solicitação, datas e decisão. No máximo uma solicitação pendente por campeonato e a decisão deve revalidar o estado atual.

## Revalidação de equivalência na reativação

A transição `CANCELADO -> ATIVO` deve sempre revalidar a identidade semântica do campeonato imediatamente antes de efetivar a reativação.

Se existir **outro** campeonato `ATIVO` semanticamente equivalente segundo:

```text
(nomeNormalizado, cdOrganizador, cdPais, cdSubdivisao, dtInicio)
```

a reativação deve ser bloqueada.

A regra vale independentemente de quem execute a operação:

- reativação direta por profissional autorizado;
- aprovação pelo proprietário de solicitação de reativação;
- reativação administrativa direta pelo proprietário.

O fato de o campeonato cancelado ser mais antigo, possuir inscrições/resultados ou ter sido originalmente criado antes do campeonato ativo não concede preferência automática nem permite duplicidade ativa.

O conflito deve ser resolvido administrativamente antes da reativação. O MVP não faz merge automático de campeonatos, inscrições ou resultados e não transfere histórico entre identidades.

A validação deve ocorrer no momento efetivo da transição, não apenas quando a solicitação for criada, protegendo também contra concorrência entre reativações/criações simultâneas.

## Efeitos do cancelamento

Cancelamento é lógico. Preserva inscrições e resultados, impede novas inscrições e associações ordinárias, suspende efeito esportivo dos resultados aprovados, mantém pendências armazenadas e força recálculo dos rankings afetados. Não altera artificialmente o status dos resultados.

## Efeitos da reativação

Reativação preserva a mesma identidade. Após passar pela revalidação de equivalência, inscrições/resultados permanecem vinculados; resultados `APROVADO` voltam a produzir efeito quando elegíveis; `PENDENTE_APROVACAO` retorna ao fluxo ordinário; resultados cancelados continuam cancelados; rankings aplicáveis são recalculados.

## Auditoria

`CampeonatoStatusHistorico` é a fonte autoritativa para reconstruir todas as transições `ATIVO <-> CANCELADO` efetivamente aplicadas.

Além dele, preservar historicamente criação, edições diretas, responsáveis, temporadas usadas como contexto de autorização, vínculos/desvínculos, solicitações cadastrais e de status, justificativas, decisões e antes/depois.

Tentativas de reativação rejeitadas por colisão semântica devem ser observáveis tecnicamente; quando houver solicitação formal, a impossibilidade de aprovação deve ficar explícita no fluxo administrativo.

## Categorias e classes

Campeonato não mantém coleção pré-configurada de categorias/classes. A combinação é registrada em `Resultado`. Não criar `CampeonatoCategoria` ou `CampeonatoClasse` no MVP.

## Invariantes consolidadas

- campeonato é compartilhado e criador é apenas auditoria;
- datas são obrigatórias, informativas e não dirigem automaticamente o ciclo de vida;
- autorização profissional sobre campeonato existente deriva de administração de temporada vinculada;
- somente administrador altera `TemporadaCampeonato` e apenas em temporada `ATIVO`;
- `ENCERRADA` exige reabertura para alteração estrutural; `CANCELADA` bloqueia alteração ordinária;
- desvinculação não modifica inscrições/resultados e apenas altera a projeção da temporada;
- campeonato utilizado exige aprovação do proprietário para alteração cadastral;
- toda alteração de status exige justificativa e registro imutável da transição;
- alteração do status corrente e inserção de `CampeonatoStatusHistorico` são atômicas;
- histórico de status é append-only e fonte autoritativa das transições;
- existência histórica de resultado condiciona alteração de status profissional à aprovação do proprietário;
- equivalência usa nome normalizado + organizador + país + subdivisão + data inicial;
- não pode existir reativação que produza dois campeonatos `ATIVO` semanticamente equivalentes;
- equivalência é revalidada no momento efetivo da reativação e deve ser protegida contra concorrência;
- conflito de equivalência não provoca merge ou transferência automática de histórico;
- cancelamento é lógico e preserva inscrições/resultados;
- categoria/classe pertencem ao `Resultado`;
- `Inscricao` pertence ao campeonato, não à temporada.
