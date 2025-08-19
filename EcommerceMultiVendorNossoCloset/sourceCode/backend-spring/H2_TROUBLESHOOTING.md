# 🔧 H2 Database Troubleshooting Guide

## Problemas Identificados e Soluções

### 1. **Erro: Palavras Reservadas SQL**

**Sintoma:** `Syntax error in SQL statement "... create table [*]user ..."; expected "identifier";`
**Causa:** H2 Database não permite usar palavras reservadas como nomes de tabela
**Solução:** Renomear tabelas usando `@Table(name = "nome_alternativo")`

#### Entidades Afetadas:

- `User.java` → `@Table(name = "app_users")`
- `Transaction.java` → `@Table(name = "app_transactions")`
- `Review.java` → `@Table(name = "app_reviews")`
- `Address.java` → `@Table(name = "app_addresses")`
- `Cart.java` → `@Table(name = "app_carts")`

### 2. **Erro: Tipo de Dados TINYINT não Suportado**

**Sintoma:** `Unknown data type: "TINYINT";`
**Causa:** H2 em modo PostgreSQL não suporta o tipo `TINYINT` que Hibernate mapeia para enums por padrão
**Solução:** Mapear explicitamente campos enum para `SMALLINT` usando `@Column(columnDefinition = "SMALLINT")`

#### Entidades Afetadas:

- `Order.java`: `PaymentStatus` → `@Column(columnDefinition = "SMALLINT")`
- `User.java`: `USER_ROLE` → `@Column(columnDefinition = "SMALLINT")`
- `Seller.java`: `USER_ROLE` e `AccountStatus` → `@Column(columnDefinition = "SMALLINT")`
- `PaymentOrder.java`: `PaymentOrderStatus` e `PaymentMethod` → `@Column(columnDefinition = "SMALLINT")`

### 3. **Erro: Tabelas não Encontradas (Cascata)**

**Sintoma:** Múltiplos erros `Table "table_name" not found;`
**Causa:** Erro em cascata devido ao problema TINYINT impedir criação completa do schema
**Solução:** Resolver primeiro o problema TINYINT, depois as tabelas serão criadas automaticamente

## Configurações Aplicadas

### application.properties

```properties
# H2 Database com modo PostgreSQL para melhor compatibilidade
spring.datasource.url=jdbc:h2:mem:ecommerce_multi_vendor;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA Configuration
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
spring.jpa.properties.hibernate.format_sql=true

# Configurações específicas do H2 para palavras reservadas
spring.jpa.properties.hibernate.globally_quoted_identifiers=true
spring.jpa.properties.hibernate.globally_quoted_identifiers_skip_column_definitions=true
```

## Como Testar as Correções

### 1. **Limpe o projeto**

```bash
mvn clean
```

### 2. **Execute a aplicação**

```bash
mvn spring-boot:run
```

### 3. **Verifique o console H2**

- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:ecommerce_multi_vendor`
- Username: `sa`
- Password: (deixe em branco)

### 4. **Verifique as tabelas criadas**

```sql
SHOW TABLES;
```

## Verificações Pós-Correção

### ✅ Tabelas que devem ser criadas:

- `app_users`
- `app_transactions`
- `app_reviews`
- `app_addresses`
- `app_carts`
- `orders`
- `categories`
- `products`
- `sellers`
- `payment_orders`
- `payouts`
- `home_categories`

### ✅ Campos enum mapeados corretamente:

- Todos os campos enum devem usar `SMALLINT` em vez de `TINYINT`

## Se Ainda Houver Problemas

### 1. **Verifique outras entidades**

```bash
# Procure por entidades sem @Table
grep -r "@Entity" src/main/java/com/zosh/model/ | grep -v "@Table"
```

### 2. **Use PostgreSQL localmente**

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce_multi_vendor
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### 3. **Use Docker**

```bash
docker-compose up -d postgres
```

## Recursos Úteis

- [H2 Database Documentation](http://www.h2database.com/html/main.html)
- [Hibernate Reserved Words](https://hibernate.org/orm/documentation/6.0/reference/en-US/html_single/#schema-generation)
- [Spring Boot Database Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-sql)
