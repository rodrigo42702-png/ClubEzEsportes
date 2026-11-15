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
public class PartidaEventoService {

    private final PartidaEventoRepository partidaEventoRepository;
    private final ClubeRepository clubeRepository;
    private final ModalidadeRepository modalidadeRepository;

    public PartidaEventoService(
            PartidaEventoRepository partidaEventoRepository,
            ClubeRepository clubeRepository,
            ModalidadeRepository modalidadeRepository) {
        this.partidaEventoRepository = partidaEventoRepository;
        this.clubeRepository = clubeRepository;
        this.modalidadeRepository = modalidadeRepository;
    }

    // Contratos JSON: relacionamentos são enviados por IDs, não objetos JPA.
    public record Entrada(
            String codigoLegado,
            LocalDate dataPartida,
            String resultadoMandante,
            Long clubeId,
            Long modalidadeId
    ) {}

    public record Saida(
            Long id,
            String codigoLegado,
            LocalDate dataPartida,
            String resultadoMandante,
            Long clubeId,
            Long modalidadeId
    ) {}

    private void validar(Entrada dados) {
        if (dados == null) throw new IllegalArgumentException("Dados obrigatórios.");
        if (dados.clubeId() == null) throw new IllegalArgumentException("clubeId inválido ou obrigatório.");
        if (dados.modalidadeId() == null) throw new IllegalArgumentException("modalidadeId inválido ou obrigatório.");
    }

    private void preencher(PartidaEvento entidade, Entrada dados) {
        validar(dados);
        entidade.setCodigoLegado(dados.codigoLegado());
        entidade.setDataPartida(dados.dataPartida());
        entidade.setResultadoMandante(dados.resultadoMandante());
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

    private Saida converter(PartidaEvento entidade) {
        return new Saida(
                entidade.getId(),
                entidade.getCodigoLegado(),
                entidade.getDataPartida(),
                entidade.getResultadoMandante(),
                entidade.getClube() == null ? null : entidade.getClube().getId(),
                entidade.getModalidade() == null ? null : entidade.getModalidade().getId()
        );
    }

    @Transactional
    public Saida cadastrar(Entrada dados) {
        PartidaEvento entidade = new PartidaEvento();
        preencher(entidade, dados);
        return converter(partidaEventoRepository.saveAndFlush(entidade));
    }

    @Transactional(readOnly = true)
    public List<Saida> listarTodos() {
        return partidaEventoRepository.findAll().stream().map(this::converter).toList();
    }

    @Transactional(readOnly = true)
    public Saida buscarPorId(Long id) {
        PartidaEvento entidade = partidaEventoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("PartidaEvento não encontrado: " + id));
        return converter(entidade);
    }

    @Transactional
    public Saida atualizar(Long id, Entrada dados) {
        PartidaEvento entidade = partidaEventoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("PartidaEvento não encontrado: " + id));
        preencher(entidade, dados);
        return converter(partidaEventoRepository.saveAndFlush(entidade));
    }

    @Transactional
    public void excluir(Long id) {
        PartidaEvento entidade = partidaEventoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("PartidaEvento não encontrado: " + id));
        partidaEventoRepository.delete(entidade);
        partidaEventoRepository.flush();
    }
}
