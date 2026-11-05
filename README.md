# AutoLeilão DS — Arquitetura de Microsserviços com CQRS, Database per Service, CosmosDB, Azure SQL & Service Bus

Evolução arquitetural do projeto de **Microsserviços com Spring Boot** desenvolvido para a disciplina de **Arquitetura de Soluções** (Engenharia de Software). Esta versão (`AULA_ARQSOL_MICROSSERVICOS_DS`) implementa os padrões **CQRS (Command Query Responsibility Segregation)**, **Database per Service**, **Aggregate Root com Optimistic Locking (`@Version`)** e **Mensageria Assíncrona com Azure Service Bus**, mantendo 100% de compatibilidade com a plataforma de Microfrontends [AULA_ARQSOL_MFE](file:///d:/ANTIGRAVITY/AULA_ARQSOL_MFE).

---

## 🎯 Contexto e Narrativa Pedagógica: Antes vs. Depois

Este repositório foi concebido como um **projeto evolutivo e isolado** para garantir o máximo contraste didático entre as fases da evolução arquitetural:

```
========================================================================================
FASE ANTERIOR: MODELO TRADICIONAL (CRUD Relacional Acoplado)
========================================================================================

  [ Frontend / Microfrontends ]
                 │
                 │ HTTP (Leituras & Lances simultâneos)
                 ▼
  ┌────────────────────────────────────────────────────────┐
  │     veiculos-service (:8081) / leilao-service (:8082)  │
  │     (Mesma entidade JPA para Leitura e Escrita)        │
  └──────────────────────────┬─────────────────────────────┘
                             │
                             ▼
  ┌────────────────────────────────────────────────────────┐
  │     BANCO RELACIONAL TRADICIONAL / COMPARTILHADO       │
  │     (Tabelas com JOINs pesados e Locks de linha)       │
  └────────────────────────────────────────────────────────┘

  ❌ Problemas da Fase Anterior:
     1. Contenção de Banco: Disputa feroz de lances a cada milissegundo trava tabelas
        e congela telas de consulta e dashboards de usuários.
     2. Mapeamento Rígido: Múltiplos JOINs lentos para montar um lote com seu histórico.
     3. Acoplamento Síncrono: Criar leilão ou integrar catálogo exige chamadas REST
        bloqueantes entre microsserviços.
     4. Risco de Deadlock: Locks pessimistas no pregão geram erros e quedas de vazão.

========================================================================================
FASE ATUAL (DS): CQRS, DATABASE PER SERVICE & EVENT-DRIVEN NA NUVEM AZURE
========================================================================================

  [ Shell Host :3000   ] ──┐
  [ MFE Cadastro :3001 ] ──┼──> [ API GATEWAY & BFF :8080 ] (Ponto Único & Borda Azure)
  [ MFE Leilão :3002   ] ──┘    │
                                ├─ CORS Centralizado & Rastreabilidade (X-Correlation-Id)
                                ├─ Agregação Concorrente (BFF Reativo WebFlux)
                                │
                                ├───> /api/veiculos/**  ──> [ veiculos-service :8081 ]
                                ├───> /api/leiloes/**   ──> [ leilao-service :8082 ]
                                └───> /api/bff/**       ──> [ Composição Reativa WebClient ]
                                                              │
                                                              │ (Domínio & Dados)
                                                              ▼
  ┌─────────────────────────────────────────┐    ┌─────────────────────────────────────────┐
  │         veiculos-service (:8081)        │    │          leilao-service (:8082)         │
  │                                         │    │                                         │
  │     COMMAND: Azure Cosmos DB (Mongo)    │    │     COMMAND: Azure Cosmos DB (Mongo)    │
  │     Banco: veiculos_command_db          │    │     Banco: leilao_command_db            │
  │                                         │    │     Aggregate Root + @Version Lock      │
  │     MENSAGERIA: Azure Service Bus       │    │     MENSAGERIA: Azure Service Bus       │
  │     Fila: veiculos-cqrs-queue           │    │     Fila: leilao-cqrs-queue             │
  │     Fila: veiculos-leilao-queue ────────┼────┼─►   Consumer: Cria Lote Automático!     │
  └────────────────────┬────────────────────┘    └────────────────────┬────────────────────┘
                       │ Atualiza Projeção (Sync)                     │ Atualiza Projeção (Sync)
                       ▼                                              ▼
  ┌────────────────────────────────────────────────────────────────────────────────────────┐
  │                    CAMADA DE LEITURA UNIFICADA (CQRS VIEW DATABASE)                    │
  │                      AZURE SQL SERVER (Banco: autoleilao-read-db)                      │
  │                                                                                        │
  │   Tabela: [veiculos_query]             Tabelas: [lotes_leilao_query] + [lances_query]  │
  │   (Filtros rápidos e estatísticas)     (Histórico e listagens sem lock de escrita)     │
  └────────────────────────────────────────────────────────────────────────────────────────┘

  ✅ Benefícios da Fase Atual (DS):
     1. Ponto Único com Gateway & BFF: Frontend consome exclusivamente a porta :8080.
     2. Desacoplamento Total (Database per Service): Cada serviço tem seu próprio banco NoSQL
        de gravação (System of Record) sem compartilhar schemas ou tabelas.
     3. Zero Contenção com @Version: Concorrência de lances resolvida com Optimistic Locking
        em documentos JSON atômicos (latência de microssegundos).
     4. Integração Assíncrona (Event-Driven): Ao cadastrar um veículo, o lote de leilão é
        criado automaticamente via fila do Azure Service Bus, sem bloqueios síncronos.
     5. Consultas Otimizadas (CQRS Read Model): O Azure SQL Server mantém projeções planas
        desnormalizadas prontas para telas, filtros e relatórios de alta performance.
     6. Resiliência e Dual-Engine: Executa 100% nativo na Nuvem Azure ou 100% offline local
        com emuladores em memória e banco H2 para desenvolvimento e testes.
========================================================================================
```

---

## 📊 Tabela Comparativa: Antes (CRUD) vs. Depois (CQRS + Database per Service)

| Característica | Modelo Tradicional (v1) | Modelo CQRS & Database per Service (v2 - DS) |
| :--- | :--- | :--- |
| **Ponto de Acesso do Frontend** | Múltiplas portas expostas (`:8081`, `:8082`) | **API Gateway & BFF (:8080)** |
| **Banco de Dados de Escrita (Command)** | Relacional com locks de tabela/linha | **Azure Cosmos DB NoSQL (Documentos Atômicos)** |
| **Isolamento de Dados** | Schemas compartilhados / Acoplamento relacional | **Database per Service (`veiculos_command_db` e `leilao_command_db`)** |
| **Controle de Concorrência nos Lances** | Locks pessimistas (`SELECT FOR UPDATE`) / Risco de Deadlock | **Optimistic Locking nativo com `@Version`** |
| **Modelo de Leitura (Query)** | Tabelas normalizadas com `JOINs` concorrendo com lances | **Azure SQL Server dedicado (`autoleilao-read-db`) com projeções planas** |
| **Integração entre Microsserviços** | Chamadas REST HTTP síncronas e bloqueantes | **Mensageria Assíncrona com Azure Service Bus (Filas AMQP)** |
| **Criação de Lotes de Leilão** | Manual ou acoplada via código síncrono | **Automática via evento `veiculos-leilao-queue`** |
| **Consistência dos Dados** | ACID estrito local (inviável em escala distribuída) | **Consistência Eventual (*Eventual Consistency*) com Padrão Saga** |
| **Governança de Arquitetura** | Nenhuma validação automática | **Testes de Arquitetura no Build com ArchUnit + JUnit 5** |

---

## 🏗️ Visão Geral da Arquitetura (CQRS + Database per Service)

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                       CAMADA DE APRESENTAÇÃO (MICROFRONTENDS)                          │
│         Shell Host (:3000)      mfe-cadastro (:3001)        mfe-leilao (:3002)         │
└───────────────────────────────────────────┬────────────────────────────────────────────┘
                                            │ HTTP /api/**
                                            ▼
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        API GATEWAY & BFF (:8080) (BORDA AZURE)                         │
│                    Spring Cloud Gateway (WebFlux) • Netty • CORS                       │
└───────────────────┬──────────────────▲──────────────────┬──────────────────▲───────────┘
                    │                  │                  │                  │
     POST/PUT/DELETE│                  │GET               │POST Lances/Lotes │GET
        (Commands)  │          (Queries│                  │(Commands)        │(Queries)
                    ▼                  │                  ▼                  │
┌──────────────────────────────────────┴───┐  ┌──────────────────────────────┴───────────┐
│        veiculos-service (:8081)          │  │          leilao-service (:8082)          │
│                                          │  │                                          │
│  • Command DB: Azure Cosmos DB (Mongo)   │  │  • Command DB: Azure Cosmos DB (Mongo)   │
│    Database: veiculos_command_db         │  │    Database: leilao_command_db           │
│  • Service Bus: veiculos-cqrs-queue      │  │    Aggregate Root + @Version Lock        │
│  • Service Bus: veiculos-leilao-queue ───┼──┼─►• Consumer: Cria Lote Automático!       │
│                                          │  │  • Service Bus: leilao-cqrs-queue        │
└────────────────────┬─────────────────────┘  └────────────────────┬─────────────────────┘
                     │ Atualiza Projeção SQL                       │ Atualiza Projeção SQL
                     ▼                                             ▼
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                   CAMADA DE LEITURA UNIFICADA (CQRS VIEW DATABASE)                     │
│                      AZURE SQL SERVER (Banco: autoleilao-read-db)                      │
│                                                                                        │
│     Tabela: [veiculos_query]             Tabelas: [lotes_leilao_query] + [lances_query]│
└────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 🎯 Padrões Arquiteturais Demonstrados

| Padrão | Onde é aplicado | Conceito Didático |
|---|---|---|
| **CQRS (Command Query Responsibility Segregation)** | Pacotes `command/` e `query/` em ambos os serviços | Separa o modelo de escrita (CosmosDB NoSQL) do modelo de leitura (Azure SQL Server), eliminando contenção entre quem dá lances e quem consulta a tela. |
| **Database per Service (No Command)** | `veiculos_command_db` e `leilao_command_db` no CosmosDB | Cada microsserviço possui seu próprio banco de gravação (*System of Record*) isolado. |
| **Aggregate Root + Optimistic Locking (`@Version`)** | `LoteLeilaoDocument` + `LanceEmbedded` | Lote e lances gravados em um único documento JSON atômico sem `JOIN`/`FK`, com controle de concorrência `@Version` contra lances simultâneos. |
| **CQRS View Database** | `autoleilao-read-db` no Azure SQL Server | Projeção relacional otimizada para leitura, filtros compostos e agregações (`SUM`, `GROUP BY`). |
| **Event-Driven Architecture (Azure Service Bus)** | `veiculos-cqrs-queue`, `leilao-cqrs-queue` e `veiculos-leilao-queue` | Sincronização assíncrona Write -> Read e criação automática de lote no leilão ao cadastrar um veículo! |


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
