package br.edu.eseg.clubesesportivos.ClubEzEsportes.service;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.*;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class ProdutoLojaService {

    private final ProdutoLojaRepository produtoLojaRepository;
    private final VendaLojaRepository vendaLojaRepository;

    public ProdutoLojaService(
            ProdutoLojaRepository produtoLojaRepository,
            VendaLojaRepository vendaLojaRepository) {
        this.produtoLojaRepository = produtoLojaRepository;
        this.vendaLojaRepository = vendaLojaRepository;
    }

    // Contratos JSON: relacionamentos são enviados por IDs, não objetos JPA.
    public record Entrada(
            String codigoLegado,
            String nome,
            String categoria,
            BigDecimal precoUnitario
    ) {}

    public record Saida(
            Long id,
            String codigoLegado,
            String nome,
            String categoria,
            BigDecimal precoUnitario
    ) {}

    private void validar(Entrada dados) {
        if (dados == null) throw new IllegalArgumentException("Dados obrigatórios.");
        if (dados.nome() == null || dados.nome().isBlank()) throw new IllegalArgumentException("nome inválido ou obrigatório.");
        if (dados.precoUnitario() == null) throw new IllegalArgumentException("precoUnitario inválido ou obrigatório.");
        if (dados.precoUnitario() != null && dados.precoUnitario().signum() < 0) throw new IllegalArgumentException("precoUnitario não pode ser negativo.");
    }

    private void preencher(ProdutoLoja entidade, Entrada dados) {
        validar(dados);
        entidade.setCodigoLegado(dados.codigoLegado());
        entidade.setNome(dados.nome());
        entidade.setCategoria(dados.categoria());
        entidade.setPrecoUnitario(dados.precoUnitario());
    }

    private Saida converter(ProdutoLoja entidade) {
        return new Saida(
                entidade.getId(),
                entidade.getCodigoLegado(),
                entidade.getNome(),
                entidade.getCategoria(),
                entidade.getPrecoUnitario()
        );
    }

    @Transactional
    public Saida cadastrar(Entrada dados) {
        ProdutoLoja entidade = new ProdutoLoja();
        preencher(entidade, dados);
        return converter(produtoLojaRepository.saveAndFlush(entidade));
    }

    @Transactional(readOnly = true)
    public List<Saida> listarTodos() {
        return produtoLojaRepository.findAll().stream().map(this::converter).toList();
    }

    @Transactional(readOnly = true)
    public Saida buscarPorId(Long id) {
        ProdutoLoja entidade = produtoLojaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("ProdutoLoja não encontrado: " + id));
        return converter(entidade);
    }

    @Transactional
    public Saida atualizar(Long id, Entrada dados) {
        ProdutoLoja entidade = produtoLojaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("ProdutoLoja não encontrado: " + id));
        if (!java.util.Objects.equals(entidade.getCodigoLegado(), dados.codigoLegado())) {
            throw new IllegalArgumentException("O codigo legado do produto nao pode ser alterado.");
        }
        preencher(entidade, dados);
        return converter(produtoLojaRepository.saveAndFlush(entidade));
    }

    @Transactional
    public void excluir(Long id) {
        ProdutoLoja entidade = produtoLojaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("ProdutoLoja não encontrado: " + id));
        if (vendaLojaRepository.existsByProdutoLoja_Id(id)) {
            throw new IllegalArgumentException("Produto possui vendas registradas e nao pode ser excluido.");
        }
        produtoLojaRepository.delete(entidade);
        produtoLojaRepository.flush();
    }
}
