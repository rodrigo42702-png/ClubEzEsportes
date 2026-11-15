package br.edu.eseg.clubesesportivos.ClubEzEsportes.repository;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Plano;
import org.springframework.data.jpa.repository.JpaRepository;

//Responsável pelas operações de banco de dados dos planos.
public interface PlanoRepository extends JpaRepository<Plano, Long> {

}
