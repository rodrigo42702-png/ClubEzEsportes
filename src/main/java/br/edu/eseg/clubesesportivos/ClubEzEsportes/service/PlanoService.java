package br.edu.eseg.clubesesportivos.ClubEzEsportes.service;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Plano;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.PlanoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PlanoService {
    private final PlanoRepository planoRepository;
    //Recebe o Repository para acessar o banco de dados.
    public PlanoService(PlanoRepository planoRepository) {
        this.planoRepository = planoRepository;
    }
    //Valida os dados antes de salvar o plano.
    private void validarPlano(Plano plano) {
        //O nome do plano é obrigatório.
        if (plano.getNome() == null || plano.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do plano é obrigatório.");
        }
        //A mensalidade não pode ser nula ou negativa.
        if (plano.getValorMensalidade() == null || plano.getValorMensalidade().signum() < 0) {throw new IllegalArgumentException("A mensalidade deve ser maior ou igual a zero.");
        }
    }
    //Cadastra ou atualiza um plano.
    public Plano salvar(Plano plano) {
        validarPlano(plano);
        return planoRepository.save(plano);
    }
    //Retorna todos os planos cadastrados.
    public List<Plano> listarTodos() {
        return planoRepository.findAll();
    }
    //Busca um plano pelo ID informado.
    public Plano buscarPorId(Long id) {
        return planoRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Plano não encontrado: " + id));
    }
    //Exclui um plano existente.
    public void excluir(Long id) {
        //Confirma que o plano existe antes de excluir.
        Plano plano = buscarPorId(id);
        planoRepository.delete(plano);
    }
}
