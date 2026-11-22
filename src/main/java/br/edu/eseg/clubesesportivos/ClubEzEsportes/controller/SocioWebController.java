package br.edu.eseg.clubesesportivos.ClubEzEsportes.controller;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Socio;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.service.SocioService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PathVariable;
import java.time.LocalDate;

@Controller
public class SocioWebController {
    private final SocioService socioService;

    public SocioWebController(SocioService socioService) {
        this.socioService = socioService;
    }

    @GetMapping("/socios")
    public String listar(Model model) {
        model.addAttribute("socios", socioService.listarTodos());
        return "socios/lista";
    }

    @PostMapping("/socios")
    public String cadastrar(@RequestParam String nome, @RequestParam String cpf, @RequestParam String email, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataNascimento, RedirectAttributes mensagens) {
        Socio socio = new Socio();
        socio.setNome(nome == null ? null : nome.trim());
        socio.setCpf(cpf == null ? null : cpf.trim());
        socio.setEmail(email == null ? null : email.trim());
        socio.setDataNascimento(dataNascimento);
        socio.setAtivo(true);
        try {
            socioService.salvar(socio);
            mensagens.addFlashAttribute("sucesso", "Sócio cadastrado com sucesso!");
        } catch (IllegalArgumentException e) {
            mensagens.addFlashAttribute("erro", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            mensagens.addFlashAttribute("erro", "CPF ou e-mail já cadastrado.");
        }
        return "redirect:/socios";
    }

    //Atualiza os dados cadastrais de um sócio.
    @PostMapping("/socios/{id}/editar")
    public String editar(@PathVariable Long id, @RequestParam String nome, @RequestParam String cpf, @RequestParam String email, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataNascimento, RedirectAttributes mensagens) {
        try {
            //O Service valida os dados antes de atualizar o registro.
            socioService.atualizar(id, nome, cpf, email, dataNascimento);
            mensagens.addFlashAttribute("sucesso", "Sócio atualizado com sucesso!");
        } catch (IllegalArgumentException e) {
            mensagens.addFlashAttribute("erro", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            mensagens.addFlashAttribute("erro", "CPF ou e-mail já cadastrado.");
        } catch (java.util.NoSuchElementException e) {
            mensagens.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/socios";
    }

    //Ativa ou desativa um sócio sem excluir seu histórico.
    @PostMapping("/socios/{id}/situacao")
    public String alterarSituacao(@PathVariable Long id, RedirectAttributes mensagens) {
        try {
            socioService.alterarSituacao(id);
            mensagens.addFlashAttribute("sucesso", "Situação do sócio atualizada!");
        } catch (IllegalArgumentException e) {
            mensagens.addFlashAttribute("erro", e.getMessage());
        } catch (java.util.NoSuchElementException e) {
            mensagens.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/socios";
    }
}
