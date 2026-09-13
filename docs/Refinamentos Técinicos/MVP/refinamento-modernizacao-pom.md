# Refinamento técnico — Modernização do `pom.xml`

Status: estratégia técnica consolidada; execução pendente.

## Objetivo

Definir a modernização controlada do build do `srv-team` antes da implementação do novo domínio do MVP.

Baseline alvo:

```text
Java: 21 LTS
Spring Boot: 4.1.1
Maven produtivo: 3.9.16 via Maven Wrapper
Maven 4: compatibilidade preparada e validação não bloqueante enquanto permanecer RC
Banco: MySQL
Schema migration: Flyway
JPA schema management: validate
```

## Estado atual

O `pom.xml` atual utiliza:

- Spring Boot `3.2.2`;
- Java `17`;
- Spring Web;
- Spring Data JPA;
- Spring Validation;
- Spring Security;
- JJWT `0.12.3` declarado explicitamente em três artefatos;
- H2 runtime;
- MySQL Connector/J runtime;
- DevTools;
- Spring Boot Test;
- Spring Security Test;
- `slf4j-api 2.0.12` declarado explicitamente;
- somente `spring-boot-maven-plugin` em build.

## Princípios da modernização

1. Preferir dependency management do Spring Boot para bibliotecas que já pertencem ao BOM da plataforma.
2. Fixar versão explicitamente apenas quando houver motivo técnico/documentado.
3. Não usar Maven 4 RC como requisito para build/release de produção.
4. Tornar o build reproduzível com Maven Wrapper.
5. Separar modernização da plataforma das migrations funcionais do MVP.
6. Fazer o projeto compilar/testar em cada marco antes do próximo salto.
7. Não aproveitar a modernização para refatorar regras de negócio sem necessidade.

## Parent e Java

Alvo:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.1.1</version>
    <relativePath/>
</parent>

<properties>
    <java.version>21</java.version>
</properties>
```

O projeto deve compilar e executar efetivamente com JDK 21. Não basta alterar apenas `java.version` no POM.

## Dependências Spring

Manter conceitualmente:

```text
spring-boot-starter-web
spring-boot-starter-data-jpa
spring-boot-starter-validation
spring-boot-starter-security
spring-boot-starter-test
spring-security-test
```

Durante a migração para Spring Boot 4, revisar imports/APIs removidas ou alteradas pelo Spring Framework 7, Spring Security e Hibernate utilizados pela nova plataforma.

Não adicionar dependências de compatibilidade sem identificar primeiro a incompatibilidade concreta.

## Logging

Remover a declaração explícita:

```text
org.slf4j:slf4j-api:2.0.12
```

O logging deve ser controlado pelo dependency management do Spring Boot e pelos starters utilizados. Fixar SLF4J isoladamente pode quebrar o alinhamento de versões da plataforma.

Somente reintroduzir override explícito mediante necessidade comprovada e registrada.

## JWT / JJWT

O projeto atualmente fixa `jjwt-api`, `jjwt-impl` e `jjwt-jackson` em `0.12.3`.

Na modernização:

1. verificar a versão estável compatível mais recente de JJWT no momento da execução;
2. manter os três artefatos apenas se a implementação atual continuar usando a arquitetura modular da biblioteca;
3. centralizar a versão em propriedade única caso não seja gerenciada pelo BOM do Spring Boot;
4. revisar APIs de parsing/signing depreciadas ou removidas;
5. preservar compatibilidade dos tokens somente quando isso fizer parte do requisito de rollout;
6. não manter segredo JWT no repositório.

Exemplo conceitual quando versão explícita continuar necessária:

```xml
<properties>
    <java.version>21</java.version>
    <jjwt.version>...</jjwt.version>
</properties>
```

Não congelar neste documento uma versão futura da JJWT sem validação no momento da implementação.

## MySQL

Manter:

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

Preferir a versão gerenciada pelo Spring Boot.

Validar explicitamente:

- compatibilidade da versão do servidor MySQL atual;
- timezone;
- charset/collation;
- comportamento de índices/constraints necessários ao MVP;
- migrations Flyway sobre uma cópia representativa da base existente.

## H2

H2 pode continuar como ferramenta auxiliar local, mas **não deve ser a referência para testes de persistência das invariantes do MVP**.

O domínio depende de comportamentos específicos de MySQL, especialmente unicidades condicionais, locking e concorrência.

Recomendação:

- testes unitários: sem banco;
- testes rápidos simples: H2 somente quando a semântica SQL não for relevante;
- testes de integração de persistência/concorrência/migration: MySQL real via Testcontainers.

## Flyway

Adicionar Flyway antes das tabelas do novo domínio.

Dependências devem seguir a integração recomendada para a versão de Spring Boot/Flyway adotada. Para MySQL, incluir o módulo de suporte ao banco quando requerido pela versão do Flyway gerenciada pela plataforma.

Objetivo:

```text
Flyway = fonte autoritativa de evolução do schema
Hibernate = validação do mapeamento
```

Configuração alvo:

```properties
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
```

Não criar novas estruturas do MVP por `ddl-auto=update`.

### Baseline da base existente

Como a aplicação já possui schema em produção antes da adoção do Flyway, a implantação precisa de estratégia explícita de baseline.

Não assumir que a primeira migration pode recriar as tabelas legadas.

A execução deve:

1. inventariar schema atual;
2. definir baseline controlado da base existente;
3. garantir que ambientes novos possam ser reconstruídos de forma determinística;
4. criar migrations aditivas do MVP após o baseline;
5. testar tanto base nova quanto upgrade de uma cópia da base existente.

## Testcontainers

Adicionar Testcontainers para integração com MySQL.

Objetivos principais:

- validar migrations Flyway;
- validar constraints;
- testar concorrência de `Resultado`/colocação;
- testar equivalência concorrente de `Campeonato`;
- validar queries de ranking;
- evitar falso positivo causado por diferenças H2/MySQL.

As versões devem preferencialmente ser controladas por BOM/dependency management adequado, evitando versões divergentes entre módulos Testcontainers.

## Maven plugins

### Spring Boot Maven Plugin

Manter:

```text
org.springframework.boot:spring-boot-maven-plugin
```

Sua versão deve acompanhar o parent do Spring Boot.

### Maven Compiler Plugin

Não fixar versão desnecessariamente quando o parent do Spring Boot já fornece configuração compatível.

`java.version=21` deve ser a fonte principal da versão Java da aplicação, salvo necessidade concreta de configuração adicional.

### Maven Enforcer Plugin

Recomendado para impedir builds com runtime incompatível.

Política alvo:

```text
Java >= 21
Maven >= 3.9.16
```

Enquanto Maven 4 não for GA, não definir limite superior que o bloqueie desnecessariamente; o objetivo é permitir validação de compatibilidade sem torná-lo baseline produtivo.

### Surefire/Failsafe

Usar convenção clara:

```text
unit tests -> Surefire
integration tests -> Failsafe
```

Não é necessário configurar versões manualmente se o parent já fornecer versões adequadas, salvo requisito específico.

## Maven Wrapper

Versionar Maven Wrapper no repositório.

Baseline produtivo inicial:

```text
Maven 3.9.16
```

Build oficial deve usar:

```text
./mvnw ...
```

ou `mvnw.cmd` no Windows.

Isso elimina dependência da versão global instalada na máquina/CI.

## Maven 4

O projeto deve ser escrito de forma compatível com Maven 4 sempre que isso não comprometer a baseline estável.

Enquanto Maven 4 permanecer release candidate:

- não usar Maven 4 como requisito de produção;
- não usar recursos exclusivos do Maven 4 no POM oficial;
- executar validação periódica com Maven 4 em ambiente separado/CI não bloqueante;
- registrar incompatibilidades encontradas;
- migrar o Wrapper para Maven 4 somente após GA e validação completa do projeto/plugins.

Após Maven 4 GA:

1. atualizar Wrapper;
2. executar build completo;
3. executar testes unitários e integração;
4. validar plugins;
5. validar CI/CD;
6. somente então tornar Maven 4 baseline obrigatória.

## Segredos e configuração

A modernização do build deve ocorrer junto da externalização dos segredos atualmente versionados.

O `application.properties` não deve conter credencial real de banco nem segredo JWT.

Alvo conceitual:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
```

Valores locais devem vir de ambiente/configuração não versionada. Produção deve usar mecanismo de secrets apropriado ao ambiente de hospedagem.

Credenciais já expostas no histórico do Git devem ser consideradas comprometidas e rotacionadas; apenas removê-las do arquivo atual não é suficiente.

## Ordem de alteração do POM

### Marco A — build reproduzível

- adicionar Maven Wrapper 3.9.16;
- garantir build/testes atuais pelo Wrapper;
- registrar baseline antes da modernização.

### Marco B — Java 21

- alterar JDK de execução/CI;
- alterar `java.version` para 21;
- compilar/testar;
- corrigir somente incompatibilidades necessárias.

### Marco C — Spring Boot 4.1.1

- atualizar parent;
- remover `slf4j-api` explícito;
- revisar JJWT;
- revisar APIs Spring/Security/JPA/Hibernate;
- executar testes e smoke test dos endpoints existentes.

### Marco D — persistência controlada

- adicionar Flyway;
- definir baseline do schema existente;
- adicionar Testcontainers/MySQL;
- substituir `ddl-auto=update` por `validate` após schema estar sob controle do Flyway.

### Marco E — segurança/configuração

- externalizar configurações sensíveis;
- rotacionar credenciais e JWT secret;
- separar profiles/configuração por ambiente;
- revisar logging de produção.

### Marco F — Maven 4 readiness

- validar build com Maven 4 RC/GA em job separado;
- corrigir warnings/incompatibilidades que também sejam válidos na baseline;
- não bloquear entrega pelo RC;
- promover Maven 4 somente após GA.

## Critério de pronto da modernização

A plataforma estará pronta para receber as novas entidades do MVP quando:

- aplicação compilar/executar em Java 21;
- Spring Boot 4.1.1 estiver operacional;
- build oficial funcionar pelo Maven Wrapper 3.9.16;
- testes existentes estiverem verdes ou divergências conhecidas documentadas;
- Flyway controlar evolução de schema;
- Hibernate não criar/alterar schema automaticamente;
- Testcontainers/MySQL validar integração crítica;
- segredos não estiverem versionados e valores expostos tiverem sido rotacionados;
- endpoints legados necessários continuarem funcionais;
- Maven 4 puder ser testado separadamente sem contaminar o build produtivo.

## Fora do escopo desta etapa

A modernização não deve, por si só:

- implementar `Temporada`, `Inscricao`, `Resultado` ou ranking;
- renomear fisicamente `tbCompeticao`;
- remover tabelas legadas;
- alterar contratos REST sem necessidade de compatibilidade;
- converter toda a arquitetura antiga para hexagonal;
- adotar recursos exclusivos do Maven 4 antes de GA.
