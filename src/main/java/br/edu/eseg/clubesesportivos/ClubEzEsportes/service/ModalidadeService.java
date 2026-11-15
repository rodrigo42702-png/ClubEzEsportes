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
public class ModalidadeService {

    private final ModalidadeRepository modalidadeRepository;

    public ModalidadeService(
            ModalidadeRepository modalidadeRepository) {
        this.modalidadeRepository = modalidadeRepository;
    }

    // Contratos JSON: relacionamentos são enviados por IDs, não objetos JPA.
    public record Entrada(
            String codigoLegado,
            String nome,
            String descricao,
            boolean ativa
    ) {}

    public record Saida(
            Long id,
            String codigoLegado,
            String nome,
            String descricao,
            boolean ativa
    ) {}

    private void validar(Entrada dados) {
        if (dados == null) throw new IllegalArgumentException("Dados obrigatórios.");
        if (dados.nome() == null || dados.nome().isBlank()) throw new IllegalArgumentException("nome inválido ou obrigatório.");
    }

    private void preencher(Modalidade entidade, Entrada dados) {
        validar(dados);
        entidade.setCodigoLegado(dados.codigoLegado());
        entidade.setNome(dados.nome());
        entidade.setDescricao(dados.descricao());
        entidade.setAtiva(dados.ativa());
    }

    private Saida converter(Modalidade entidade) {
        return new Saida(
                entidade.getId(),
                entidade.getCodigoLegado(),
                entidade.getNome(),
                entidade.getDescricao(),
                entidade.isAtiva()
        );
    }

    @Transactional
    public Saida cadastrar(Entrada dados) {
        Modalidade entidade = new Modalidade();
        preencher(entidade, dados);
        return converter(modalidadeRepository.saveAndFlush(entidade));
    }

    @Transactional(readOnly = true)
    public List<Saida> listarTodos() {
        return modalidadeRepository.findAll().stream().map(this::converter).toList();
    }

    @Transactional(readOnly = true)
    public Saida buscarPorId(Long id) {
        Modalidade entidade = modalidadeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Modalidade não encontrado: " + id));
        return converter(entidade);
    }

    @Transactional
    public Saida atualizar(Long id, Entrada dados) {
        Modalidade entidade = modalidadeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Modalidade não encontrado: " + id));
        preencher(entidade, dados);
        return converter(modalidadeRepository.saveAndFlush(entidade));
    }

    @Transactional
    public void excluir(Long id) {
        Modalidade entidade = modalidadeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Modalidade não encontrado: " + id));
        modalidadeRepository.delete(entidade);
        modalidadeRepository.flush();
    }
}
