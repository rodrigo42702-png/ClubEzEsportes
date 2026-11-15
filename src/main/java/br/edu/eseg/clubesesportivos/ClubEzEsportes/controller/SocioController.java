package br.edu.eseg.clubesesportivos.ClubEzEsportes.controller;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Socio;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.service.SocioService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/socios")
public class SocioController {
    private final SocioService socioService;

    //Recebe o Service responsável pelas regras de negócio.
    public SocioController(SocioService socioService) {
        this.socioService = socioService;
    }

    //Lista todos os sócios cadastrados.
    @GetMapping
    public List<Socio> listarTodos() {
        return socioService.listarTodos();
    }

    //Busca um sócio pelo ID.
    @GetMapping("/{id}")
    public Socio buscarPorId(@PathVariable Long id) {
        return socioService.buscarPorId(id);
    }

    //Cadastra um novo sócio.
    @PostMapping
    public Socio cadastrar(@RequestBody Socio socio) {
        return socioService.salvar(socio);
    }

    //Atualiza os dados de um sócio existente.
    @PutMapping("/{id}")
    public Socio atualizar(@PathVariable Long id, @RequestBody Socio socio) {
        //Verifica se o sócio existe.
        socioService.buscarPorId(id);

        //Define o ID do registro que será atualizado.
        socio.setId(id);

        //Valida os dados e salva as alterações.
        return socioService.salvar(socio);
    }

    //Exclui um sócio pelo ID informado.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        socioService.excluir(id);
        //Retorna HTTP 204 quando a exclusão é concluída.
        return ResponseEntity.noContent().build();
    }

    //Trata os erros de validação e retorna HTTP 400.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> tratarErro(IllegalArgumentException erro) {
        return ResponseEntity.badRequest().body(erro.getMessage());
    }

    //Retorna HTTP 404 quando o sócio não existe.
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> tratarNaoEncontrado(NoSuchElementException erro) {
        return ResponseEntity.status(404).body(erro.getMessage());
    }
}
