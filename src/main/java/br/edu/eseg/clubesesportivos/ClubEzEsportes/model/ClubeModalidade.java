package br.edu.eseg.clubesesportivos.ClubEzEsportes.model;

import jakarta.persistence.*;

@Entity
@Table(name = "clube_modalidade", uniqueConstraints = {@UniqueConstraint(name = "uk_clube_modalidade", columnNames = {"clube_id", "modalidade_id"})})
public class ClubeModalidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "capacidade_maxima", nullable = false)
    private int capacidadeMaxima;

    @Column(name = "ativa")
    private boolean ativa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clube_id", nullable = false)
    private Clube clube;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modalidade_id", nullable = false)
    private Modalidade modalidade;

    public ClubeModalidade() {}

    public boolean verificarVagas() { return ativa && capacidadeMaxima > 0; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getCapacidadeMaxima() { return capacidadeMaxima; }
    public void setCapacidadeMaxima(int capacidadeMaxima) { this.capacidadeMaxima = capacidadeMaxima; }

    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }

    public Clube getClube() { return clube; }
    public void setClube(Clube clube) { this.clube = clube; }

    public Modalidade getModalidade() { return modalidade; }
    public void setModalidade(Modalidade modalidade) { this.modalidade = modalidade; }

}
