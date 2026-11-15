
package br.edu.eseg.clubesesportivos.ClubEzEsportes.repository;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Socio;
import org.springframework.data.jpa.repository.JpaRepository;

//Interface responsável pelas operações de banco de dados dos sócios.
public interface SocioRepository extends JpaRepository<Socio, Long> {

}
