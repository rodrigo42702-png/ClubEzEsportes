package br.edu.eseg.clubesesportivos.ClubEzEsportes.service;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.*;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDate;

class PagamentoServiceTransicaoTest {
    private final PagamentoRepository pagamentos = mock(PagamentoRepository.class);
    private final MatriculaRepository matriculas = mock(MatriculaRepository.class);
    private final PagamentoService service = new PagamentoService(pagamentos, matriculas);

    @Test void naoPermiteVoltarDePagoParaPendente() {
        Pagamento entidade = new Pagamento();
        entidade.setStatus(StatusPagamento.PAGO);
        when(pagamentos.findById(10L)).thenReturn(Optional.of(entidade));
        PagamentoService.Entrada dados = new PagamentoService.Entrada(null, null, null, new BigDecimal("99.90"), StatusPagamento.PENDENTE, 1L);
        assertThrows(IllegalArgumentException.class, () -> service.atualizar(10L, dados));
        verify(pagamentos, never()).saveAndFlush(any());
    }

    @Test void naoPermiteValorNegativoAoCadastrar() {
        PagamentoService.Entrada dados = new PagamentoService.Entrada(null, null, null, new BigDecimal("-1.00"), StatusPagamento.PENDENTE, 1L);
        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(dados));
        verifyNoInteractions(pagamentos, matriculas);
    }

    @Test
    void permiteEditarPagamentoPendente() {
        Pagamento pagamento = new Pagamento();
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setValor(new BigDecimal("99.90"));
        pagamento.setVencimento(LocalDate.of(2026, 11, 30));
        Matricula matricula = new Matricula();
        matricula.setId(1L);
        pagamento.setMatricula(matricula);
        when(pagamentos.findById(10L)).thenReturn(Optional.of(pagamento));
        when(matriculas.findById(1L)).thenReturn(Optional.of(matricula));
        when(pagamentos.saveAndFlush(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PagamentoService.Entrada dados = new PagamentoService.Entrada(null, LocalDate.of(2026, 12, 10), null, new BigDecimal("120.00"), StatusPagamento.PENDENTE, 1L);

        PagamentoService.Saida resultado = service.atualizar(10L, dados);

        assertEquals(new BigDecimal("120.00"), resultado.valor());
        assertEquals(LocalDate.of(2026, 12, 10), resultado.vencimento());

        verify(pagamentos).saveAndFlush(pagamento);
    }

    @Test
    void naoPermiteAlterarValorDePagamentoPago() {
        Pagamento pagamento = new Pagamento();
        pagamento.setStatus(StatusPagamento.PAGO);
        pagamento.setValor(new BigDecimal("99.90"));
        when(pagamentos.findById(10L)).thenReturn(Optional.of(pagamento));

        PagamentoService.Entrada dados = new PagamentoService.Entrada(null, null, null, new BigDecimal("10.00"), StatusPagamento.PAGO, 1L);

        assertThrows(IllegalArgumentException.class, () -> service.atualizar(10L, dados));

        assertEquals(new BigDecimal("99.90"), pagamento.getValor());

        verify(pagamentos, never()).saveAndFlush(any());
    }

    @Test
    void naoPermiteAlterarVencimentoDePagamentoEstornado() {
        Pagamento pagamento = new Pagamento();
        pagamento.setStatus(StatusPagamento.ESTORNADO);
        pagamento.setValor(new BigDecimal("99.90"));
        pagamento.setVencimento(LocalDate.of(2026, 11, 30));

        when(pagamentos.findById(10L)).thenReturn(Optional.of(pagamento));

        PagamentoService.Entrada dados = new PagamentoService.Entrada(null, LocalDate.of(2026, 12, 20), null, new BigDecimal("99.90"), StatusPagamento.ESTORNADO, 1L);

        assertThrows(IllegalArgumentException.class, () -> service.atualizar(10L, dados));

        assertEquals(LocalDate.of(2026, 11, 30), pagamento.getVencimento());

        verify(pagamentos, never()).saveAndFlush(any());
    }

    @Test
    void permiteEstornarPagamentoPagoSemAlterarValor() {
        Pagamento pagamento = new Pagamento();
        pagamento.setStatus(StatusPagamento.PAGO);
        pagamento.setValor(new BigDecimal("99.90"));
        pagamento.setDataPagamento(LocalDate.of(2026, 11, 22));

        Matricula matricula = new Matricula();
        matricula.setId(1L);
        pagamento.setMatricula(matricula);

        when(pagamentos.findById(10L)).thenReturn(Optional.of(pagamento));
        when(matriculas.findById(1L)).thenReturn(Optional.of(matricula));
        when(pagamentos.saveAndFlush(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PagamentoService.Entrada dados = new PagamentoService.Entrada(null, null, LocalDate.of(2026, 11, 22), new BigDecimal("99.90"), StatusPagamento.ESTORNADO, 1L);

        PagamentoService.Saida resultado = service.atualizar(10L, dados);

        assertEquals(StatusPagamento.ESTORNADO, resultado.status());
        assertEquals(new BigDecimal("99.90"), resultado.valor());

        verify(pagamentos).saveAndFlush(pagamento);
    }

    @Test
    void deveRegistrarDataAoConfirmarPagamento() {
        Pagamento pagamento = new Pagamento();
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setValor(new BigDecimal("95.00"));

        Matricula matricula = new Matricula();
        matricula.setId(2L);
        pagamento.setMatricula(matricula);

        when(pagamentos.findById(4L)).thenReturn(Optional.of(pagamento));
        when(matriculas.findById(2L)).thenReturn(Optional.of(matricula));
        when(pagamentos.saveAndFlush(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDate inicio = LocalDate.now();

        PagamentoService.Saida resultado = service.atualizar(4L, new PagamentoService.Entrada(null, null, null, new BigDecimal("95.00"), StatusPagamento.PAGO, 2L));

        LocalDate fim = LocalDate.now();

        assertEquals(StatusPagamento.PAGO, resultado.status());
        assertNotNull(resultado.dataPagamento());
        assertFalse(resultado.dataPagamento().isBefore(inicio));
        assertFalse(resultado.dataPagamento().isAfter(fim));

        verify(pagamentos).saveAndFlush(pagamento);
    }

    @Test
    void devePreservarDataOriginalAoEstornar() {
        Pagamento pagamento = new Pagamento();
        pagamento.setStatus(StatusPagamento.PAGO);
        pagamento.setValor(new BigDecimal("95.00"));
        pagamento.setDataPagamento(LocalDate.of(2026, 11, 15));

        Matricula matricula = new Matricula();
        matricula.setId(2L);
        pagamento.setMatricula(matricula);

        when(pagamentos.findById(4L)).thenReturn(Optional.of(pagamento));
        when(matriculas.findById(2L)).thenReturn(Optional.of(matricula));
        when(pagamentos.saveAndFlush(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PagamentoService.Saida resultado = service.atualizar(4L, new PagamentoService.Entrada(null, null, LocalDate.of(2026, 11, 15), new BigDecimal("95.00"), StatusPagamento.ESTORNADO, 2L));

        assertEquals(StatusPagamento.ESTORNADO, resultado.status());
        assertEquals(LocalDate.of(2026, 11, 15), resultado.dataPagamento());

        verify(pagamentos).saveAndFlush(pagamento);
    }

    @Test
    void devePreservarDataHistoricaInformada() {
        Pagamento pagamento = new Pagamento();
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setValor(new BigDecimal("95.00"));

        Matricula matricula = new Matricula();
        matricula.setId(2L);
        pagamento.setMatricula(matricula);

        when(pagamentos.findById(4L)).thenReturn(Optional.of(pagamento));
        when(matriculas.findById(2L)).thenReturn(Optional.of(matricula));
        when(pagamentos.saveAndFlush(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDate dataHistorica = LocalDate.of(2025, 6, 15);

        PagamentoService.Saida resultado = service.atualizar(4L, new PagamentoService.Entrada(null, null, dataHistorica, new BigDecimal("95.00"), StatusPagamento.PAGO, 2L));

        assertEquals(StatusPagamento.PAGO, resultado.status());
        assertEquals(dataHistorica, resultado.dataPagamento());

        verify(pagamentos).saveAndFlush(pagamento);
    }

    @Test
    void naoDeveAceitarDataPagamentoFutura() {
        Pagamento pagamento = new Pagamento();
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setValor(new BigDecimal("95.00"));

        Matricula matricula = new Matricula();
        matricula.setId(2L);
        pagamento.setMatricula(matricula);

        when(pagamentos.findById(4L)).thenReturn(Optional.of(pagamento));
        when(matriculas.findById(2L)).thenReturn(Optional.of(matricula));

        PagamentoService.Entrada dados = new PagamentoService.Entrada(null, null, LocalDate.now().plusDays(1), new BigDecimal("95.00"), StatusPagamento.PAGO, 2L);

        assertThrows(IllegalArgumentException.class, () -> service.atualizar(4L, dados));

        verify(pagamentos, never()).saveAndFlush(any());

        //Confirma que o pagamento não foi modificado após o erro.
        assertEquals(StatusPagamento.PENDENTE, pagamento.getStatus());
        assertEquals(new BigDecimal("95.00"), pagamento.getValor());
        assertNull(pagamento.getDataPagamento());

        //A matrícula original também deve ser preservada.
        assertEquals(2L, pagamento.getMatricula().getId());
    }

    @Test
    void devePermitirExcluirPagamentoPendente() {
        Pagamento pagamento = new Pagamento();
        pagamento.setId(4L);
        pagamento.setStatus(StatusPagamento.PENDENTE);

        when(pagamentos.findById(4L)).thenReturn(Optional.of(pagamento));

        //Pagamentos pendentes podem ser excluídos.
        assertDoesNotThrow(() -> service.excluir(4L));

        verify(pagamentos).delete(pagamento);
        verify(pagamentos).flush();
    }

    @Test
    void naoDevePermitirExcluirPagamentoPago() {
        Pagamento pagamento = new Pagamento();
        pagamento.setId(3L);
        pagamento.setStatus(StatusPagamento.PAGO);
        when(pagamentos.findById(3L)).thenReturn(Optional.of(pagamento));

        //Um pagamento concluído deve permanecer no histórico.
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> service.excluir(3L));

        assertEquals("Somente pagamentos pendentes podem ser excluídos.", erro.getMessage());

        //Confirma que nenhuma exclusão foi solicitada.
        verify(pagamentos, never()).delete(any(Pagamento.class));
        verify(pagamentos, never()).flush();
    }

    @Test
    void naoDeveCriarPagamentoJaPago() {
        PagamentoService.Entrada dados = new PagamentoService.Entrada(null, LocalDate.now(), LocalDate.now(), new BigDecimal("100.00"), StatusPagamento.PAGO, 1L);
        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(dados));
        verifyNoInteractions(pagamentos, matriculas);
    }

    @Test
    void naoDeveCadastrarPendenteComDataPagamento() {
        PagamentoService.Entrada dados = new PagamentoService.Entrada(null, LocalDate.now().plusDays(5), LocalDate.now(), new BigDecimal("100.00"), StatusPagamento.PENDENTE, 1L);
        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(dados));
        verifyNoInteractions(pagamentos, matriculas);
    }

    @Test
    void naoDeveCadastrarSemValor() {
        PagamentoService.Entrada dados = new PagamentoService.Entrada(null, LocalDate.now(), null, null, StatusPagamento.PENDENTE, 1L);
        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(dados));
        verifyNoInteractions(pagamentos, matriculas);
    }

    @Test
    void naoDeveInserirDataEmPagamentoAindaPendente() {
        Pagamento pagamento = new Pagamento();
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setValor(new BigDecimal("50.00"));
        Matricula matricula = new Matricula();
        matricula.setId(1L);
        pagamento.setMatricula(matricula);
        when(pagamentos.findById(5L)).thenReturn(Optional.of(pagamento));

        PagamentoService.Entrada dados = new PagamentoService.Entrada(null, null, LocalDate.now(), new BigDecimal("50.00"), StatusPagamento.PENDENTE, 1L);
        assertThrows(IllegalArgumentException.class, () -> service.atualizar(5L, dados));
        assertNull(pagamento.getDataPagamento());
        verify(pagamentos, never()).saveAndFlush(any());
    }
}
