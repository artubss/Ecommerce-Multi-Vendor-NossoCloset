# Nosso Closet - E-commerce de Moda

## 🎯 Sobre o Projeto

O **Nosso Closet** é uma plataforma de e-commerce multi-vendedor especializada em moda, roupas e acessórios. O projeto foi transformado de "Zosh Bazar" para focar exclusivamente no mercado brasileiro de moda.

## 🚀 Funcionalidades Principais

- **Multi-vendedor**: Sistema completo para vendedores e administradores
- **Categorias de Moda**: Masculino, Feminino, Infantil, Academia & Esporte, Acessórios
- **Sistema de Pagamento**: Integração com Razorpay e Stripe
- **Chatbot IA**: Assistente virtual para clientes
- **Sistema de Cupons**: Gestão de descontos e promoções
- **Gestão de Pedidos**: Controle completo de vendas

## 👑 Acesso de Administrador

### Credenciais de Login:
- **Email**: `codewithzosh@gmail.com`
- **Senha**: `codewithzosh`
- **Função**: Administrador Principal

### Funcionalidades do Admin:
- Gestão de vendedores
- Aprovação de produtos
- Gestão de cupons e descontos
- Relatórios de vendas
- Controle de categorias
- Gestão de usuários

## 🏷️ Categorias Principais

### 1. **Masculino**
- Camisetas (Básicas, Polo, Esportivas, Estampadas)
- Camisas (Sociais, Casuais, Manga Longa/Curta)
- Calças (Jeans, Sociais, Esportivas, Cargo)
- Jaquetas (Couro, Esportivas, Casuais)
- Roupas Íntimas
- Calçados (Tênis, Sociais, Sapatênis, Sandálias)
- Acessórios

### 2. **Feminino**
- Vestidos
- Blusas
- Calças
- Saias
- Jaquetas
- Casacos
- Roupas Íntimas
- Calçados
- Acessórios
- Bolsas

### 3. **Infantil**
- Meninos (2-12 anos)
- Meninas (2-12 anos)
- Bebês (0-2 anos)
- Calçados Infantis
- Acessórios Infantis

### 4. **Academia & Esporte**
- Roupas de Academia
- Roupas de Corrida
- Roupas de Yoga
- Calçados Esportivos
- Equipamentos

### 5. **Acessórios**
- Bolsas
- Cintos
- Joias
- Relógios
- Óculos
- Lenços e Cachecóis

## 💰 Filtros de Preço (em Reais)

- Abaixo de R$ 50
- R$ 50 - R$ 100
- R$ 100 - R$ 200
- R$ 200 - R$ 300
- R$ 300 - R$ 500
- R$ 500 - R$ 1000
- Acima de R$ 1000

## 🎨 Filtros de Cores

Todas as cores foram traduzidas para português brasileiro:
- Rosa, Verde, Azul, Vermelho, Amarelo
- Preto, Branco, Cinza, Marrom
- E muitas outras cores de moda

## 🏪 Marcas Disponíveis

- **Internacionais**: Nike, Adidas, Puma, Under Armour, Lacoste, Tommy Hilfiger, Calvin Klein, Levi's, H&M, Zara
- **Nacionais**: C&A, Renner, Riachuelo, Marisa, Lojas Americanas

## 🔧 Tecnologias Utilizadas

### Frontend:
- React + TypeScript
- Vite
- Material-UI
- Redux Toolkit
- React Router

### Backend:
- Spring Boot (Java)
- Spring Security
- JWT Authentication
- PostgreSQL/H2 Database

### Infraestrutura:
- Docker
- Nginx
- MySQL/PostgreSQL

## 📁 Estrutura do Projeto

```
sourceCode/
├── frontend-vite/          # Frontend React
│   ├── src/
│   │   ├── admin/         # Painel Administrativo
│   │   ├── customer/      # Área do Cliente
│   │   ├── seller/        # Área do Vendedor
│   │   ├── data/          # Dados e Configurações
│   │   └── Redux Toolkit/ # Gerenciamento de Estado
├── backend-spring/         # Backend Java
│   ├── src/main/java/
│   │   ├── controller/    # Controladores REST
│   │   ├── service/       # Lógica de Negócio
│   │   ├── model/         # Entidades
│   │   └── config/        # Configurações
└── docker-compose.yml      # Configuração Docker
```

## 🚀 Como Executar

### 1. **Backend**
```bash
cd sourceCode/backend-spring
./mvnw spring-boot:run
```

### 2. **Frontend**
```bash
cd sourceCode/frontend-vite
npm install
npm run dev
```

### 3. **Docker (Recomendado)**
```bash
docker-compose up -d
```

## 🌐 URLs de Acesso

- **Frontend**: http://localhost:5173
- **Backend**: http://localhost:5454
- **Admin**: http://localhost:5173/admin
- **Vendedor**: http://localhost:5173/seller

## 📧 Configuração de Email

O sistema está configurado para usar Gmail SMTP:
- **Host**: smtp.gmail.com
- **Porta**: 587
- **Usuário**: arthurbezerra5000@gmail.com

## 🔐 Segurança

- Autenticação JWT
- Validação OTP para login
- Controle de acesso baseado em roles
- Criptografia de senhas

## 📱 Responsividade

O projeto é totalmente responsivo e funciona em:
- Desktop
- Tablet
- Mobile

## 🎨 Tema

- **Cor Principal**: #00927c (Verde-azulado)
- **Cor Secundária**: #EAF0F1 (Cinza claro)
- Design moderno e limpo

## 📞 Suporte

Para dúvidas ou suporte técnico, entre em contato através do email configurado no sistema.

---

**Desenvolvido com ❤️ para o mercado brasileiro de moda**
