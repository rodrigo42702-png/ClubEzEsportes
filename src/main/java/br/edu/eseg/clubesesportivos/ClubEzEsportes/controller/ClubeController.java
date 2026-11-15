package br.edu.eseg.clubesesportivos.ClubEzEsportes.controller;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Clube;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.service.ClubeService;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/clubes")
public class ClubeController {
    private final ClubeService clubeService;

    //Injeta o Service pelo construtor
    public ClubeController(ClubeService clubeService) {
        this.clubeService = clubeService;
    }

    //GET /api/clubes
    //Retorna todos os clubes cadastrados
    @GetMapping
    public List<Clube> listar() {
        return clubeService.listarTodos();
    }

    //GET /api/clubes/1
    //Busca um clube pelo ID
    @GetMapping("/{id}")
    public Clube buscar(@PathVariable Long id) {
        return clubeService.buscarPorId(id);
    }

    //POST /api/clubes
    //Cadastra um novo clube
    @PostMapping
    public Clube cadastrar(@RequestBody Clube clube) {
        return clubeService.salvar(clube);
    }

    //PUT /api/clubes/1
    //Atualiza um clube existente
    @PutMapping("/{id}")
    public Clube atualizar(@PathVariable Long id, @RequestBody Clube clube) {
        clubeService.buscarPorId(id);
        clube.setId(id);
        return clubeService.salvar(clube);
    }

    //DELETE /api/clubes/1
    //Exclui um clube
    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        clubeService.excluir(id);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> tratarErro(IllegalArgumentException erro) {
        return ResponseEntity.badRequest().body(erro.getMessage());
    }

    @ExceptionHandler(java.util.NoSuchElementException.class)
    public ResponseEntity<String> tratarNaoEncontrado(java.util.NoSuchElementException erro) {
        return ResponseEntity.status(404).body(erro.getMessage());
    }
}
