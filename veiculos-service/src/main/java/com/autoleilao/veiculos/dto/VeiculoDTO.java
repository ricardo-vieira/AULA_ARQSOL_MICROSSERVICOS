package com.autoleilao.veiculos.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class VeiculoDTO {

    private Long id;

    @NotBlank(message = "A marca é obrigatória")
    private String marca;

    @NotBlank(message = "O modelo é obrigatório")
    private String modelo;

    @NotNull(message = "O ano é obrigatório")
    @Min(value = 1900, message = "Ano deve ser maior ou igual a 1900")
    @Max(value = 2030, message = "Ano inválido")
    private Integer ano;

    @NotBlank(message = "A placa é obrigatória")
    private String placa;

    @NotBlank(message = "A cor é obrigatória")
    private String cor;

    @NotBlank(message = "O tipo de veículo é obrigatório")
    private String tipo;

    @NotNull(message = "O valor mínimo é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor mínimo deve ser maior que zero")
    private BigDecimal valorMinimo;

    private String descricao;
    private String status;

    public VeiculoDTO() {}

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
        this.placa = placa != null ? placa.toUpperCase().trim() : null;
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
}
