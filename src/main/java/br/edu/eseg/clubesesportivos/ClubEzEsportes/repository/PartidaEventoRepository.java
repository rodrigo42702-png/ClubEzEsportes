package br.edu.eseg.clubesesportivos.ClubEzEsportes.repository;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.PartidaEvento;
import org.springframework.data.jpa.repository.JpaRepository;

// Operações de persistência de PartidaEvento.
public interface PartidaEventoRepository extends JpaRepository<PartidaEvento, Long> {}
