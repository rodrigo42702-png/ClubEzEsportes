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
public class ClubeModalidadeService {

    private final ClubeModalidadeRepository clubeModalidadeRepository;
    private final ClubeRepository clubeRepository;
    private final ModalidadeRepository modalidadeRepository;

    public ClubeModalidadeService(
            ClubeModalidadeRepository clubeModalidadeRepository,
            ClubeRepository clubeRepository,
            ModalidadeRepository modalidadeRepository) {
        this.clubeModalidadeRepository = clubeModalidadeRepository;
        this.clubeRepository = clubeRepository;
        this.modalidadeRepository = modalidadeRepository;
    }

    // Contratos JSON: relacionamentos são enviados por IDs, não objetos JPA.
    public record Entrada(
            int capacidadeMaxima,
            boolean ativa,
            Long clubeId,
            Long modalidadeId
    ) {}

    public record Saida(
            Long id,
            int capacidadeMaxima,
            boolean ativa,
            Long clubeId,
            Long modalidadeId
    ) {}

    private void validar(Entrada dados) {
        if (dados == null) throw new IllegalArgumentException("Dados obrigatórios.");
        if (dados.capacidadeMaxima() < 0) throw new IllegalArgumentException("capacidadeMaxima inválido ou obrigatório.");
        if (dados.clubeId() == null) throw new IllegalArgumentException("clubeId inválido ou obrigatório.");
        if (dados.modalidadeId() == null) throw new IllegalArgumentException("modalidadeId inválido ou obrigatório.");
    }

    private void preencher(ClubeModalidade entidade, Entrada dados) {
        validar(dados);
        entidade.setCapacidadeMaxima(dados.capacidadeMaxima());
        entidade.setAtiva(dados.ativa());
        Clube clube = null;
        if (dados.clubeId() != null) {
            clube = clubeRepository.findById(dados.clubeId())
                    .orElseThrow(() -> new NoSuchElementException("Clube não encontrado: " + dados.clubeId()));
        }
        entidade.setClube(clube);
        Modalidade modalidade = null;
        if (dados.modalidadeId() != null) {
            modalidade = modalidadeRepository.findById(dados.modalidadeId())
                    .orElseThrow(() -> new NoSuchElementException("Modalidade não encontrado: " + dados.modalidadeId()));
        }
        entidade.setModalidade(modalidade);
    }

    private Saida converter(ClubeModalidade entidade) {
        return new Saida(
                entidade.getId(),
                entidade.getCapacidadeMaxima(),
                entidade.isAtiva(),
                entidade.getClube() == null ? null : entidade.getClube().getId(),
                entidade.getModalidade() == null ? null : entidade.getModalidade().getId()
        );
    }

    @Transactional
    public Saida cadastrar(Entrada dados) {
        ClubeModalidade entidade = new ClubeModalidade();
        preencher(entidade, dados);
        return converter(clubeModalidadeRepository.saveAndFlush(entidade));
    }

    @Transactional(readOnly = true)
    public List<Saida> listarTodos() {
        return clubeModalidadeRepository.findAll().stream().map(this::converter).toList();
    }

    @Transactional(readOnly = true)
    public Saida buscarPorId(Long id) {
        ClubeModalidade entidade = clubeModalidadeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("ClubeModalidade não encontrado: " + id));
        return converter(entidade);
    }

    @Transactional
    public Saida atualizar(Long id, Entrada dados) {
        ClubeModalidade entidade = clubeModalidadeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("ClubeModalidade não encontrado: " + id));
        preencher(entidade, dados);
        return converter(clubeModalidadeRepository.saveAndFlush(entidade));
    }

    @Transactional
    public void excluir(Long id) {
        ClubeModalidade entidade = clubeModalidadeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("ClubeModalidade não encontrado: " + id));
        clubeModalidadeRepository.delete(entidade);
        clubeModalidadeRepository.flush();
    }
}
