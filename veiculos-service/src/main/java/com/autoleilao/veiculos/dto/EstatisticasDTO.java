package com.autoleilao.veiculos.dto;

import java.math.BigDecimal;
import java.util.Map;

public class EstatisticasDTO {

    private long totalVeiculos;
    private BigDecimal valorTotalMinimo;
    private Map<String, Long> porTipo;
    private Map<String, Long> porMarca;

    public EstatisticasDTO(long totalVeiculos, BigDecimal valorTotalMinimo, Map<String, Long> porTipo, Map<String, Long> porMarca) {
        this.totalVeiculos = totalVeiculos;
        this.valorTotalMinimo = valorTotalMinimo;
        this.porTipo = porTipo;
        this.porMarca = porMarca;
    }

    public long getTotalVeiculos() {
        return totalVeiculos;
    }

    public BigDecimal getValorTotalMinimo() {
        return valorTotalMinimo;
    }

    public Map<String, Long> getPorTipo() {
        return porTipo;
    }

    public Map<String, Long> getPorMarca() {
        return porMarca;
    }
}
