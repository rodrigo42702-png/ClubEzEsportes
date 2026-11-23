package br.edu.eseg.clubesesportivos.ClubEzEsportes.service;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.*;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class IndicadorMensalServiceTest {
    private final IndicadorMensalRepository indicadores=mock(IndicadorMensalRepository.class);
    private final ClubeRepository clubes=mock(ClubeRepository.class);
    private final IndicadorMensalService service=new IndicadorMensalService(indicadores,clubes);
    @Test void naoDeveAceitarChurnSuperiorACemPorCento() {
        var dados=new IndicadorMensalService.Entrada(LocalDate.of(2026,11,1),10,new BigDecimal("1000"),new BigDecimal("101"),1L);
        assertThrows(IllegalArgumentException.class,()->service.cadastrar(dados));
        verify(indicadores,never()).saveAndFlush(any());
    }
    @Test void deveAceitarChurnDeCemPorCento() {
        Clube clube=new Clube();clube.setId(1L);when(clubes.findById(1L)).thenReturn(Optional.of(clube));
        when(indicadores.saveAndFlush(any())).thenAnswer(a->{IndicadorMensal i=a.getArgument(0);i.setId(2L);return i;});
        var dados=new IndicadorMensalService.Entrada(LocalDate.of(2026,11,1),10,new BigDecimal("1000"),new BigDecimal("100"),1L);
        assertEquals(0,service.cadastrar(dados).churnPercentual().compareTo(new BigDecimal("100")));
    }
}
