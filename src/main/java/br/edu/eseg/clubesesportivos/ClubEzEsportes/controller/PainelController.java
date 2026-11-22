
package br.edu.eseg.clubesesportivos.ClubEzEsportes.controller;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.ClubeRepository;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.SocioRepository;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.MatriculaRepository;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PainelController {
    private final ClubeRepository clubeRepository;
    private final SocioRepository socioRepository;
    private final MatriculaRepository matriculaRepository;

    //Recebe os repositórios pelo construtor.
    public PainelController(ClubeRepository clubeRepository, SocioRepository socioRepository, MatriculaRepository matriculaRepository) {
        this.clubeRepository = clubeRepository;
        this.socioRepository = socioRepository;
        this.matriculaRepository = matriculaRepository;
    }

    @GetMapping("/painel")
    public String painel(Authentication authentication, Model model) {
        //Recupera o e-mail do usuário autenticado.
        model.addAttribute("email", authentication.getName());
        // Recupera o perfil do usuário.
        String perfil = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        model.addAttribute("perfil", perfil);

        //Consulta a quantidade real de registros no banco H2.
        model.addAttribute("totalClubes", clubeRepository.count());
        model.addAttribute("totalSocios", socioRepository.count());
        model.addAttribute("totalMatriculas", matriculaRepository.count());

        //O nome corresponde ao arquivo painel.html.
        return "painel";
    }
}
