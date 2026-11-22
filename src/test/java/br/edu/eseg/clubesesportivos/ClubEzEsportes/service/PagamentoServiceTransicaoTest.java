package br.edu.eseg.clubesesportivos.ClubEzEsportes.service;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.*;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PagamentoServiceTransicaoTest {
    private final PagamentoRepository pagamentos = mock(PagamentoRepository.class);
    private final MatriculaRepository matriculas = mock(MatriculaRepository.class);
    private final PagamentoService service = new PagamentoService(pagamentos, matriculas);

    @Test void naoPermiteVoltarDePagoParaPendente() {
        Pagamento entidade = new Pagamento();
        entidade.setStatus(StatusPagamento.PAGO);
        when(pagamentos.findById(10L)).thenReturn(Optional.of(entidade));
        PagamentoService.Entrada dados = new PagamentoService.Entrada(null, null, null,
                new BigDecimal("99.90"), StatusPagamento.PENDENTE, 1L);
        assertThrows(IllegalArgumentException.class, () -> service.atualizar(10L, dados));
        verify(pagamentos, never()).saveAndFlush(any());
    }

    @Test void naoPermiteValorNegativoAoCadastrar() {
        PagamentoService.Entrada dados = new PagamentoService.Entrada(null, null, null,
                new BigDecimal("-1.00"), StatusPagamento.PENDENTE, 1L);
        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(dados));
        verifyNoInteractions(pagamentos, matriculas);
    }
}
