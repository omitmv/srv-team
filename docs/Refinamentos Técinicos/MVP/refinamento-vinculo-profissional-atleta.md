# Refinamento técnico — vínculo profissional-atleta

Status: decisões de negócio consolidadas para o MVP; refinamento de implementação pendente.

## Objetivo

Modelar explicitamente a relação entre atleta e profissional, incluindo solicitação, aprovação, reprovação, criação automática, encerramento e vigência temporal do vínculo.

`VinculoProfissionalAtleta` é a fonte autoritativa da existência e da vigência histórica da relação atleta-profissional. Não existe vínculo específico por temporada no MVP.

## Decisão de modelagem

Criar entidade própria `VinculoProfissionalAtleta` com identidade própria. Não utilizar chave composta atleta/profissional como chave primária, pois o vínculo possui ciclo de vida, histórico e múltiplas tentativas ao longo do tempo.

### Estrutura proposta

- `cdVinculo`
- `cdAtleta`
- `cdProfissional`
- `status`
- `origem`
- `cdSolicitante`
- `dtSolicitacao`
- `dtInicio`
- `dtEncerramento`
- `motivoReprovacao`
- `motivoEncerramento`
- campos de auditoria

## Status do vínculo

Estados mínimos:

- `PENDENTE`
- `ATIVO`
- `REPROVADO`
- `ENCERRADO`

Transições principais:

```text
PENDENTE -> ATIVO
PENDENTE -> REPROVADO
ATIVO -> ENCERRADO
```

Uma nova tentativa após reprovação ou encerramento gera novo registro, preservando o histórico anterior.

Um vínculo encerrado nunca volta para `ATIVO`. Retomada futura da relação cria novo `VinculoProfissionalAtleta`.

## Vigência temporal

A existência histórica da relação não deve ser inferida apenas pelo `status` atual.

Para operações atuais que exigem relacionamento direto profissional-atleta, o vínculo precisa estar `ATIVO`.

Para validar se a relação existia em determinada data histórica, usar o intervalo do vínculo:

```text
vinculoVigenteNaData(atleta, profissional, dataReferencia)
= dtInicio <= dataReferencia
  E (dtEncerramento IS NULL OU dtEncerramento >= dataReferencia)
```

Consequências:

- vínculo atualmente `ENCERRADO` pode comprovar relação válida no passado;
- `status = ENCERRADO` não invalida retroativamente o período em que o vínculo esteve vigente;
- um novo vínculo posterior não preenche lacuna entre dois vínculos;
- histórico esportivo deve considerar a vigência correspondente à data de referência definida pela regra consumidora;
- para elegibilidade histórica de resultado em temporada, a data de referência é `Campeonato.dtInicio`.

Exemplo:

```text
Vínculo #1
01/01 -> 31/03

sem vínculo
01/04 -> 30/04

Vínculo #2
01/05 -> atual
```

Um campeonato em 15/04 não se torna elegível retroativamente apenas porque um novo vínculo foi criado em 01/05.

## Origem

A origem deve permitir distinguir, no mínimo:

- cadastro direto de novo atleta por profissional;
- pré-cadastro iniciado pelo atleta e liberado pelo profissional responsável;
- solicitação manual iniciada pelo atleta;
- solicitação manual iniciada pelo profissional;
- intervenção administrativa do proprietário.

Enum recomendado:

- `CADASTRO_DIRETO_PROFISSIONAL`
- `PRE_CADASTRO_ATLETA`
- `SOLICITACAO_ATLETA`
- `SOLICITACAO_PROFISSIONAL`
- `ADMINISTRATIVO`

## Regras de criação

### Cadastro direto de novo atleta por profissional

O vínculo nasce diretamente `ATIVO`, sem aprovação adicional do atleta.

`dtInicio` deve representar o início efetivo da relação.

### Pré-cadastro iniciado pelo atleta

A liberação da conta pelo profissional responsável cria vínculo diretamente `ATIVO`.

### Solicitação envolvendo atleta já cadastrado

O vínculo nasce `PENDENTE` e depende da aprovação do destinatário. Ao ser aprovado, deve receber `dtInicio` correspondente ao início efetivo da relação.

### Intervenção administrativa

O proprietário pode criar ou ativar diretamente um vínculo por ação administrativa. A origem deve ser `ADMINISTRATIVO`, com auditoria explícita.

## Regras de duplicidade

- no máximo uma solicitação `PENDENTE` por par atleta/profissional;
- no máximo um vínculo `ATIVO` por par atleta/profissional;
- vínculo ativo bloqueia nova solicitação;
- reenvio pelo mesmo solicitante reutiliza a pendência existente;
- solicitação inversa enquanto existe pendência é bloqueada;
- `REPROVADO` e `ENCERRADO` permanecem históricos e não impedem nova tentativa futura.

Múltiplos registros históricos para o mesmo par são esperados quando a relação é encerrada e posteriormente retomada.

## Reprovação

- justificativa obrigatória;
- justificativa preservada no histórico;
- consulta da justificativa exclusiva do proprietário, conforme regra do MVP.

## Encerramento

- atleta e profissional podem encerrar unilateralmente vínculo ativo;
- justificativa obrigatória;
- registrar responsável, `dtEncerramento` e auditoria;
- encerramento não cancela conta, inscrição ou resultado;
- vínculo deixa de autorizar operações futuras que dependem diretamente da relação;
- período histórico anterior permanece válido;
- novo relacionamento futuro exige novo registro.

O encerramento de vínculo com o criador de uma temporada pode retirar o atleta da composição corrente do ranking, mas não apaga resultados históricos elegíveis de campeonatos ocorridos enquanto o vínculo estava vigente.

## Solicitações pendentes

Solicitações `PENDENTE` não expiram automaticamente no MVP. Permanecem abertas até decisão explícita.

## Elegibilidade de usuários

`cdAtleta` deve apontar para usuário com perfil `ATLETA`.

`cdProfissional` deve apontar para perfil de negócio elegível. No modelo atual: `NUTRITIONISTA`, `TREINADOR` e `COACH`.

Não inferir profissional apenas por exclusão de `ATLETA`, pois `ADMINISTRADOR` e `FUNCIONARIO` não representam necessariamente atendimento profissional.

### Papel de negócio versus permissão administrativa

`ADMINISTRADOR` não substitui papel profissional de atendimento.

Administração é permissão/contexto de acesso. `NUTRITIONISTA`, `TREINADOR` e `COACH` são papéis de negócio.

Se `cdTpAcesso` único impedir a composição entre papel profissional e permissão administrativa, o modelo deverá evoluir para separar esses conceitos.

## Relação com Temporada

`VinculoProfissionalAtleta` e `TemporadaProfissional` possuem responsabilidades diferentes:

```text
VinculoProfissionalAtleta
-> existência real da relação atleta-profissional
-> composição corrente quando o profissional é o criador da temporada
-> comprovação histórica da relação na data do campeonato

TemporadaProfissional
-> autorização contextual para ADMINISTRACAO ou CONSULTA
-> não cria relação atleta-profissional
```

A composição dos atletas da temporada é derivada exclusivamente dos vínculos do `Temporada.cdCriador`.

Vínculo com administrador ou consultor não adiciona atleta à temporada.

Um profissional com `ADMINISTRACAO` pode operar, dentro do contexto da temporada, atletas pertencentes à temporada mesmo sem vínculo profissional-atleta próprio. Essa autorização contextual não cria nem simula um `VinculoProfissionalAtleta`.

`CONSULTA` continua sujeita às regras de visibilidade definidas na temporada e, para dados individualizados, exige vínculo próprio ativo quando assim definido.

## Relação com ranking e resultados

### Composição corrente do ranking

Para o atleta integrar atualmente a composição esportiva da temporada, deve existir vínculo `ATIVO` com `Temporada.cdCriador`, além das demais regras de elegibilidade.

### Elegibilidade histórica de resultado

Para um resultado contribuir para uma temporada:

```text
vinculoVigenteNaData(
  atleta,
  Temporada.cdCriador,
  Campeonato.dtInicio
)
```

O fato de o vínculo estar hoje `ENCERRADO` não elimina contribuição histórica ocorrida durante sua vigência.

Da mesma forma, reativar a relação através de novo vínculo não torna elegível campeonato ocorrido durante intervalo sem relação.

### Autorização operacional

Não usar vínculo direto como substituto de autorização contextual de temporada.

Em `Inscricao` e `Resultado`, a operação ordinária é autorizada pelas regras da temporada: criador ou `ADMINISTRACAO`, conforme o contexto definido nos respectivos refinamentos.

Portanto:

- vínculo direto isolado não autoriza lançamento/edição de resultado;
- vínculo direto isolado não autoriza decisão de inscrição;
- `ADMINISTRACAO` contextual pode autorizar operação sem vínculo próprio com o atleta;
- nenhuma dessas permissões altera a existência real do vínculo atleta-profissional.

## Índices e restrições recomendados

- índice por `cdAtleta`;
- índice por `cdProfissional`;
- índice composto `(cdAtleta, cdProfissional, status)`;
- unicidade lógica de uma única pendência por par;
- unicidade lógica de um único vínculo ativo por par;
- índice temporal por `(cdAtleta, cdProfissional, dtInicio, dtEncerramento)` quando útil para consultas históricas.

A estratégia física deve respeitar o SGBD adotado. Proteções críticas de unicidade devem ser resistentes a concorrência.

## Invariantes consolidadas

- vínculo possui identidade própria e histórico por ciclos;
- `PENDENTE`, `ATIVO`, `REPROVADO`, `ENCERRADO` são os estados do MVP;
- vínculo encerrado não é reativado; retomada cria novo registro;
- vínculo atual `ATIVO` controla operações atuais que dependem diretamente da relação;
- intervalo `dtInicio`/`dtEncerramento` controla validade histórica;
- vínculo encerrado pode ser historicamente válido em data anterior;
- novo vínculo não preenche retroativamente lacunas;
- para resultado em temporada, referência histórica é `Campeonato.dtInicio`;
- encerramento não apaga inscrição, resultado nem contribuição histórica válida;
- atleta e profissional podem encerrar unilateralmente com justificativa;
- solicitações pendentes não expiram automaticamente;
- proprietário pode criar/ativar vínculo administrativamente com auditoria;
- perfil administrativo não substitui papel profissional;
- mesma equipe/time não cria vínculo automaticamente;
- vínculo do criador determina composição da temporada;
- vínculo de administrador/consultor não adiciona atleta à temporada;
- `TemporadaProfissional.ADMINISTRACAO` é autorização contextual e não cria vínculo;
- administrador pode operar atleta da temporada sem vínculo próprio quando a regra da temporada permitir;
- vínculo direto isolado não substitui autorização de `Inscricao`/`Resultado`.
