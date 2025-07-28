# Microsserviços User & Email
Projeto de microsserviços com foco em gestão de usuários e envio de e-mails, utilizando Spring Boot, RabbitMQ, PostgreSQL e API Gateway.

---

## Sumário

- [Descrição do Projeto](#descrição-do-projeto)
- [Arquitetura](#arquitetura)
- [Detalhes dos Componentes](#detalhes-dos-componentes)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Pré-requisitos](#pré-requisitos)
- [Configuração do Ambiente](#configuração-do-ambiente)
- [Como Rodar](#como-rodar)
- [Endpoints](#endpoints)
- [Testes](#testes)
- [Exceções Tratadas](#exceções-tratadas)
- [Licença](#licença)

---

## Descrição do Projeto

Este projeto é composto por dois microsserviços:

- **User Service:** Responsável por CRUD de usuários, validação de dados, publicação de mensagens em RabbitMQ para envio de e-mails.
- **Email Service:** Consumidor das mensagens da fila RabbitMQ para envio efetivo de e-mails, registrando o status e informações do envio.
- **Docker:** Orquestração dos serviços em containers

Há um **API Gateway** para rotear as requisições para os microsserviços correspondentes.

---

## Arquitetura

```
client
  │
  ▼
API Gateway
  │
  ├──────────────→ User Microservice ──→ PostgreSQL (User)
  │                        │
  │                        └──── publish (UserCreated)
  │                                  ↓
  │                            📨 RabbitMQ Broker
  │                                  ↓
  └──────────────→ Email Microservice ──→ PostgreSQL (Email)
                                 │
                                 └── envia e-mail
```

### Detalhes dos Componentes

- **Client:** Aplicação cliente que consome a API via API Gateway.
  
- **API Gateway:**
  - Responsável por rotear as requisições HTTP para os microsserviços apropriados (`User Service` ou `Email Service`).
  - Facilita a orquestração e gerenciamento de rotas.
  - Expõe uma interface unificada para os clientes.

- **User Service:**
  - Microsserviço responsável pelo gerenciamento de usuários.
  - Implementa operações CRUD de usuários.
  - Valida dados e evita duplicidade de emails.
  - Publica mensagens na fila RabbitMQ para notificar o `Email Service` quando um usuário é criado.

- **Email Service:**
  - Microsserviço responsável por enviar e-mails.
  - Consome mensagens da fila RabbitMQ.
  - Processa o envio de e-mails através de SMTP.
  - Armazena registros de e-mails enviados com status (enviado com sucesso ou erro).

- **RabbitMQ:**
  - Broker de mensagens que permite comunicação assíncrona entre microsserviços.
  - Garante que o `Email Service` receba notificações do `User Service` para enviar e-mails de boas-vindas.

- **Banco de Dados:**
  - Cada microsserviço possui seu próprio banco PostgreSQL isolado.
  - `User Service` armazena dados de usuários.
  - `Email Service` armazena registros de e-mails enviados.

---

## Tecnologias Utilizadas

- Java 17
- Spring Boot (Web, Data JPA, AMQP, Validation)
- PostgreSQL
- RabbitMQ
- Docker & Docker Compose
- JUnit 5 + Mockito (Testes unitários e integração)
- Lombok
- Spring Cloud Gateway

## Pré-requisitos

- Docker & Docker Compose instalados
- Java 17 (para rodar local sem docker)
- Maven (para build local)
- Conta de e-mail para envio SMTP configurada (para Email Service)


## Configuração do Ambiente

Crie um arquivo `.env` na raiz do projeto com as variáveis necessárias, por exemplo:

```dotenv

# User DB
USER_DB_PASSWORD=senhaUser
USER_DB_URL=jdbc:urlDoBanco
USER_DB_USERNAME=userDb
USER_SERVER_PORT=8081

# Email DB
EMAIL_DB_PASSWORD=senhaEmail
EMAIL_DB_URL=jdbc:urlDoBanco
EMAIL_DB_USERNAME=UserMsEmailDb
EMAIL_SERVER_PORT=8082

# RabbitMQ
RABBITMQ_ADDRESSES=endereçoRabbitMQ
BROKER_QUEUE_EMAIL_NAME=email.queue

# API Gateway
GATEWAY_SERVER_PORT=8080

# SMTP Email (configurar conforme seu servidor SMTP)
MAIL_SMTP_HOST=smtp.exemplo.com
MAIL_PORT=587
MAIL_USERNAME=seu-email@exemplo.com
MAIL_PASSWORD=sua-senha
MAIL_AUTH=true
MAIL_STARTTLS_ENABLE=true
MAIL_PROTOCOL=smtp
MAIL_TEST_CONNECTION=false
MAIL_STARTTLS_REQUIRED=true
MAIL_SSL_TRUST=smtp.exemplo.com
````

## Como Rodar

1. Clone o repositório:

```bash
git clone https://github.com/seu-usuario/seu-repositorio.git
cd seu-repositorio
```

2. Suba os containers:

```bash
docker-compose up --build
```

3. Acesse os microsserviços via:

- `http://localhost:8080/users` (User)

---

## Endpoints

### User Microservice

| Método | Endpoint        | Descrição             |
|--------|------------------|------------------------|
| GET    | `/users`         | Listar todos os usuários |
| GET    | `/users/{id}`    | Buscar usuário por ID |
| POST   | `/users`         | Criar novo usuário    |
| PUT    | `/users/{id}`    | Atualizar usuário     |
| DELETE | `/users/{id}`    | Deletar usuário       |


### Email Microservice (via RabbitMQ)

| Evento RabbitMQ  | Ação Executada             |
|------------------|----------------------------|
| `UserCreated`    | Envia e-mail de boas-vindas |

---

## Testes

O projeto contém testes unitários e de integração com [Testcontainers](https://www.testcontainers.org/).

Para rodar os testes:

```bash
./mvnw test
```

Testes com containers:

- Banco de dados PostgreSQL
- Broker RabbitMQ

---

## Exceções Tratadas

- `UserNotFoundException`: Usuário não encontrado.
- `EmailException`: Erro ao enviar e-mail.
- Validação de campos obrigatórios com mensagens personalizadas.

---

## Licença

Este projeto está sob a licença [MIT](LICENSE).

---








