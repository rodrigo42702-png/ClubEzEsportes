package br.edu.eseg.clubesesportivos.ClubEzEsportes.repository;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;

// Responsável pelas operações de persistência das matrículas.
public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

}
