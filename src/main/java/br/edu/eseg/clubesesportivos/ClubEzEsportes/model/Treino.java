package br.edu.eseg.clubesesportivos.ClubEzEsportes.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "treino")
public class Treino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_legado", unique = true, length = 40)
    private String codigoLegado;

    @Column(name = "clube_legado_codigo")
    private String clubeLegadoCodigo;

    @Column(name = "modalidade_legado_codigo")
    private String modalidadeLegadoCodigo;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fim")
    private LocalTime horaFim;

    @Column(name = "local")
    private String local;

    @Column(name = "cancelado")
    private boolean cancelado;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "equipe_id", nullable = true)
    private Equipe equipe;

    public Treino() {}

    public void cancelar() { this.cancelado = true; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoLegado() { return codigoLegado; }
    public void setCodigoLegado(String codigoLegado) { this.codigoLegado = codigoLegado; }

    public String getClubeLegadoCodigo() { return clubeLegadoCodigo; }
    public void setClubeLegadoCodigo(String clubeLegadoCodigo) { this.clubeLegadoCodigo = clubeLegadoCodigo; }

    public String getModalidadeLegadoCodigo() { return modalidadeLegadoCodigo; }
    public void setModalidadeLegadoCodigo(String modalidadeLegadoCodigo) { this.modalidadeLegadoCodigo = modalidadeLegadoCodigo; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }

    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }

    public boolean isCancelado() { return cancelado; }
    public void setCancelado(boolean cancelado) { this.cancelado = cancelado; }

    public Equipe getEquipe() { return equipe; }
    public void setEquipe(Equipe equipe) { this.equipe = equipe; }

}
