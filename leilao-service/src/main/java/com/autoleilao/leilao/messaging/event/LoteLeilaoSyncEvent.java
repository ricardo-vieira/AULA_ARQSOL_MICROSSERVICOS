package com.autoleilao.leilao.messaging.event;

import com.autoleilao.leilao.command.model.LanceEmbedded;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Evento de Sincronização CQRS do Leilão publicado na fila 'leilao-cqrs-queue' do Azure Service Bus
 * para atualizar as tabelas relacionais 'lotes_leilao_query' e 'lances_query' no Azure SQL Server.
 */
public class LoteLeilaoSyncEvent {

    private String tipoEvento; // LOTE_CRIADO, LANCE_REGISTRADO, LOTE_ENCERRADO
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
    private Integer totalLances;
    private Long tempoRestante;
    private String status;
    private LocalDateTime dataCriacao;
    private List<LanceEmbedded> bids = new ArrayList<>();

    public LoteLeilaoSyncEvent() {}

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
