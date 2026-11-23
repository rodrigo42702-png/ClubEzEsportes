package br.edu.eseg.clubesesportivos.ClubEzEsportes.service;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.*;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TreinoServiceTest {
    @Test
    void treinoNovoPrecisaDeEquipe() {
        var service = new TreinoService(mock(TreinoRepository.class), mock(EquipeRepository.class));
        var entrada = new TreinoService.Entrada(null, null, null, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), "Quadra", false, null);
        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(entrada));
    }

    @Test
    void permiteTreinoHistoricoSemEquipe() {
        var repo = mock(TreinoRepository.class);
        var service = new TreinoService(repo, mock(EquipeRepository.class));
        when(repo.saveAndFlush(any())).thenAnswer(inv -> { Treino t = inv.getArgument(0); t.setId(6L); return t; });
        var entrada = new TreinoService.Entrada("LEG-1", "CL-1", "MOD-1", LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), "Quadra", false, null);
        assertEquals(6L, service.cadastrar(entrada).id());
    }

    @Test
    void naoPermiteTreinoEmEquipeInativa() {
        var treinos = mock(TreinoRepository.class);
        var equipes = mock(EquipeRepository.class);
        var service = new TreinoService(treinos, equipes);
        var oferta = new ClubeModalidade(); oferta.setAtiva(true);
        var equipe = new Equipe(); equipe.setId(2L); equipe.setAtiva(false); equipe.setClubeModalidade(oferta);
        when(equipes.findById(2L)).thenReturn(Optional.of(equipe));
        var entrada = new TreinoService.Entrada(null, null, null, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), "Quadra", false, 2L);
        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(entrada));
        verify(treinos, never()).saveAndFlush(any());
    }
}
