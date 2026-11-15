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
public class IndicadorMensalService {

    private final IndicadorMensalRepository indicadorMensalRepository;
    private final ClubeRepository clubeRepository;

    public IndicadorMensalService(
            IndicadorMensalRepository indicadorMensalRepository,
            ClubeRepository clubeRepository) {
        this.indicadorMensalRepository = indicadorMensalRepository;
        this.clubeRepository = clubeRepository;
    }

    // Contratos JSON: relacionamentos são enviados por IDs, não objetos JPA.
    public record Entrada(
            LocalDate competencia,
            int sociosAtivos,
            BigDecimal receitaMensal,
            BigDecimal churnPercentual,
            Long clubeId
    ) {}

    public record Saida(
            Long id,
            LocalDate competencia,
            int sociosAtivos,
            BigDecimal receitaMensal,
            BigDecimal churnPercentual,
            Long clubeId
    ) {}

    private void validar(Entrada dados) {
        if (dados == null) throw new IllegalArgumentException("Dados obrigatórios.");
        if (dados.competencia() == null) throw new IllegalArgumentException("competencia inválido ou obrigatório.");
        if (dados.sociosAtivos() < 0) throw new IllegalArgumentException("sociosAtivos não pode ser negativo.");
        if (dados.receitaMensal() != null && dados.receitaMensal().signum() < 0) throw new IllegalArgumentException("receitaMensal não pode ser negativo.");
        if (dados.churnPercentual() != null && dados.churnPercentual().signum() < 0) throw new IllegalArgumentException("churnPercentual não pode ser negativo.");
        if (dados.clubeId() == null) throw new IllegalArgumentException("clubeId inválido ou obrigatório.");
    }

    private void preencher(IndicadorMensal entidade, Entrada dados) {
        validar(dados);
        entidade.setCompetencia(dados.competencia());
        entidade.setSociosAtivos(dados.sociosAtivos());
        entidade.setReceitaMensal(dados.receitaMensal());
        entidade.setChurnPercentual(dados.churnPercentual());
        Clube clube = null;
        if (dados.clubeId() != null) {
            clube = clubeRepository.findById(dados.clubeId())
                    .orElseThrow(() -> new NoSuchElementException("Clube não encontrado: " + dados.clubeId()));
        }
        entidade.setClube(clube);
    }

    private Saida converter(IndicadorMensal entidade) {
        return new Saida(
                entidade.getId(),
                entidade.getCompetencia(),
                entidade.getSociosAtivos(),
                entidade.getReceitaMensal(),
                entidade.getChurnPercentual(),
                entidade.getClube() == null ? null : entidade.getClube().getId()
        );
    }

    @Transactional
    public Saida cadastrar(Entrada dados) {
        IndicadorMensal entidade = new IndicadorMensal();
        preencher(entidade, dados);
        return converter(indicadorMensalRepository.saveAndFlush(entidade));
    }

    @Transactional(readOnly = true)
    public List<Saida> listarTodos() {
        return indicadorMensalRepository.findAll().stream().map(this::converter).toList();
    }

    @Transactional(readOnly = true)
    public Saida buscarPorId(Long id) {
        IndicadorMensal entidade = indicadorMensalRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("IndicadorMensal não encontrado: " + id));
        return converter(entidade);
    }

    @Transactional
    public Saida atualizar(Long id, Entrada dados) {
        IndicadorMensal entidade = indicadorMensalRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("IndicadorMensal não encontrado: " + id));
        preencher(entidade, dados);
        return converter(indicadorMensalRepository.saveAndFlush(entidade));
    }

    @Transactional
    public void excluir(Long id) {
        IndicadorMensal entidade = indicadorMensalRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("IndicadorMensal não encontrado: " + id));
        indicadorMensalRepository.delete(entidade);
        indicadorMensalRepository.flush();
    }
}
