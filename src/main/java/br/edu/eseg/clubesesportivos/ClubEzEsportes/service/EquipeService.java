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
public class EquipeService {

    private final EquipeRepository equipeRepository;
    private final ClubeModalidadeRepository clubeModalidadeRepository;

    public EquipeService(
            EquipeRepository equipeRepository,
            ClubeModalidadeRepository clubeModalidadeRepository) {
        this.equipeRepository = equipeRepository;
        this.clubeModalidadeRepository = clubeModalidadeRepository;
    }

    // Contratos JSON: relacionamentos são enviados por IDs, não objetos JPA.
    public record Entrada(
            String codigoLegado,
            String nome,
            int capacidadeMaxima,
            boolean ativa,
            Long clubeModalidadeId
    ) {}

    public record Saida(
            Long id,
            String codigoLegado,
            String nome,
            int capacidadeMaxima,
            boolean ativa,
            Long clubeModalidadeId
    ) {}

    private void validar(Entrada dados) {
        if (dados == null) throw new IllegalArgumentException("Dados obrigatórios.");
        if (dados.nome() == null || dados.nome().isBlank()) throw new IllegalArgumentException("nome inválido ou obrigatório.");
        if (dados.capacidadeMaxima() < 0) throw new IllegalArgumentException("capacidadeMaxima inválido ou obrigatório.");
        if (dados.clubeModalidadeId() == null) throw new IllegalArgumentException("clubeModalidadeId inválido ou obrigatório.");
    }

    private void preencher(Equipe entidade, Entrada dados) {
        validar(dados);
        entidade.setCodigoLegado(dados.codigoLegado());
        entidade.setNome(dados.nome());
        entidade.setCapacidadeMaxima(dados.capacidadeMaxima());
        entidade.setAtiva(dados.ativa());
        ClubeModalidade clubeModalidade = null;
        if (dados.clubeModalidadeId() != null) {
            clubeModalidade = clubeModalidadeRepository.findById(dados.clubeModalidadeId())
                    .orElseThrow(() -> new NoSuchElementException("ClubeModalidade não encontrado: " + dados.clubeModalidadeId()));
        }
        entidade.setClubeModalidade(clubeModalidade);
    }

    private Saida converter(Equipe entidade) {
        return new Saida(
                entidade.getId(),
                entidade.getCodigoLegado(),
                entidade.getNome(),
                entidade.getCapacidadeMaxima(),
                entidade.isAtiva(),
                entidade.getClubeModalidade() == null ? null : entidade.getClubeModalidade().getId()
        );
    }

    @Transactional
    public Saida cadastrar(Entrada dados) {
        Equipe entidade = new Equipe();
        preencher(entidade, dados);
        return converter(equipeRepository.saveAndFlush(entidade));
    }

    @Transactional(readOnly = true)
    public List<Saida> listarTodos() {
        return equipeRepository.findAll().stream().map(this::converter).toList();
    }

    @Transactional(readOnly = true)
    public Saida buscarPorId(Long id) {
        Equipe entidade = equipeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Equipe não encontrado: " + id));
        return converter(entidade);
    }

    @Transactional
    public Saida atualizar(Long id, Entrada dados) {
        Equipe entidade = equipeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Equipe não encontrado: " + id));
        preencher(entidade, dados);
        return converter(equipeRepository.saveAndFlush(entidade));
    }

    @Transactional
    public void excluir(Long id) {
        Equipe entidade = equipeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Equipe não encontrado: " + id));
        equipeRepository.delete(entidade);
        equipeRepository.flush();
    }
}
