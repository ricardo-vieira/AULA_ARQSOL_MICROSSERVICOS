package com.autoleilao.leilao.command.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate Root de Escrita (COMMAND) do CQRS — Armazenado como um único Documento JSON
 * no Azure Cosmos DB (database exclusivo: leilao_command_db).
 * Utiliza @Version (Optimistic Locking) para garantir atomicidade e impedir Race Conditions
 * quando múltiplos licitantes enviam lances no mesmo milissegundo.
 */
@Document(collection = "lotes_leilao_command")
public class LoteLeilaoDocument {

    @Id
    private String documentId;

    @Indexed(unique = true)
    private Long id;

    private Long veiculoId;
    private String marca;
    private String modelo;
    private Integer ano;
    private String placa;
    private String cor;
    private String tipo;
    private BigDecimal valorMinimo;
    private String descricao;
    private BigDecimal lanceAtual;
    private Integer totalLances = 0;
    private Long tempoRestante;
    private String status = "ativo";
    private LocalDateTime dataCriacao;

    private List<LanceEmbedded> bids = new ArrayList<>();

    @Version
    private Long version;

    public LoteLeilaoDocument() {
        this.dataCriacao = LocalDateTime.now();
        this.totalLances = 0;
        this.status = "ativo";
    }

    public LoteLeilaoDocument(Long id, Long veiculoId, String marca, String modelo, Integer ano,
                              String placa, String cor, String tipo, BigDecimal valorMinimo,
                              String descricao, Long tempoRestante) {
        this.id = id;
        if (id != null) {
            this.documentId = String.valueOf(id);
        }
        this.veiculoId = veiculoId;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.placa = placa;
        this.cor = cor;
        this.tipo = tipo;
        this.valorMinimo = valorMinimo;
        this.descricao = descricao;
        this.lanceAtual = valorMinimo;
        this.totalLances = 0;
        this.tempoRestante = tempoRestante != null ? tempoRestante : 3600L;
        this.status = "ativo";
        this.dataCriacao = LocalDateTime.now();
    }

    public void addLance(LanceEmbedded lance) {
        this.bids.add(0, lance);
        this.lanceAtual = lance.getValor();
        this.totalLances = this.bids.size();
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
        if (id != null) {
            this.documentId = String.valueOf(id);
        }
    }

    public Long getVeiculoId() {
        return veiculoId;
    }

    public void setVeiculoId(Long veiculoId) {
        this.veiculoId = veiculoId;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getValorMinimo() {
        return valorMinimo;
    }

    public void setValorMinimo(BigDecimal valorMinimo) {
        this.valorMinimo = valorMinimo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getLanceAtual() {
        return lanceAtual;
    }

    public void setLanceAtual(BigDecimal lanceAtual) {
        this.lanceAtual = lanceAtual;
    }

    public Integer getTotalLances() {
        return totalLances;
    }

    public void setTotalLances(Integer totalLances) {
        this.totalLances = totalLances;
    }

    public Long getTempoRestante() {
        return tempoRestante;
    }

    public void setTempoRestante(Long tempoRestante) {
        this.tempoRestante = tempoRestante;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public List<LanceEmbedded> getBids() {
        return bids;
    }

    public void setBids(List<LanceEmbedded> bids) {
        this.bids = bids;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
