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
import java.util.Objects;

@Service
public class PagamentoService {
    private final PagamentoRepository pagamentoRepository;
    private final MatriculaRepository matriculaRepository;

    public PagamentoService(PagamentoRepository pagamentoRepository, MatriculaRepository matriculaRepository) {
        this.pagamentoRepository = pagamentoRepository;
        this.matriculaRepository = matriculaRepository;
    }

    //Contratos JSON: relacionamentos são enviados por IDs, não objetos JPA.
    public record Entrada(String codigoLegado, LocalDate vencimento, LocalDate dataPagamento, BigDecimal valor, StatusPagamento status, Long matriculaId) {}

    public record Saida(Long id, String codigoLegado, LocalDate vencimento, LocalDate dataPagamento, BigDecimal valor, StatusPagamento status, Long matriculaId) {}

    private void validar(Entrada dados) {
        if (dados == null) throw new IllegalArgumentException("Dados obrigatórios.");
        if (dados.valor() != null && dados.valor().signum() < 0) throw new IllegalArgumentException("valor não pode ser negativo.");
        if (dados.status() == null) throw new IllegalArgumentException("status inválido ou obrigatório.");
        if (dados.matriculaId() == null) throw new IllegalArgumentException("matriculaId inválido ou obrigatório.");
    }

    private void preencher(Pagamento entidade, Entrada dados) {
        validar(dados);
        //Primeiro verifica se a matrícula informada existe.
        //Assim evitamos modificar a entidade antes dessa validação.
        Matricula matricula = matriculaRepository.findById(dados.matriculaId()).orElseThrow(() -> new NoSuchElementException("Matricula não encontrada: " + dados.matriculaId()));
        //Só altera os campos depois de encontrar a matrícula.
        entidade.setCodigoLegado(dados.codigoLegado());
        entidade.setVencimento(dados.vencimento());
        entidade.setDataPagamento(dados.dataPagamento());
        entidade.setValor(dados.valor());
        entidade.setStatus(dados.status());
        entidade.setMatricula(matricula);
    }

    private Saida converter(Pagamento entidade) {
        return new Saida(entidade.getId(), entidade.getCodigoLegado(), entidade.getVencimento(), entidade.getDataPagamento(), entidade.getValor(), entidade.getStatus(), entidade.getMatricula() == null ? null : entidade.getMatricula().getId());
    }

    @Transactional
    public Saida cadastrar(Entrada dados) {
        validar(dados);
        // Todo pagamento novo começa pendente; a confirmação passa pela transição de status.
        if (dados.status() != StatusPagamento.PENDENTE) {
            throw new IllegalArgumentException("Um novo pagamento deve iniciar como PENDENTE.");
        }
        if (dados.dataPagamento() != null) {
            throw new IllegalArgumentException("Pagamento pendente não pode ter data de pagamento.");
        }
        if (dados.valor() == null) {
            throw new IllegalArgumentException("O valor do novo pagamento é obrigatório.");
        }
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
        Pagamento entidade = pagamentoRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Pagamento não encontrado: " + id));
        return converter(entidade);
    }

    private void validarTransicaoStatus(StatusPagamento atual, StatusPagamento novo) {
        if (atual == novo) {
            return;
        }
        boolean permitido = switch (atual) {
            case PENDENTE -> novo == StatusPagamento.ATRASADO || novo == StatusPagamento.PAGO;
            case ATRASADO -> novo == StatusPagamento.PAGO;
            case PAGO -> novo == StatusPagamento.ESTORNADO;
            case ESTORNADO -> false;
        };
        if (!permitido) {
            throw new IllegalArgumentException("Transição de pagamento não permitida: " + atual + " para " + novo);
        }
    }

    @Transactional
    public Saida atualizar(Long id, Entrada dados) {
        Pagamento entidade = pagamentoRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Pagamento não encontrado: " + id));
        //Valida os dados recebidos.
        validar(dados);
        //Um registro ainda pendente não pode conter data de quitação.
        if (dados.status() == StatusPagamento.PENDENTE && dados.dataPagamento() != null) {
            throw new IllegalArgumentException("Pagamento pendente não pode ter data de pagamento.");
        }
        //Verifica se a mudança de situação é permitida.
        validarTransicaoStatus(entidade.getStatus(), dados.status());
        //Confere se houve alteração nos dados financeiros ou cadastrais.
        boolean alterouDados = !Objects.equals(entidade.getValor(), dados.valor()) || !Objects.equals(entidade.getVencimento(), dados.vencimento()) || !Objects.equals(entidade.getCodigoLegado(), dados.codigoLegado()) || !Objects.equals(entidade.getMatricula() == null ? null : entidade.getMatricula().getId(), dados.matriculaId());
        //Os dados só podem ser editados enquanto o pagamento
        //está PENDENTE e continuará PENDENTE.
        if (alterouDados && (entidade.getStatus() != StatusPagamento.PENDENTE || dados.status() != StatusPagamento.PENDENTE)) {
            throw new IllegalArgumentException("Somente pagamentos pendentes podem ter seus dados alterados.");
        }
        //Não permite reescrever a data de um pagamento já concluído.
        if (entidade.getStatus() != StatusPagamento.PENDENTE && entidade.getStatus() != StatusPagamento.ATRASADO && !Objects.equals(entidade.getDataPagamento(), dados.dataPagamento())) {
            throw new IllegalArgumentException("A data de pagamento de um registro concluído não pode ser alterada.");
        }
        //Guarda a situação e a data anteriores.
        StatusPagamento statusAnterior = entidade.getStatus();
        LocalDate dataAnterior = entidade.getDataPagamento();
        //Verifica a data futura ANTES de alterar a entidade.
        if ((statusAnterior == StatusPagamento.PENDENTE || statusAnterior == StatusPagamento.ATRASADO) && dados.status() == StatusPagamento.PAGO && dados.dataPagamento() != null && dados.dataPagamento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data do pagamento não pode estar no futuro.");
        }
        //Somente depois das validações altera os campos.
        preencher(entidade, dados);
        //Ao confirmar um pagamento, registra automaticamente a data.
        if ((statusAnterior == StatusPagamento.PENDENTE || statusAnterior == StatusPagamento.ATRASADO) && dados.status() == StatusPagamento.PAGO) {
            //Se não foi informada uma data, usa a data atual.
            //Se já existe uma data histórica, preserva essa informação.
            LocalDate dataConfirmacao = dados.dataPagamento();
            if (dataConfirmacao == null) {
                dataConfirmacao = LocalDate.now();
            }
            entidade.setDataPagamento(dataConfirmacao);
        }
        //No estorno, mantém a data em que o pagamento ocorreu.
        if (statusAnterior == StatusPagamento.PAGO && dados.status() == StatusPagamento.ESTORNADO) {
            entidade.setDataPagamento(dataAnterior);
        }
        return converter(pagamentoRepository.saveAndFlush(entidade));
    }

    @Transactional
    public void excluir(Long id) {
        Pagamento entidade = pagamentoRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Pagamento não encontrado: " + id));
        //Só permite excluir pagamentos que ainda estão pendentes.
        if (entidade.getStatus() != StatusPagamento.PENDENTE) {
            throw new IllegalArgumentException("Somente pagamentos pendentes podem ser excluídos.");
        }
        pagamentoRepository.delete(entidade);
        pagamentoRepository.flush();
    }
}
