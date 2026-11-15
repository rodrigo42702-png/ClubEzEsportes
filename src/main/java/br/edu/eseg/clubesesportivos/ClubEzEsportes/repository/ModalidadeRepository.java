package br.edu.eseg.clubesesportivos.ClubEzEsportes.repository;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Modalidade;
import org.springframework.data.jpa.repository.JpaRepository;

// Operações de persistência de Modalidade.
public interface ModalidadeRepository extends JpaRepository<Modalidade, Long> {}
