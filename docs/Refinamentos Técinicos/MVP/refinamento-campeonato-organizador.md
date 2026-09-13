# Refinamento técnico — Campeonato e Organizador

Status: modelagem técnica em andamento; ciclo de criação, alteração, cancelamento, reativação, localização, equivalência, autorização, vínculo com temporada e semântica das datas consolidado para o MVP.

## Objetivo

Modelar `Campeonato` como evento compartilhado entre profissionais e temporadas, sem propriedade exclusiva de uma temporada, com organizador centralizado, localização padronizada e regras explícitas de criação, alteração, cancelamento, reativação, equivalência, autorização e associação com temporadas.

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
- `dtInicio` e `dtFim` são obrigatórias e têm finalidade informativa sobre o período do campeonato.
- Campeonato de um único dia deve possuir `dtInicio = dtFim`.
- Campeonato de vários dias possui `dtFim > dtInicio`.
- As datas do campeonato não controlam automaticamente status, elegibilidade, inscrições, resultados ou pontuação.
- O transcorrer da data final não encerra nem inativa automaticamente o campeonato.
- Campeonato pode integrar várias temporadas via relação N:N `TemporadaCampeonato`.
- Somente profissional com permissão de `ADMINISTRACAO` sobre a temporada pode vincular ou desvincular campeonato daquela temporada.
- Alterar `TemporadaCampeonato` é alteração estrutural da temporada e somente é permitido enquanto a temporada estiver `ATIVO`.
- Temporada `ENCERRADA` deve ser reaberta para `ATIVO` antes de vincular ou desvincular campeonato.
- Temporada `CANCELADA` não permite alteração ordinária de sua composição de campeonatos enquanto permanecer cancelada.
- Profissional com permissão apenas de `CONSULTA` não pode alterar `TemporadaCampeonato`.
- O vínculo pode ser criado ou removido independentemente da existência de inscrições ou resultados, desde que o ciclo de vida da temporada permita a alteração estrutural.
- Desvincular campeonato de uma temporada não cancela, não inativa e não altera o status de inscrições ou resultados referentes ao campeonato.
- Categoria e classe são informadas no `Resultado`, não pré-configuradas no campeonato.
- Para operar um campeonato já existente como profissional autorizado, o profissional deve possuir relação de administração com ao menos uma temporada à qual o campeonato esteja vinculado.
- Relação exclusivamente de consulta com a temporada não concede autorização de manutenção do campeonato.
- Campeonato que já teve `Inscricao` ou `Resultado` é considerado utilizado, ainda que esses registros tenham sido posteriormente cancelados.
- Campeonato não utilizado pode ser editado diretamente por qualquer profissional autorizado.
- Campeonato utilizado não pode ser editado diretamente por profissional; alteração cadastral exige solicitação e decisão do proprietário.
- Toda alteração de status de campeonato exige justificativa obrigatória.
- Se nunca tiver existido `Resultado` para o campeonato, profissional autorizado pode cancelar ou reativar diretamente.
- Se já tiver existido ao menos um `Resultado`, cancelamento ou reativação pelo profissional exige solicitação e aprovação do proprietário.
- A existência de lançamento é histórica: resultado posteriormente cancelado continua caracterizando campeonato com lançamento para fins de alteração de status.
- O proprietário pode efetivar diretamente cancelamento ou reativação, sempre registrando justificativa obrigatória e auditoria.
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

### Datas do campeonato

`dtInicio` e `dtFim` são obrigatórias e representam somente o período informado do evento.

Regras:

- `dtInicio` obrigatória;
- `dtFim` obrigatória;
- campeonato de um dia: `dtInicio = dtFim`;
- campeonato de vários dias: `dtFim > dtInicio`;
- `dtFim` nunca pode ser anterior a `dtInicio`;
- as datas não determinam transição automática de estado;
- campeonato cuja `dtFim` já passou não se torna automaticamente encerrado, inativo ou cancelado;
- não existe estado `FINALIZADO` derivado automaticamente da data no MVP;
- datas não cancelam inscrições nem resultados;
- datas não habilitam ou desabilitam pontuação automaticamente;
- a validade esportiva para uma temporada continua sendo determinada pelas regras da temporada, vínculo `TemporadaCampeonato`, estado do campeonato e demais critérios de elegibilidade;
- alteração das datas segue as mesmas regras gerais de alteração cadastral do campeonato, independentemente de a data do evento já ter ocorrido.

Em consequência, o estado `ATIVO` deve ser entendido como estado administrativo do cadastro, e não como indicação de que o evento está acontecendo na data atual.

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

## Associação Campeonato ↔ Temporada

A relação entre campeonato e temporada é representada por `TemporadaCampeonato`.

### Autoridade

Somente profissional com permissão de `ADMINISTRACAO` sobre a temporada pode vincular ou desvincular campeonato, e somente quando o ciclo de vida da própria temporada admitir alteração estrutural.

No MVP:

- `ATIVO`: permite vincular e desvincular campeonato;
- `ENCERRADA`: não permite alterar `TemporadaCampeonato`; deve ser reaberta para `ATIVO` antes da operação;
- `CANCELADA`: não permite alteração ordinária de `TemporadaCampeonato` enquanto permanecer cancelada.

Profissional com permissão `CONSULTA` não pode executar nenhuma dessas operações.

A autorização é avaliada pelo contexto da `Temporada`, e não pela autoria do campeonato.

A possibilidade de processar `Inscricao` ou `Resultado` tardio em temporada `ENCERRADA` não autoriza alteração da composição de campeonatos: fatos esportivos tardios e configuração estrutural são conceitos distintos.

### Vinculação

Para vincular:

- temporada deve estar `ATIVO`;
- campeonato deve estar apto a ser associado segundo seu próprio estado;
- profissional deve possuir `ADMINISTRACAO` sobre a temporada;
- duplicidade da mesma associação ativa deve ser impedida.

Uma temporada `ENCERRADA` deve ser explicitamente reaberta para `ATIVO` antes de receber novo campeonato. A reabertura segue as regras de justificativa e auditoria definidas em `refinamento-temporada.md`.

### Desvinculação

Para desvincular, a temporada também deve estar `ATIVO`.

A existência de inscrições ou resultados do campeonato não impede a desvinculação.

Desvincular `Campeonato` de `Temporada` significa apenas encerrar/remover a relação estrutural `TemporadaCampeonato`.

A operação **não** deve:

- cancelar `Inscricao`;
- cancelar `Resultado`;
- inativar `Resultado`;
- alterar `situacaoResultado`;
- alterar `status` de `Resultado`;
- apagar histórico esportivo do campeonato.

Os lançamentos continuam existindo normalmente vinculados à `Inscricao` e ao `Campeonato`.

### Efeito sobre pontuação/ranking da temporada

A associação `TemporadaCampeonato` define se aquele campeonato participa da composição esportiva da temporada.

Portanto, ao desvincular um campeonato:

- os resultados permanecem íntegros e com seus estados originais;
- deixam de ser considerados na projeção de pontuação/ranking daquela temporada enquanto não houver vínculo ativo;
- rankings da temporada devem ser recalculados;
- nenhuma mutação deve ser feita nos próprios resultados.

Se o campeonato for novamente vinculado à mesma temporada, resultados já existentes e válidos voltam a ser considerados pela projeção da temporada, conforme as demais regras de elegibilidade e pontuação vigentes.

### Auditoria

Registrar para cada vínculo/desvínculo:

- temporada;
- campeonato;
- responsável;
- data/hora;
- operação executada;
- histórico suficiente para reconstrução da associação.

Recomendação técnica: preferir vínculo lógico/auditável em vez de apagar definitivamente o histórico da relação.

## Criação

Profissional com permissão geral de criação de campeonato pode criar diretamente, sem aprovação prévia do proprietário.

Fluxo:

1. informa nome, `dtInicio`, `dtFim` e localização;
2. valida presença obrigatória de `dtInicio` e `dtFim` e a regra `dtFim >= dtInicio`;
3. seleciona organizador ativo;
4. seleciona país ativo;
5. seleciona subdivisão válida quando aplicável;
6. sistema valida campos e equivalência;
7. verifica campeonatos ativos e cancelados equivalentes;
8. inexistindo conflito, cria no catálogo compartilhado.

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
- toda alteração deve revalidar organizador, localização, datas e equivalência antes de persistir;
- alteração não pode produzir duplicidade com outro campeonato ativo ou cancelado equivalente.

### Campeonato utilizado

A partir do momento em que já tiver existido ao menos uma `Inscricao` ou `Resultado`, a condição de utilizado é histórica e irreversível para fins de alteração cadastral.

Profissional autorizado não altera diretamente. Qualquer alteração cadastral gera solicitação ao proprietário.

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
- aprovação revalida estado, versão, organizador, localização, datas e equivalência;
- alteração concorrente não pode ser sobrescrita silenciosamente;
- recomendar `@Version` em `Campeonato` e guardar versão-base na solicitação.

## Cancelamento e reativação

A alteração de status do campeonato é sempre auditada e exige justificativa obrigatória, independentemente de quem a execute.

Transições permitidas:

```text
ATIVO -> CANCELADO
CANCELADO -> ATIVO
```

### Definição de campeonato com lançamento

Para este fluxo, considera-se que o campeonato **possui lançamento** quando já tiver existido ao menos um `Resultado` associado a qualquer `Inscricao` daquele campeonato.

A verificação é histórica:

- `Resultado` pendente conta como lançamento;
- `Resultado` aprovado conta como lançamento;
- `Resultado` cancelado continua contando como lançamento;
- correções ou versões históricas não apagam essa condição.

Assim, depois que o campeonato tiver o primeiro lançamento, ele permanece definitivamente classificado como campeonato **com lançamento** para fins de autorização de alteração de status.

### Campeonato sem lançamento

Quando nunca tiver existido `Resultado` relacionado ao campeonato:

- profissional autorizado pode cancelar diretamente;
- profissional autorizado pode reativar diretamente;
- justificativa é obrigatória;
- responsável, data/hora, estado anterior, novo estado e justificativa devem ser auditados;
- proprietário também pode executar diretamente, igualmente com justificativa obrigatória.

Não é necessária solicitação de aprovação do proprietário nesse cenário.

### Campeonato com lançamento

Quando já tiver existido ao menos um `Resultado`:

- profissional autorizado não pode alterar o status diretamente;
- deve criar solicitação com justificativa obrigatória;
- somente o proprietário aprova ou reprova a solicitação;
- o status permanece inalterado enquanto a solicitação estiver `PENDENTE`;
- aprovação efetiva a transição solicitada;
- reprovação mantém o estado atual;
- proprietário pode efetuar diretamente a mudança administrativa, mas deve registrar justificativa obrigatória e auditoria equivalente.

O fluxo é o mesmo tanto para cancelamento quanto para reativação.

### Solicitação de alteração de status

Para evitar duplicação estrutural entre cancelamento e reativação, a implementação pode utilizar uma entidade conceitual única:

```text
SolicitacaoAlteracaoStatusCampeonato
 ├── cdSolicitacao
 ├── cdCampeonato
 ├── cdSolicitante
 ├── statusOrigem
 ├── statusDestino
 ├── justificativa
 ├── statusSolicitacao
 ├── dtSolicitacao
 ├── cdResponsavelDecisao
 ├── dtDecisao
 └── motivoReprovacao
```

`statusSolicitacao`:

- `PENDENTE`
- `APROVADA`
- `REPROVADA`

Regras:

- solicitante deve estar autorizado para o campeonato;
- justificativa obrigatória;
- origem e destino devem corresponder a uma transição válida;
- permitir no máximo uma solicitação de alteração de status `PENDENTE` por campeonato;
- somente proprietário decide;
- decisão deve revalidar o estado atual para evitar aplicar solicitação obsoleta.

A adoção de entidade única é recomendação técnica; funcionalmente, cancelamento e reativação seguem exatamente a mesma regra de aprovação condicionada à existência histórica de lançamento.

### Efeitos do cancelamento

O cancelamento é lógico.

Ao cancelar:

- preserva inscrições e resultados;
- impede novas inscrições ordinárias;
- impede novas associações ordinárias com temporadas;
- resultados aprovados deixam de produzir efeito esportivo enquanto cancelado;
- resultados pendentes permanecem armazenados, porém inativos para operação esportiva ordinária;
- não altera artificialmente o status próprio dos resultados;
- rankings afetados devem ser recalculados.

### Efeitos da reativação

Ao reativar:

- preserva a mesma identidade do campeonato;
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
- vínculos e desvínculos com temporadas;
- solicitações de alteração cadastral;
- solicitações de alteração de status;
- justificativas obrigatórias de cancelamento e reativação, inclusive quando executados diretamente;
- dados atuais e propostos;
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
- `dtInicio` e `dtFim` são obrigatórias;
- datas do campeonato são informativas e não dirigem automaticamente o ciclo de vida;
- campeonato de um dia exige `dtInicio = dtFim`;
- campeonato de vários dias exige `dtFim > dtInicio`;
- terminar a data do campeonato não muda automaticamente seu estado;
- autorização profissional sobre campeonato existente deriva de relação administrativa com ao menos uma temporada vinculada ao campeonato;
- acesso de consulta à temporada não concede manutenção do campeonato;
- administrar qualquer uma das temporadas vinculadas é suficiente para autorização contextual;
- somente administrador de temporada vincula ou desvincula campeonato daquela temporada;
- `TemporadaCampeonato` somente pode ser alterada enquanto a temporada estiver `ATIVO`;
- temporada `ENCERRADA` exige reabertura para `ATIVO` antes de vincular/desvincular campeonato;
- temporada `CANCELADA` não admite alteração ordinária de sua composição de campeonatos;
- processamento tardio de inscrição/resultado em `ENCERRADA` não libera alteração estrutural;
- vínculo/desvínculo independe da existência de inscrições ou resultados quando a temporada permite alteração estrutural;
- desvincular não cancela nem inativa inscrições ou resultados;
- desvincular remove apenas a participação do campeonato na projeção daquela temporada;
- revincular faz resultados válidos voltarem a participar da projeção da temporada;
- qualquer profissional autorizado pode editar diretamente campeonato não utilizado;
- campeonato utilizado exige solicitação de alteração cadastral ao proprietário;
- a condição de utilizado é histórica;
- toda alteração de status exige justificativa;
- campeonato sem qualquer lançamento histórico pode ser cancelado ou reativado diretamente por profissional autorizado;
- campeonato com qualquer lançamento histórico exige aprovação do proprietário para alteração de status solicitada por profissional;
- lançamento cancelado continua caracterizando existência histórica de lançamento;
- proprietário pode alterar status diretamente, sempre com justificativa obrigatória e auditoria;
- campeonato exige organizador, país e subdivisão quando aplicável;
- equivalência usa nome normalizado + organizador + país + subdivisão + data inicial;
- local detalhado e data final não fazem parte da identidade;
- cancelamento é lógico;
- campeonato cancelado equivalente não pode ser recadastrado;
- inscrições e resultados são preservados no cancelamento;
- categoria/classe pertencem ao `Resultado`, não ao `Campeonato`;
- inscrição pertence ao campeonato, não à temporada.
