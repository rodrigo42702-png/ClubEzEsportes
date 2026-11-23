package br.edu.eseg.clubesesportivos.ClubEzEsportes.service;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.*;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PartidaEventoServiceTest {

    private PartidaEventoRepository partidas;
    private ClubeRepository clubes;
    private ModalidadeRepository modalidades;
    private PartidaEventoService service;
    private Clube clube;
    private Modalidade modalidade;

    @BeforeEach
    void preparar() {
        partidas = mock(PartidaEventoRepository.class);
        clubes = mock(ClubeRepository.class);
        modalidades = mock(ModalidadeRepository.class);
        service = new PartidaEventoService(partidas, clubes, modalidades);

        clube = new Clube();
        clube.setId(1L);
        modalidade = new Modalidade();
        modalidade.setId(2L);
        when(clubes.findById(1L)).thenReturn(Optional.of(clube));
        when(modalidades.findById(2L)).thenReturn(Optional.of(modalidade));
    }

    private PartidaEventoService.Entrada entrada(LocalDate data, String resultado) {
        return new PartidaEventoService.Entrada(null, data, resultado, 1L, 2L);
    }

    private PartidaEvento existente() {
        PartidaEvento partida = new PartidaEvento();
        partida.setId(10L);
        partida.setClube(clube);
        partida.setModalidade(modalidade);
        partida.setCodigoLegado("LEG-10");
        partida.setDataPartida(LocalDate.now().minusDays(2));
        return partida;
    }

    @Test
    void naoDeveCadastrarSemData() {
        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(entrada(null, null)));
        verify(partidas, never()).saveAndFlush(any());
    }

    @Test
    void naoDeveRegistrarResultadoDePartidaFutura() {
        assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(entrada(LocalDate.now().plusDays(1), "2x1")));
        verify(partidas, never()).saveAndFlush(any());
    }

    @Test
    void deveCadastrarPartidaFuturaSemResultado() {
        when(partidas.saveAndFlush(any(PartidaEvento.class))).thenAnswer(chamada -> {
            PartidaEvento partida = chamada.getArgument(0);
            partida.setId(20L);
            return partida;
        });
        var resultado = service.cadastrar(entrada(LocalDate.now().plusDays(1), null));
        assertEquals(20L, resultado.id());
        assertEquals(1L, resultado.clubeId());
        assertEquals(2L, resultado.modalidadeId());
        verify(partidas).saveAndFlush(any(PartidaEvento.class));
    }

    @Test
    void naoDeveAlterarRelacionamentosDaPartida() {
        when(partidas.findById(10L)).thenReturn(Optional.of(existente()));
        var dados = new PartidaEventoService.Entrada("LEG-10", LocalDate.now(), null, 99L, 2L);
        assertThrows(IllegalArgumentException.class, () -> service.atualizar(10L, dados));
        verify(partidas, never()).saveAndFlush(any());
    }

    @Test
    void naoDeveAlterarCodigoLegado() {
        when(partidas.findById(10L)).thenReturn(Optional.of(existente()));
        var dados = new PartidaEventoService.Entrada("NOVO", LocalDate.now(), null, 1L, 2L);
        assertThrows(IllegalArgumentException.class, () -> service.atualizar(10L, dados));
        verify(partidas, never()).saveAndFlush(any());
    }
}
