package br.edu.eseg.clubesesportivos.ClubEzEsportes.repository;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Equipe;
import org.springframework.data.jpa.repository.JpaRepository;

// Operações de persistência de Equipe.
public interface EquipeRepository extends JpaRepository<Equipe, Long> {}
