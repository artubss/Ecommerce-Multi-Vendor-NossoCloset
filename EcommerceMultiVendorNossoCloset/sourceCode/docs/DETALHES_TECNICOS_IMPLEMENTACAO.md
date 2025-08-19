# 🔧 **DETALHES TÉCNICOS DE IMPLEMENTAÇÃO - NOSSO CLOSET**

## 🏗️ **ARQUITETURA DE DADOS REFATORADA**

### **1. NOVAS ENTIDADES PRINCIPAIS**

#### **Supplier (Fornecedor)**

```java
@Entity
@Table(name = "suppliers")
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String contactName;

    @Column(nullable = false)
    private String whatsapp;

    private String website;
    private String email;

    @Column(nullable = false)
    private BigDecimal minimumOrderValue;

    @Column(nullable = false)
    private Integer deliveryTimeDays;

    @Column(nullable = false)
    private BigDecimal adminFeePercentage;

    @ElementCollection
    private List<String> categories = new ArrayList<>();

    @Column(nullable = false)
    private Integer performanceRating; // 1-5

    @Enumerated(EnumType.STRING)
    private SupplierStatus status = SupplierStatus.ACTIVE;

    private String notes;

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL)
    private List<Catalog> catalogs = new ArrayList<>();

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL)
    private List<CollectiveOrder> collectiveOrders = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### **CustomOrder (Pedido Personalizado)**

```java
@Entity
@Table(name = "custom_orders")
public class CustomOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier; // Pode ser null inicialmente

    @Column(nullable = false)
    private String productImageUrl; // URL da imagem no S3

    private String referenceCode; // Código de referência se disponível

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private String preferredColor;

    @ElementCollection
    private List<String> alternativeColors = new ArrayList<>();

    @Column(nullable = false)
    private String size;

    @Column(nullable = false)
    private String category;

    private String observations;

    private BigDecimal estimatedPrice;
    private BigDecimal finalPrice;

    @Enumerated(EnumType.STRING)
    private CustomOrderStatus status = CustomOrderStatus.PENDING_ANALYSIS;

    @Enumerated(EnumType.STRING)
    private UrgencyLevel urgency = UrgencyLevel.NORMAL;

    @Column(nullable = false)
    private Integer quantity = 1;

    private LocalDateTime createdAt;
    private LocalDateTime analyzedAt;
    private LocalDateTime confirmedAt;
}
```

#### **CollectiveOrder (Pedido Coletivo)**

```java
@Entity
@Table(name = "collective_orders")
public class CollectiveOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Enumerated(EnumType.STRING)
    private CollectiveOrderStatus status = CollectiveOrderStatus.OPEN;

    @Column(nullable = false)
    private BigDecimal minimumValue;

    @Column(nullable = false)
    private BigDecimal currentValue = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDateTime paymentDeadline;

    private LocalDateTime supplierPaymentDate;

    @Column(nullable = false)
    private BigDecimal anticipatedAmount = BigDecimal.ZERO; // Valor que a empresa pagou

    @Column(nullable = false)
    private BigDecimal totalReceived = BigDecimal.ZERO; // Valor recebido dos clientes

    @Column(nullable = false)
    private BigDecimal profitMargin = BigDecimal.ZERO;

    @OneToMany(mappedBy = "collectiveOrder", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "collectiveOrder", cascade = CascadeType.ALL)
    private List<CustomOrder> customOrders = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
}
```

#### **Catalog (Catálogo do Fornecedor)**

```java
@Entity
@Table(name = "catalogs")
public class Catalog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String fileUrl; // URL do PDF no S3

    @ElementCollection
    private List<String> imageUrls = new ArrayList<>(); // Imagens do catálogo

    @Enumerated(EnumType.STRING)
    private CatalogType type = CatalogType.PDF; // PDF, IMAGE_COLLECTION

    @Enumerated(EnumType.STRING)
    private CatalogStatus status = CatalogStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDateTime validFrom;

    private LocalDateTime validUntil;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
```

#### **CreditTransaction (Movimentação de Créditos)**

```java
@Entity
@Table(name = "credit_transactions")
public class CreditTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reference_order_id")
    private CollectiveOrder referenceOrder;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    private CreditStatus status = CreditStatus.ACTIVE;

    @Column(nullable = false)
    private BigDecimal balanceAfterTransaction;
}
```

### **2. ENUMERAÇÕES NECESSÁRIAS**

#### **SupplierStatus**

```java
public enum SupplierStatus {
    ACTIVE, INACTIVE, SUSPENDED, PENDING_VERIFICATION
}
```

#### **CustomOrderStatus**

```java
public enum CustomOrderStatus {
    PENDING_ANALYSIS,    // Aguardando análise administrativa
    PRICED,              // Preço definido, aguardando confirmação do cliente
    CONFIRMED,           // Cliente confirmou interesse
    IN_COLLECTIVE,       // Adicionado a um pedido coletivo
    SUPPLIER_PAID,       // Fornecedor foi pago
    IN_TRANSIT,          // Produtos em trânsito
    RECEIVED,            // Produtos recebidos
    DELIVERED,           // Entregue ao cliente
    CANCELLED,           // Cancelado
    REFUNDED             // Reembolsado
}
```

#### **CollectiveOrderStatus**

```java
public enum CollectiveOrderStatus {
    OPEN,               // Aberto para novos pedidos
    MINIMUM_REACHED,    // Mínimo atingido, fechando para novos pedidos
    PAYMENT_WINDOW,     // Janela de pagamento aberta
    SUPPLIER_PAID,      // Fornecedor foi pago
    IN_TRANSIT,         // Produtos em trânsito
    RECEIVED,           // Produtos recebidos
    DELIVERED,          // Todos os produtos entregues
    CLOSED,             // Pedido coletivo fechado
    CANCELLED           // Cancelado
}
```

#### **UrgencyLevel**

```java
public enum UrgencyLevel {
    LOW, NORMAL, HIGH, URGENT
}
```

#### **TransactionType**

```java
public enum TransactionType {
    REFUND,             // Reembolso
    CREDIT_BONUS,       // Bônus por aceitar crédito
    REFERRAL_BONUS,     // Bônus por indicação
    LOYALTY_BONUS,      // Bônus de fidelidade
    PROMOTIONAL_CREDIT, // Crédito promocional
    TRANSFER            // Transferência entre clientes
}
```

### **3. REFATORAÇÕES NAS ENTIDADES EXISTENTES**

#### **User (Refatorado)**

```java
@Entity
@Table(name = "app_users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String mobile;

    @Column(nullable = false)
    private String whatsapp;

    @Enumerated(EnumType.STRING)
    private USER_ROLE role = USER_ROLE.CLIENT;

    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(nullable = false)
    private BigDecimal creditBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDateTime registrationDate = LocalDateTime.now();

    private LocalDateTime lastAccess;

    @Enumerated(EnumType.STRING)
    private DeliveryPreference deliveryPreference = DeliveryPreference.NATAL_PICKUP;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private List<CustomOrder> customOrders = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private List<CreditTransaction> creditTransactions = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private List<Address> addresses = new ArrayList<>();

    // Campos para auditoria
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
```

#### **Product (Refatorado para Catálogo)**

```java
@Entity
@Table(name = "catalog_products")
public class CatalogProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_id", nullable = false)
    private Catalog catalog;

    @Column(nullable = false)
    private String referenceCode; // Código de referência do fornecedor

    @ElementCollection
    private List<String> images = new ArrayList<>();

    @ElementCollection
    private List<String> availableColors = new ArrayList<>();

    @ElementCollection
    private List<String> availableSizes = new ArrayList<>();

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String subcategory;

    private String material;
    private String brand;

    @Column(nullable = false)
    private BigDecimal supplierPrice; // Preço do fornecedor

    @Column(nullable = false)
    private BigDecimal sellingPrice; // Preço final para o cliente

    @Enumerated(EnumType.STRING)
    private ProductStatus status = ProductStatus.AVAILABLE;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
```

---

## 🚀 **APIs E ENDPOINTS NECESSÁRIOS**

### **1. SUPPLIER MANAGEMENT**

#### **GET /api/suppliers**

- Lista todos os fornecedores ativos
- Filtros: status, categoria, rating
- Paginação e ordenação

#### **GET /api/suppliers/{id}**

- Detalhes completos do fornecedor
- Inclui catálogos e histórico de pedidos

#### **POST /api/suppliers**

- Cria novo fornecedor
- Validação de dados obrigatórios

#### **PUT /api/suppliers/{id}**

- Atualiza dados do fornecedor
- Auditoria de mudanças

#### **DELETE /api/suppliers/{id}**

- Desativa fornecedor (soft delete)
- Verifica dependências antes de remover

### **2. CUSTOM ORDERS**

#### **GET /api/custom-orders**

- Lista pedidos personalizados
- Filtros: status, cliente, fornecedor, data
- Paginação para admins

#### **POST /api/custom-orders**

- Cria novo pedido personalizado
- Upload de imagem para S3
- Validação de dados obrigatórios

#### **PUT /api/custom-orders/{id}/analyze**

- Admin analisa e precifica pedido
- Define fornecedor e preço final

#### **PUT /api/custom-orders/{id}/confirm**

- Cliente confirma interesse no preço
- Adiciona ao pedido coletivo

#### **GET /api/custom-orders/my-orders**

- Cliente vê seus próprios pedidos
- Filtros por status

### **3. COLLECTIVE ORDERS**

#### **GET /api/collective-orders**

- Lista pedidos coletivos
- Filtros: status, fornecedor, data
- Dashboard administrativo

#### **POST /api/collective-orders**

- Cria novo pedido coletivo
- Agrupa pedidos personalizados por fornecedor

#### **PUT /api/collective-orders/{id}/close**

- Fecha pedido coletivo quando atinge mínimo
- Inicia janela de pagamento

#### **PUT /api/collective-orders/{id}/pay-supplier**

- Marca fornecedor como pago
- Registra valor antecipado

#### **GET /api/collective-orders/{id}/status**

- Cliente acompanha status do pedido coletivo
- Inclui lista de participantes

### **4. CATALOG MANAGEMENT**

#### **GET /api/catalogs**

- Lista catálogos públicos
- Filtros: fornecedor, categoria, data

#### **POST /api/catalogs**

- Admin faz upload de novo catálogo
- Upload de PDF/imagens para S3

#### **GET /api/catalogs/{id}/products**

- Lista produtos de um catálogo específico
- Filtros avançados por características

#### **GET /api/catalogs/search**

- Busca em catálogos
- Filtros por texto, categoria, cor, tamanho

### **5. CREDIT SYSTEM**

#### **GET /api/credits/balance**

- Cliente vê saldo atual de créditos

#### **GET /api/credits/transactions**

- Histórico de movimentações de crédito

#### **POST /api/credits/transfer**

- Transferência de créditos entre clientes

#### **POST /api/credits/refund-request**

- Solicita reembolso ou crédito

---

## 🔐 **CONFIGURAÇÕES DE SEGURANÇA**

### **1. SPRING SECURITY CONFIGURATION**

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/suppliers/**").permitAll()
                .requestMatchers("/api/catalogs/**").permitAll()
                .requestMatchers("/api/custom-orders").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/client/**").hasRole("CLIENT")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

### **2. RATE LIMITING**

```java
@Component
public class RateLimitFilter implements Filter {

    private final RateLimiter rateLimiter;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String clientId = getClientId(httpRequest);

        if (!rateLimiter.tryAcquire(clientId)) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            return;
        }

        chain.doFilter(request, response);
    }
}
```

### **3. AUDIT SYSTEM**

```java
@Aspect
@Component
public class AuditAspect {

    @Autowired
    private AuditService auditService;

    @Around("@annotation(Audited)")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();

        AuditLog auditLog = new AuditLog();
        auditLog.setAction(methodName);
        auditLog.setEntityType(className);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setAdminId(getCurrentAdminId());
        auditLog.setIpAddress(getCurrentIpAddress());

        try {
            Object result = joinPoint.proceed();
            auditLog.setSuccess(true);
            return result;
        } catch (Exception e) {
            auditLog.setSuccess(false);
            auditLog.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            auditService.saveAuditLog(auditLog);
        }
    }
}
```

---

## 📱 **CONFIGURAÇÕES DO FRONTEND**

### **1. AWS S3 CONFIGURATION**

```typescript
// src/config/aws.ts
import AWS from "aws-sdk";

AWS.config.update({
  region: process.env.REACT_APP_AWS_REGION,
  accessKeyId: process.env.REACT_APP_AWS_ACCESS_KEY_ID,
  secretAccessKey: process.env.REACT_APP_AWS_SECRET_ACCESS_KEY,
});

export const s3 = new AWS.S3();
export const BUCKET_NAME = process.env.REACT_APP_S3_BUCKET_NAME;

export const uploadToS3 = async (file: File, key: string): Promise<string> => {
  const params = {
    Bucket: BUCKET_NAME,
    Key: key,
    Body: file,
    ContentType: file.type,
    ACL: "public-read",
  };

  try {
    const result = await s3.upload(params).promise();
    return result.Location;
  } catch (error) {
    console.error("Error uploading to S3:", error);
    throw error;
  }
};
```

### **2. REACT QUERY CONFIGURATION**

```typescript
// src/config/queryClient.ts
import { QueryClient } from "@tanstack/react-query";

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5 minutes
      cacheTime: 10 * 60 * 1000, // 10 minutes
      retry: 3,
      refetchOnWindowFocus: false,
    },
  },
});

// src/hooks/useCustomOrders.ts
export const useCustomOrders = (filters?: CustomOrderFilters) => {
  return useQuery({
    queryKey: ["customOrders", filters],
    queryFn: () => customOrderService.getCustomOrders(filters),
    enabled: !!filters,
  });
};

export const useCreateCustomOrder = () => {
  return useMutation({
    mutationFn: customOrderService.createCustomOrder,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["customOrders"] });
      toast.success("Pedido personalizado criado com sucesso!");
    },
    onError: (error) => {
      toast.error("Erro ao criar pedido personalizado");
      console.error(error);
    },
  });
};
```

### **3. FORM VALIDATION SCHEMA**

```typescript
// src/schemas/customOrderSchema.ts
import * as yup from "yup";

export const customOrderSchema = yup.object({
  productImage: yup
    .mixed()
    .required("Imagem do produto é obrigatória")
    .test("fileSize", "Arquivo muito grande", (value) => {
      if (!value) return false;
      return value.size <= 5 * 1024 * 1024; // 5MB
    })
    .test("fileType", "Formato não suportado", (value) => {
      if (!value) return false;
      return ["image/jpeg", "image/png", "image/webp"].includes(value.type);
    }),

  description: yup
    .string()
    .required("Descrição é obrigatória")
    .min(10, "Descrição deve ter pelo menos 10 caracteres")
    .max(1000, "Descrição deve ter no máximo 1000 caracteres"),

  preferredColor: yup.string().required("Cor preferencial é obrigatória"),

  size: yup.string().required("Tamanho é obrigatório"),

  category: yup.string().required("Categoria é obrigatória"),

  quantity: yup
    .number()
    .required("Quantidade é obrigatória")
    .min(1, "Quantidade deve ser pelo menos 1")
    .max(10, "Quantidade máxima é 10"),

  urgency: yup
    .string()
    .oneOf(["LOW", "NORMAL", "HIGH", "URGENT"], "Nível de urgência inválido"),

  observations: yup
    .string()
    .max(500, "Observações devem ter no máximo 500 caracteres"),
});
```

---

## 🐳 **CONFIGURAÇÕES DOCKER**

### **1. DOCKER-COMPOSE PARA DESENVOLVIMENTO**

```yaml
# docker-compose.dev.yml
version: "3.8"

services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: nosso_closet_dev
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./postgres/init:/docker-entrypoint-initdb.d

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  rabbitmq:
    image: rabbitmq:3-management-alpine
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: password
    ports:
      - "5672:5672"
      - "15672:15672"
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq

  backend:
    build:
      context: ./backend-spring
      dockerfile: Dockerfile.dev
    ports:
      - "5454:5454"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: nosso_closet_dev
      DB_USERNAME: postgres
      DB_PASSWORD: password
      REDIS_HOST: redis
      REDIS_PORT: 6379
      RABBITMQ_HOST: rabbitmq
      RABBITMQ_PORT: 5672
    depends_on:
      - postgres
      - redis
      - rabbitmq
    volumes:
      - ./backend-spring:/app
      - /app/target

  frontend:
    build:
      context: ./frontend-vite
      dockerfile: Dockerfile.dev
    ports:
      - "3000:3000"
    volumes:
      - ./frontend-vite:/app
      - /app/node_modules

volumes:
  postgres_data:
  redis_data:
  rabbitmq_data:
```

### **2. DOCKERFILE PARA PRODUÇÃO**

```dockerfile
# backend-spring/Dockerfile.prod
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 5454

ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 📊 **MONITORAMENTO E MÉTRICAS**

### **1. SPRING BOOT ACTUATOR**

```yaml
# application-prod.properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=always
management.metrics.export.prometheus.enabled=true
management.metrics.tags.application=nosso-closet
management.metrics.tags.environment=production

# Métricas customizadas
management.metrics.enable.jvm=true
management.metrics.enable.process=true
management.metrics.enable.system=true
management.metrics.enable.logback=true
```

### **2. CUSTOM METRICS**

```java
@Component
public class BusinessMetrics {

    private final MeterRegistry meterRegistry;

    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordCustomOrderCreated() {
        Counter.builder("custom_orders.created")
            .register(meterRegistry)
            .increment();
    }

    public void recordCollectiveOrderClosed() {
        Counter.builder("collective_orders.closed")
            .register(meterRegistry)
            .increment();
    }

    public void recordSupplierPayment(Supplier supplier, BigDecimal amount) {
        Timer.builder("supplier.payment.duration")
            .tag("supplier", supplier.getName())
            .register(meterRegistry)
            .record(Duration.between(startTime, endTime));
    }
}
```

---

## 🔄 **JOBS AGENDADOS E AUTOMAÇÕES**

### **1. COBRANÇA AUTOMATIZADA**

```java
@Component
@EnableScheduling
public class AutomatedBillingJob {

    @Autowired
    private CollectiveOrderService collectiveOrderService;

    @Autowired
    private NotificationService notificationService;

    @Scheduled(cron = "0 0 9 * * *") // Todos os dias às 9h
    public void sendPaymentReminders() {
        List<CollectiveOrder> ordersNeedingPayment =
            collectiveOrderService.findOrdersNeedingPayment();

        for (CollectiveOrder order : ordersNeedingPayment) {
            notificationService.sendPaymentReminder(order);
        }
    }

    @Scheduled(cron = "0 0 18 * * *") // Todos os dias às 18h
    public void escalateOverduePayments() {
        List<CollectiveOrder> overdueOrders =
            collectiveOrderService.findOverdueOrders();

        for (CollectiveOrder order : overdueOrders) {
            notificationService.sendEscalationNotification(order);
        }
    }
}
```

### **2. PROCESSAMENTO ASSÍNCRONO**

```java
@Component
public class OrderProcessingQueue {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void processCustomOrder(CustomOrder order) {
        rabbitTemplate.convertAndSend("order.exchange", "order.custom", order);
    }

    public void processCollectiveOrder(CollectiveOrder order) {
        rabbitTemplate.convertAndSend("order.exchange", "order.collective", order);
    }
}

@Component
public class OrderProcessingListener {

    @RabbitListener(queues = "custom.order.queue")
    public void processCustomOrder(CustomOrder order) {
        // Processa pedido personalizado
        // Análise automática, precificação, etc.
    }

    @RabbitListener(queues = "collective.order.queue")
    public void processCollectiveOrder(CollectiveOrder order) {
        // Processa pedido coletivo
        // Agrupamento, cálculo de mínimos, etc.
    }
}
```

---

Este documento técnico fornece todos os detalhes necessários para implementar a refatoração do sistema Nosso Closet, incluindo arquitetura de dados, APIs, configurações de segurança, e automações necessárias para o modelo de negócio específico.
