package br.edu.eseg.clubesesportivos.ClubEzEsportes.service;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.*;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final MatriculaRepository matriculaRepository;

    public PagamentoService(
            PagamentoRepository pagamentoRepository,
            MatriculaRepository matriculaRepository) {
        this.pagamentoRepository = pagamentoRepository;
        this.matriculaRepository = matriculaRepository;
    }

    // Contratos JSON: relacionamentos são enviados por IDs, não objetos JPA.
    public record Entrada(
            String codigoLegado,
            LocalDate vencimento,
            LocalDate dataPagamento,
            BigDecimal valor,
            StatusPagamento status,
            Long matriculaId
    ) {}

    public record Saida(
            Long id,
            String codigoLegado,
            LocalDate vencimento,
            LocalDate dataPagamento,
            BigDecimal valor,
            StatusPagamento status,
            Long matriculaId
    ) {}

    private void validar(Entrada dados) {
        if (dados == null) throw new IllegalArgumentException("Dados obrigatórios.");
        if (dados.valor() != null && dados.valor().signum() < 0) throw new IllegalArgumentException("valor não pode ser negativo.");
        if (dados.status() == null) throw new IllegalArgumentException("status inválido ou obrigatório.");
        if (dados.matriculaId() == null) throw new IllegalArgumentException("matriculaId inválido ou obrigatório.");
    }

    private void preencher(Pagamento entidade, Entrada dados) {
        validar(dados);
        entidade.setCodigoLegado(dados.codigoLegado());
        entidade.setVencimento(dados.vencimento());
        entidade.setDataPagamento(dados.dataPagamento());
        entidade.setValor(dados.valor());
        entidade.setStatus(dados.status());
        Matricula matricula = null;
        if (dados.matriculaId() != null) {
            matricula = matriculaRepository.findById(dados.matriculaId())
                    .orElseThrow(() -> new NoSuchElementException("Matricula não encontrado: " + dados.matriculaId()));
        }
        entidade.setMatricula(matricula);
    }

    private Saida converter(Pagamento entidade) {
        return new Saida(
                entidade.getId(),
                entidade.getCodigoLegado(),
                entidade.getVencimento(),
                entidade.getDataPagamento(),
                entidade.getValor(),
                entidade.getStatus(),
                entidade.getMatricula() == null ? null : entidade.getMatricula().getId()
        );
    }

    @Transactional
    public Saida cadastrar(Entrada dados) {
        Pagamento entidade = new Pagamento();
        preencher(entidade, dados);
        return converter(pagamentoRepository.saveAndFlush(entidade));
    }

    @Transactional(readOnly = true)
    public List<Saida> listarTodos() {
        return pagamentoRepository.findAll().stream().map(this::converter).toList();
    }

    @Transactional(readOnly = true)
    public Saida buscarPorId(Long id) {
        Pagamento entidade = pagamentoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pagamento não encontrado: " + id));
        return converter(entidade);
    }

    @Transactional
    public Saida atualizar(Long id, Entrada dados) {
        Pagamento entidade = pagamentoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pagamento não encontrado: " + id));
        preencher(entidade, dados);
        return converter(pagamentoRepository.saveAndFlush(entidade));
    }

    @Transactional
    public void excluir(Long id) {
        Pagamento entidade = pagamentoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pagamento não encontrado: " + id));
        pagamentoRepository.delete(entidade);
        pagamentoRepository.flush();
    }
}
