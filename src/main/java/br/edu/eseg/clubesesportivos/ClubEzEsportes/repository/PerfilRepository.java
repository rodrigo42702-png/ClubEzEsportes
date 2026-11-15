package br.edu.eseg.clubesesportivos.ClubEzEsportes.repository;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

// Operações de persistência de Perfil.
public interface PerfilRepository extends JpaRepository<Perfil, Long> {}
