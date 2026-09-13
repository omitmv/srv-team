# Refinamento técnico — Modernização da plataforma

Status: estratégia técnica consolidada para modernização do `srv-team` antes da implementação do novo domínio do MVP.

## Objetivo

Modernizar a base tecnológica do `srv-team` antes da expansão do domínio de pontuação, reduzindo risco de implementar o novo MVP sobre uma stack desatualizada e evitando uma segunda modernização estrutural logo após a entrega.

A modernização deve ocorrer de forma controlada e separável das mudanças funcionais do MVP.

## Estado atual

No branch `release`, o projeto utiliza atualmente:

- Java 17;
- Spring Boot 3.2.2;
- Maven/POM tradicional;
- Spring Web;
- Spring Data JPA;
- Spring Validation;
- Spring Security;
- JJWT;
- MySQL;
- H2 para desenvolvimento;
- Hibernate com `spring.jpa.hibernate.ddl-auto=update`.

## Stack alvo

### Java

Adotar **Java 21 LTS** como versão mínima de compilação e execução da aplicação.

Diretriz:

```text
source = 21
target = 21
runtime = Java 21+
```

Motivos:

- versão LTS;
- base adequada para a modernização do Spring;
- melhor longevidade do projeto;
- permite utilização de recursos modernos da linguagem quando agregarem clareza, sem exigir reescrita do código existente.

A modernização não deve transformar automaticamente todo código legado para records, sealed classes ou outras features. Esses recursos devem ser usados apenas quando fizerem sentido no novo código.

### Spring Boot

Adotar **Spring Boot 4.1.1** como baseline alvo.

Spring Boot 4.1.1 é versão estável e suporta Java 21.

A migração deve considerar explicitamente os impactos de:

- Spring Framework 7;
- Jakarta EE / Servlet atuais;
- Hibernate/JPA gerenciados pelo BOM do Spring Boot;
- Spring Security;
- Jackson;
- validação Jakarta;
- mudanças de propriedades e auto-configurações entre Spring Boot 3.2 e 4.1.

Não fixar manualmente versões de bibliotecas já gerenciadas pelo BOM do Spring Boot sem necessidade comprovada.

## Maven

### Decisão para produção

**Não adotar Maven 4 como requisito produtivo enquanto a versão disponível permanecer Release Candidate.**

Na data deste refinamento, Maven 4 ainda está em `4.0.0-rc-6` e não é GA.

Para build produtivo, usar **Maven 3.9.16** como baseline estável.

### Preparação para Maven 4

O projeto deve, porém, ser modernizado para ficar compatível com Maven 4 desde já.

Isso significa:

- eliminar plugins obsoletos;
- utilizar versões modernas dos plugins essenciais quando não forem gerenciadas;
- evitar comportamentos implícitos/depreciados do Maven;
- manter POM válido e simples;
- configurar Maven Wrapper;
- testar periodicamente o build com Maven 4 RC em pipeline não bloqueante;
- migrar o baseline oficial para Maven 4 somente após release GA e validação dos plugins utilizados.

Baseline recomendado:

```text
Produção/CI obrigatório: Maven 3.9.16
Validação antecipada: Maven 4.0.0-rc-6
Futuro: Maven 4.x GA após homologação
```

## Maven Wrapper

Adicionar Maven Wrapper ao repositório.

O build oficial deve ser executado preferencialmente por:

```bash
./mvnw clean verify
```

ou, no Windows:

```bat
mvnw.cmd clean verify
```

Isso reduz dependência da versão instalada localmente e melhora reprodutibilidade.

Enquanto Maven 4 não for GA, o wrapper oficial do branch produtivo deve apontar para Maven 3.9.16.

## Estratégia de upgrade

A modernização deve ser feita em etapas, evitando misturar centenas de alterações funcionais com quebra de framework.

### Etapa 1 — baseline de build

- configurar Java 21;
- atualizar Maven Wrapper para Maven 3.9.16;
- garantir `mvnw clean verify`;
- remover versões redundantes de dependências gerenciadas pelo Spring Boot;
- adicionar configuração explícita do compiler apenas se necessária;
- validar encoding e configuração de testes.

### Etapa 2 — Spring Boot

Migrar:

```text
Spring Boot 3.2.2
        -> 4.1.1
```

Corrigir incompatibilidades de compilação e comportamento antes de iniciar implementação do novo domínio.

### Etapa 3 — dependências

Revisar especialmente:

- JJWT;
- MySQL Connector/J;
- H2;
- plugins Maven;
- Spring Security;
- bibliotecas que utilizem APIs internas do Hibernate/Spring.

Preferir versões gerenciadas pelo Spring Boot quando disponíveis.

### Etapa 4 — testes de regressão

Antes de implementar o novo MVP:

- aplicação deve iniciar com Java 21;
- autenticação/JWT deve continuar funcional;
- endpoints legados essenciais devem continuar respondendo;
- persistência JPA deve continuar funcional;
- build deve passar integralmente;
- diferenças de serialização JSON devem ser verificadas;
- segurança deve ser validada por testes mínimos automatizados.

## Banco e migrations

A modernização deve eliminar a dependência de:

```properties
spring.jpa.hibernate.ddl-auto=update
```

para evolução produtiva de schema.

Adotar Flyway antes das novas entidades do MVP.

Alvo após estabilização:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

O Hibernate valida o modelo; Flyway evolui o banco.

## Segurança de configuração

Antes do primeiro deploy modernizado:

- remover usuário/senha de banco versionados;
- remover segredo JWT versionado;
- rotacionar os segredos já expostos;
- utilizar variáveis de ambiente/secret manager;
- separar configuração por ambiente;
- reduzir logs sensíveis em produção.

A troca de Java/Spring não deve ser considerada concluída enquanto segredos permanecerem versionados.

## Compatibilidade da API

A modernização de plataforma não deve, por si só, quebrar contratos REST legados.

Mudanças de endpoint pertencem ao refinamento funcional/API e devem ser versionadas ou tratadas por camada de compatibilidade quando necessário.

A modernização técnica deve buscar inicialmente:

```text
mesmo contrato externo
+ runtime moderno
+ build moderno
+ segurança/configuração corrigidas
```

## Campeonato / Competição

Decisão técnica confirmada para a modernização do domínio:

```text
Java/domínio novo: Campeonato
API nova: /campeonatos
Banco inicialmente: tbCompeticao / cdCompeticao
```

Não manter duas entidades JPA autoritativas (`Competicao` e `Campeonato`) simultaneamente sobre `tbCompeticao`.

A migração deve substituir a entidade Java de forma controlada e manter endpoints legados temporariamente por adapter/facade de compatibilidade quando o `fed-team` ainda depender deles.

## Política de dependências

- usar BOM do Spring Boot como fonte principal de versões;
- evitar versões explícitas desnecessárias;
- dependência nova deve justificar finalidade;
- não adicionar biblioteca para problema resolvido pela JDK/Spring;
- revisar dependências transitivas vulneráveis no build;
- manter `mvn dependency:tree` e análise de CVEs no pipeline.

## Critérios de aceite da modernização

A modernização é considerada concluída quando:

1. aplicação compila e executa com Java 21;
2. Spring Boot 4.1.1 está ativo;
3. `./mvnw clean verify` passa com Maven 3.9.16;
4. build experimental com Maven 4 RC não apresenta incompatibilidade crítica conhecida, embora não seja requisito produtivo;
5. autenticação e autorização existentes continuam funcionais;
6. endpoints legados essenciais possuem regressão validada;
7. Flyway passa a controlar novas migrations;
8. `ddl-auto=update` deixa de ser mecanismo produtivo de schema;
9. credenciais e segredo JWT deixam de estar versionados e são rotacionados;
10. o projeto está pronto para receber os novos módulos do MVP.

## Ordem recomendada em relação ao MVP

```text
1. Modernizar Java/Maven Wrapper
2. Migrar Spring Boot
3. Corrigir dependências/configuração
4. Introduzir Flyway e segurança de segredos
5. Executar regressão do legado
6. Somente então iniciar implementação do novo domínio MVP
```

Evitar implementar entidades e workflows novos simultaneamente à migração principal de Spring Boot. Isso reduz significativamente a superfície de diagnóstico quando houver falha.

## Decisão consolidada

A plataforma alvo do MVP será:

```text
Java 21 LTS
Spring Boot 4.1.1
Maven 3.9.16 (baseline produtivo)
Maven 4 RC apenas para compatibilidade antecipada
MySQL
Flyway
Spring Data JPA
Spring Security
```

Quando Maven 4 atingir GA, a migração do baseline deverá ser tratada como atualização técnica pequena e isolada, desde que os testes antecipados de compatibilidade tenham sido mantidos.
