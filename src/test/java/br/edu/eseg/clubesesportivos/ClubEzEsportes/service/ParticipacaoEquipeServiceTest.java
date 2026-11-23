package br.edu.eseg.clubesesportivos.ClubEzEsportes.service;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.*;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ParticipacaoEquipeServiceTest {
    @Test
    void naoDeveAceitarInscricaoPendente() {
        var participacoes = mock(ParticipacaoEquipeRepository.class);
        var equipes = mock(EquipeRepository.class);
        var inscricoes = mock(InscricaoModalidadeRepository.class);
        var service = new ParticipacaoEquipeService(participacoes, equipes, inscricoes);
        var oferta = new ClubeModalidade(); oferta.setId(7L); oferta.setAtiva(true);
        var equipe = new Equipe(); equipe.setId(2L); equipe.setClubeModalidade(oferta);
        equipe.setAtiva(true); equipe.setCapacidadeMaxima(10);
        var inscricao = new InscricaoModalidade(); inscricao.setId(3L);
        inscricao.setClubeModalidade(oferta); inscricao.setStatus(StatusInscricao.PENDENTE);
        when(equipes.findById(2L)).thenReturn(Optional.of(equipe));
        when(inscricoes.findById(3L)).thenReturn(Optional.of(inscricao));
        var entrada = new ParticipacaoEquipeService.Entrada(LocalDate.now(), null, true, 2L, 3L);
        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(entrada));
        verify(participacoes, never()).saveAndFlush(any());
    }

    @Test
    void naoDeveUltrapassarCapacidadeDaEquipe() {
        var participacoes = mock(ParticipacaoEquipeRepository.class);
        var equipes = mock(EquipeRepository.class);
        var inscricoes = mock(InscricaoModalidadeRepository.class);
        var service = new ParticipacaoEquipeService(participacoes, equipes, inscricoes);
        var oferta = new ClubeModalidade(); oferta.setId(7L); oferta.setAtiva(true);
        var equipe = new Equipe(); equipe.setId(2L); equipe.setClubeModalidade(oferta);
        equipe.setAtiva(true); equipe.setCapacidadeMaxima(1);
        var inscricao = new InscricaoModalidade(); inscricao.setId(3L);
        inscricao.setClubeModalidade(oferta); inscricao.setStatus(StatusInscricao.ATIVA);
        when(equipes.findById(2L)).thenReturn(Optional.of(equipe));
        when(inscricoes.findById(3L)).thenReturn(Optional.of(inscricao));
        when(participacoes.countByEquipe_IdAndAtivaTrue(2L)).thenReturn(1L);
        var entrada = new ParticipacaoEquipeService.Entrada(LocalDate.now(), null, true, 2L, 3L);
        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(entrada));
        verify(participacoes, never()).saveAndFlush(any());
    }

    @Test
    void devePermitirParticipacaoAtivaQuandoHaVaga() {
        var participacoes = mock(ParticipacaoEquipeRepository.class);
        var equipes = mock(EquipeRepository.class);
        var inscricoes = mock(InscricaoModalidadeRepository.class);
        var service = new ParticipacaoEquipeService(participacoes, equipes, inscricoes);
        var oferta = new ClubeModalidade(); oferta.setId(7L); oferta.setAtiva(true);
        var equipe = new Equipe(); equipe.setId(2L); equipe.setClubeModalidade(oferta);
        equipe.setAtiva(true); equipe.setCapacidadeMaxima(2);
        var inscricao = new InscricaoModalidade(); inscricao.setId(3L);
        inscricao.setClubeModalidade(oferta); inscricao.setStatus(StatusInscricao.ATIVA);
        when(equipes.findById(2L)).thenReturn(Optional.of(equipe));
        when(inscricoes.findById(3L)).thenReturn(Optional.of(inscricao));
        when(participacoes.saveAndFlush(any())).thenAnswer(inv -> {
            ParticipacaoEquipe p = inv.getArgument(0); p.setId(4L); return p;
        });
        var entrada = new ParticipacaoEquipeService.Entrada(LocalDate.now(), null, true, 2L, 3L);
        assertEquals(4L, service.cadastrar(entrada).id());
    }
}
