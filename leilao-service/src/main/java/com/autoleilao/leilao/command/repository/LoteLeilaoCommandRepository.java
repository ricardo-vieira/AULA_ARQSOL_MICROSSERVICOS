package com.autoleilao.leilao.command.repository;

import com.autoleilao.leilao.command.model.LoteLeilaoDocument;
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

@Repository
public class LoteLeilaoCommandRepository {

    private static final Logger log = LoggerFactory.getLogger(LoteLeilaoCommandRepository.class);

    private final MongoTemplate mongoTemplate;
    private final String databaseName;

    private final ConcurrentHashMap<Long, LoteLeilaoDocument> localDocumentStore = new ConcurrentHashMap<>();
    private final AtomicLong loteIdSequence = new AtomicLong(0);
    private final AtomicLong lanceIdSequence = new AtomicLong(0);

    public LoteLeilaoCommandRepository(
            @Autowired(required = false) MongoTemplate mongoTemplate,
            @Value("${azure.cosmos.database:leilao_command_db}") String databaseName) {
        this.mongoTemplate = mongoTemplate;
        this.databaseName = databaseName;
    }

    public boolean isCloudCosmosActive() {
        return mongoTemplate != null;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public Long nextLanceId() {
        return lanceIdSequence.incrementAndGet();
    }

    public LoteLeilaoDocument save(LoteLeilaoDocument doc) {
        if (doc.getId() == null) {
            doc.setId(nextLoteId());
        } else {
            loteIdSequence.accumulateAndGet(doc.getId(), Math::max);
        }
        if (isCloudCosmosActive()) {
            log.info("[CQRS-COMMAND -> CosmosDB ({})] Gravando Aggregate LoteLeilaoDocument ID={} LanceAtual={} TotalLances={} (v{})",
                    databaseName, doc.getId(), doc.getLanceAtual(), doc.getTotalLances(), doc.getVersion() != null ? doc.getVersion() : 1L);
            return mongoTemplate.save(doc);
        }

        if (doc.getVersion() == null) {
            doc.setVersion(1L);
        } else {
            doc.setVersion(doc.getVersion() + 1);
        }

        log.info("[CQRS-COMMAND -> LocalDocumentStore ({})] Gravando Aggregate LoteLeilaoDocument ID={} LanceAtual={} TotalLances={} (v{})",
                databaseName, doc.getId(), doc.getLanceAtual(), doc.getTotalLances(), doc.getVersion());
        localDocumentStore.put(doc.getId(), doc);
        return doc;
    }

    public Optional<LoteLeilaoDocument> findById(Long id) {
        if (isCloudCosmosActive()) {
            Query query = new Query(Criteria.where("id").is(id));
            return Optional.ofNullable(mongoTemplate.findOne(query, LoteLeilaoDocument.class));
        }
        return Optional.ofNullable(localDocumentStore.get(id));
    }

    public boolean existsByPlacaIgnoreCase(String placa) {
        if (placa == null) return false;
        String normalized = placa.trim();
        if (isCloudCosmosActive()) {
            Query query = new Query(Criteria.where("placa").regex("^" + normalized + "$", "i"));
            return mongoTemplate.exists(query, LoteLeilaoDocument.class);
        }
        return localDocumentStore.values().stream()
                .anyMatch(l -> l.getPlaca() != null && l.getPlaca().equalsIgnoreCase(normalized));
    }

    public long count() {
        if (isCloudCosmosActive()) {
            return mongoTemplate.count(new Query(), LoteLeilaoDocument.class);
        }
        return localDocumentStore.size();
    }

    public List<LoteLeilaoDocument> findAll() {
        if (isCloudCosmosActive()) {
            return mongoTemplate.findAll(LoteLeilaoDocument.class);
        }
        List<LoteLeilaoDocument> list = new ArrayList<>(localDocumentStore.values());
        list.sort(Comparator.comparing(LoteLeilaoDocument::getId));
        return list;
    }

    private synchronized Long nextLoteId() {
        if (isCloudCosmosActive()) {
            long currentMax = mongoTemplate.findAll(LoteLeilaoDocument.class).stream()
                    .mapToLong(l -> l.getId() != null ? l.getId() : 0L)
                    .max()
                    .orElse(0L);
            if (loteIdSequence.get() < currentMax) {
                loteIdSequence.set(currentMax);
            }
        }
        return loteIdSequence.incrementAndGet();
    }
}
