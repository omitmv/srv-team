# API de Time-Profissional - Documentação

## Estrutura da Entidade TimeProfissional ✅

### Tabela: `tbTimeProfissional`

- **cdTime**: Integer (PK, FK) - Código do time (referência à tbTime)
- **cdProfissional**: Integer (PK, FK) - Código do profissional (referência à tbUsuario)
- **Chave Primária Composta**: (cdTime, cdProfissional)

## Relacionamento N:N

Esta entidade representa um relacionamento Muitos-para-Muitos entre **Times** e **Profissionais (Usuários)**.

## Endpoints Implementados

### 📋 **POST /v1/time-profissional** - Adicionar profissional ao time

**Request Body:**

```json
{
  "cdTime": 1,
  "cdProfissional": 2
}
```

**Response:**

```json
{
  "cdTime": 1,
  "cdProfissional": 2,
  "nomeTime": "Time Alpha",
  "logoTime": "https://example.com/logo.png",
  "nomeProfissional": "João Silva",
  "emailProfissional": "joao@example.com",
  "cdTpAcesso": 3,
  "tipoAcesso": "Treinador"
}
```

### 🗑️ **DELETE /v1/time-profissional/{cdTime}/{cdProfissional}** - Remover profissional do time

**Response:** `"Profissional removido do time com sucesso"`

### 🔍 **GET /v1/time-profissional/{cdTime}/{cdProfissional}** - Obter associação específica

**Response:** Objeto `TimeProfissionalResponse` com dados completos

### 👥 **GET /v1/time-profissional/time/{cdTime}** - Listar profissionais de um time

**Response:** Array de `TimeProfissionalResponse` com todos os profissionais do time

### 🏆 **GET /v1/time-profissional/profissional/{cdProfissional}** - Listar times de um profissional

**Response:** Array de `TimeProfissionalResponse` com todos os times do profissional

### 📊 **GET /v1/time-profissional/time/{cdTime}/count** - Contar profissionais no time

**Response:** Número (long) de profissionais no time

### 📈 **GET /v1/time-profissional/profissional/{cdProfissional}/count** - Contar times do profissional

**Response:** Número (long) de times do profissional

### ✅ **GET /v1/time-profissional/existe/{cdTime}/{cdProfissional}** - Verificar se associação existe

**Response:** `true` ou `false`

### 🧹 **DELETE /v1/time-profissional/time/{cdTime}/todos** - Remover todos os profissionais do time

**Response:** `"Todos os profissionais foram removidos do time com sucesso"`

## Validações Implementadas ✅

### ✅ **Criação de Associação:**

- Time deve existir
- Profissional deve existir e estar ativo
- Profissional não pode ser do tipo "Atleta" (código 6)
- Associação não pode ser duplicada
- Campos obrigatórios validados

### ✅ **Regras de Negócio:**

- Apenas profissionais ativos podem ser associados
- Atletas são impedidos de fazer parte da equipe técnica
- Validação de existência antes de operações
- Transações para operações críticas

## Tipos de Acesso Permitidos na Equipe Técnica

| Código | Tipo          | Pode Integrar Equipe |
| ------ | ------------- | -------------------- |
| 1      | Administrador | ✅ Sim               |
| 2      | Nutricionista | ✅ Sim               |
| 3      | Treinador     | ✅ Sim               |
| 4      | Coach         | ✅ Sim               |
| 5      | Funcionário   | ✅ Sim               |
| 6      | Atleta        | ❌ Não               |

## Exemplos de Uso

### 1. Adicionar treinador ao time

```bash
POST /v1/time-profissional
{
  "cdTime": 1,
  "cdProfissional": 3
}
```

### 2. Listar equipe técnica de um time

```bash
GET /v1/time-profissional/time/1
```

### 3. Ver todos os times que um nutricionista trabalha

```bash
GET /v1/time-profissional/profissional/5
```

### 4. Verificar se um coach está em um time específico

```bash
GET /v1/time-profissional/existe/1/7
```

### 5. Contar quantos profissionais tem no time

```bash
GET /v1/time-profissional/time/1/count
```

### 6. Remover profissional específico do time

```bash
DELETE /v1/time-profissional/1/3
```

## Relacionamentos e Performance ✅

### 🔗 **Relacionamentos**

- **TimeProfissional → Time**: Many-to-One (Lazy Loading)
- **TimeProfissional → Usuario**: Many-to-One (Lazy Loading)
- **Carregamento otimizado**: JOIN FETCH para dados completos

### 🚀 **Otimizações**

- ✅ **Índices**: cdTime, cdProfissional
- ✅ **Queries customizadas**: Com JOIN FETCH
- ✅ **Chave primária composta**: Performance otimizada
- ✅ **Cascata configurada**: DELETE CASCADE para integridade

## Tratamento de Erros

### 🚫 **Erros Comuns:**

- **400 Bad Request**: Dados inválidos, atleta como profissional, associação duplicada
- **404 Not Found**: Time/profissional não encontrado, associação inexistente
- **401 Unauthorized**: Token inválido ou ausente

### 📝 **Mensagens de Erro:**

- `"Time não encontrado com ID: {id}"`
- `"Profissional não encontrado com ID: {id}"`
- `"Profissional está inativo"`
- `"Atletas não podem fazer parte da equipe técnica de times"`
- `"Profissional já está associado a este time"`
- `"Associação não encontrada entre time {id} e profissional {id}"`

## Casos de Uso Práticos

### 🎯 **Gestão de Equipe Técnica**

1. **Formar equipe multidisciplinar**: Adicionar nutricionista, treinador, coach
2. **Reorganização de equipes**: Transferir profissionais entre times
3. **Auditoria**: Verificar composição de equipes técnicas
4. **Relatórios**: Listar times por profissional para controle de carga

### 📋 **Cenários Comuns**

- Time de futebol com treinador principal + nutricionista + preparador físico
- Profissional que trabalha em múltiplos times
- Substituição temporária de membros da equipe técnica
- Controle de acesso baseado na participação em times

## Testando com cURL

```bash
# Adicionar profissional ao time
curl -X POST "http://localhost:8080/v1/time-profissional" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer {token}" \
     -d '{"cdTime": 1, "cdProfissional": 3}'

# Listar equipe técnica
curl -H "Authorization: Bearer {token}" \
     "http://localhost:8080/v1/time-profissional/time/1"

# Remover profissional
curl -X DELETE \
     -H "Authorization: Bearer {token}" \
     "http://localhost:8080/v1/time-profissional/1/3"
```

## Arquivos Criados

✅ **Model**: `TimeProfissional.java`, `TimeProfissionalId.java`  
✅ **Repository**: `TimeProfissionalRepository.java`  
✅ **Service**: `TimeProfissionalService.java`  
✅ **Controller**: `TimeProfissionalController.java`  
✅ **DTOs**: `TimeProfissionalRequest.java`, `TimeProfissionalResponse.java`  
✅ **Mapper**: `TimeProfissionalMapper.java`  
✅ **SQL**: `create_table_tbTimeProfissional.sql`

## Estrutura do Banco

```sql
-- Chave primária composta
PRIMARY KEY (cdTime, cdProfissional)

-- Constraints de integridade
FOREIGN KEY (cdTime) REFERENCES tbTime(cdTime)
FOREIGN KEY (cdProfissional) REFERENCES tbUsuario(cdUsuario)

-- Trigger para validar tipo de usuário
TRIGGER para impedir atletas na equipe técnica
```

## Status: ✅ IMPLEMENTAÇÃO COMPLETA

Todos os requisitos foram implementados com sucesso:

- ✅ Repositório TimeProfissionalRepository
- ✅ Model TimeProfissional com chave composta e relacionamentos
- ✅ Service com métodos: insTimeProfissional, delTimeProfissional, getTimeProfissional, listTimeProfissionalByTime, listTimeProfissionalByProfissional
- ✅ Controller com endpoints /v1/time-profissional
- ✅ Autenticação obrigatória em todos os endpoints
- ✅ Validações de negócio e tratamento de erros
- ✅ Performance otimizada com índices e queries customizadas
- ✅ Documentação completa e exemplos práticos

**Próximo passo**: Execute o script SQL `create_table_tbTimeProfissional.sql` no banco de dados para criar a tabela e começar a gerenciar equipes técnicas! 🚀
