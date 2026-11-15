package br.edu.eseg.clubesesportivos.ClubEzEsportes.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "plano")
public class Plano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_legado", unique = true, length = 40)
    private String codigoLegado;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "valor_mensalidade", precision = 12, scale = 2, nullable = false)
    private BigDecimal valorMensalidade;

    @Column(name = "ativo")
    private boolean ativo;

    public Plano() {}

    public void atualizarValor(BigDecimal valor) { if (valor == null || valor.signum() < 0) throw new IllegalArgumentException("Valor inválido"); this.valorMensalidade = valor; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoLegado() { return codigoLegado; }
    public void setCodigoLegado(String codigoLegado) { this.codigoLegado = codigoLegado; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public BigDecimal getValorMensalidade() { return valorMensalidade; }
    public void setValorMensalidade(BigDecimal valorMensalidade) { this.valorMensalidade = valorMensalidade; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

}
