package br.edu.eseg.clubesesportivos.ClubEzEsportes.controller;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.StatusPagamento;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.MatriculaRepository;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.service.PagamentoService;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.service.PagamentoService.Entrada;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.service.PagamentoService.Saida;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NoSuchElementException;

@Controller
public class PagamentoWebController {
    private final PagamentoService pagamentoService;
    private final MatriculaRepository matriculaRepository;

    public PagamentoWebController(PagamentoService pagamentoService, MatriculaRepository matriculaRepository) {
        this.pagamentoService = pagamentoService;
        this.matriculaRepository = matriculaRepository;
    }

    @GetMapping("/pagamentos")
    public String listar(Model model) {
        model.addAttribute("pagamentos", pagamentoService.listarTodos());
        model.addAttribute("matriculas", matriculaRepository.findAll());
        model.addAttribute("situacoes", StatusPagamento.values());
        return "pagamentos/lista";
    }

    //Novos pagamentos sempre começam pendentes; não é permitido marcá-los como pagos por engano.
    @PostMapping("/pagamentos")
    public String cadastrar(@RequestParam Long matriculaId, @RequestParam BigDecimal valor, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vencimento, RedirectAttributes mensagens) {
        try {
            pagamentoService.cadastrar(new Entrada(null, vencimento, null, valor, StatusPagamento.PENDENTE, matriculaId));
            mensagens.addFlashAttribute("sucesso", "Pagamento pendente registrado!");
        } catch (IllegalArgumentException | NoSuchElementException e) {
            mensagens.addFlashAttribute("erro", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            mensagens.addFlashAttribute("erro", "Não foi possível cadastrar o pagamento por conflito nos dados.");
        }
        return "redirect:/pagamentos";
    }

    //Usa a regra de transição existente no PagamentoService (não muda status diretamente no banco).
    @PostMapping("/pagamentos/{id}/status")
    public String atualizarStatus(@PathVariable Long id, @RequestParam StatusPagamento status, RedirectAttributes mensagens) {
        try {
            Saida atual = pagamentoService.buscarPorId(id);
            //O Service é responsável por registrar a data, quando o pagamento passa para PAGO.
            pagamentoService.atualizar(id, new Entrada(atual.codigoLegado(), atual.vencimento(), atual.dataPagamento(), atual.valor(), status, atual.matriculaId()));
        } catch (IllegalArgumentException | NoSuchElementException e) {
            mensagens.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/pagamentos";
    }

    //Permite editar valor e vencimento de pagamentos pendentes.
    @PostMapping("/pagamentos/{id}/editar")
    public String editar(@PathVariable Long id, @RequestParam BigDecimal valor, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vencimento, RedirectAttributes mensagens) {
        try {
            //Recupera os dados atuais do pagamento.
            Saida atual = pagamentoService.buscarPorId(id);
            //Somente pagamentos pendentes podem ser editados.
            if (atual.status() != StatusPagamento.PENDENTE) {
                throw new IllegalArgumentException("Somente pagamentos pendentes podem ser editados.");
            }
            //Mantém os demais campos e altera apenas valor e vencimento.
            pagamentoService.atualizar(id, new Entrada(atual.codigoLegado(), vencimento, atual.dataPagamento(), valor, atual.status(), atual.matriculaId()));
            mensagens.addFlashAttribute("sucesso", "Pagamento atualizado com sucesso!");
        } catch (IllegalArgumentException | NoSuchElementException e) {
            mensagens.addFlashAttribute("erro", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            mensagens.addFlashAttribute("erro", "Não foi possível atualizar o pagamento.");
        }
        return "redirect:/pagamentos";
    }
}
