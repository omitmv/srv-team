# Refinamento técnico — vínculo profissional-atleta

Status: decisões de negócio consolidadas para o MVP; refinamento de implementação pendente.

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
- solicitação manual iniciada pelo profissional;
- intervenção administrativa do proprietário.

Sugestão de enum:

- `CADASTRO_DIRETO_PROFISSIONAL`
- `PRE_CADASTRO_ATLETA`
- `SOLICITACAO_ATLETA`
- `SOLICITACAO_PROFISSIONAL`
- `ADMINISTRATIVO`

## Regras de criação

### Cadastro direto de novo atleta por profissional

O vínculo nasce diretamente como `ATIVO`, sem aprovação adicional do atleta.

### Pré-cadastro iniciado pelo atleta

A liberação da conta pelo profissional responsável cria o vínculo diretamente como `ATIVO`.

### Solicitação envolvendo atleta já cadastrado

O vínculo nasce como `PENDENTE` e depende da aprovação do destinatário.

### Intervenção administrativa

O proprietário pode criar ou ativar diretamente um vínculo por ação administrativa. A origem deve ser registrada como `ADMINISTRATIVO`, preservando auditoria da ação.

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

- Atleta e profissional podem encerrar unilateralmente um vínculo ativo, sem aprovação da outra parte.
- Justificativa obrigatória.
- Encerrar vínculo não cancela conta, inscrição ou resultado.
- O encerramento afeta apenas autorizações futuras dependentes daquele vínculo.
- O histórico deve ser preservado.
- O encerramento deve registrar quem executou a ação e a data correspondente.

## Solicitações pendentes

Solicitações `PENDENTE` não expiram automaticamente no MVP. Permanecem abertas até uma decisão explícita de aprovação, reprovação ou cancelamento.

Essa decisão evita introduzir scheduler, política de validade e regras adicionais de notificação sem necessidade funcional imediata.

## Elegibilidade de usuários

`cdAtleta` deve apontar para usuário com perfil `ATLETA`.

`cdProfissional` deve apontar para perfil elegível como profissional. No modelo atual, os perfis explicitamente profissionais são `NUTRITIONISTA`, `TREINADOR` e `COACH`.

Não inferir profissional apenas por exclusão de `ATLETA`, pois existem outros perfis (`ADMINISTRADOR`, `FUNCIONARIO`) que não representam necessariamente atendimento profissional.

### Separação entre papel de negócio e permissão administrativa

`ADMINISTRADOR` não deve ser utilizado como substituto de um papel profissional de atendimento.

Administração representa permissão/contexto de acesso, enquanto `NUTRITIONISTA`, `TREINADOR` e `COACH` representam papéis de negócio.

No refinamento de implementação, deve-se evitar modelagem que force um usuário a perder seu papel profissional apenas porque recebeu permissão administrativa. Caso a estrutura atual de `cdTpAcesso` único impeça essa composição, deverá ser evoluída para separar perfil profissional de permissões administrativas.

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

## Decisões fechadas

1. Administração é permissão/contexto e não substitui o papel profissional de atendimento.
2. O proprietário pode criar ou ativar vínculo diretamente por ação administrativa, com origem auditável.
3. Atleta e profissional podem encerrar unilateralmente o vínculo, com justificativa obrigatória.
4. Solicitações pendentes não expiram automaticamente no MVP.
