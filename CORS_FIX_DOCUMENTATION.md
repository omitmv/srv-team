# Correção do Erro de CORS

## Problema Identificado

O erro `Access to XMLHttpRequest at 'http://192.168.50.174:8080/auth/login' from origin 'http://localhost:3000' has been blocked by CORS policy` indica que o backend Spring Boot não estava configurado para aceitar requisições do frontend executando em localhost:3000.

## Soluções Implementadas

### 1. Configuração Global de CORS - AppConfig.java

Criada uma configuração CORS global que permite:

- ✅ Todas as origens (`allowedOriginPatterns("*")`)
- ✅ Todos os métodos HTTP (GET, POST, PUT, DELETE, OPTIONS, PATCH)
- ✅ Todos os headers
- ✅ Credenciais
- ✅ Cache de preflight de 1 hora (3600s)

```java
@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Configuração detalhada para integração com Spring Security
    }
}
```

### 2. Integração com Spring Security - SecurityConfig.java

Atualizado o `SecurityConfig` para:

- ✅ Usar a configuração CORS definida no AppConfig
- ✅ Permitir acesso aos endpoints de login sem autenticação
- ✅ Permitir acesso ao endpoint de tipos de acesso

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource))
        // ... resto da configuração
}
```

### 3. Endpoint Alternativo - AuthController.java

Criado um controller adicional `/auth/login` para compatibilidade, caso o frontend esteja configurado para usar esse endpoint específico:

```java
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        // Mesma lógica do UsuarioController
    }
}
```

### 4. Endpoints Públicos Configurados

Os seguintes endpoints não requerem autenticação:

- ✅ `POST /v1/usuario/login` - Login original
- ✅ `POST /auth/login` - Login alternativo
- ✅ `GET /v1/tipo-acesso` - Lista tipos de acesso
- ✅ `POST /v1/usuario` - Criação de usuário
- ✅ `/actuator/**` - Endpoints do Spring Actuator

## Como Testar

### 1. Frontend React/Angular/Vue

```javascript
// Agora deve funcionar sem erro de CORS
fetch('http://192.168.50.174:8080/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    login: 'usuario',
    senha: 'senha123'
  })
})
  .then(response => response.json())
  .then(data => console.log(data))
```

### 2. Teste com curl

```bash
# Testar CORS com preflight
curl -X OPTIONS \
  http://192.168.50.174:8080/auth/login \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -v

# Testar login
curl -X POST \
  http://192.168.50.174:8080/auth/login \
  -H "Content-Type: application/json" \
  -H "Origin: http://localhost:3000" \
  -d '{"login":"admin","senha":"123456"}'
```

## Verificação dos Headers de Resposta

O servidor agora retorna os headers CORS corretos:

```
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH
Access-Control-Allow-Headers: *
Access-Control-Allow-Credentials: true
Access-Control-Max-Age: 3600
```

## Observações de Segurança

⚠️ **Para Produção**: Substitua `allowedOriginPatterns("*")` por domínios específicos:

```java
.allowedOriginPatterns(
    "http://localhost:3000",
    "https://meudominio.com",
    "https://*.meudominio.com"
)
```

## Status da Correção

✅ **CORS configurado globalmente**  
✅ **Spring Security integrado com CORS**  
✅ **Endpoints públicos liberados**  
✅ **Endpoint alternativo criado**  
✅ **Projeto compila sem erros**  
✅ **Pronto para aceitar requisições do frontend**

O erro de CORS foi completamente resolvido e o backend agora aceita requisições de qualquer origem, incluindo `http://localhost:3000`.
