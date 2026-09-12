# Refinamento técnico — Campeonato e Organizador

Status: modelagem técnica em andamento; ciclo de criação, alteração, cancelamento, reativação, localização, equivalência e autorização consolidado para o MVP.

## Objetivo

Modelar `Campeonato` como evento compartilhado entre profissionais e temporadas, sem propriedade exclusiva de uma temporada, com organizador centralizado, localização padronizada e regras explícitas de criação, alteração, cancelamento, reativação, equivalência e autorização.

## Regras confirmadas

- `Organizador` pertence a catálogo central administrado exclusivamente pelo proprietário.
- Profissional pode criar campeonato diretamente, conforme regra específica de criação.
- Campeonato criado integra catálogo compartilhado.
- `cdCriador` é dado de auditoria e não confere propriedade exclusiva nem privilégio especial de edição.
- Todo campeonato exige organizador ativo.
- Todo campeonato exige país.
- Deve informar subdivisão administrativa de primeiro nível quando aplicável ao país.
- País e subdivisão pertencem a catálogo interno de referência baseado em códigos padronizados, preferencialmente ISO 3166 / ISO 3166-2.
- O cadastro de campeonato não depende de API pública em tempo real.
- Campeonato pode integrar várias temporadas via relação N:N `TemporadaCampeonato`.
- Categoria e classe são informadas no `Resultado`, não pré-configuradas no campeonato.
- Para operar um campeonato já existente como profissional autorizado, o profissional deve possuir relação de administração com ao menos uma temporada à qual o campeonato esteja vinculado.
- Relação exclusivamente de consulta com a temporada não concede autorização de manutenção do campeonato.
- Campeonato que já teve `Inscricao` ou `Resultado` é considerado utilizado, ainda que esses registros tenham sido posteriormente cancelados.
- Campeonato não utilizado pode ser editado diretamente por qualquer profissional autorizado.
- Campeonato utilizado não pode ser editado diretamente por profissional; alteração exige solicitação e decisão do proprietário.
- Somente o proprietário efetiva cancelamento ou reativação.
- Profissional autorizado pode solicitar alteração, cancelamento ou reativação conforme o estado do campeonato e as regras do fluxo.
- Campeonato cancelado não pode ser recadastrado com nova identidade se for equivalente a registro existente.

## Organizador

```text
Organizador
 ├── cdOrganizador
 ├── dsNome
 ├── flAtivo
 └── auditoria
```

Somente o proprietário pode criar, editar, inativar ou reativar organizador.

Organizador já utilizado não deve ser fisicamente excluído. Inativação preserva histórico e impede seleção em novos campeonatos.

## Catálogo geográfico

### País

```text
Pais
 ├── cdPais
 ├── codigoIso2
 ├── codigoIso3
 ├── dsNome
 ├── flAtivo
 └── auditoria técnica
```

### Subdivisão administrativa

```text
Subdivisao
 ├── cdSubdivisao
 ├── cdPais
 ├── codigoIso
 ├── dsNome
 ├── flAtivo
 └── auditoria técnica
```

Regras:

- catálogo interno e pré-carregado;
- profissional não cadastra país/subdivisão durante o fluxo de campeonato;
- indisponibilidade externa não bloqueia operação;
- país/subdivisão inativo não pode ser selecionado em novo campeonato, mas permanece válido para histórico;
- `cdSubdivisao` deve pertencer ao `cdPais` informado.

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
 ├── dtCancelamento
 ├── cdResponsavelCancelamento
 ├── dtReativacao
 ├── cdResponsavelReativacao
 └── auditoria
```

Estados mínimos:

- `ATIVO`
- `CANCELADO`

Regras de localização:

- `cdPais` obrigatório;
- `cdSubdivisao` obrigatório quando aplicável;
- `dsLocal` opcional e livre para ginásio, centro de eventos, endereço etc.;
- `dsLocal` não participa da identidade semântica.

Pode-se manter fisicamente `cdCompeticao`/`tbCompeticao` no MVP para reduzir impacto de migração, tratando semanticamente a entidade como Campeonato.

O campo legado `federacao` deixa de ser fonte autoritativa; `Campeonato` referencia `Organizador`.

## Autorização profissional sobre campeonato

A autorização de manutenção de um campeonato existente é derivada das temporadas às quais ele está vinculado.

Um profissional é considerado **autorizado para o campeonato** quando:

1. possui relação administrativa válida com pelo menos uma `Temporada`;
2. essa temporada possui associação ativa com o `Campeonato` por `TemporadaCampeonato`;
3. a relação do profissional com a temporada concede capacidade de administração, e não apenas consulta.

Em termos conceituais:

```text
Profissional
    |
    | administra
    v
Temporada
    |
    | TemporadaCampeonato
    v
Campeonato
```

Não é necessário que o profissional seja o `cdCriador` do campeonato.

Se o campeonato estiver associado a várias temporadas, basta que o profissional administre ao menos uma delas para ser considerado autorizado no contexto do campeonato.

A mera existência do profissional no sistema, pertencer à mesma equipe de outro profissional, possuir vínculo com algum atleta ou ter acesso somente de consulta a uma temporada não concede, isoladamente, autorização para manutenção do campeonato.

O proprietário permanece com autoridade administrativa global, independentemente de associação com temporada.

### Campeonato ainda não associado a temporada

A regra acima governa a manutenção de campeonato já associado. A criação inicial continua permitida ao profissional conforme a autorização geral de criação do MVP, pois antes de existir o campeonato ainda não há `TemporadaCampeonato` que possa ser usada como origem de autorização.

Após a criação, operações profissionais que dependam de autorização contextual devem ser avaliadas pelas associações do campeonato com temporadas.

## Criação

Profissional com permissão geral de criação de campeonato pode criar diretamente, sem aprovação prévia do proprietário.

Fluxo:

1. informa nome, período e localização;
2. seleciona organizador ativo;
3. seleciona país ativo;
4. seleciona subdivisão válida quando aplicável;
5. sistema valida campos e equivalência;
6. verifica campeonatos ativos e cancelados equivalentes;
7. inexistindo conflito, cria no catálogo compartilhado.

A criação não exige relação prévia com temporada porque o campeonato ainda não existe para ser associado. `cdCriador` registra autoria para auditoria, sem gerar propriedade exclusiva futura.

## Equivalência e duplicidade

Identidade semântica aproximada no MVP:

```text
(nomeNormalizado, cdOrganizador, cdPais, cdSubdivisao, dtInicio)
```

Quando não houver subdivisão aplicável, `cdSubdivisao` pode ser nulo e deve ser comparado de forma consistente.

Normalização do nome deve considerar ao menos:

- trim;
- comparação case-insensitive;
- normalização de espaços repetidos;
- normalização segura de acentuação/pontuação.

`dtFim` e `dsLocal` não participam da equivalência.

Comportamento:

- equivalente `ATIVO` -> bloquear novo cadastro e direcionar ao existente;
- equivalente `CANCELADO` -> bloquear recadastro e oferecer solicitação de reativação;
- inexistente -> permitir criação.

A equivalência é invariante de domínio e não deve depender exclusivamente de uma `UNIQUE CONSTRAINT` textual simples. A implementação deve proteger também contra concorrência.

## Alteração de campeonato

### Campeonato não utilizado

Enquanto nunca tiver existido `Inscricao` nem `Resultado` associado:

- qualquer profissional autorizado para o campeonato pode editar diretamente;
- autorização deriva de relação administrativa com ao menos uma temporada vinculada ao campeonato;
- não é necessário ser o criador;
- `cdCriador` não concede exclusividade;
- cada edição deve ser auditada com responsável, data/hora e valores alterados;
- toda alteração deve revalidar organizador, localização e equivalência antes de persistir;
- alteração não pode produzir duplicidade com outro campeonato ativo ou cancelado equivalente.

### Campeonato utilizado

A partir do momento em que já tiver existido ao menos uma `Inscricao` ou `Resultado`, a condição de utilizado é histórica e irreversível para fins de alteração cadastral.

Profissional autorizado não altera diretamente. Qualquer alteração gera solicitação ao proprietário.

```text
Profissional autorizado propõe alteração
        |
        | justificativa obrigatória
        v
Solicitação PENDENTE
        |
        +--> notificação ao proprietário
        v
Proprietário analisa antes/depois
        |
   +----+----+
   |         |
aprova     reprova
   |         |
aplica    mantém cadastro atual
```

### Solicitação de alteração

```text
SolicitacaoAlteracaoCampeonato
 ├── cdSolicitacao
 ├── cdCampeonato
 ├── cdSolicitante
 ├── status
 ├── dadosAtuais
 ├── dadosPropostos
 ├── camposAlterados
 ├── justificativa
 ├── nrVersaoBaseCampeonato
 ├── dtSolicitacao
 ├── cdResponsavelDecisao
 ├── dtDecisao
 └── motivoReprovacao
```

Status:

- `PENDENTE`
- `APROVADA`
- `REPROVADA`

Regras:

- solicitante deve estar autorizado para o campeonato no momento da solicitação;
- justificativa obrigatória;
- snapshot suficiente para comparação;
- no máximo uma solicitação `PENDENTE` por campeonato;
- somente proprietário decide;
- aprovação revalida estado, versão, organizador, localização e equivalência;
- alteração concorrente não pode ser sobrescrita silenciosamente;
- recomendar `@Version` em `Campeonato` e guardar versão-base na solicitação.

## Cancelamento

Somente proprietário efetiva:

```text
ATIVO -> CANCELADO
```

Profissional autorizado pode solicitar cancelamento com justificativa obrigatória.

```text
SolicitacaoCancelamentoCampeonato
 ├── cdSolicitacao
 ├── cdCampeonato
 ├── cdSolicitante
 ├── justificativa
 ├── status
 ├── dtSolicitacao
 ├── cdResponsavelDecisao
 ├── dtDecisao
 └── motivoReprovacao
```

No máximo uma solicitação `PENDENTE` por campeonato.

Efeitos do cancelamento:

- preserva inscrições e resultados;
- impede novas inscrições ordinárias;
- impede novas associações ordinárias com temporadas;
- resultados aprovados deixam de produzir efeito esportivo enquanto cancelado;
- resultados pendentes permanecem armazenados, porém inativos para operação esportiva ordinária;
- não altera artificialmente o status próprio dos resultados;
- rankings afetados devem ser recalculados.

## Reativação

Campeonato cancelado pode ser reativado preservando a mesma identidade.

Somente proprietário efetiva:

```text
CANCELADO -> ATIVO
```

Profissional autorizado pode solicitar reativação com justificativa obrigatória.

```text
SolicitacaoReativacaoCampeonato
 ├── cdSolicitacao
 ├── cdCampeonato
 ├── cdSolicitante
 ├── justificativa
 ├── status
 ├── dtSolicitacao
 ├── cdResponsavelDecisao
 ├── dtDecisao
 └── motivoReprovacao
```

Regras:

- somente campeonato `CANCELADO` recebe solicitação;
- solicitante deve possuir relação administrativa com ao menos uma temporada já vinculada ao campeonato;
- no máximo uma `PENDENTE`;
- somente proprietário decide.

Ao reativar:

- inscrições permanecem vinculadas;
- resultados permanecem vinculados;
- resultados `APROVADO` voltam a produzir efeito se ainda válidos;
- resultados `PENDENTE_APROVACAO` retornam ao fluxo ordinário;
- resultados cancelados pelo próprio fluxo permanecem cancelados;
- rankings aplicáveis são recalculados.

## Auditoria

Registrar historicamente:

- criação;
- edições diretas de campeonato não utilizado;
- responsável por cada edição direta;
- temporadas que fundamentaram autorização administrativa quando relevante;
- solicitações de alteração;
- dados atuais e propostos;
- justificativas;
- decisões do proprietário;
- antes/depois;
- cancelamentos;
- reativações;
- responsáveis e datas.

## Categorias e classes

Campeonato não mantém coleção pré-configurada de categorias/classes.

A combinação é registrada em `Resultado`.

Não criar `CampeonatoCategoria` ou `CampeonatoClasse` no MVP.

## Invariantes consolidadas

- campeonato é compartilhado;
- criador é auditoria, não proprietário exclusivo;
- autorização profissional sobre campeonato existente deriva de relação administrativa com ao menos uma temporada vinculada ao campeonato;
- acesso de consulta à temporada não concede manutenção do campeonato;
- administrar qualquer uma das temporadas vinculadas é suficiente para autorização contextual;
- qualquer profissional autorizado pode editar diretamente campeonato não utilizado;
- campeonato utilizado exige solicitação de alteração ao proprietário;
- a condição de utilizado é histórica;
- campeonato exige organizador, país e subdivisão quando aplicável;
- equivalência usa nome normalizado + organizador + país + subdivisão + data inicial;
- local detalhado e data final não fazem parte da identidade;
- cancelamento é lógico;
- somente proprietário cancela ou reativa;
- campeonato cancelado equivalente não pode ser recadastrado;
- inscrições e resultados são preservados no cancelamento;
- categoria/classe pertencem ao `Resultado`, não ao `Campeonato`;
- inscrição pertence ao campeonato, não à temporada.
