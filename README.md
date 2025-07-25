# SRV-Team - API de Gerenciamento de Times

Este é um projeto Spring Boot que fornece uma API REST para gerenciamento de times de trabalho.

## 🚀 Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.2.2**
- **Spring Data JPA**
- **Spring Web**
- **Maven**
- **H2 Database** (desenvolvimento)
- **MySQL** (produção)
- **Spring Boot DevTools**
- **Spring Boot Validation**

## 📁 Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/example/srvteam/
│   │   ├── SrvTeamApplication.java      # Classe principal
│   │   ├── controller/
│   │   │   └── TeamController.java      # Controlador REST
│   │   ├── model/
│   │   │   └── Team.java               # Entidade Team
│   │   ├── repository/
│   │   │   └── TeamRepository.java     # Repositório JPA
│   │   └── service/
│   │       └── TeamService.java        # Lógica de negócio
│   └── resources/
│       └── application.properties       # Configurações
└── test/
    └── java/com/example/srvteam/
        ├── SrvTeamApplicationTests.java
        └── controller/
            └── TeamControllerTest.java
```

## 🛠️ Como Executar

### Pré-requisitos
- Java 17 ou superior
- Maven 3.6 ou superior

### Passos para execução

1. **Clone o repositório**
   ```bash
   git clone <url-do-repositorio>
   cd srv-team
   ```

2. **Compile o projeto**
   ```bash
   mvn clean compile
   ```

3. **Execute os testes**
   ```bash
   mvn test
   ```

4. **Execute a aplicação**
   ```bash
   mvn spring-boot:run
   ```

A aplicação estará disponível em: `http://localhost:8080`

## 📊 Banco de Dados

### Desenvolvimento
- **H2 Database** (em memória)
- Console H2: `http://localhost:8080/h2-console`
- URL: `jdbc:h2:mem:testdb`
- Usuário: `sa`
- Senha: `password`

### Produção
- Configure as propriedades do MySQL no `application.properties`

## 🔗 Endpoints da API

### Teams

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/teams` | Lista todos os times |
| GET | `/api/teams/{id}` | Busca time por ID |
| GET | `/api/teams/name/{name}` | Busca time por nome |
| GET | `/api/teams/search?name={name}` | Busca times por nome (parcial) |
| GET | `/api/teams/search?lead={lead}` | Busca times por líder |
| GET | `/api/teams/search?description={keyword}` | Busca times por palavra-chave na descrição |
| POST | `/api/teams` | Cria novo time |
| PUT | `/api/teams/{id}` | Atualiza time existente |
| DELETE | `/api/teams/{id}` | Remove time |

### Exemplo de JSON para Team

```json
{
  "name": "Desenvolvimento",
  "description": "Time responsável pelo desenvolvimento de software",
  "email": "dev@example.com",
  "teamLead": "João Silva"
}
```

## 🧪 Testando a API

### Criar um time
```bash
curl -X POST http://localhost:8080/api/teams \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Desenvolvimento",
    "description": "Time de desenvolvimento de software",
    "email": "dev@example.com",
    "teamLead": "João Silva"
  }'
```

### Listar todos os times
```bash
curl -X GET http://localhost:8080/api/teams
```

### Buscar time por ID
```bash
curl -X GET http://localhost:8080/api/teams/1
```

## 🔧 Configurações

As principais configurações estão no arquivo `src/main/resources/application.properties`:

- Porta do servidor: `8080`
- Banco H2 em memória para desenvolvimento
- Logs detalhados para debug
- Console H2 habilitado

## 📝 Validações

A entidade Team possui as seguintes validações:

- **Nome**: Obrigatório, entre 2 e 100 caracteres
- **Descrição**: Máximo 500 caracteres
- **Email**: Formato de email válido
- **Team Lead**: Opcional

## 🚀 Próximos Passos

- [ ] Implementar autenticação JWT
- [ ] Adicionar paginação nos endpoints
- [ ] Implementar cache com Redis
- [ ] Adicionar documentação Swagger/OpenAPI
- [ ] Implementar logs estruturados
- [ ] Adicionar métricas com Micrometer
- [ ] Implementar testes de integração

## 📄 Licença

Este projeto está sob a licença MIT.
