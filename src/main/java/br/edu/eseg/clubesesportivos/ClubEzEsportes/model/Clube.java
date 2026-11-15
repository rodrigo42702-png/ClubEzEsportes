package br.edu.eseg.clubesesportivos.ClubEzEsportes.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "clube")
public class Clube {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_legado", unique = true, length = 40)
    private String codigoLegado;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "cidade")
    private String cidade;

    @Column(name = "estado")
    private String estado;

    @Column(name = "ativo")
    private boolean ativo;

    public Clube() {}

    public void ativar() { this.ativo = true; }
        public void desativar() { this.ativo = false; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoLegado() { return codigoLegado; }
    public void setCodigoLegado(String codigoLegado) { this.codigoLegado = codigoLegado; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

}
