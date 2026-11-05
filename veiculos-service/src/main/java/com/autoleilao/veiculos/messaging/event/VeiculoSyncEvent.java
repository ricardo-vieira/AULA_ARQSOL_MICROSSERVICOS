package com.autoleilao.veiculos.messaging.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento de Domínio / Sincronização CQRS publicado no Azure Service Bus
 * quando um comando (CRIADO, ATUALIZADO, EXCLUIDO) ocorre no banco CosmosDB.
 */
public class VeiculoSyncEvent {

    private String tipoEvento; // CRIADO, ATUALIZADO, EXCLUIDO
    private Long id;
    private String marca;
    private String modelo;
    private Integer ano;
    private String placa;
    private String cor;
    private String tipo;
    private BigDecimal valorMinimo;
    private String descricao;
    private String status;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataEvento;

    public VeiculoSyncEvent() {
        this.dataEvento = LocalDateTime.now();
    }

    public VeiculoSyncEvent(String tipoEvento, Long id, String marca, String modelo, Integer ano,
                            String placa, String cor, String tipo, BigDecimal valorMinimo,
                            String descricao, String status, LocalDateTime dataCadastro) {
        this.tipoEvento = tipoEvento;
        this.id = id;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.placa = placa;
        this.cor = cor;
        this.tipo = tipo;
        this.valorMinimo = valorMinimo;
        this.descricao = descricao;
        this.status = status;
        this.dataCadastro = dataCadastro;
        this.dataEvento = LocalDateTime.now();
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public LocalDateTime getDataEvento() {
        return dataEvento;
    }

    public void setDataEvento(LocalDateTime dataEvento) {
        this.dataEvento = dataEvento;
    }
}
