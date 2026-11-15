package br.edu.eseg.clubesesportivos.ClubEzEsportes.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "modalidade")
public class Modalidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_legado", unique = true, length = 40)
    private String codigoLegado;

    @Column(name = "nome", unique = true, nullable = false, length = 150)
    private String nome;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "ativa")
    private boolean ativa;

    public Modalidade() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoLegado() { return codigoLegado; }
    public void setCodigoLegado(String codigoLegado) { this.codigoLegado = codigoLegado; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }

}
