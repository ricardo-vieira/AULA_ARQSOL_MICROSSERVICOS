package com.autoleilao.veiculos.command.repository;

import com.autoleilao.veiculos.command.model.VeiculoDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Repositório de Escrita (COMMAND) do CQRS.
 * Persiste documentos no Azure Cosmos DB (database: veiculos_command_db) ou em Document Store em memória no modo local.
 */
@Repository
public class VeiculoCommandRepository {

    private static final Logger log = LoggerFactory.getLogger(VeiculoCommandRepository.class);

    private final MongoTemplate mongoTemplate;
    private final String databaseName;

    private final ConcurrentHashMap<Long, VeiculoDocument> localDocumentStore = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    public VeiculoCommandRepository(
            @Autowired(required = false) MongoTemplate mongoTemplate,
            @Value("${azure.cosmos.database:veiculos_command_db}") String databaseName) {
        this.mongoTemplate = mongoTemplate;
        this.databaseName = databaseName;
    }

    public boolean isCloudCosmosActive() {
        return mongoTemplate != null;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public VeiculoDocument save(VeiculoDocument doc) {
        if (doc.getId() == null) {
            doc.setId(nextId());
        } else {
            idSequence.accumulateAndGet(doc.getId(), Math::max);
        }
        if (isCloudCosmosActive()) {
            log.info("[CQRS-COMMAND -> CosmosDB ({})] Gravando VeiculoDocument ID={} Placa={} (v{})",
                    databaseName, doc.getId(), doc.getPlaca(), doc.getVersion() != null ? doc.getVersion() : 1L);
            return mongoTemplate.save(doc);
        }

        if (doc.getVersion() == null) {
            doc.setVersion(1L);
        } else {
            doc.setVersion(doc.getVersion() + 1);
        }

        log.info("[CQRS-COMMAND -> LocalDocumentStore ({})] Gravando VeiculoDocument ID={} Placa={} (v{})",
                databaseName, doc.getId(), doc.getPlaca(), doc.getVersion());
        localDocumentStore.put(doc.getId(), doc);
        return doc;
    }

    public Optional<VeiculoDocument> findById(Long id) {
        if (isCloudCosmosActive()) {
            Query query = new Query(Criteria.where("id").is(id));
            return Optional.ofNullable(mongoTemplate.findOne(query, VeiculoDocument.class));
        }
        return Optional.ofNullable(localDocumentStore.get(id));
    }

    public boolean existsByPlacaIgnoreCase(String placa) {
        String normalized = placa.toUpperCase().trim();
        if (isCloudCosmosActive()) {
            Query query = new Query(Criteria.where("placa").regex("^" + normalized + "$", "i"));
            return mongoTemplate.exists(query, VeiculoDocument.class);
        }
        return localDocumentStore.values().stream()
                .anyMatch(v -> v.getPlaca() != null && v.getPlaca().equalsIgnoreCase(normalized));
    }

    public boolean existsByPlacaIgnoreCaseAndIdNot(String placa, Long id) {
        String normalized = placa.toUpperCase().trim();
        if (isCloudCosmosActive()) {
            Query query = new Query(Criteria.where("placa").regex("^" + normalized + "$", "i")
                    .and("id").ne(id));
            return mongoTemplate.exists(query, VeiculoDocument.class);
        }
        return localDocumentStore.values().stream()
                .anyMatch(v -> !v.getId().equals(id) && v.getPlaca() != null && v.getPlaca().equalsIgnoreCase(normalized));
    }

    public void deleteById(Long id) {
        if (isCloudCosmosActive()) {
            Query query = new Query(Criteria.where("id").is(id));
            mongoTemplate.remove(query, VeiculoDocument.class);
            return;
        }
        localDocumentStore.remove(id);
    }

    public long count() {
        if (isCloudCosmosActive()) {
            return mongoTemplate.count(new Query(), VeiculoDocument.class);
        }
        return localDocumentStore.size();
    }

    public List<VeiculoDocument> findAll() {
        if (isCloudCosmosActive()) {
            return mongoTemplate.findAll(VeiculoDocument.class);
        }
        List<VeiculoDocument> list = new ArrayList<>(localDocumentStore.values());
        list.sort(Comparator.comparing(VeiculoDocument::getId));
        return list;
    }

    private synchronized Long nextId() {
        if (isCloudCosmosActive()) {
            long currentMax = mongoTemplate.findAll(VeiculoDocument.class).stream()
                    .mapToLong(v -> v.getId() != null ? v.getId() : 0L)
                    .max()
                    .orElse(0L);
            if (idSequence.get() < currentMax) {
                idSequence.set(currentMax);
            }
        }
        return idSequence.incrementAndGet();
    }
}
