# Refinamento técnico — vínculo profissional-atleta

Status: proposta técnica derivada das regras já consolidadas em `mvp-pontuacao.md`.

## Objetivo

Modelar explicitamente a relação entre atleta e profissional, incluindo solicitação, aprovação, reprovação, criação automática e encerramento do vínculo.

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

Campos de auditoria adicionais podem ser acrescentados no refinamento de implementação sem alterar a regra de negócio.

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

Uma nova tentativa após reprovação ou encerramento deve gerar um novo registro, preservando o histórico anterior.

## Origem

A origem deve permitir distinguir, no mínimo:

- cadastro direto de novo atleta por profissional;
- pré-cadastro iniciado pelo atleta e liberado pelo profissional responsável;
- solicitação manual iniciada pelo atleta;
- solicitação manual iniciada pelo profissional.

Sugestão de enum:

- `CADASTRO_DIRETO_PROFISSIONAL`
- `PRE_CADASTRO_ATLETA`
- `SOLICITACAO_ATLETA`
- `SOLICITACAO_PROFISSIONAL`

## Regras de criação

### Cadastro direto de novo atleta por profissional

O vínculo nasce diretamente como `ATIVO`, sem aprovação adicional do atleta.

### Pré-cadastro iniciado pelo atleta

A liberação da conta pelo profissional responsável cria o vínculo diretamente como `ATIVO`.

### Solicitação envolvendo atleta já cadastrado

O vínculo nasce como `PENDENTE` e depende da aprovação do destinatário.

## Regras de duplicidade

- Pode existir no máximo uma solicitação `PENDENTE` por par atleta/profissional, independentemente de quem iniciou.
- Se o par já possuir vínculo `ATIVO`, não deve ser criada nova solicitação.
- Se a solicitação pendente foi iniciada pelo mesmo solicitante, um novo envio deve reutilizar a mesma pendência e apenas reenviar a notificação.
- Se a pendência foi iniciada pela outra parte, bloquear nova solicitação e informar que já existe uma decisão aguardando o usuário atual.
- Registros `REPROVADO` e `ENCERRADO` permanecem como histórico e não impedem uma nova tentativa futura.

## Reprovação

- Justificativa obrigatória.
- A justificativa é preservada no histórico.
- Sua consulta é exclusiva do proprietário, conforme regra já definida no MVP.

## Encerramento

- Justificativa obrigatória.
- Encerrar vínculo não cancela conta, inscrição ou resultado.
- O encerramento afeta apenas autorizações futuras dependentes daquele vínculo.
- O histórico deve ser preservado.

## Elegibilidade de usuários

`cdAtleta` deve apontar para usuário com perfil `ATLETA`.

`cdProfissional` deve apontar para perfil elegível como profissional. No modelo atual, os perfis explicitamente profissionais são `NUTRITIONISTA`, `TREINADOR` e `COACH`.

Não inferir profissional apenas por exclusão de `ATLETA`, pois existem outros perfis (`ADMINISTRADOR`, `FUNCIONARIO`) que não representam necessariamente atendimento profissional.

## Índices e restrições recomendados

- índice por `cdAtleta`;
- índice por `cdProfissional`;
- índice composto por `(cdAtleta, cdProfissional, status)`;
- garantia de unicidade lógica de uma única pendência por par atleta/profissional;
- garantia de unicidade lógica de um único vínculo ativo por par atleta/profissional.

A estratégia exata para unicidade condicional deve respeitar o SGBD adotado; caso o banco não ofereça índice parcial, aplicar proteção no serviço e mecanismo complementar no banco quando possível.

## Autorização

A existência de vínculo `ATIVO` é condição necessária para operações que exigem relacionamento profissional-atleta, mas não é condição suficiente para acesso a temporada, inscrição ou resultado. Essas operações continuam sujeitas às demais regras de autorização do MVP.

Pertencer ao mesmo `Time` não cria vínculo profissional-atleta automaticamente.

## Pontos ainda a fechar

1. Se `ADMINISTRADOR` também pode atuar simultaneamente como profissional de atendimento ou se o tipo de acesso deve permanecer exclusivo.
2. Se o proprietário pode criar/ativar vínculo diretamente por ação administrativa.
3. Se o encerramento pode ser solicitado unilateralmente por atleta e profissional ou se algum caso exige aprovação.
4. Se haverá expiração automática de solicitações `PENDENTE`.
