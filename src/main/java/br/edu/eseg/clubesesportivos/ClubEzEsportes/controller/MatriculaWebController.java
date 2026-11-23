package br.edu.eseg.clubesesportivos.ClubEzEsportes.controller;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.StatusMatricula;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.service.ClubeService;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.service.MatriculaService;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.service.PlanoService;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.service.SocioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.NoSuchElementException;

@Controller
public class MatriculaWebController {
    private final MatriculaService matriculaService;
    private final ClubeService clubeService;
    private final SocioService socioService;
    private final PlanoService planoService;

    public MatriculaWebController(MatriculaService matriculaService, ClubeService clubeService, SocioService socioService, PlanoService planoService) {
        this.matriculaService = matriculaService;
        this.clubeService = clubeService;
        this.socioService = socioService;
        this.planoService = planoService;
    }

    //A tela oferece as opções cadastradas e exibe as matrículas existentes.
    @GetMapping("/matriculas")
    public String listarMatriculas(Model model) {
        model.addAttribute("matriculas", matriculaService.listarTodos());
        model.addAttribute("clubes", clubeService.listarTodos());
        model.addAttribute("socios", socioService.listarTodos());
        model.addAttribute("planos", planoService.listarTodos());
        return "matriculas/lista";
    }

    //O valor contratado é definido pelo Service, usando a mensalidade atual do plano. Código legado é nulo para registros novos, o histórico importado permanece intacto.
    @PostMapping("/matriculas")
    public String cadastrarMatricula(@RequestParam Long clubeId, @RequestParam Long socioId, @RequestParam Long planoId,@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio, RedirectAttributes redirectAttributes) {
        try {
            MatriculaService.EntradaMatricula entrada = new MatriculaService.EntradaMatricula(null, dataInicio, null, null, StatusMatricula.ATIVA, clubeId, socioId, planoId);
            matriculaService.cadastrar(entrada);
            redirectAttributes.addFlashAttribute("sucesso", "Matrícula cadastrada com sucesso!");
        } catch (IllegalArgumentException | NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/matriculas";
    }
}
