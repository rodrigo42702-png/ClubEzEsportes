package br.edu.eseg.clubesesportivos.ClubEzEsportes.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    //Abre a página de login personalizada.
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
