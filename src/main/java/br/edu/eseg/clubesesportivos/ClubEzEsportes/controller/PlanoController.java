package br.edu.eseg.clubesesportivos.ClubEzEsportes.controller;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Plano;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.service.PlanoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/planos")
public class PlanoController {
    private final PlanoService planoService;
    //Recebe o Service responsável pelas regras de negócio.
    public PlanoController(PlanoService planoService) {
        this.planoService = planoService;
    }
    //Lista todos os planos cadastrados.
    @GetMapping
    public List<Plano> listarTodos() {
        return planoService.listarTodos();
    }
    //Busca um plano pelo ID.
    @GetMapping("/{id}")
    public Plano buscarPorId(@PathVariable Long id) {
        return planoService.buscarPorId(id);
    }
    //Cadastra um novo plano.
    @PostMapping
    public Plano cadastrar(@RequestBody Plano plano) {
        return planoService.salvar(plano);
    }
    //Atualiza um plano existente.
    @PutMapping("/{id}")
    public Plano atualizar(@PathVariable Long id, @RequestBody Plano plano) {
        //Confirma que o plano existe.
        planoService.buscarPorId(id);
        //Mantém o ID do registro original.
        plano.setId(id);
        return planoService.salvar(plano);
    }
    //Exclui um plano pelo ID.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        planoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
    //Retorna HTTP 400 para dados inválidos.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> tratarErro(IllegalArgumentException erro) {
        return ResponseEntity.badRequest().body(erro.getMessage());
    }
    //Retorna HTTP 404 quando o plano não existe.
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> tratarNaoEncontrado(NoSuchElementException erro) {
        return ResponseEntity.status(404).body(erro.getMessage());
    }
}
