# SRV-Team - API de Gerenciamento de Usuários

Este é um projeto Spring Boot que fornece uma API REST para gerenciamento de usuários.

## 🚀 Tecnologias Utilizadas### Fazer login
```bash
curl -X POST http://localhost:8080/v1/usuarios/login \
  -H "Content-Type: application/json" \
  -d '{
    "login": "joao.silva",
    "senha": "123456"
  }'
```va 17**
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
│   │   │   └── UsuarioController.java   # Controlador REST
│   │   ├── model/
│   │   │   └── Usuario.java            # Entidade Usuario
│   │   ├── repository/
│   │   │   └── UsuarioRepository.java  # Repositório JPA
│   │   ├── service/
│   │   │   └── UsuarioService.java     # Lógica de negócio
│   │   └── util/
│   │       └── PasswordUtil.java       # Utilitário de criptografia
│   └── resources/
│       └── application.properties       # Configurações
└── test/
    └── java/com/example/srvteam/
        ├── SrvTeamApplicationTests.java
        ├── controller/
        │   └── UsuarioControllerTest.java
        ├── service/
        │   └── UsuarioServiceTest.java
        └── util/
            └── PasswordUtilTest.java
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

### Usuários

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/v1/usuarios` | Lista todos os usuários |
| GET | `/v1/usuarios/{cdUsuario}` | Busca usuário por ID |
| GET | `/v1/usuarios/login/{login}` | Busca usuário por login |
| GET | `/v1/usuarios/email/{email}` | Busca usuário por email |
| GET | `/v1/usuarios/ativos` | Lista usuários ativos |
| POST | `/v1/usuarios` | Cria novo usuário |
| PUT | `/v1/usuarios/{cdUsuario}` | Atualiza usuário existente |
| DELETE | `/v1/usuarios/{cdUsuario}` | Remove usuário |
| POST | `/v1/usuarios/login` | Verifica credenciais de login |
| PATCH | `/v1/usuarios/{cdUsuario}/ativar` | Ativa usuário |
| PATCH | `/v1/usuarios/{cdUsuario}/inativar` | Inativa usuário |

### Exemplo de JSON para Usuario

```json
{
  "login": "joao.silva",
  "senha": "123456",
  "nome": "João Silva",
  "email": "joao.silva@example.com",
  "flAtivo": true,
  "dtExpiracao": "2024-12-31T23:59:59"
}
```

### Exemplo de JSON para Login

```json
{
  "login": "joao.silva",
  "senha": "123456"
}
```

## 🧪 Testando a API

### Criar um usuário
```bash
curl -X POST http://localhost:8080/v1/usuarios \
  -H "Content-Type: application/json" \
  -d '{
    "login": "joao.silva",
    "senha": "123456",
    "nome": "João Silva",
    "email": "joao.silva@example.com"
  }'
```

### Fazer login
```bash
curl -X POST http://localhost:8080/v1/usuarios/login \
  -H "Content-Type: application/json" \
  -d '{
    "login": "joao.silva",
    "senha": "123456"
  }'
```

### Listar usuários ativos
```bash
curl -X GET http://localhost:8080/v1/usuarios/ativos
```

## 🔧 Configurações

As principais configurações estão no arquivo `src/main/resources/application.properties`:

- Porta do servidor: `8080`
- Banco H2 em memória para desenvolvimento
- Logs detalhados para debug
- Console H2 habilitado

## 📝 Validações

A entidade Usuario possui as seguintes validações:

- **Login**: Obrigatório, entre 3 e 50 caracteres, deve ser único
- **Senha**: Obrigatória, criptografada com Base64 + MD5
- **Nome**: Obrigatório, entre 2 e 100 caracteres
- **Email**: Formato de email válido, deve ser único
- **Data de Cadastro**: Preenchida automaticamente
- **Flag Ativo**: Controla se o usuário está ativo
- **Data de Expiração**: Opcional, define quando o usuário expira

## 🚀 Próximos Passos

- [ ] Implementar autenticação JWT
- [ ] Adicionar paginação nos endpoints
- [ ] Implementar cache com Redis
- [ ] Adicionar documentação Swagger/OpenAPI
- [ ] Implementar logs estruturados
- [ ] Adicionar métricas com Micrometer
- [ ] Implementar testes de integração
- [ ] Melhorar criptografia de senhas (BCrypt)
- [ ] Implementar recuperação de senha
- [ ] Adicionar controle de sessão

## 📄 Licença

Este projeto está sob a licença MIT.
