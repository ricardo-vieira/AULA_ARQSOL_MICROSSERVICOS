package com.autoleilao.leilao.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CriarLoteDTO {

    private Long veiculoId;

    @NotBlank(message = "A marca é obrigatória")
    private String marca;

    @NotBlank(message = "O modelo é obrigatório")
    private String modelo;

    @NotNull(message = "O ano é obrigatório")
    private Integer ano;

    @NotBlank(message = "A placa é obrigatória")
    private String placa;

    @NotBlank(message = "A cor é obrigatória")
    private String cor;

    @NotBlank(message = "O tipo é obrigatório")
    private String tipo;

    @NotNull(message = "O valor mínimo é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor mínimo deve ser maior que zero")
    private BigDecimal valorMinimo;

    private String descricao;
    private Long tempoRestante = 3600L; // default 1h

    public CriarLoteDTO() {}

    // Getters and Setters
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

    public Long getTempoRestante() {
        return tempoRestante;
    }

    public void setTempoRestante(Long tempoRestante) {
        this.tempoRestante = tempoRestante;
    }
}
