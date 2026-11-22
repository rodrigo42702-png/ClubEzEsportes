package br.edu.eseg.clubesesportivos.ClubEzEsportes.service;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Usuario;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;
    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        //Busca o usuário cadastrado no banco pelo e-mail.
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
        //O perfil define a autorização do usuário.
        String perfil = usuario.getPerfil().getNome();
        //O Spring Security usa ROLE_ para identificar perfis.
        String permissao = "ROLE_" + perfil.toUpperCase();
        return User.withUsername(usuario.getEmail()).password(usuario.getSenhaHash()).authorities(permissao).disabled(!usuario.isAtivo()).build();
    }
}
