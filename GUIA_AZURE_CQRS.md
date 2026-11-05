# Passo a Passo de Provisionamento na Nuvem Azure (CQRS + Database per Service)

Este guia documenta como provisionar os **3 recursos nativos da Azure (em camadas gratuitas/econômicas)** para o projeto **`AULA_ARQSOL_MICROSSERVICOS_DS`** e configurar as variáveis de ambiente nas Azure Web Apps (`autoleilao-veiculos-api` e `autoleilao-leilao-api`).

---

## 1. Camada COMMAND (Escrita): Azure Cosmos DB for MongoDB (Database per Service)

1. No **Portal da Azure**, pesquise por **Azure Cosmos DB** e clique em **Create**.
2. Selecione **Azure Cosmos DB for MongoDB** ➡️ modelo **Request Units (RU)**.
3. Preencha:
   - **Account Name:** ex: `autoleilao-cosmos-cqrs`
   - **Free Tier Discount:** Marque **Apply** (garante 1.000 RU/s e 25 GB **gratuitos**).
4. Após a criação do recurso, vá no menu lateral **Settings > Connection Strings** e copie a **PRIMARY CONNECTION STRING** (ela será a variável `AZURE_COSMOS_URI`).
5. **Isolamento Database per Service:**
   - A própria aplicação cria automaticamente na mesma conta gratuita dois bancos de dados isolados:
     - `veiculos_command_db` (exclusivo do `veiculos-service`, coleção `veiculos_command`)
     - `leilao_command_db` (exclusivo do `leilao-service`, coleção `lotes_leilao_command`)

---

## 2. Camada MENSAGERIA (Sincronização e Integração): Azure Service Bus

1. No **Portal da Azure**, pesquise por **Service Bus** e clique em **Create**.
2. Preencha:
   - **Namespace Name:** ex: `autoleilao-sb-cqrs`
   - **Pricing Tier:** Selecione **Basic** (custa centavos por milhão de mensagens e suporta todas as filas).
3. Após criar o Namespace, entre em **Entities > Queues** e clique em **+ Queue** para criar as **3 filas**:
   1. `veiculos-cqrs-queue` *(Sincroniza Command CosmosDB ➡️ Query Azure SQL no `veiculos-service`)*
   2. `veiculos-leilao-queue` *(Integração entre domínios: avisa o `leilao-service` para criar automaticamente um Lote quando um veículo é cadastrado)*
   3. `leilao-cqrs-queue` *(Sincroniza Command CosmosDB ➡️ Query Azure SQL no `leilao-service`)*
4. No menu lateral, vá em **Settings > Shared access policies** ➡️ clique em **RootManageSharedAccessKey** e copie a **Primary Connection String** (ela será a variável `AZURE_SERVICEBUS_CONNECTION_STRING`).

---

## 3. Camada QUERY (Leitura Unificada): Azure SQL Server

1. No **Portal da Azure**, pesquise por **SQL databases** e clique em **Create**.
2. Selecione a oferta gratuita (**Want to try Azure SQL Database for free? -> Apply free offer**) ou o tier **Basic / Serverless com Auto-Pause**.
3. Preencha:
   - **Database name:** `autoleilao-read-db`
   - **Server:** Crie um servidor lógico (ex: `autoleilao-sqlserver.database.windows.net`) com autenticação SQL (`AZURE_SQL_USERNAME` e `AZURE_SQL_PASSWORD`).
4. **Importante — Liberar Firewall para as Azure Web Apps:**
   - Acesse o recurso do **SQL Server** no Portal da Azure ➡️ menu **Security > Networking**.
   - Marque a opção **"Allow Azure services and resources to access this server"** (e adicione o seu IP local caso queira conectar da sua máquina durante a aula) ➡️ clique em **Save**.
5. O Hibernate criará automaticamente as tabelas de projeção de leitura no banco `autoleilao-read-db`:
   - `veiculos_query`
   - `lotes_leilao_query`
   - `lances_query`

---

## 4. Variáveis de Ambiente nas Azure Web Apps (`autoleilao-veiculos-api` e `autoleilao-leilao-api`)

Em cada uma das duas **Azure Web Apps** (menu **Settings > Environment variables**), adicione:

| Variável | Valor de Exemplo |
|---|---|
| `AZURE_COSMOS_URI` | `mongodb://autoleilao-cosmos-cqrs:CHAVE@autoleilao-cosmos-cqrs.mongo.cosmos.azure.com:10255/?ssl=true&replicaSet=globaldb&retrywrites=false&maxIdleTimeMS=120000` |
| `AZURE_SERVICEBUS_CONNECTION_STRING` | `Endpoint=sb://autoleilao-sb-cqrs.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=CHAVE` |
| `AZURE_SQL_URL` | `jdbc:sqlserver://autoleilao-sqlserver.database.windows.net:1433;database=autoleilao-read-db;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;` |
| `AZURE_SQL_USERNAME` | `seu_usuario_sql` |
| `AZURE_SQL_PASSWORD` | `sua_senha_sql` |

---

## 5. Verificação em Tempo Real no Swagger

Após o deploy automático via **GitHub Actions**, acesse os endpoints de diagnóstico CQRS:
- `GET /api/veiculos/cqrs-status`
- `GET /api/leiloes/cqrs-status`

Eles exibem um JSON detalhando se os conectores da nuvem (`commandCloudCosmosAtivo`, `serviceBusCloudAtivo`) estão ativos e a contagem em tempo real de documentos no **CosmosDB** vs. registros projetados no **Azure SQL Server**!


**Saga pattern**


```mermaid
sequenceDiagram
    autonumber
    participant P as 📦 Serviço de Pedidos
    participant B as 📨 Event Broker (Pub/Sub)
    participant Pag as 💳 Serviço de Pagamento
    participant E as 🏭 Serviço de Estoque

    Note over P,E: CAMINHO DE IDA (Transações Locais)
    P->>P: Cria Pedido (Status: PENDENTE) [COMMIT]
    P->>B: Publica Evento: PedidoCriado
    B->>Pag: Entrega Evento: PedidoCriado
    Pag->>Pag: Debita R$ 500 do Cartão [COMMIT]
    Pag->>B: Publica Evento: PagamentoAprovado
    B->>E: Entrega Evento: PagamentoAprovado
    
    Note over E: ❌ FALHA! Produto Esgotado
    
    Note over P,E: CAMINHO DE VOLTA (Transações Compensatórias)
    E->>B: Publica Evento: FalhaAoReservarEstoque
    B->>Pag: Entrega Evento: FalhaAoReservarEstoque
    Pag->>Pag: 🔄 COMPENSAÇÃO: Estorna R$ 500 [COMMIT]
    Pag->>B: Publica Evento: PagamentoEstornado
    B->>P: Entrega Evento: PagamentoEstornado
    P->>P: 🔄 COMPENSAÇÃO: Atualiza Status = CANCELADO [COMMIT]
```


```mermaid

sequenceDiagram
    autonumber
    participant O as 🎼 Orquestrador da Saga
    participant P as 📦 Serviço de Pedidos
    participant Pag as 💳 Serviço de Pagamento
    participant E as 🏭 Serviço de Estoque

    Note over O,E: CAMINHO DE IDA (Comandos do Maestro)
    O->>P: Comando: CriarPedido()
    P-->>O: Resposta: Pedido #101 Criado (PENDENTE)
    O->>Pag: Comando: CobrarCartao(Pedido #101, R$ 500)
    Pag-->>O: Resposta: Pagamento Confirmado
    O->>E: Comando: ReservarItem(Pedido #101)
    
    Note over E: ❌ FALHA! Produto Esgotado
    E-->>O: Resposta de Erro: EstoqueInsuficiente
    
    Note over O,E: CAMINHO DE VOLTA (Orquestrador aciona Compensações)
    O->>Pag: 🔄 Comando Compensatório: EstornarCartao(Pedido #101)
    Pag-->>O: Resposta: Estorno Concluído
    O->>P: 🔄 Comando Compensatório: CancelarPedido(Pedido #101)
    P-->>O: Resposta: Pedido #101 CANCELADO
```


