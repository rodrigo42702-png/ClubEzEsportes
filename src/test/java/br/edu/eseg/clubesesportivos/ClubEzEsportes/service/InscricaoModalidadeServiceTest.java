package br.edu.eseg.clubesesportivos.ClubEzEsportes.service;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.*;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.*;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InscricaoModalidadeServiceTest {

    @Test
    void devePermitirInscricaoComMatriculaValida() {
        //Simula os repositories.
        InscricaoModalidadeRepository inscricaoRepository = mock(InscricaoModalidadeRepository.class);

        SocioRepository socioRepository = mock(SocioRepository.class);
        ClubeModalidadeRepository clubeModalidadeRepository = mock(ClubeModalidadeRepository.class);

        MatriculaRepository matriculaRepository = mock(MatriculaRepository.class);

        InscricaoModalidadeService service = new InscricaoModalidadeService(inscricaoRepository, socioRepository, clubeModalidadeRepository, matriculaRepository);

        //Cria um sócio e um clube fictícios.
        Socio socio = new Socio();
        socio.setId(1L);

        Clube clube = new Clube();
        clube.setId(2L);

        //Modalidade oferecida pelo clube.
        ClubeModalidade clubeModalidade = new ClubeModalidade();
        clubeModalidade.setId(3L);
        clubeModalidade.setClube(clube);
        clubeModalidade.setAtiva(true);
        clubeModalidade.setCapacidadeMaxima(20);

        //Matrícula ativa e dentro do período de vigência.
        Matricula matricula = new Matricula();
        matricula.setId(10L);
        matricula.setStatus(StatusMatricula.ATIVA);
        matricula.setDataInicio(LocalDate.now().minusDays(10));
        matricula.setDataFim(null);

        when(socioRepository.findById(1L)).thenReturn(Optional.of(socio));

        when(clubeModalidadeRepository.findById(3L)).thenReturn(Optional.of(clubeModalidade));

        //O sócio possui uma matrícula ativa neste clube.
        when(matriculaRepository.findBySocio_IdAndClube_IdAndStatus(1L, 2L, StatusMatricula.ATIVA)).thenReturn(List.of(matricula));

        //Simula o salvamento da inscrição.
        when(inscricaoRepository.saveAndFlush(any(InscricaoModalidade.class))).thenAnswer(invocacao -> {InscricaoModalidade inscricao = invocacao.getArgument(0);inscricao.setId(20L);return inscricao;});

        //Solicita uma inscrição na modalidade.
        InscricaoModalidadeService.Entrada dados = new InscricaoModalidadeService.Entrada(LocalDate.now(), StatusInscricao.PENDENTE, 1L, 3L);

        InscricaoModalidadeService.Saida resultado = service.cadastrar(dados);

        //Confirma que a inscrição foi aceita.
        assertEquals(20L, resultado.id());
        assertEquals(1L, resultado.socioId());
        assertEquals(3L, resultado.clubeModalidadeId());
        assertEquals(StatusInscricao.PENDENTE, resultado.status());

        //Confirma que a matrícula foi consultada.
        verify(matriculaRepository).findBySocio_IdAndClube_IdAndStatus(1L, 2L, StatusMatricula.ATIVA);

        //Confirma que a inscrição foi salva.
        verify(inscricaoRepository).saveAndFlush(any(InscricaoModalidade.class));
    }

    @Test
    void naoDevePermitirInscricaoSemMatriculaValida() {
        InscricaoModalidadeRepository inscricaoRepo = mock(InscricaoModalidadeRepository.class);
        SocioRepository socioRepo = mock(SocioRepository.class);
        ClubeModalidadeRepository ofertaRepo = mock(ClubeModalidadeRepository.class);
        MatriculaRepository matriculaRepo = mock(MatriculaRepository.class);
        InscricaoModalidadeService service = new InscricaoModalidadeService(inscricaoRepo, socioRepo, ofertaRepo, matriculaRepo);

        Socio socio = new Socio(); socio.setId(1L);
        Clube clube = new Clube(); clube.setId(2L);
        ClubeModalidade oferta = new ClubeModalidade();
        oferta.setId(3L); oferta.setClube(clube); oferta.setAtiva(true); oferta.setCapacidadeMaxima(10);
        when(socioRepo.findById(1L)).thenReturn(Optional.of(socio));
        when(ofertaRepo.findById(3L)).thenReturn(Optional.of(oferta));
        when(matriculaRepo.findBySocio_IdAndClube_IdAndStatus(1L, 2L, StatusMatricula.ATIVA))
                .thenReturn(List.of());

        InscricaoModalidadeService.Entrada dados = new InscricaoModalidadeService.Entrada(LocalDate.now(), StatusInscricao.PENDENTE, 1L, 3L);
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> service.cadastrar(dados));
        assertEquals("O sócio precisa possuir matrícula ativa e vigente neste clube.", erro.getMessage());
        verify(inscricaoRepo, never()).saveAndFlush(any());
    }

    @Test
    void naoDevePermitirInscricaoEmOfertaInativa() {
        InscricaoModalidadeRepository inscricaoRepo = mock(InscricaoModalidadeRepository.class);
        SocioRepository socioRepo = mock(SocioRepository.class);
        ClubeModalidadeRepository ofertaRepo = mock(ClubeModalidadeRepository.class);
        MatriculaRepository matriculaRepo = mock(MatriculaRepository.class);
        InscricaoModalidadeService service = new InscricaoModalidadeService(inscricaoRepo, socioRepo, ofertaRepo, matriculaRepo);

        Socio socio = new Socio(); socio.setId(1L);
        Clube clube = new Clube(); clube.setId(2L);
        ClubeModalidade oferta = new ClubeModalidade();
        oferta.setId(3L); oferta.setClube(clube); oferta.setAtiva(false); oferta.setCapacidadeMaxima(10);
        when(socioRepo.findById(1L)).thenReturn(Optional.of(socio));
        when(ofertaRepo.findById(3L)).thenReturn(Optional.of(oferta));

        InscricaoModalidadeService.Entrada dados = new InscricaoModalidadeService.Entrada(LocalDate.now(), StatusInscricao.PENDENTE, 1L, 3L);
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> service.cadastrar(dados));
        assertEquals("A modalidade não está disponível para inscrições.", erro.getMessage());
        verifyNoInteractions(matriculaRepo);
        verify(inscricaoRepo, never()).saveAndFlush(any());
    }

    @Test
    void naoDevePermitirTrocarSocioDeInscricao() {
        InscricaoModalidadeRepository inscricaoRepo = mock(InscricaoModalidadeRepository.class);
        SocioRepository socioRepo = mock(SocioRepository.class);
        ClubeModalidadeRepository ofertaRepo = mock(ClubeModalidadeRepository.class);
        MatriculaRepository matriculaRepo = mock(MatriculaRepository.class);
        InscricaoModalidadeService service = new InscricaoModalidadeService(inscricaoRepo, socioRepo, ofertaRepo, matriculaRepo);

        Socio socio = new Socio(); socio.setId(1L);
        ClubeModalidade oferta = new ClubeModalidade(); oferta.setId(3L);
        InscricaoModalidade existente = new InscricaoModalidade();
        existente.setId(4L); existente.setSocio(socio); existente.setClubeModalidade(oferta);
        existente.setStatus(StatusInscricao.PENDENTE); existente.setDataInscricao(LocalDate.now().minusDays(1));
        when(inscricaoRepo.findById(4L)).thenReturn(Optional.of(existente));

        InscricaoModalidadeService.Entrada dados = new InscricaoModalidadeService.Entrada(existente.getDataInscricao(), StatusInscricao.PENDENTE, 99L, 3L);
        assertThrows(IllegalArgumentException.class, () -> service.atualizar(4L, dados));
        assertEquals(1L, existente.getSocio().getId());
        verify(inscricaoRepo, never()).saveAndFlush(any());
    }
}