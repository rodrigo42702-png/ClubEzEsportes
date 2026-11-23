package br.edu.eseg.clubesesportivos.ClubEzEsportes.repository;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.VendaLoja;
import org.springframework.data.jpa.repository.JpaRepository;

//Operações de persistência de VendaLoja.
public interface VendaLojaRepository extends JpaRepository<VendaLoja, Long> {
    boolean existsByProdutoLoja_Id(Long produtoId);
}
