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
public class InscricaoModalidadeService {

    private final InscricaoModalidadeRepository inscricaoModalidadeRepository;
    private final SocioRepository socioRepository;
    private final ClubeModalidadeRepository clubeModalidadeRepository;

    public InscricaoModalidadeService(
            InscricaoModalidadeRepository inscricaoModalidadeRepository,
            SocioRepository socioRepository,
            ClubeModalidadeRepository clubeModalidadeRepository) {
        this.inscricaoModalidadeRepository = inscricaoModalidadeRepository;
        this.socioRepository = socioRepository;
        this.clubeModalidadeRepository = clubeModalidadeRepository;
    }

    // Contratos JSON: relacionamentos são enviados por IDs, não objetos JPA.
    public record Entrada(
            LocalDate dataInscricao,
            StatusInscricao status,
            Long socioId,
            Long clubeModalidadeId
    ) {}

    public record Saida(
            Long id,
            LocalDate dataInscricao,
            StatusInscricao status,
            Long socioId,
            Long clubeModalidadeId
    ) {}

    private void validar(Entrada dados) {
        if (dados == null) throw new IllegalArgumentException("Dados obrigatórios.");
        if (dados.dataInscricao() == null) throw new IllegalArgumentException("dataInscricao inválido ou obrigatório.");
        if (dados.status() == null) throw new IllegalArgumentException("status inválido ou obrigatório.");
        if (dados.socioId() == null) throw new IllegalArgumentException("socioId inválido ou obrigatório.");
        if (dados.clubeModalidadeId() == null) throw new IllegalArgumentException("clubeModalidadeId inválido ou obrigatório.");
    }

    private void preencher(InscricaoModalidade entidade, Entrada dados) {
        validar(dados);
        entidade.setDataInscricao(dados.dataInscricao());
        entidade.setStatus(dados.status());
        Socio socio = null;
        if (dados.socioId() != null) {
            socio = socioRepository.findById(dados.socioId())
                    .orElseThrow(() -> new NoSuchElementException("Socio não encontrado: " + dados.socioId()));
        }
        entidade.setSocio(socio);
        ClubeModalidade clubeModalidade = null;
        if (dados.clubeModalidadeId() != null) {
            clubeModalidade = clubeModalidadeRepository.findById(dados.clubeModalidadeId())
                    .orElseThrow(() -> new NoSuchElementException("ClubeModalidade não encontrado: " + dados.clubeModalidadeId()));
        }
        entidade.setClubeModalidade(clubeModalidade);
    }

    private Saida converter(InscricaoModalidade entidade) {
        return new Saida(
                entidade.getId(),
                entidade.getDataInscricao(),
                entidade.getStatus(),
                entidade.getSocio() == null ? null : entidade.getSocio().getId(),
                entidade.getClubeModalidade() == null ? null : entidade.getClubeModalidade().getId()
        );
    }

    @Transactional
    public Saida cadastrar(Entrada dados) {
        InscricaoModalidade entidade = new InscricaoModalidade();
        preencher(entidade, dados);
        return converter(inscricaoModalidadeRepository.saveAndFlush(entidade));
    }

    @Transactional(readOnly = true)
    public List<Saida> listarTodos() {
        return inscricaoModalidadeRepository.findAll().stream().map(this::converter).toList();
    }

    @Transactional(readOnly = true)
    public Saida buscarPorId(Long id) {
        InscricaoModalidade entidade = inscricaoModalidadeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("InscricaoModalidade não encontrado: " + id));
        return converter(entidade);
    }

    @Transactional
    public Saida atualizar(Long id, Entrada dados) {
        InscricaoModalidade entidade = inscricaoModalidadeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("InscricaoModalidade não encontrado: " + id));
        preencher(entidade, dados);
        return converter(inscricaoModalidadeRepository.saveAndFlush(entidade));
    }

    @Transactional
    public void excluir(Long id) {
        InscricaoModalidade entidade = inscricaoModalidadeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("InscricaoModalidade não encontrado: " + id));
        inscricaoModalidadeRepository.delete(entidade);
        inscricaoModalidadeRepository.flush();
    }
}
