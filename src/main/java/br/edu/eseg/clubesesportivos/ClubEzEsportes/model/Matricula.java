package br.edu.eseg.clubesesportivos.ClubEzEsportes.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "matricula")
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_legado", unique = true, length = 40)
    private String codigoLegado;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(name = "valor_contratado", precision = 12, scale = 2)
    private BigDecimal valorContratado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusMatricula status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clube_id", nullable = false)
    private Clube clube;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "socio_id", nullable = false)
    private Socio socio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plano_id", nullable = false)
    private Plano plano;

    public Matricula() {}

    public void encerrar() { this.status = StatusMatricula.ENCERRADA; if (this.dataFim == null) this.dataFim = LocalDate.now(); }
        public boolean verificarVigencia() { return status == StatusMatricula.ATIVA && !dataInicio.isAfter(LocalDate.now()) && (dataFim == null || !dataFim.isBefore(LocalDate.now())); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoLegado() { return codigoLegado; }
    public void setCodigoLegado(String codigoLegado) { this.codigoLegado = codigoLegado; }

    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }

    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }

    public BigDecimal getValorContratado() { return valorContratado; }
    public void setValorContratado(BigDecimal valorContratado) { this.valorContratado = valorContratado; }

    public StatusMatricula getStatus() { return status; }
    public void setStatus(StatusMatricula status) { this.status = status; }

    public Clube getClube() { return clube; }
    public void setClube(Clube clube) { this.clube = clube; }

    public Socio getSocio() { return socio; }
    public void setSocio(Socio socio) { this.socio = socio; }

    public Plano getPlano() { return plano; }
    public void setPlano(Plano plano) { this.plano = plano; }

}
