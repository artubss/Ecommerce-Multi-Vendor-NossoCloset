# 🔄 Instruções para Reiniciar o Docker e Testar as Novas Credenciais

## 🐳 Reiniciar o Docker

### 1. **Parar os Containers Atuais**

```bash
docker-compose down
```

### 2. **Remover Volumes (para limpar dados antigos)**

```bash
docker-compose down -v
```

### 3. **Reconstruir e Iniciar**

```bash
docker-compose up --build -d
```

### 4. **Verificar Status**

```bash
docker-compose ps
```

## 🚀 Testar as Novas Credenciais

### **Credenciais Atualizadas:**

- **Email**: `arthurbezerra5000@gmail.com`
- **Senha**: `nossoClosetAmoVerMe`

### **URLs de Acesso:**

- **Frontend**: http://localhost:3001
- **Backend**: http://localhost:5454
- **Admin**: http://localhost:3001/admin

## 📋 Passos para Teste

### 1. **Acessar o Frontend**

```bash
cd sourceCode/frontend-vite
npm run dev
```

### 2. **Acessar a Área Admin**

- Navegue para: http://localhost:3001/admin
- Use as credenciais acima
- Verifique se consegue fazer login

### 3. **Verificar Funcionalidades**

- ✅ Login como administrador
- ✅ Acesso ao painel administrativo
- ✅ Visualização das novas categorias em português
- ✅ Nome "Nosso Closet" em toda a interface

## 🔧 Solução de Problemas

### **Se o Login não funcionar:**

1. **Verificar se o Backend está rodando:**

```bash
curl http://localhost:5454/health
```

2. **Verificar logs do Backend:**

```bash
docker-compose logs backend-spring
```

3. **Verificar se o usuário admin foi criado:**

- Acesse o console H2: http://localhost:5454/h2-console
- Conecte com: `jdbc:h2:mem:ecommerce_multi_vendor`
- Usuário: `sa` (sem senha)
- Verifique a tabela `user`

### **Se houver erro de CORS:**

- O backend já está configurado para aceitar conexões do frontend na porta 3001

## 📝 Notas Importantes

- **Porta do Frontend**: 3001 (configurada no vite.config.ts)
- **Porta do Backend**: 5454 (configurada no application.properties)
- **Banco de Dados**: H2 em memória (para desenvolvimento)
- **Email SMTP**: Configurado para arthurbezerra5000@gmail.com

## ✅ Checklist de Verificação

- [ ] Docker reiniciado com sucesso
- [ ] Frontend rodando na porta 3001
- [ ] Backend rodando na porta 5454
- [ ] Login como admin funcionando
- [ ] Interface mostrando "Nosso Closet"
- [ ] Categorias em português visíveis
- [ ] Filtros de preço em reais funcionando

---

**Após reiniciar o Docker, as novas credenciais estarão ativas e você poderá fazer login como administrador!** 🎉



