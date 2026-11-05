package com.autoleilao.leilao.command.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Subdocumento de Lance embutido diretamente dentro do LoteLeilaoDocument (Aggregate Root no CosmosDB).
 * Evita JOINs e locks em múltiplas tabelas durante picos de lances em tempo real.
 */
public class LanceEmbedded {

    private Long id;
    private String bidder;
    private BigDecimal valor;
    private LocalDateTime dataHora;
    private String time;

    public LanceEmbedded() {
        this.dataHora = LocalDateTime.now();
        this.time = this.dataHora.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public LanceEmbedded(Long id, String bidder, BigDecimal valor, String customTime) {
        this.id = id;
        this.bidder = bidder;
        this.valor = valor;
        this.dataHora = LocalDateTime.now();
        this.time = customTime != null ? customTime : this.dataHora.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBidder() {
        return bidder;
    }

    public void setBidder(String bidder) {
        this.bidder = bidder;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
