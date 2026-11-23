package br.edu.eseg.clubesesportivos.ClubEzEsportes.service;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Plano;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.MatriculaRepository;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.PlanoRepository;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlanoServiceTest {
    private final PlanoRepository planos = mock(PlanoRepository.class);
    private final MatriculaRepository matriculas = mock(MatriculaRepository.class);
    private final PlanoService service = new PlanoService(planos, matriculas);

    private Plano planoExistente() {
        Plano plano = new Plano();
        plano.setId(3L);
        plano.setCodigoLegado("LEG-03");
        plano.setNome("Mensal");
        plano.setValorMensalidade(new BigDecimal("80.00"));
        plano.setAtivo(true);
        return plano;
    }

    @Test
    void naoDeveExcluirPlanoComMatriculas() {
        Plano plano = planoExistente();
        when(planos.findById(3L)).thenReturn(Optional.of(plano));
        when(matriculas.existsByPlano_Id(3L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.excluir(3L));
        verify(planos, never()).delete(any(Plano.class));
    }

    @Test
    void deveExcluirPlanoSemMatriculas() {
        Plano plano = planoExistente();
        when(planos.findById(3L)).thenReturn(Optional.of(plano));
        when(matriculas.existsByPlano_Id(3L)).thenReturn(false);

        service.excluir(3L);
        verify(planos).delete(plano);
    }

    @Test
    void deveReajustarPlanoSemPerderCodigoLegado() {
        Plano plano = planoExistente();
        when(planos.findById(3L)).thenReturn(Optional.of(plano));
        when(planos.save(plano)).thenReturn(plano);

        Plano dados = new Plano();
        dados.setId(3L);
        // API pode não enviar novamente o código legado.
        dados.setNome("Mensal reajustado");
        dados.setValorMensalidade(new BigDecimal("95.00"));
        dados.setAtivo(true);

        Plano retorno = service.salvar(dados);
        assertSame(plano, retorno);
        assertEquals("LEG-03", retorno.getCodigoLegado());
        assertEquals(new BigDecimal("95.00"), retorno.getValorMensalidade());
        verify(planos).save(plano);
    }

    @Test
    void naoDevePermitirTrocarCodigoLegado() {
        Plano plano = planoExistente();
        when(planos.findById(3L)).thenReturn(Optional.of(plano));
        Plano dados = new Plano();
        dados.setId(3L);
        dados.setCodigoLegado("OUTRO-CODIGO");
        dados.setNome("Mensal");
        dados.setValorMensalidade(new BigDecimal("100.00"));
        dados.setAtivo(true);

        assertThrows(IllegalArgumentException.class, () -> service.salvar(dados));
        assertEquals("LEG-03", plano.getCodigoLegado());
        verify(planos, never()).save(any(Plano.class));
    }

    @Test
    void naoDeveAceitarMensalidadeNegativa() {
        Plano plano = new Plano();
        plano.setNome("Mensal");
        plano.setValorMensalidade(new BigDecimal("-10.00"));
        assertThrows(IllegalArgumentException.class, () -> service.salvar(plano));
        verifyNoInteractions(planos, matriculas);
    }
}
