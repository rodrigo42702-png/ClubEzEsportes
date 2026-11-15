package br.edu.eseg.clubesesportivos.ClubEzEsportes.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "inscricao_modalidade")
public class InscricaoModalidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_inscricao", nullable = false)
    private LocalDate dataInscricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusInscricao status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "socio_id", nullable = false)
    private Socio socio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clube_modalidade_id", nullable = false)
    private ClubeModalidade clubeModalidade;

    public InscricaoModalidade() {}

    public void cancelar() { this.status = StatusInscricao.CANCELADA; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDataInscricao() { return dataInscricao; }
    public void setDataInscricao(LocalDate dataInscricao) { this.dataInscricao = dataInscricao; }

    public StatusInscricao getStatus() { return status; }
    public void setStatus(StatusInscricao status) { this.status = status; }

    public Socio getSocio() { return socio; }
    public void setSocio(Socio socio) { this.socio = socio; }

    public ClubeModalidade getClubeModalidade() { return clubeModalidade; }
    public void setClubeModalidade(ClubeModalidade clubeModalidade) { this.clubeModalidade = clubeModalidade; }

}
