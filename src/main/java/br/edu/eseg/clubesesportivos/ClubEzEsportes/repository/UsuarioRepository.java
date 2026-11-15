package br.edu.eseg.clubesesportivos.ClubEzEsportes.repository;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

// Operações de persistência de Usuario.
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {}
