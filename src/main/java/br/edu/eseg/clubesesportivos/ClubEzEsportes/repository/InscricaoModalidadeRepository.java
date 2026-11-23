package br.edu.eseg.clubesesportivos.ClubEzEsportes.repository;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.InscricaoModalidade;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.StatusInscricao;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

//Operações de persistência de InscricaoModalidade.
public interface InscricaoModalidadeRepository extends JpaRepository<InscricaoModalidade, Long> {
    // etecta inscrições não canceladas de um sócio na oferta do clube.
    boolean existsBySocio_IdAndClubeModalidade_Clube_IdAndStatusIn(Long socioId, Long clubeId, Collection<StatusInscricao> status);
}
