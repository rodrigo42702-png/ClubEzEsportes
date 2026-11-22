package br.edu.eseg.clubesesportivos.ClubEzEsportes.controller;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Clube;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.service.ClubeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ClubeWebController {
    private final ClubeService clubeService;

    public ClubeWebController(ClubeService clubeService) {
        this.clubeService = clubeService;
    }

    //Abre a página com os clubes cadastrados.
    @GetMapping("/clubes")
    public String listarClubes(Model model) {
        model.addAttribute("clubes", clubeService.listarTodos());
        return "clubes/lista";
    }

    //Recebe o formulário e cadastra um novo clube.
    @PostMapping("/clubes")
    public String cadastrarClube(@RequestParam String nome, @RequestParam String cidade, @RequestParam String estado, RedirectAttributes redirectAttributes) {
        Clube clube = new Clube();
        clube.setNome(nome == null ? null : nome.trim());
        clube.setCidade(cidade == null ? null : cidade.trim());
        clube.setEstado(estado == null ? null : estado.trim().toUpperCase(java.util.Locale.ROOT));
        clube.setAtivo(true);
        try {
            clubeService.salvar(clube);
            redirectAttributes.addFlashAttribute("sucesso", "Clube cadastrado com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/clubes";
    }
}
