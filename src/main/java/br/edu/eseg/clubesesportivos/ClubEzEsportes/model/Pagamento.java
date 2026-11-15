package br.edu.eseg.clubesesportivos.ClubEzEsportes.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pagamento")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_legado", unique = true, length = 40)
    private String codigoLegado;

    @Column(name = "vencimento")
    private LocalDate vencimento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Column(name = "valor", precision = 12, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPagamento status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "matricula_id", nullable = false)
    private Matricula matricula;

    public Pagamento() {}

    public void registrarPagamento() { this.dataPagamento = LocalDate.now(); this.status = StatusPagamento.PAGO; }
        public boolean verificarAtraso() { return (status == StatusPagamento.PENDENTE || status == StatusPagamento.ATRASADO) && vencimento != null && vencimento.isBefore(LocalDate.now()); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoLegado() { return codigoLegado; }
    public void setCodigoLegado(String codigoLegado) { this.codigoLegado = codigoLegado; }

    public LocalDate getVencimento() { return vencimento; }
    public void setVencimento(LocalDate vencimento) { this.vencimento = vencimento; }

    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public StatusPagamento getStatus() { return status; }
    public void setStatus(StatusPagamento status) { this.status = status; }

    public Matricula getMatricula() { return matricula; }
    public void setMatricula(Matricula matricula) { this.matricula = matricula; }

}
