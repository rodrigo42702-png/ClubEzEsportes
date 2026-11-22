package br.edu.eseg.clubesesportivos.ClubEzEsportes.service;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Clube;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.ClubeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClubeService {
    private final ClubeRepository clubeRepository;

    //Injeta o Repository pelo construtor
    public ClubeService(ClubeRepository clubeRepository) {
        this.clubeRepository = clubeRepository;
    }


    public Clube salvar(Clube clube) {
        //Valida os dados antes de acessar o banco
        validarClube(clube);
        return clubeRepository.save(clube);
    }

    //Lista todos os clubes cadastrados
    public List<Clube> listarTodos() {
        return clubeRepository.findAll();
    }

    //Busca um clube pelo ID
    public Clube buscarPorId(Long id) {
        return clubeRepository.findById(id).orElseThrow(() -> new java.util.NoSuchElementException("Clube não encontrado: " + id));
    }

    //Exclui um clube pelo ID
    public void excluir(Long id) {
        Clube clube = buscarPorId(id);
        clubeRepository.delete(clube);
    }

    private void validarClube(Clube clube) {
        //Verifica se o nome foi informado
        if (clube.getNome() == null || clube.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do clube é obrigatório.");
        }
        //A cidade é necessária no cadastro pela interface e na API.
        if (clube.getCidade() == null || clube.getCidade().isBlank()) {
            throw new IllegalArgumentException("A cidade do clube é obrigatória.");
        }
        //Verifica se o estado possui exatamente duas letras
        if (clube.getEstado() == null || !clube.getEstado().matches("[a-zA-Z]{2}")) {
            throw new IllegalArgumentException("O estado deve conter exatamente duas letras.");
        }
    }
}
