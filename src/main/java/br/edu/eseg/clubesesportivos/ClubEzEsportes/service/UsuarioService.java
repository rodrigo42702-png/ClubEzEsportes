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
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final SocioRepository socioRepository;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PerfilRepository perfilRepository,
            SocioRepository socioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.socioRepository = socioRepository;
    }

    // Contratos JSON: relacionamentos são enviados por IDs, não objetos JPA.
    public record Entrada(
            String email,
            String senhaHash,
            boolean ativo,
            Long perfilId,
            Long socioId
    ) {}

    public record Saida(
            Long id,
            String email,
            boolean ativo,
            Long perfilId,
            Long socioId
    ) {}

    private void validar(Entrada dados) {
        if (dados == null) throw new IllegalArgumentException("Dados obrigatórios.");
        if (dados.email() == null || dados.email().isBlank()) throw new IllegalArgumentException("email inválido ou obrigatório.");
        if (dados.senhaHash() == null || dados.senhaHash().isBlank()) throw new IllegalArgumentException("senhaHash inválido ou obrigatório.");
        // O campo senhaHash deve conter hash previamente gerado; não é senha em texto puro.
        if (dados.perfilId() == null) throw new IllegalArgumentException("perfilId inválido ou obrigatório.");
    }

    private void preencher(Usuario entidade, Entrada dados) {
        validar(dados);
        entidade.setEmail(dados.email());
        entidade.setSenhaHash(dados.senhaHash());
        entidade.setAtivo(dados.ativo());
        Perfil perfil = null;
        if (dados.perfilId() != null) {
            perfil = perfilRepository.findById(dados.perfilId())
                    .orElseThrow(() -> new NoSuchElementException("Perfil não encontrado: " + dados.perfilId()));
        }
        entidade.setPerfil(perfil);
        Socio socio = null;
        if (dados.socioId() != null) {
            socio = socioRepository.findById(dados.socioId())
                    .orElseThrow(() -> new NoSuchElementException("Socio não encontrado: " + dados.socioId()));
        }
        entidade.setSocio(socio);
    }

    private Saida converter(Usuario entidade) {
        return new Saida(
                entidade.getId(),
                entidade.getEmail(),
                entidade.isAtivo(),
                entidade.getPerfil() == null ? null : entidade.getPerfil().getId(),
                entidade.getSocio() == null ? null : entidade.getSocio().getId()
        );
    }

    @Transactional
    public Saida cadastrar(Entrada dados) {
        Usuario entidade = new Usuario();
        preencher(entidade, dados);
        return converter(usuarioRepository.saveAndFlush(entidade));
    }

    @Transactional(readOnly = true)
    public List<Saida> listarTodos() {
        return usuarioRepository.findAll().stream().map(this::converter).toList();
    }

    @Transactional(readOnly = true)
    public Saida buscarPorId(Long id) {
        Usuario entidade = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario não encontrado: " + id));
        return converter(entidade);
    }

    @Transactional
    public Saida atualizar(Long id, Entrada dados) {
        Usuario entidade = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario não encontrado: " + id));
        preencher(entidade, dados);
        return converter(usuarioRepository.saveAndFlush(entidade));
    }

    @Transactional
    public void excluir(Long id) {
        Usuario entidade = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario não encontrado: " + id));
        usuarioRepository.delete(entidade);
        usuarioRepository.flush();
    }
}
