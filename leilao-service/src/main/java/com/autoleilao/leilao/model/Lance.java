package com.autoleilao.leilao.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "lances")
public class Lance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String bidder;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(length = 10)
    private String time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id")
    @JsonIgnore
    private LoteLeilao lote;

    public Lance() {
        this.dataHora = LocalDateTime.now();
        this.time = this.dataHora.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public Lance(String bidder, BigDecimal valor, LoteLeilao lote) {
        this.bidder = bidder;
        this.valor = valor;
        this.lote = lote;
        this.dataHora = LocalDateTime.now();
        this.time = this.dataHora.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public Lance(String bidder, BigDecimal valor, LoteLeilao lote, String customTime) {
        this.bidder = bidder;
        this.valor = valor;
        this.lote = lote;
        this.dataHora = LocalDateTime.now();
        this.time = customTime;
    }

    // Getters and Setters
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

    public LoteLeilao getLote() {
        return lote;
    }

    public void setLote(LoteLeilao lote) {
        this.lote = lote;
    }
}
