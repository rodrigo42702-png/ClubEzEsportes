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
public class TreinoService {

    private final TreinoRepository treinoRepository;
    private final EquipeRepository equipeRepository;

    public TreinoService(
            TreinoRepository treinoRepository,
            EquipeRepository equipeRepository) {
        this.treinoRepository = treinoRepository;
        this.equipeRepository = equipeRepository;
    }

    // Contratos JSON: relacionamentos são enviados por IDs, não objetos JPA.
    public record Entrada(
            String codigoLegado,
            String clubeLegadoCodigo,
            String modalidadeLegadoCodigo,
            LocalDate data,
            LocalTime horaInicio,
            LocalTime horaFim,
            String local,
            boolean cancelado,
            Long equipeId
    ) {}

    public record Saida(
            Long id,
            String codigoLegado,
            String clubeLegadoCodigo,
            String modalidadeLegadoCodigo,
            LocalDate data,
            LocalTime horaInicio,
            LocalTime horaFim,
            String local,
            boolean cancelado,
            Long equipeId
    ) {}

    private void validar(Entrada dados) {
        if (dados == null) throw new IllegalArgumentException("Dados obrigatórios.");
        if (dados.data() == null) throw new IllegalArgumentException("data inválido ou obrigatório.");
        if (dados.horaInicio() != null && dados.horaFim() != null && !dados.horaFim().isAfter(dados.horaInicio())) throw new IllegalArgumentException("horaFim deve ser após horaInicio.");
    }

    private void preencher(Treino entidade, Entrada dados) {
        validar(dados);
        entidade.setCodigoLegado(dados.codigoLegado());
        entidade.setClubeLegadoCodigo(dados.clubeLegadoCodigo());
        entidade.setModalidadeLegadoCodigo(dados.modalidadeLegadoCodigo());
        entidade.setData(dados.data());
        entidade.setHoraInicio(dados.horaInicio());
        entidade.setHoraFim(dados.horaFim());
        entidade.setLocal(dados.local());
        entidade.setCancelado(dados.cancelado());
        Equipe equipe = null;
        if (dados.equipeId() != null) {
            equipe = equipeRepository.findById(dados.equipeId())
                    .orElseThrow(() -> new NoSuchElementException("Equipe não encontrado: " + dados.equipeId()));
        }
        entidade.setEquipe(equipe);
    }

    private Saida converter(Treino entidade) {
        return new Saida(
                entidade.getId(),
                entidade.getCodigoLegado(),
                entidade.getClubeLegadoCodigo(),
                entidade.getModalidadeLegadoCodigo(),
                entidade.getData(),
                entidade.getHoraInicio(),
                entidade.getHoraFim(),
                entidade.getLocal(),
                entidade.isCancelado(),
                entidade.getEquipe() == null ? null : entidade.getEquipe().getId()
        );
    }

    @Transactional
    public Saida cadastrar(Entrada dados) {
        Treino entidade = new Treino();
        preencher(entidade, dados);
        return converter(treinoRepository.saveAndFlush(entidade));
    }

    @Transactional(readOnly = true)
    public List<Saida> listarTodos() {
        return treinoRepository.findAll().stream().map(this::converter).toList();
    }

    @Transactional(readOnly = true)
    public Saida buscarPorId(Long id) {
        Treino entidade = treinoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Treino não encontrado: " + id));
        return converter(entidade);
    }

    @Transactional
    public Saida atualizar(Long id, Entrada dados) {
        Treino entidade = treinoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Treino não encontrado: " + id));
        preencher(entidade, dados);
        return converter(treinoRepository.saveAndFlush(entidade));
    }

    @Transactional
    public void excluir(Long id) {
        Treino entidade = treinoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Treino não encontrado: " + id));
        treinoRepository.delete(entidade);
        treinoRepository.flush();
    }
}
