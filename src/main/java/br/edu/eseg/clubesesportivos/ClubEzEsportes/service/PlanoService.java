package br.edu.eseg.clubesesportivos.ClubEzEsportes.service;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Plano;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.MatriculaRepository;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.PlanoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PlanoService {
    private final PlanoRepository planoRepository;
    private final MatriculaRepository matriculaRepository;

    public PlanoService(PlanoRepository planoRepository, MatriculaRepository matriculaRepository) {
        this.planoRepository = planoRepository;
        this.matriculaRepository = matriculaRepository;
    }

    private void validarPlano(Plano plano) {
        if (plano == null) throw new IllegalArgumentException("Os dados do plano são obrigatórios.");
        if (plano.getNome() == null || plano.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do plano é obrigatório.");
        }
        if (plano.getValorMensalidade() == null || plano.getValorMensalidade().signum() < 0) {
            throw new IllegalArgumentException("A mensalidade deve ser maior ou igual a zero.");
        }
    }

    @Transactional
    public Plano salvar(Plano dados) {
        validarPlano(dados);
        if (dados.getId() == null) {
            return planoRepository.save(dados);
        }
        //Atualiza a entidade gerenciada, sem recriar o registro do banco.
        Plano atual = buscarPorId(dados.getId());
        if (dados.getCodigoLegado() != null && !dados.getCodigoLegado().equals(atual.getCodigoLegado())) {
            throw new IllegalArgumentException("O código legado do plano não pode ser alterado.");
        }
        atual.setNome(dados.getNome());
        atual.atualizarValor(dados.getValorMensalidade());
        atual.setAtivo(dados.isAtivo());
        //O código legado existente permanece inalterado.
        return planoRepository.save(atual);
    }

    @Transactional(readOnly = true)
    public List<Plano> listarTodos() {
        return planoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Plano buscarPorId(Long id) {
        return planoRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Plano não encontrado: " + id));
    }

    @Transactional
    public void excluir(Long id) {
        Plano plano = buscarPorId(id);
        //Matrículas antigas também precisam manter seu vínculo com o plano.
        if (matriculaRepository.existsByPlano_Id(id)) {
            throw new IllegalArgumentException("Não é permitido excluir um plano com matrículas vinculadas. Desative o plano.");
        }
        planoRepository.delete(plano);
    }
}
