package br.edu.eseg.clubesesportivos.ClubEzEsportes.controller;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.service.MatriculaService;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.service.MatriculaService.EntradaMatricula;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.service.MatriculaService.SaidaMatricula;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/matriculas")
public class MatriculaController {
    private final MatriculaService matriculaService;

    //Recebe o Service responsável pelas matrículas.
    public MatriculaController(MatriculaService matriculaService) {
        this.matriculaService = matriculaService;
    }

    //Lista todas as matrículas.
    @GetMapping
    public List<SaidaMatricula> listarTodos() {
        return matriculaService.listarTodos();
    }

    //Consulta uma matrícula pelo ID.
    @GetMapping("/{id}")
    public SaidaMatricula buscarPorId(@PathVariable Long id) {
        return matriculaService.buscarPorId(id);
    }

    //Cadastra uma matrícula.
    @PostMapping
    public SaidaMatricula cadastrar(@RequestBody EntradaMatricula dados) {
        return matriculaService.cadastrar(dados);
    }

    //Atualiza uma matrícula existente.
    @PutMapping("/{id}")
    public SaidaMatricula atualizar(@PathVariable Long id, @RequestBody EntradaMatricula dados) {
        return matriculaService.atualizar(id, dados);
    }

    //Exclui uma matrícula.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        matriculaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    //Retorna HTTP 400 para dados inválidos.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> tratarErro(IllegalArgumentException erro) {
        return ResponseEntity.badRequest().body(erro.getMessage());
    }

    //Retorna HTTP 404 para registros inexistentes.
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> tratarNaoEncontrado(NoSuchElementException erro) {
        return ResponseEntity.status(404).body(erro.getMessage());
    }
}
