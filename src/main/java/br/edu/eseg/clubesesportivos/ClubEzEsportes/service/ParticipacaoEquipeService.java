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
public class ParticipacaoEquipeService {
    private final ParticipacaoEquipeRepository participacaoEquipeRepository;
    private final EquipeRepository equipeRepository;
    private final InscricaoModalidadeRepository inscricaoModalidadeRepository;

    public ParticipacaoEquipeService(ParticipacaoEquipeRepository participacaoEquipeRepository, EquipeRepository equipeRepository, InscricaoModalidadeRepository inscricaoModalidadeRepository) {
        this.participacaoEquipeRepository = participacaoEquipeRepository;
        this.equipeRepository = equipeRepository;
        this.inscricaoModalidadeRepository = inscricaoModalidadeRepository;
    }

    //Contratos JSON: relacionamentos são enviados por IDs, não objetos JPA.
    public record Entrada(LocalDate dataEntrada, LocalDate dataSaida, boolean ativa, Long equipeId, Long inscricaoModalidadeId) {}

    public record Saida(Long id, LocalDate dataEntrada, LocalDate dataSaida, boolean ativa, Long equipeId, Long inscricaoModalidadeId) {}

    private void validar(Entrada dados) {
        if (dados == null) throw new IllegalArgumentException("Dados obrigatórios.");
        if (dados.dataEntrada() == null) throw new IllegalArgumentException("dataEntrada inválido ou obrigatório.");
        if (dados.equipeId() == null) throw new IllegalArgumentException("equipeId inválido ou obrigatório.");
        if (dados.inscricaoModalidadeId() == null) throw new IllegalArgumentException("inscricaoModalidadeId inválido ou obrigatório.");
        if (dados.dataSaida() != null && dados.dataSaida().isBefore(dados.dataEntrada())) throw new IllegalArgumentException("dataSaida anterior à dataEntrada.");
    }

    private void preencher(ParticipacaoEquipe entidade, Entrada dados) {
        validar(dados);
        Equipe equipe = equipeRepository.findById(dados.equipeId()).orElseThrow(() -> new NoSuchElementException("Equipe não encontrada: " + dados.equipeId()));
        InscricaoModalidade inscricao = inscricaoModalidadeRepository.findById(dados.inscricaoModalidadeId()).orElseThrow(() -> new NoSuchElementException("Inscrição não encontrada: " + dados.inscricaoModalidadeId()));
        //A inscrição e a equipe precisam pertencer à mesma modalidade oferecida pelo clube.
        Long clubeModalidadeEquipe = equipe.getClubeModalidade().getId();
        Long clubeModalidadeInscricao = inscricao.getClubeModalidade().getId();
        if (!clubeModalidadeEquipe.equals(clubeModalidadeInscricao)) {
            throw new IllegalArgumentException("A inscrição e a equipe pertencem a modalidades " + "ou clubes diferentes.");
        }
        entidade.setDataEntrada(dados.dataEntrada());
        entidade.setDataSaida(dados.dataSaida());
        entidade.setAtiva(dados.ativa());
        entidade.setEquipe(equipe);
        entidade.setInscricaoModalidade(inscricao);
    }

    private Saida converter(ParticipacaoEquipe entidade) {
        return new Saida(entidade.getId(), entidade.getDataEntrada(), entidade.getDataSaida(), entidade.isAtiva(), entidade.getEquipe() == null ? null : entidade.getEquipe().getId(), entidade.getInscricaoModalidade() == null ? null : entidade.getInscricaoModalidade().getId());
    }

    @Transactional
    public Saida cadastrar(Entrada dados) {
        ParticipacaoEquipe entidade = new ParticipacaoEquipe();
        preencher(entidade, dados);
        return converter(participacaoEquipeRepository.saveAndFlush(entidade));
    }

    @Transactional(readOnly = true)
    public List<Saida> listarTodos() {
        return participacaoEquipeRepository.findAll().stream().map(this::converter).toList();
    }

    @Transactional(readOnly = true)
    public Saida buscarPorId(Long id) {
        ParticipacaoEquipe entidade = participacaoEquipeRepository.findById(id).orElseThrow(() -> new NoSuchElementException("ParticipacaoEquipe não encontrado: " + id));
        return converter(entidade);
    }

    @Transactional
    public Saida atualizar(Long id, Entrada dados) {
        ParticipacaoEquipe entidade = participacaoEquipeRepository.findById(id).orElseThrow(() -> new NoSuchElementException("ParticipacaoEquipe não encontrado: " + id));
        preencher(entidade, dados);
        return converter(participacaoEquipeRepository.saveAndFlush(entidade));
    }

    @Transactional
    public void excluir(Long id) {
        ParticipacaoEquipe entidade = participacaoEquipeRepository.findById(id).orElseThrow(() -> new NoSuchElementException("ParticipacaoEquipe não encontrado: " + id));
        participacaoEquipeRepository.delete(entidade);
        participacaoEquipeRepository.flush();
    }
}
