package br.edu.eseg.clubesesportivos.ClubEzEsportes.repository;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;

// Operações de persistência de Pagamento.
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {}
