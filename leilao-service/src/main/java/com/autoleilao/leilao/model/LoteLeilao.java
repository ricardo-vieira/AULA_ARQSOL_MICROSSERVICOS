package com.autoleilao.leilao.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lotes_leilao")
public class LoteLeilao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referência lógica ao ID do veículo no microsserviço de veículos
    @Column(name = "veiculo_id")
    private Long veiculoId;

    @Column(nullable = false, length = 60)
    private String marca;

    @Column(nullable = false, length = 100)
    private String modelo;

    @Column(nullable = false)
    private Integer ano;

    @Column(nullable = false, length = 10)
    private String placa;

    @Column(nullable = false, length = 40)
    private String cor;

    @Column(nullable = false, length = 40)
    private String tipo;

    @Column(name = "valor_minimo", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorMinimo;

    @Column(length = 1000)
    private String descricao;

    @Column(name = "lance_atual", nullable = false, precision = 12, scale = 2)
    private BigDecimal lanceAtual;

    @Column(name = "total_lances", nullable = false)
    private Integer totalLances = 0;

    @Column(name = "tempo_restante", nullable = false)
    private Long tempoRestante; // em segundos

    @Column(nullable = false, length = 20)
    private String status = "ativo"; // 'ativo' ou 'encerrado'

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @OneToMany(mappedBy = "lote", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("dataHora DESC")
    private List<Lance> bids = new ArrayList<>();

    public LoteLeilao() {
        this.dataCriacao = LocalDateTime.now();
        this.totalLances = 0;
        this.status = "ativo";
    }

    public LoteLeilao(Long veiculoId, String marca, String modelo, Integer ano, String placa, String cor,
                      String tipo, BigDecimal valorMinimo, String descricao, Long tempoRestante) {
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
        this.tempoRestante = tempoRestante;
        this.status = "ativo";
        this.dataCriacao = LocalDateTime.now();
    }

    // Getters and Setters
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

    public List<Lance> getBids() {
        return bids;
    }

    public void setBids(List<Lance> bids) {
        this.bids = bids;
    }

    public void addLance(Lance lance) {
        this.bids.add(0, lance);
        lance.setLote(this);
        this.lanceAtual = lance.getValor();
        this.totalLances = this.bids.size();
    }
}
