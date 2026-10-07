package com.autoleilao.veiculos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "veiculos")
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String marca;

    @Column(nullable = false, length = 100)
    private String modelo;

    @Column(nullable = false)
    private Integer ano;

    @Column(nullable = false, length = 10, unique = true)
    private String placa;

    @Column(nullable = false, length = 40)
    private String cor;

    @Column(nullable = false, length = 40)
    private String tipo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorMinimo;

    @Column(length = 1000)
    private String descricao;

    @Column(nullable = false, length = 20)
    private String status = "DISPONIVEL";

    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro;

    public Veiculo() {
        this.dataCadastro = LocalDateTime.now();
    }

    public Veiculo(String marca, String modelo, Integer ano, String placa, String cor, String tipo, BigDecimal valorMinimo, String descricao) {
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.placa = placa;
        this.cor = cor;
        this.tipo = tipo;
        this.valorMinimo = valorMinimo;
        this.descricao = descricao;
        this.status = "DISPONIVEL";
        this.dataCadastro = LocalDateTime.now();
    }

    // Getters and Setters
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

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
}
