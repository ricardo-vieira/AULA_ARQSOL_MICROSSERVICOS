# AutoLeilão — Arquitetura de Microsserviços com Spring Boot & Java 25

Projeto prático de **Microsserviços com Spring Boot** desenvolvido para a disciplina de **Arquitetura de Soluções** (Engenharia de Software). Este backend provê as APIs REST consumidas pela plataforma de Microfrontends [AULA_ARQSOL_MFE](file:///d:/ANTIGRAVITY/AULA_ARQSOL_MFE).

---

## 🏗️ Visão Geral da Arquitetura

A solução adota os padrões canônicos da arquitetura de microsserviços:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        CAMADA DE APRESENTAÇÃO                          │
│                                                                        │
│               SHELL HOST (React + MFE) — localhost:3000                │
│             ┌──────────────────────────┬──────────────────────────┐    │
│             │   mfe-cadastro (:3001)   │    mfe-leilao (:3002)    │    │
│             └────────────┬─────────────┴─────────────┬────────────┘    │
└──────────────────────────┼───────────────────────────┼─────────────────┘
                           │                           │
          HTTP /api/veiculos                           │ HTTP /api/leiloes
          (Porta :8081)    │                           │ (Porta :8082)
                           ▼                           ▼
┌──────────────────────────────────────┐  ┌──────────────────────────────┐
│           veiculos-service           │  │        leilao-service        │
│            localhost:8081            │  │        localhost:8082        │
│                                      │  │                              │
│ • CRUD de Veículos                   │  │ • Pregão e Lotes Ativos      │
│ • Validações de Domínio              │  │ • Submissão de Lances        │
│ • Estatísticas do Catálogo           │  │ • Regra de incremento R$ 500 │
│ • Documentação Swagger/OpenAPI       │  │ • Regra Anti-Sniping (+60s)  │
│                                      │  │ • Documentação Swagger       │
│  ┌────────────────────────────────┐  │  │  ┌────────────────────────┐  │
│  │  Banco H2: mem:veiculosdb      │  │  │  │  Banco H2: mem:leilaodb│  │
│  └────────────────────────────────┘  │  │  └────────────────────────┘  │
│        (Database per Service)        │  │     (Database per Service)   │
└──────────────────────────────────────┘  └──────────────────────────────┘
```

---

## 🎯 Padrões Arquiteturais Demonstrados

| Padrão | Onde é aplicado | Conceito Didático |
|---|---|---|
| **Database per Service** | `veiculos-service` e `leilao-service` | Cada microsserviço gerencia seu próprio banco de dados H2. Nenhuma tabela é compartilhada, garantindo total desacoplamento e autonomia de deploy. |
| **Comunicação REST Direta MFE ↔ API** | Microfrontends ↔ Microsserviços | Cada microfrontend consome diretamente a API REST do seu respectivo serviço de domínio. |
| **Domain-Driven Design (Bounded Contexts)** | Separação entre Catálogo e Pregão | O contexto de cadastro e especificação de veículos é delimitado e independente do contexto de lances e regras de leilão. |
| **Resiliência e Fallback Gracioso** | Frontend MFE ↔ Backend | Se os microsserviços estiverem desligados, o frontend opera em modo de demonstração com dados mock sem quebrar a tela. Quando a API sobe, conecta-se automaticamente. |
| **Health Checks (Actuator)** | `/actuator/health` | Padrão de observabilidade para monitoramento de disponibilidade e integridade em tempo de execução. |

---

## 🗺️ Mapa de Portas e Endpoints

### 1. `veiculos-service` — Porta `8081`
- **Swagger UI:** [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
- **H2 Console:** [http://localhost:8081/h2-console](http://localhost:8081/h2-console) (JDBC URL: `jdbc:h2:mem:veiculosdb`, user: `sa`, password: vazio)
- **Actuator Health:** [http://localhost:8081/actuator/health](http://localhost:8081/actuator/health)
- **Endpoints REST:**
  - `GET /api/veiculos` — Lista todos os veículos (suporta filtros `?tipo=Carro` ou `?marca=Toyota`)
  - `GET /api/veiculos/{id}` — Busca veículo por ID
  - `POST /api/veiculos` — Cadastra veículo com validações de campos obrigatórios e unicidade de placa
  - `PUT /api/veiculos/{id}` — Atualiza os dados de um veículo
  - `DELETE /api/veiculos/{id}` — Exclui um veículo
  - `GET /api/veiculos/estatisticas` — Métricas agregadas (total de veículos, valor total, totais por tipo)

### 2. `leilao-service` — Porta `8082`
- **Swagger UI:** [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
- **H2 Console:** [http://localhost:8082/h2-console](http://localhost:8082/h2-console) (JDBC URL: `jdbc:h2:mem:leilaodb`, user: `sa`, password: vazio)
- **Actuator Health:** [http://localhost:8082/actuator/health](http://localhost:8082/actuator/health)
- **Endpoints REST:**
  - `GET /api/leiloes` — Lista lotes em disputa (suporta filtro `?status=ativo`)
  - `GET /api/leiloes/{id}` — Detalhes do lote e histórico cronológico de lances
  - `POST /api/leiloes/{id}/lances` — Registra novo lance (valida se `valor >= lanceAtual + 500.00` e se leilão está ativo)
  - `POST /api/leiloes` — Cria um novo lote de leilão
  - `PATCH /api/leiloes/{id}/encerrar` — Encerra o lote manualmente

---

## 🚀 Como Executar

### Opção 1: Iniciar os microsserviços de uma vez (Recomendado)

Basta dar duplo clique ou executar no terminal:
```cmd
.\start-all.bat
```
ou via PowerShell:
```powershell
.\start-all.ps1
```
Duas janelas de terminal serão abertas, iniciando os serviços nas portas `8081` (veiculos-service) e `8082` (leilao-service).

---

### Opção 2: Iniciar individualmente via Maven Wrapper

```cmd
# Terminal 1: Veículos
.\mvnw.bat spring-boot:run -pl veiculos-service

# Terminal 2: Leilão
.\mvnw.bat spring-boot:run -pl leilao-service
```

---

## 🧪 Como Rodar os Testes Automatizados

Para executar os testes de unidade e integração dos 3 serviços:
```cmd
.\mvnw.bat clean test
```

---

## 🎓 Roteiro Didático para Lecionar a Aula

Confira o arquivo completo com o roteiro passo a passo e tópicos de discussão em:
[GUIA_AULA.md](file:///d:/ANTIGRAVITY/AULA_ARQSOL_MICROSSERVICOS/GUIA_AULA.md)
