package br.edu.eseg.clubesesportivos.ClubEzEsportes.repository;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Matricula;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.StatusMatricula;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

//Responsável pelas operações de persistência das matrículas.
public interface MatriculaRepository extends JpaRepository<Matricula, Long> {
    //Busca matrículas de um sócio em um clube, filtrando pelo status.
    List<Matricula> findBySocio_IdAndClube_IdAndStatus(Long socioId, Long clubeId, StatusMatricula status);
    // Impede apagar um plano usado por matrículas, inclusive históricas.
    boolean existsByPlano_Id(Long planoId);
}