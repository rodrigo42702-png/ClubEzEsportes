package br.edu.eseg.clubesesportivos.ClubEzEsportes.repository;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Treino;
import org.springframework.data.jpa.repository.JpaRepository;

// Operações de persistência de Treino.
public interface TreinoRepository extends JpaRepository<Treino, Long> {}
