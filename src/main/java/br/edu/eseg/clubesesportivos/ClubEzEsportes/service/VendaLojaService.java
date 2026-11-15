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
public class VendaLojaService {

    private final VendaLojaRepository vendaLojaRepository;
    private final ClubeRepository clubeRepository;
    private final ProdutoLojaRepository produtoLojaRepository;

    public VendaLojaService(
            VendaLojaRepository vendaLojaRepository,
            ClubeRepository clubeRepository,
            ProdutoLojaRepository produtoLojaRepository) {
        this.vendaLojaRepository = vendaLojaRepository;
        this.clubeRepository = clubeRepository;
        this.produtoLojaRepository = produtoLojaRepository;
    }

    // Contratos JSON: relacionamentos são enviados por IDs, não objetos JPA.
    public record Entrada(
            String codigoLegado,
            LocalDate dataVenda,
            int quantidade,
            BigDecimal receita,
            Long clubeId,
            Long produtoLojaId
    ) {}

    public record Saida(
            Long id,
            String codigoLegado,
            LocalDate dataVenda,
            int quantidade,
            BigDecimal receita,
            Long clubeId,
            Long produtoLojaId
    ) {}

    private void validar(Entrada dados) {
        if (dados == null) throw new IllegalArgumentException("Dados obrigatórios.");
        if (dados.dataVenda() == null) throw new IllegalArgumentException("dataVenda inválido ou obrigatório.");
        if (dados.quantidade() < 0) throw new IllegalArgumentException("quantidade inválido ou obrigatório.");
        if (dados.receita() != null && dados.receita().signum() < 0) throw new IllegalArgumentException("receita não pode ser negativo.");
        if (dados.clubeId() == null) throw new IllegalArgumentException("clubeId inválido ou obrigatório.");
        if (dados.produtoLojaId() == null) throw new IllegalArgumentException("produtoLojaId inválido ou obrigatório.");
    }

    private void preencher(VendaLoja entidade, Entrada dados) {
        validar(dados);
        entidade.setCodigoLegado(dados.codigoLegado());
        entidade.setDataVenda(dados.dataVenda());
        entidade.setQuantidade(dados.quantidade());
        entidade.setReceita(dados.receita());
        Clube clube = null;
        if (dados.clubeId() != null) {
            clube = clubeRepository.findById(dados.clubeId())
                    .orElseThrow(() -> new NoSuchElementException("Clube não encontrado: " + dados.clubeId()));
        }
        entidade.setClube(clube);
        ProdutoLoja produtoLoja = null;
        if (dados.produtoLojaId() != null) {
            produtoLoja = produtoLojaRepository.findById(dados.produtoLojaId())
                    .orElseThrow(() -> new NoSuchElementException("ProdutoLoja não encontrado: " + dados.produtoLojaId()));
        }
        entidade.setProdutoLoja(produtoLoja);
    }

    private Saida converter(VendaLoja entidade) {
        return new Saida(
                entidade.getId(),
                entidade.getCodigoLegado(),
                entidade.getDataVenda(),
                entidade.getQuantidade(),
                entidade.getReceita(),
                entidade.getClube() == null ? null : entidade.getClube().getId(),
                entidade.getProdutoLoja() == null ? null : entidade.getProdutoLoja().getId()
        );
    }

    @Transactional
    public Saida cadastrar(Entrada dados) {
        VendaLoja entidade = new VendaLoja();
        preencher(entidade, dados);
        return converter(vendaLojaRepository.saveAndFlush(entidade));
    }

    @Transactional(readOnly = true)
    public List<Saida> listarTodos() {
        return vendaLojaRepository.findAll().stream().map(this::converter).toList();
    }

    @Transactional(readOnly = true)
    public Saida buscarPorId(Long id) {
        VendaLoja entidade = vendaLojaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("VendaLoja não encontrado: " + id));
        return converter(entidade);
    }

    @Transactional
    public Saida atualizar(Long id, Entrada dados) {
        VendaLoja entidade = vendaLojaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("VendaLoja não encontrado: " + id));
        preencher(entidade, dados);
        return converter(vendaLojaRepository.saveAndFlush(entidade));
    }

    @Transactional
    public void excluir(Long id) {
        VendaLoja entidade = vendaLojaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("VendaLoja não encontrado: " + id));
        vendaLojaRepository.delete(entidade);
        vendaLojaRepository.flush();
    }
}
