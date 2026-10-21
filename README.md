# API de Leads

[![CI](https://github.com/levyespigue14-ux/api-leads/actions/workflows/ci.yml/badge.svg)](https://github.com/levyespigue14-ux/api-leads/actions/workflows/ci.yml)

API REST em **Java 21 + Spring Boot 3** para organizar a **prospecção de clientes**: cadastro de leads, funil de status, agendamento automático de follow-up e resumo do funil.

Nasceu de um problema real: eu prospecto clientes para os sites que desenvolvo e controlava tudo em planilha. Esta API é a versão back-end desse processo.

> 🇺🇸 *REST API (Java 21, Spring Boot 3, JPA, PostgreSQL) to manage sales leads: CRUD, status funnel, automatic follow-up scheduling and funnel summary. Tested with JUnit 5, Mockito and MockMvc; CI on GitHub Actions; runs with Docker Compose.*

## Funcionalidades

- **CRUD de leads** com validação (Bean Validation) e WhatsApp normalizado (`(86) 99999-0000` → `5586999990000`), com link `wa.me` pronto na resposta
- **Sem duplicidade:** o mesmo WhatsApp não é cadastrado duas vezes (HTTP 409)
- **Funil de status:** `NOVO → ABORDADO → FOLLOWUP → RESPONDEU / FECHADO / SEM_INTERESSE`
- **Follow-up automático:** ao registrar a abordagem, o follow-up é agendado para daqui a N dias (configurável)
- **Follow-ups vencidos:** endpoint com quem precisa ser chamado de novo hoje
- **Filtros e paginação:** por status, nicho e cidade
- **Erros padronizados** em JSON (400 com os campos inválidos, 404, 409)
- **Documentação interativa** com Swagger (OpenAPI)

## Endpoints

| Método | Rota | O que faz |
|---|---|---|
| `POST` | `/api/leads` | Cadastra um lead |
| `GET` | `/api/leads?status=&nicho=&cidade=&page=&size=` | Lista com filtros e paginação |
| `GET` | `/api/leads/{id}` | Busca um lead |
| `PUT` | `/api/leads/{id}` | Atualiza os dados |
| `DELETE` | `/api/leads/{id}` | Remove |
| `PATCH` | `/api/leads/{id}/abordagem` | Registra a primeira mensagem e agenda o follow-up |
| `PATCH` | `/api/leads/{id}/followup` | Registra um follow-up e agenda o próximo |
| `PATCH` | `/api/leads/{id}/status` | Muda o status (ex.: `RESPONDEU`, `FECHADO`) |
| `GET` | `/api/leads/followups` | Follow-ups vencidos |
| `GET` | `/api/leads/resumo` | Total de leads por etapa do funil |

Exemplo:

```bash
curl -X POST http://localhost:8080/api/leads -H "Content-Type: application/json" \
  -d '{"nome":"Ana Cakes","nicho":"confeitaria","cidade":"Teresina","whatsapp":"(86) 99981-0000","avaliacao":5.0}'
```

## Tecnologias

Java 21 · Spring Boot 3 (Web, Data JPA, Validation) · Hibernate · PostgreSQL · H2 · springdoc-openapi (Swagger) · JUnit 5 · Mockito · MockMvc · Docker · GitHub Actions

## Como rodar

**Rápido (banco em memória, sem instalar nada além do Java 21 e do Maven):**

```bash
mvn spring-boot:run
```

Abra a documentação em **http://localhost:8080/swagger-ui.html**.

**Com PostgreSQL (Docker):**

```bash
docker compose up --build
```

**Testes:**

```bash
mvn test
```

## Estrutura

```
src/main/java/dev/joaolevy/leads
├── lead/      entidade, repositório, filtros, serviço (regras do funil), controller e DTOs
└── common/    tratamento de erros e relógio injetável (datas testáveis)
src/test/java  testes de unidade (serviço) e de ponta a ponta (API + banco H2)
```

## Autor

**João Levy**: desenvolvedor back-end (Python e Java) · [LinkedIn](https://www.linkedin.com/in/joao-levy-4008521b2) · [GitHub](https://github.com/levyespigue14-ux)

## Licença

[MIT](LICENSE)
