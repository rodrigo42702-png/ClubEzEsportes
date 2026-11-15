package br.edu.eseg.clubesesportivos.ClubEzEsportes.repository;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.ClubeModalidade;
import org.springframework.data.jpa.repository.JpaRepository;

// Operações de persistência de ClubeModalidade.
public interface ClubeModalidadeRepository extends JpaRepository<ClubeModalidade, Long> {}
