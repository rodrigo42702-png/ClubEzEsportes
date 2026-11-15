package br.edu.eseg.clubesesportivos.ClubEzEsportes.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "venda_loja")
public class VendaLoja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_legado", unique = true, length = 40)
    private String codigoLegado;

    @Column(name = "data_venda", nullable = false)
    private LocalDate dataVenda;

    @Column(name = "quantidade", nullable = false)
    private int quantidade;

    @Column(name = "receita", precision = 12, scale = 2)
    private BigDecimal receita;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clube_id", nullable = false)
    private Clube clube;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_loja_id", nullable = false)
    private ProdutoLoja produtoLoja;

    public VendaLoja() {}

    public BigDecimal calcularReceita() { return receita; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoLegado() { return codigoLegado; }
    public void setCodigoLegado(String codigoLegado) { this.codigoLegado = codigoLegado; }

    public LocalDate getDataVenda() { return dataVenda; }
    public void setDataVenda(LocalDate dataVenda) { this.dataVenda = dataVenda; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public BigDecimal getReceita() { return receita; }
    public void setReceita(BigDecimal receita) { this.receita = receita; }

    public Clube getClube() { return clube; }
    public void setClube(Clube clube) { this.clube = clube; }

    public ProdutoLoja getProdutoLoja() { return produtoLoja; }
    public void setProdutoLoja(ProdutoLoja produtoLoja) { this.produtoLoja = produtoLoja; }

}
