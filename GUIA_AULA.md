# Plano de Aula: Arquitetura de Microsserviços na Prática

- **Disciplina:** Arquitetura de Soluções
- **Carga Horária Sugerida:** 2 a 3 horas / aula
- **Tema:** Decomposição em Microsserviços, Database-per-Service, API Gateway e Integração com Microfrontends

---

## 🎯 Objetivos de Aprendizagem

Ao final da aula, os alunos serão capazes de:
1. Compreender a transição de monólitos para microsserviços orientados a domínios de negócio (*Bounded Contexts*).
2. Explicar e demonstrar por que cada microsserviço deve possuir seu próprio banco de dados (*Database per Service*).
3. Analisar o papel do **API Gateway** na simplificação da interface para clientes (como Microfrontends) e na aplicação de políticas transversais (CORS, roteamento, resiliência).
4. Demonstrar a comunicação entre sistemas cliente (React/MFE) e serviços de backend via APIs REST padronizadas.
5. Observar na prática os conceitos de **Tolerância a Falhas** e **Degradação Graciosa (Fallback)** quando um serviço sai do ar.

---

## ⏱️ Roteiro da Aula Passo a Passo

### Bloco 1: Introdução Teórica & Contextualização (25 min)
1. **O Problema da Centralização:**
   - Apresente o sistema de leilão de veículos.
   - Discuta por que o cadastro de veículos tem requisitos de escala e frequência de escrita muito diferentes do pregão de lances (que tem picos intensos e exige baixa latência).
2. **Definindo os Bounded Contexts:**
   - *Catálogo de Veículos:* Foco em ciclo de vida, integridade de dados e atributos técnicos do veículo.
   - *Pregão de Leilão:* Foco em temporalidade, regras de incremento mínimo e histórico auditável de lances.
3. **Padrão Database-per-Service:**
   - Por que o `leilao-service` NÃO tem uma Foreign Key tradicional no banco apontando para o `veiculos-service`?
   - Explique o uso de IDs lógicos e a eventual consistência de dados.

---

### Bloco 2: Demonstração Prática dos Microsserviços (35 min)
1. **Iniciando o ecossistema:**
   - Execute `.\start-all.bat` no terminal.
   - Mostre as 3 janelas abrindo: portas 8081 (Veículos), 8082 (Leilão) e 8080 (Gateway).
2. **Explorando os Contratos OpenAPI (Swagger):**
   - Acesse [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html).
   - Execute o `GET /api/veiculos` e mostre o JSON retornado.
   - Execute o `POST /api/veiculos` cadastrando um carro novo (ex: Fiat Pulse, 2023, placa XYZ-9988).
   - Tente cadastrar o mesmo carro novamente com a mesma placa e mostre o erro `400 Bad Request` com a mensagem de violação de regra de negócio.
3. **Explorando o Serviço de Leilão:**
   - Acesse [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html).
   - Execute `GET /api/leiloes/2` (Hilux).
   - Tente dar um lance de R$ 185.100 (lance atual é 185.000, incremento mínimo é R$ 500).
   - Mostre o retorno da validação da regra de negócio exigindo no mínimo R$ 185.500.
   - Envie um lance válido de R$ 186.000 e mostre a lista de lances sendo atualizada em tempo real!

---

### Bloco 3: Integração Direta MFE ↔ Microsserviços REST (20 min)
1. **Comunicação Direta por Domínio:**
   - Mostre como cada Microfrontend consome diretamente a API REST do seu domínio:
     - `mfe-cadastro` (:3001) consome `http://localhost:8081/api/veiculos`
     - `mfe-leilao` (:3002) consome `http://localhost:8082/api/leiloes`
   - Discuta as vantagens da autonomia entre equipes de frontend e backend em cada domínio.
2. **CORS Descentralizado:**
   - Mostre os arquivos `CorsConfig.java` configurados em cada microsserviço permitindo as origens dos MFEs.

---

### Bloco 4: Conectando com os Microfrontends (AULA_ARQSOL_MFE) (30 min)
1. **Inicie o Frontend:**
   - Na pasta `AULA_ARQSOL_MFE`, execute `npm start`.
   - Acesse o Shell em `http://localhost:3000`.
2. **Demonstração de Integração:**
   - Abra a aba **Cadastro de Veículos**: aponte o chip no canto superior direito:
     `Microsserviço REST :8081 Online`.
   - Cadastre um novo veículo pelo formulário: mostre a notificação de confirmação e a linha inserida na tabela vinda diretamente do Spring Boot!
   - Abra a aba **Leilão ao Vivo**: veja os lotes carregados da API.
   - Dê um lance na Hilux com o nome de um aluno: veja o lance sendo computado no backend e a listagem sendo atualizada.

---

### Bloco 5: Teste de Resiliência ao Vivo (O "Momento Uau" da Aula) (20 min)
1. **Simulando Queda de Serviço:**
   - Feche a janela do terminal do `veiculos-service` (porta 8081).
2. **Isolamento de Falha:**
   - Mostre que o `leilao-service` CONTINUA FUNCIONANDO 100%! Essa é a prova viva da vantagem dos microsserviços: a falha em um domínio não derruba o outro.
3. **Comportamento do Microfrontend:**
   - No frontend, mostre o chip mudando para `Modo Fallback Offline`. A interface não quebra e continua navegável.
4. **Recuperação:**
   - Suba o serviço novamente com `.\mvnw.bat spring-boot:run -pl veiculos-service`.
   - Dê F5 no frontend e veja a reconexão automática e o chip verde retornando.

---

## 💬 Perguntas para Discussão com os Alunos

1. *"Se o leilao-service precisa de dados do veículo, por que não fazer um JOIN no banco do veiculos-service?"*
   - **Gabarito para o professor:** Porque violaria o princípio de desacoplamento. Se o schema do banco de veículos mudar, o serviço de leilão quebraria. Microsserviços devem se comunicar via contratos de API ou mensageria/eventos, nunca por banco compartilhado.

2. *"O que acontece se o API Gateway cair?"*
   - **Gabarito para o professor:** Ele se torna um ponto único de falha (*Single Point of Failure - SPOF*). Em produção corporativa, o Gateway roda com múltiplas instâncias atrás de um Load Balancer (ex: AWS ALB, NGINX ou Kubernetes Ingress).

3. *"Qual a relação entre Microfrontends (MFE) e Microsserviços (Backend)?"*
   - **Gabarito para o professor:** Ambos seguem a mesma filosofia arquitetural: dividir sistemas grandes e monolíticos em módulos menores, autônomos, de deploy independente e alinhados a domínios de negócio.
