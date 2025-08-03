# API de Times - Documentação

## Estrutura da Entidade Time ✅

### Tabela: `tbTime`

- **cdTime**: Integer (PK, Auto-increment) - Código único do time
- **dsNome**: String (250 chars, NOT NULL) - Nome do time
- **logo**: String (500 chars, NULLABLE) - URL/caminho do logo
- **cdProfissionalResponsavel**: Integer (FK, NOT NULL) - Referência ao usuário responsável

## Endpoints Implementados

### 📋 **POST /v1/time** - Criar novo time

**Request Body:**

```json
{
  "dsNome": "Time Alpha",
  "logo": "https://example.com/logo.png",
  "cdProfissionalResponsavel": 1
}
```

**Response:**

```json
{
  "cdTime": 1,
  "dsNome": "Time Alpha",
  "logo": "https://example.com/logo.png",
  "cdProfissionalResponsavel": 1,
  "nomeProfissionalResponsavel": "João Silva",
  "emailProfissionalResponsavel": "joao@example.com"
}
```

### 🗑️ **DELETE /v1/time/{cdTime}** - Deletar time

**Response:** `"Time deletado com sucesso"`

### 🔍 **GET /v1/time/{cdTime}** - Obter time por ID

**Response:** Objeto `TimeResponse` com dados do time e profissional responsável

### 📊 **GET /v1/time/profissional/{cdProfissionalResponsavel}** - Listar times por profissional

**Response:** Array de `TimeResponse` com todos os times do profissional

### 📋 **GET /v1/time** - Listar todos os times

**Response:** Array de `TimeResponse` com todos os times cadastrados

### 🔎 **GET /v1/time/buscar/{nome}** - Buscar times por nome (LIKE)

**Response:** Array de `TimeResponse` com times que contêm o nome informado

### ✅ **GET /v1/time/existe/{nome}** - Verificar se time existe

**Response:** `true` ou `false`

## Validações Implementadas ✅

### ✅ **Criação de Time:**

- Nome obrigatório (max 250 caracteres)
- Profissional responsável obrigatório e deve existir
- Profissional deve estar ativo
- Profissional não pode ser do tipo "Atleta" (código 6)
- Nome do time deve ser único

### ✅ **Segurança:**

- Todos os endpoints protegidos por autenticação
- Validação de DTOs com Bean Validation
- Tratamento de erros personalizado

## Tipos de Acesso Permitidos como Responsável

| Código | Tipo          | Pode ser Responsável |
| ------ | ------------- | -------------------- |
| 1      | Administrador | ✅ Sim               |
| 2      | Nutricionista | ✅ Sim               |
| 3      | Treinador     | ✅ Sim               |
| 4      | Coach         | ✅ Sim               |
| 5      | Funcionário   | ✅ Sim               |
| 6      | Atleta        | ❌ Não               |

## Exemplos de Uso

### 1. Criar um time

```bash
POST /v1/time
{
  "dsNome": "Equipe Futebol Profissional",
  "logo": "https://cdn.example.com/logos/equipe-futebol.png",
  "cdProfissionalResponsavel": 3
}
```

### 2. Listar times de um treinador

```bash
GET /v1/time/profissional/3
```

### 3. Buscar times com "futebol" no nome

```bash
GET /v1/time/buscar/futebol
```

### 4. Verificar se time "Equipe Alpha" já existe

```bash
GET /v1/time/existe/Equipe Alpha
```

## Relacionamentos

### 🔗 **Time → Usuario (Profissional Responsável)**

- **Tipo**: Many-to-One
- **Carregamento**: Lazy (otimizado com JOIN FETCH)
- **Cascata**: Nenhuma (preserva integridade)

## Performance e Otimizações ✅

- ✅ **Índices criados**: dsNome, cdProfissionalResponsavel
- ✅ **JOIN FETCH**: Carregamento otimizado do profissional
- ✅ **Queries customizadas**: Para busca com detalhes
- ✅ **Validação prévia**: Evita consultas desnecessárias

## Tratamento de Erros

### 🚫 **Erros Comuns:**

- **400 Bad Request**: Dados inválidos, time duplicado, atleta como responsável
- **404 Not Found**: Time não encontrado, profissional não encontrado
- **401 Unauthorized**: Token inválido ou ausente

### 📝 **Mensagens de Erro:**

- `"Já existe um time com esse nome: {nome}"`
- `"Profissional responsável não encontrado com ID: {id}"`
- `"Profissional responsável está inativo"`
- `"Atletas não podem ser responsáveis por times"`
- `"Time não encontrado com ID: {id}"`

## Testando com cURL

```bash
# Criar time
curl -X POST "http://localhost:8080/v1/time" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer {token}" \
     -d '{
       "dsNome": "Time Exemplo",
       "logo": "https://example.com/logo.png",
       "cdProfissionalResponsavel": 1
     }'

# Listar times de um profissional
curl -H "Authorization: Bearer {token}" \
     "http://localhost:8080/v1/time/profissional/1"

# Deletar time
curl -X DELETE \
     -H "Authorization: Bearer {token}" \
     "http://localhost:8080/v1/time/1"
```

## Arquivos Criados

✅ **Model**: `Time.java`
✅ **Repository**: `TimeRepository.java`  
✅ **Service**: `TimeService.java`
✅ **Controller**: `TimeController.java`
✅ **DTOs**: `TimeRequest.java`, `TimeResponse.java`
✅ **Mapper**: `TimeMapper.java`
✅ **SQL**: `create_table_tbTime.sql`

## Status: ✅ IMPLEMENTAÇÃO COMPLETA

Todos os requisitos foram implementados com sucesso:

- ✅ Repositório TimeRepository
- ✅ Model Time com tabela tbTime
- ✅ Service com métodos insTime, delTime, getTime, listTime
- ✅ Controller com endpoints /v1/time
- ✅ Autenticação obrigatória em todos os endpoints
- ✅ Validações e tratamento de erros
- ✅ Documentação completa
