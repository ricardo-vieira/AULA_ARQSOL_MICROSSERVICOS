package com.autoleilao.leilao.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class NovoLanceDTO {

    @NotBlank(message = "O nome do licitante é obrigatório")
    private String bidder;

    @NotNull(message = "O valor do lance é obrigatório")
    @DecimalMin(value = "0.01", message = "O lance deve ser maior que zero")
    private BigDecimal valor;

    public NovoLanceDTO() {}

    public NovoLanceDTO(String bidder, BigDecimal valor) {
        this.bidder = bidder;
        this.valor = valor;
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
}
