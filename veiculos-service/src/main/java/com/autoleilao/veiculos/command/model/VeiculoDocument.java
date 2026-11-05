package com.autoleilao.veiculos.command.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Modelo de Escrita (COMMAND) do CQRS — Armazenado como Documento JSON no Azure Cosmos DB (API for MongoDB).
 * Database isolado: veiculos_command_db (Padrão Database per Service).
 */
@Document(collection = "veiculos_command")
public class VeiculoDocument {

    @Id
    private String documentId;

    @Indexed(unique = true)
    private Long id;

    @Indexed(unique = true)
    private String placa;

    private String marca;
    private String modelo;
    private Integer ano;
    private String cor;
    private String tipo;
    private BigDecimal valorMinimo;
    private String descricao;
    private String status = "DISPONIVEL";
    private LocalDateTime dataCadastro;

    @Version
    private Long version;

    public VeiculoDocument() {
        this.dataCadastro = LocalDateTime.now();
    }

    public VeiculoDocument(Long id, String marca, String modelo, Integer ano, String placa,
                           String cor, String tipo, BigDecimal valorMinimo, String descricao, String status) {
        this.id = id;
        this.documentId = String.valueOf(id);
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.placa = placa != null ? placa.toUpperCase().trim() : null;
        this.cor = cor;
        this.tipo = tipo;
        this.valorMinimo = valorMinimo;
        this.descricao = descricao;
        this.status = status != null && !status.isBlank() ? status : "DISPONIVEL";
        this.dataCadastro = LocalDateTime.now();
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

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa != null ? placa.toUpperCase().trim() : null;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
