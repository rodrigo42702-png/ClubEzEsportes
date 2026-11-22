package br.edu.eseg.clubesesportivos.ClubEzEsportes.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaginaInicialController {
    //A tela de login é o ponto de entrada, após autenticar o Spring redireciona para /painel.
    @GetMapping("/")
    public String inicio() {
        return "redirect:/painel";
    }
}
