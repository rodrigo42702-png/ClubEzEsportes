package br.edu.eseg.clubesesportivos.ClubEzEsportes.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "participacao_equipe")
public class ParticipacaoEquipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_entrada", nullable = false)
    private LocalDate dataEntrada;

    @Column(name = "data_saida")
    private LocalDate dataSaida;

    @Column(name = "ativa")
    private boolean ativa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipe_id", nullable = false)
    private Equipe equipe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inscricao_modalidade_id", nullable = false)
    private InscricaoModalidade inscricaoModalidade;

    public ParticipacaoEquipe() {}

    public void encerrarParticipacao() { this.ativa = false; this.dataSaida = LocalDate.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDataEntrada() { return dataEntrada; }
    public void setDataEntrada(LocalDate dataEntrada) { this.dataEntrada = dataEntrada; }

    public LocalDate getDataSaida() { return dataSaida; }
    public void setDataSaida(LocalDate dataSaida) { this.dataSaida = dataSaida; }

    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }

    public Equipe getEquipe() { return equipe; }
    public void setEquipe(Equipe equipe) { this.equipe = equipe; }

    public InscricaoModalidade getInscricaoModalidade() { return inscricaoModalidade; }
    public void setInscricaoModalidade(InscricaoModalidade inscricaoModalidade) { this.inscricaoModalidade = inscricaoModalidade; }

}
