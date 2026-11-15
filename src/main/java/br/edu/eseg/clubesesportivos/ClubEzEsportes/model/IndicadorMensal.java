package br.edu.eseg.clubesesportivos.ClubEzEsportes.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "indicador_mensal", uniqueConstraints = {@UniqueConstraint(name = "uk_indicador_clube_competencia", columnNames = {"clube_id", "competencia"})})
public class IndicadorMensal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "competencia", nullable = false)
    private LocalDate competencia;

    @Column(name = "socios_ativos")
    private int sociosAtivos;

    @Column(name = "receita_mensal", precision = 12, scale = 2)
    private BigDecimal receitaMensal;

    @Column(name = "churn_percentual", precision = 12, scale = 2)
    private BigDecimal churnPercentual;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clube_id", nullable = false)
    private Clube clube;

    public IndicadorMensal() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getCompetencia() { return competencia; }
    public void setCompetencia(LocalDate competencia) { this.competencia = competencia; }

    public int getSociosAtivos() { return sociosAtivos; }
    public void setSociosAtivos(int sociosAtivos) { this.sociosAtivos = sociosAtivos; }

    public BigDecimal getReceitaMensal() { return receitaMensal; }
    public void setReceitaMensal(BigDecimal receitaMensal) { this.receitaMensal = receitaMensal; }

    public BigDecimal getChurnPercentual() { return churnPercentual; }
    public void setChurnPercentual(BigDecimal churnPercentual) { this.churnPercentual = churnPercentual; }

    public Clube getClube() { return clube; }
    public void setClube(Clube clube) { this.clube = clube; }

}
