package br.edu.eseg.clubesesportivos.ClubEzEsportes.repository;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.ParticipacaoEquipe;
import org.springframework.data.jpa.repository.JpaRepository;

// Operações de persistência de ParticipacaoEquipe.
public interface ParticipacaoEquipeRepository extends JpaRepository<ParticipacaoEquipe, Long> {}
