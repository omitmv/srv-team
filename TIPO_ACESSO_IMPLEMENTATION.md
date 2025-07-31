# Implementação do Tipo de Acesso do Usuário

## Resumo das Alterações

Foi implementado um sistema de tipos de acesso para usuários, adicionando uma nova coluna `cdTpAcesso` na tabela `tbUsuario` e atualizando todas as referências relacionadas.

## Alterações Realizadas

### 1. Modelo Usuario.java

- Adicionada nova coluna `cdTpAcesso` do tipo `Integer`
- Adicionado import do `EnumTpAcesso`
- Criados novos construtores incluindo o tipo de acesso
- Adicionados getters/setters para `cdTpAcesso`
- Adicionado método `getTipoAcesso()` que retorna o enum correspondente
- Atualizado o método `toString()` para incluir o tipo de acesso

### 2. UsuarioService.java

- Adicionado import do `EnumTpAcesso`
- Implementada lógica para definir tipo de acesso padrão (ATLETA) quando não informado
- Atualizada lógica do método `updUsuario()` para permitir alteração do tipo de acesso

### 3. DTOs Criados

#### UsuarioRequest.java

- DTO para requests de criação/atualização de usuário
- Inclui validação obrigatória para `cdTpAcesso`
- Contém todos os campos necessários para operações CRUD

#### UsuarioResponse.java

- DTO para responses de usuário
- Inclui tanto o código (`cdTpAcesso`) quanto a descrição (`tipoAcesso`)
- Remove automaticamente a senha por segurança

#### TipoAcessoResponse.java

- DTO simples para retornar códigos e descrições dos tipos de acesso

### 4. UsuarioMapper.java

- Classe utilitária para conversão entre Entity e DTOs
- Métodos `toEntity()`, `toResponse()` e `updateEntity()`

### 5. UsuarioController.java

- Atualizado para usar DTOs em vez da entidade diretamente
- Todos os endpoints agora retornam informações do tipo de acesso
- Maior segurança ao não expor senhas nas responses

### 6. LoginResponse.java

- Adicionados campos `cdTpAcesso` e `tipoAcesso`
- Novo construtor que aceita o tipo de acesso
- Lógica automática para definir descrição baseada no código

### 7. TipoAcessoController.java

- Novo controller com endpoint `GET /v1/tipo-acesso`
- Retorna lista de todos os tipos de acesso disponíveis

## Tipos de Acesso Disponíveis

1. **Administrador (1)** - Acesso total ao sistema
2. **Nutricionista (2)** - Focado em aspectos nutricionais
3. **Treinador (3)** - Focado em treinos e exercícios
4. **Coach (4)** - Orientação e acompanhamento
5. **Funcionário (5)** - Acesso limitado a funcionalidades específicas
6. **Atleta (6)** - Acesso básico, foco em consulta e acompanhamento

## Scripts de Banco de Dados

O arquivo `database_migration.sql` contém os comandos necessários para:

- Adicionar a coluna `cdTpAcesso` na tabela `tbUsuario`
- Definir valor padrão (6 = ATLETA) para usuários existentes
- Criar índice para melhor performance

## Endpoints Afetados

### Existentes (atualizados):

- `POST /v1/usuario` - Agora requer `cdTpAcesso` no request
- `PUT /v1/usuario/{id}` - Permite atualização do tipo de acesso
- `GET /v1/usuario/{id}` - Retorna informações do tipo de acesso
- `GET /v1/usuario` - Lista usuários com tipos de acesso
- `GET /v1/usuario/ativos` - Lista usuários ativos com tipos de acesso
- `GET /v1/usuario/login/{login}` - Busca usuário com tipo de acesso
- `GET /v1/usuario/email/{email}` - Busca usuário com tipo de acesso
- `POST /v1/usuario/login` - Login retorna tipo de acesso do usuário

### Novos:

- `GET /v1/tipo-acesso` - Lista todos os tipos de acesso disponíveis

## Validações

- `cdTpAcesso` é obrigatório em criações de usuário
- Validação automática dos códigos através do enum
- Valor padrão (ATLETA) aplicado quando não informado no service

## Segurança

- Senhas continuam sendo removidas das responses
- DTOs garantem que apenas dados apropriados sejam expostos
- Validações de entrada mantidas e aprimoradas

## Como Testar

1. Execute o script de migração do banco de dados
2. Compile e execute a aplicação
3. Teste os endpoints:

   ```bash
   # Listar tipos de acesso
   GET /v1/tipo-acesso

   # Criar usuário com tipo de acesso
   POST /v1/usuario
   {
     "login": "teste",
     "senha": "123456",
     "nome": "Teste",
     "email": "teste@email.com",
     "cdTpAcesso": 1
   }

   # Login (deve retornar tipo de acesso)
   POST /v1/usuario/login
   {
     "login": "teste",
     "senha": "123456"
   }
   ```

## Observações

- A implementação mantém compatibilidade com código existente
- Usuários criados sem especificar tipo de acesso recebem automaticamente o tipo ATLETA
- Todas as responses de usuário incluem tanto o código quanto a descrição do tipo de acesso
- O sistema de autenticação foi atualizado para incluir informações do tipo de acesso no token de resposta
