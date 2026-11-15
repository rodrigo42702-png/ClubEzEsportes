package br.edu.eseg.clubesesportivos.ClubEzEsportes.repository;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.ProdutoLoja;
import org.springframework.data.jpa.repository.JpaRepository;

// Operações de persistência de ProdutoLoja.
public interface ProdutoLojaRepository extends JpaRepository<ProdutoLoja, Long> {}
