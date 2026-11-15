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
public class PerfilService {

    private final PerfilRepository perfilRepository;

    public PerfilService(
            PerfilRepository perfilRepository) {
        this.perfilRepository = perfilRepository;
    }

    // Contratos JSON: relacionamentos são enviados por IDs, não objetos JPA.
    public record Entrada(
            String nome
    ) {}

    public record Saida(
            Long id,
            String nome
    ) {}

    private void validar(Entrada dados) {
        if (dados == null) throw new IllegalArgumentException("Dados obrigatórios.");
        if (dados.nome() == null || dados.nome().isBlank()) throw new IllegalArgumentException("nome inválido ou obrigatório.");
    }

    private void preencher(Perfil entidade, Entrada dados) {
        validar(dados);
        entidade.setNome(dados.nome());
    }

    private Saida converter(Perfil entidade) {
        return new Saida(
                entidade.getId(),
                entidade.getNome()
        );
    }

    @Transactional
    public Saida cadastrar(Entrada dados) {
        Perfil entidade = new Perfil();
        preencher(entidade, dados);
        return converter(perfilRepository.saveAndFlush(entidade));
    }

    @Transactional(readOnly = true)
    public List<Saida> listarTodos() {
        return perfilRepository.findAll().stream().map(this::converter).toList();
    }

    @Transactional(readOnly = true)
    public Saida buscarPorId(Long id) {
        Perfil entidade = perfilRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Perfil não encontrado: " + id));
        return converter(entidade);
    }

    @Transactional
    public Saida atualizar(Long id, Entrada dados) {
        Perfil entidade = perfilRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Perfil não encontrado: " + id));
        preencher(entidade, dados);
        return converter(perfilRepository.saveAndFlush(entidade));
    }

    @Transactional
    public void excluir(Long id) {
        Perfil entidade = perfilRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Perfil não encontrado: " + id));
        perfilRepository.delete(entidade);
        perfilRepository.flush();
    }
}
