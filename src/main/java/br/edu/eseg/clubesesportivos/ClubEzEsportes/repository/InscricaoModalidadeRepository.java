package br.edu.eseg.clubesesportivos.ClubEzEsportes.repository;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.InscricaoModalidade;
import org.springframework.data.jpa.repository.JpaRepository;

// Operações de persistência de InscricaoModalidade.
public interface InscricaoModalidadeRepository extends JpaRepository<InscricaoModalidade, Long> {}
