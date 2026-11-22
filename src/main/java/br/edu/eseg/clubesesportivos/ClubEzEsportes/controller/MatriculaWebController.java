package br.edu.eseg.clubesesportivos.ClubEzEsportes.controller;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.service.MatriculaService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MatriculaWebController {
    private final MatriculaService matriculaService;

    //Recebe o serviço pelo construtor.
    public MatriculaWebController(MatriculaService matriculaService) {
        this.matriculaService = matriculaService;
    }

    //Exibe a página com as matrículas cadastradas.
    @GetMapping("/matriculas")
    public String listarMatriculas(Model model) {
        //Busca os dados usando o serviço existente.
        model.addAttribute("matriculas", matriculaService.listarTodos());
        //Retorna o arquivo templates/matriculas/lista.html.
        return "matriculas/lista";
    }
}
