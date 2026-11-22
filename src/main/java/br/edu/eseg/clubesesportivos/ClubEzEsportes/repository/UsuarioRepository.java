package br.edu.eseg.clubesesportivos.ClubEzEsportes.repository;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    //Busca o usuário pelo e-mail informado no login.
    Optional<Usuario> findByEmail(String email);
}
