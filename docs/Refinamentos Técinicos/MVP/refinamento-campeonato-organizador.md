# Refinamento técnico — Campeonato e Organizador

Status: modelagem técnica em andamento; ciclo de criação, alteração, cancelamento, reativação, localização e equivalência consolidado para o MVP.

## Objetivo

Modelar `Campeonato` como evento compartilhado entre profissionais e temporadas, independente de propriedade exclusiva de uma temporada, substituir organizador/federação como texto livre por referência a catálogo central de `Organizador` administrado pelo proprietário e padronizar a localização do evento por catálogo interno de país e subdivisão administrativa.

## Regras confirmadas

- `Organizador` pertence a catálogo central administrado exclusivamente pelo proprietário.
- O profissional pode criar campeonato.
- Todo campeonato deve possuir organizador selecionado do catálogo central.
- Todo campeonato deve informar país.
- Todo campeonato deve informar subdivisão administrativa de primeiro nível quando aplicável ao país (por exemplo, Estado/UF no Brasil).
- País e subdivisão não são digitados livremente pelo profissional.
- País e subdivisão pertencem a catálogo interno de referência baseado em códigos padronizados, preferencialmente ISO 3166 / ISO 3166-2.
- O fluxo de cadastro de campeonato não deve depender de consulta a API pública em tempo real.
- Campeonato criado por profissional integra o catálogo compartilhado.
- Campeonato não pertence exclusivamente a uma temporada.
- Associação campeonato-temporada é N:N via `TemporadaCampeonato`.
- Profissionais não editam diretamente campeonato já utilizado por inscrição ou resultado.
- Quando campeonato já possui inscrição ou resultado, qualquer alteração cadastral exige solicitação ao proprietário.
- A solicitação de alteração deve conter os dados atuais, os novos dados propostos e justificativa obrigatória do profissional.
- O proprietário deve ser notificado para aprovar ou reprovar a alteração.
- Somente o proprietário pode cancelar ou reativar um campeonato.
- Profissional pode solicitar cancelamento ou reativação mediante justificativa obrigatória.
- Campeonato cancelado não pode ser recadastrado como novo campeonato equivalente.
- Categoria e classe são informadas no `Resultado`, não pré-configuradas no campeonato.

## Organizador

Entidade conceitual:

```text
Organizador
 ├── cdOrganizador
 ├── dsNome
 ├── flAtivo
 └── auditoria
```

Somente o proprietário pode criar, editar, inativar ou reativar organizador.

Organizador já utilizado não deve ser fisicamente excluído. Inativação preserva campeonatos históricos e impede seleção em novos campeonatos.

## Catálogo geográfico

### País

Entidade conceitual:

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

Entidade conceitual:

```text
Subdivisao
 ├── cdSubdivisao
 ├── cdPais
 ├── codigoIso
 ├── dsNome
 ├── flAtivo
 └── auditoria técnica
```

`Subdivisao` representa o primeiro nível administrativo relevante do país. Exemplos:

- Brasil: Estado/UF;
- Estados Unidos: State;
- Canadá: Province/Territory;
- outros países: região, província ou equivalente quando aplicável.

Regras:

- o catálogo é interno ao sistema;
- os registros são pré-carregados e atualizados administrativamente/sistemicamente;
- profissional não cadastra país ou subdivisão durante o cadastro do campeonato;
- o proprietário não precisa cadastrar manualmente cada país ou subdivisão;
- utilizar códigos padronizados como referência estável;
- indisponibilidade de serviço externo não pode impedir o cadastro ou consulta de campeonatos;
- país inativo não pode ser selecionado em novo campeonato, mas continua válido para histórico;
- subdivisão inativa segue a mesma regra;
- `cdSubdivisao` deve pertencer ao `cdPais` informado.

## Campeonato

Entidade conceitual:

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

Estados de negócio mínimos:

- `ATIVO`
- `CANCELADO`

Regras de localização:

- `cdPais` é obrigatório;
- `cdSubdivisao` é obrigatório quando o país possuir subdivisão administrativa aplicável ao catálogo;
- `dsLocal` é opcional e livre para detalhes como ginásio, centro de eventos ou endereço;
- `dsLocal` não participa da identidade semântica do campeonato;
- alteração de `dsLocal` não caracteriza novo campeonato.

Para reduzir impacto de migração, pode ser mantida a identidade física atual `cdCompeticao`/`tbCompeticao`, tratando semanticamente a entidade como Campeonato.

O campo legado `federacao` não deve permanecer como fonte autoritativa. `Campeonato` referencia `Organizador` por identidade.

## Criação

Profissional autorizado pode criar campeonato diretamente, sem aprovação prévia do proprietário.

Na criação:

1. informa nome, período e localização;
2. seleciona organizador ativo;
3. seleciona país ativo;
4. seleciona subdivisão válida para o país quando aplicável;
5. sistema valida campos e equivalência;
6. verifica campeonatos ativos e cancelados equivalentes;
7. se não houver conflito, cria campeonato no catálogo compartilhado.

`cdCriador` é informação de auditoria, não propriedade exclusiva do registro.

## Equivalência e duplicidade

A identidade semântica aproximada do campeonato no MVP é:

```text
(nomeNormalizado, cdOrganizador, cdPais, cdSubdivisao, dtInicio)
```

Quando o país não possuir subdivisão aplicável, `cdSubdivisao` pode ser nulo e a comparação considera essa ausência de forma consistente.

### Normalização do nome

A comparação deve usar uma representação normalizada para reduzir duplicidades meramente textuais, considerando pelo menos:

- trim;
- comparação case-insensitive;
- normalização de espaços repetidos;
- normalização de acentuação/pontuação quando tecnicamente segura.

Exemplos semanticamente equivalentes:

```text
Mr. Rio 2026
MR RIO 2026
Mr Rio 2026
```

### Campos que não participam da identidade

Não usar como componente obrigatório da equivalência:

- `dtFim`;
- `dsLocal`.

Esses dados podem mudar sem representar nascimento de outro campeonato.

### Comportamento

- equivalente `ATIVO` -> bloquear novo cadastro e direcionar para o campeonato existente;
- equivalente `CANCELADO` -> bloquear novo cadastro e oferecer solicitação de reativação;
- inexistente -> permitir criação ordinária.

A regra de equivalência é invariante de domínio. Não depender exclusivamente de `UNIQUE CONSTRAINT` textual simples, pois a normalização pode evoluir. A implementação deve também considerar proteção contra concorrência para impedir duplicidades simultâneas.

## Alteração de campeonato

### Campeonato ainda sem inscrição e sem resultado

Enquanto o campeonato ainda não possuir qualquer `Inscricao` ou `Resultado`, a política de edição pode permanecer mais simples conforme a autorização ordinária definida para cadastro.

A partir do momento em que existir ao menos uma inscrição ou resultado associado, o campeonato passa a ser considerado **utilizado** para efeito de alteração cadastral.

A verificação é histórica: basta já ter existido inscrição ou resultado, mesmo que posteriormente cancelado ou inativado.

### Campeonato utilizado

Quando o campeonato já possuir inscrição ou resultado, o profissional não pode alterar seus dados diretamente.

Qualquer alteração deve gerar uma solicitação para decisão do proprietário.

O fluxo é:

```text
Profissional informa alteração
        |
        | justificativa obrigatória
        v
Sistema captura:
- dados atuais
- novos dados propostos
- campos alterados
        |
        v
Solicitação PENDENTE
        |
        +--> notificação ao proprietário
        |
        v
Proprietário analisa antes/depois + justificativa
        |
   +----+----+
   |         |
aprova     reprova
   |         |
aplica    mantém cadastro atual
```

### Solicitação de alteração

Estrutura conceitual:

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

Status mínimos:

- `PENDENTE`
- `APROVADA`
- `REPROVADA`

A justificativa do profissional é obrigatória.

A solicitação deve preservar snapshot suficiente para comparação, inclusive alterações de organizador, país, subdivisão, datas e local.

Persistir somente uma descrição textual genérica não é suficiente.

### Notificação ao proprietário

Ao criar uma solicitação de alteração de campeonato utilizado, o sistema deve gerar uma notificação destinada ao proprietário.

A notificação deve permitir identificar no mínimo:

- campeonato;
- profissional solicitante;
- data da solicitação;
- justificativa;
- dados atuais;
- dados propostos;
- campos modificados.

A notificação representa uma pendência de decisão e não altera o campeonato antes da aprovação.

### Aprovação

Somente o proprietário decide a solicitação.

Ao aprovar:

1. revalidar que a solicitação continua `PENDENTE`;
2. revalidar a versão atual do campeonato;
3. verificar se o estado atual ainda corresponde à base usada na solicitação;
4. revalidar organizador, país, subdivisão e equivalência;
5. aplicar os novos valores;
6. registrar antes/depois definitivo;
7. registrar proprietário responsável e data/hora;
8. marcar solicitação como `APROVADA`.

Se o campeonato tiver sido alterado desde a solicitação, a aprovação não deve sobrescrever silenciosamente dados mais recentes. Deve ocorrer conflito e nova análise.

Recomendação técnica: `@Version` em `Campeonato` e armazenamento da versão-base na solicitação.

### Reprovação

Ao reprovar:

- campeonato permanece sem alteração;
- solicitação passa a `REPROVADA`;
- responsável e data são registrados;
- motivo da reprovação deve ser preservado para auditoria.

### Concorrência

Para o MVP, permitir no máximo uma `SolicitacaoAlteracaoCampeonato` `PENDENTE` por campeonato.

## Cancelamento

Somente o proprietário pode efetivamente executar:

```text
ATIVO -> CANCELADO
```

Nenhum profissional, inclusive criador do campeonato ou administrador de temporada, pode cancelar diretamente.

Profissional pode solicitar cancelamento mediante justificativa obrigatória.

Entidade conceitual:

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

Permitir no máximo uma solicitação de cancelamento `PENDENTE` por campeonato.

## Efeito do cancelamento sobre inscrições, resultados e ranking

O cancelamento é lógico e preserva todos os registros.

Ao cancelar o campeonato:

- campeonato passa a `CANCELADO`;
- não aceita novas inscrições ordinárias;
- não aceita novas associações ordinárias com temporadas;
- inscrições existentes são preservadas;
- resultados existentes são preservados;
- resultados do campeonato deixam de produzir efeito esportivo enquanto o campeonato estiver cancelado;
- resultados `APROVADO` deixam de contribuir com pontos e rankings;
- resultados `PENDENTE_APROVACAO` permanecem armazenados, porém ficam inativos para operação esportiva ordinária enquanto o campeonato estiver cancelado;
- nenhuma entidade é fisicamente excluída.

Os resultados não devem ter seu próprio status funcional sobrescrito para `CANCELADO` somente porque o campeonato foi cancelado.

## Reativação de campeonato cancelado

Diferentemente de uma `Temporada` cancelada, um campeonato cancelado pode ser reativado preservando a mesma identidade.

Somente o proprietário pode executar:

```text
CANCELADO -> ATIVO
```

Quando um profissional tentar cadastrar um campeonato equivalente a um registro `CANCELADO`, o sistema não deve permitir novo cadastro e deve oferecer solicitação de reativação.

A solicitação exige justificativa e somente o proprietário pode aprovar.

### Solicitação de reativação

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

- somente campeonato `CANCELADO` pode receber solicitação de reativação;
- justificativa obrigatória;
- profissional apenas solicita;
- somente proprietário decide;
- no máximo uma solicitação `PENDENTE` por campeonato.

## Efeito da reativação sobre resultados

Ao reativar:

- mesmo campeonato retorna a `ATIVO`;
- inscrições existentes continuam vinculadas ao mesmo ID;
- resultados existentes continuam vinculados às mesmas inscrições;
- resultados anteriormente `APROVADO` voltam a produzir efeito esportivo, se ainda válidos;
- resultados `PENDENTE_APROVACAO` voltam ao fluxo ordinário;
- resultados `CANCELADO` por seu próprio fluxo continuam cancelados;
- rankings aplicáveis são recalculados.

## Auditoria

O histórico deve permitir identificar:

- criação;
- alterações solicitadas;
- dados atuais e propostos;
- justificativa do profissional;
- notificações geradas;
- decisões do proprietário;
- valores antes/depois;
- cancelamentos;
- reativações;
- responsáveis e datas.

## Categorias e classes

Campeonato não mantém coleção pré-configurada de categorias/classes. A combinação é registrada em `Resultado`.

Não criar `CampeonatoCategoria` ou `CampeonatoClasse` no MVP.

## Invariantes consolidadas

- Organizador é catálogo central administrado pelo proprietário.
- Campeonato exige organizador.
- Campeonato exige país.
- Campeonato exige subdivisão administrativa quando aplicável ao país.
- País e subdivisão são selecionados de catálogo interno de referência.
- O cadastro de campeonato não depende de API pública em tempo real.
- `dsLocal` é opcional e não participa da identidade semântica.
- Profissional pode criar campeonato diretamente.
- Campeonato integra catálogo compartilhado.
- Campeonato pode integrar várias temporadas.
- Equivalência no MVP usa `(nomeNormalizado, organizador, país, subdivisão, data inicial)`.
- `dtFim` e `dsLocal` não compõem a equivalência.
- Campeonato que já teve inscrição ou resultado é considerado utilizado.
- Profissional não altera diretamente campeonato utilizado.
- Alteração de campeonato utilizado exige solicitação com justificativa.
- Solicitação de alteração deve conter dados atuais e novos dados.
- Proprietário deve ser notificado sobre solicitação de alteração.
- Somente proprietário aprova ou reprova alteração de campeonato utilizado.
- Alteração só é aplicada após aprovação.
- Somente proprietário cancela campeonato.
- Profissional pode solicitar cancelamento com justificativa obrigatória.
- Cancelamento é lógico.
- Campeonato cancelado não é fisicamente excluído.
- Cancelamento preserva inscrições e resultados.
- Resultados de campeonato cancelado ficam esportivamente inativos sem alteração artificial de seu próprio status.
- Campeonato cancelado não pode ser recadastrado com novo ID.
- Tentativa de recadastro equivalente deve oferecer solicitação de reativação.
- Solicitação de reativação exige justificativa.
- Somente proprietário pode efetivar reativação.
- Reativação utiliza o mesmo `cdCampeonato`/`cdCompeticao`.
- Resultados previamente aprovados voltam a produzir efeito esportivo na reativação, se ainda válidos.
- Resultados cancelados por seu próprio fluxo não são reativados.
- Rankings são recalculados no cancelamento e na reativação.
- Categoria/classe não são pré-configuradas no campeonato.
- Inscrição pertence ao campeonato, não à temporada.
