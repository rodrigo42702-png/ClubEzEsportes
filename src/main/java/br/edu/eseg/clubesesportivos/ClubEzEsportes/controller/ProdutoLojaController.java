package br.edu.eseg.clubesesportivos.ClubEzEsportes.controller;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.service.ProdutoLojaService;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.service.ProdutoLojaService.Entrada;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.service.ProdutoLojaService.Saida;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/produtos-loja")
public class ProdutoLojaController {
    private final ProdutoLojaService service;

    public ProdutoLojaController(ProdutoLojaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Saida> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public Saida buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public Saida cadastrar(@RequestBody Entrada dados) {
        return service.cadastrar(dados);
    }

    @PutMapping("/{id}")
    public Saida atualizar(@PathVariable Long id, @RequestBody Entrada dados) {
        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> dadosInvalidos(IllegalArgumentException erro) {
        return ResponseEntity.badRequest().body(erro.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> naoEncontrado(NoSuchElementException erro) {
        return ResponseEntity.status(404).body(erro.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> conflitoDeDados(DataIntegrityViolationException erro) {
        return ResponseEntity.status(409).body("Conflito de dados: registro duplicado ou referenciado por outro registro.");
    }
}
