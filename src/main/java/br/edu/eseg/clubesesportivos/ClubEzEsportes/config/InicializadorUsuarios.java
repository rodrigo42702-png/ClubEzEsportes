package br.edu.eseg.clubesesportivos.ClubEzEsportes.config;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Perfil;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Usuario;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.PerfilRepository;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "clubez.bootstrap.enabled", havingValue = "true")
public class InicializadorUsuarios implements CommandLineRunner {
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    public InicializadorUsuarios(UsuarioRepository usuarioRepository, PerfilRepository perfilRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String adminEmail = exigirVariavel("CLUBEZ_ADMIN_EMAIL");
        String adminSenha = exigirVariavel("CLUBEZ_ADMIN_SENHA");
        String funcionarioEmail = exigirVariavel("CLUBEZ_FUNCIONARIO_EMAIL");
        String funcionarioSenha = exigirVariavel("CLUBEZ_FUNCIONARIO_SENHA");
        if (adminEmail.equalsIgnoreCase(funcionarioEmail)) {
            throw new IllegalStateException("ADMIN e FUNCIONARIO precisam de e-mails diferentes.");
        }
        Perfil admin = buscarOuCriarPerfil("ADMIN");
        Perfil funcionario = buscarOuCriarPerfil("FUNCIONARIO");
        criarUsuarioSeNaoExistir(adminEmail, adminSenha, admin);
        criarUsuarioSeNaoExistir(funcionarioEmail, funcionarioSenha, funcionario);
        System.out.println("Usuarios iniciais verificados.");
    }

    private String exigirVariavel(String nome) {
        String valor = System.getenv(nome);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Variavel de ambiente obrigatoria: " + nome);
        }
        return valor.trim();
    }

    private Perfil buscarOuCriarPerfil(String nome) {
        return perfilRepository.findByNome(nome).orElseGet(() -> {Perfil perfil = new Perfil();perfil.setNome(nome);return perfilRepository.save(perfil);});
    }

    private void criarUsuarioSeNaoExistir(String email, String senha, Perfil perfil) {
        if (usuarioRepository.findByEmail(email).isPresent()) {
            return;
        }
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        usuario.setAtivo(true);
        usuario.setPerfil(perfil);
        usuarioRepository.save(usuario);
    }
}
