package br.edu.eseg.clubesesportivos.ClubEzEsportes.service;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.*;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.*;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MatriculaServiceTest {

    @Test
    void naoDevePermitirAlterarValorContratado() {
        //Criando os repositories simulados.
        MatriculaRepository matriculaRepository = mock(MatriculaRepository.class);
        ClubeRepository clubeRepository = mock(ClubeRepository.class);
        SocioRepository socioRepository = mock(SocioRepository.class);
        PlanoRepository planoRepository = mock(PlanoRepository.class);

        MatriculaService service = new MatriculaService(matriculaRepository, clubeRepository, socioRepository, planoRepository,
                mock(PagamentoRepository.class), mock(InscricaoModalidadeRepository.class));

        //Matrícula já existente, com valor contratado de R$ 100,00.
        Matricula matricula = new Matricula();
        matricula.setId(1L);
        matricula.setValorContratado(new BigDecimal("100.00"));
        matricula.setStatus(StatusMatricula.ATIVA);

        when(matriculaRepository.findById(1L)).thenReturn(Optional.of(matricula));

        //Tentativa de alterar o valor contratado para R$ 150,00.
        MatriculaService.EntradaMatricula dados = new MatriculaService.EntradaMatricula(null, LocalDate.of(2026, 11, 22), null, new BigDecimal("150.00"), StatusMatricula.ATIVA, 1L, 1L, 1L);

        //Verifica se a alteração é rejeitada.
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> service.atualizar(1L, dados));

        assertEquals("O valor contratado não pode ser alterado após o cadastro.", erro.getMessage());

        //Confirma que o valor original foi preservado.
        assertEquals(0, new BigDecimal("100.00").compareTo(matricula.getValorContratado()));

        //Confirma que nenhuma atualização foi enviada ao repository.
        verify(matriculaRepository, never()).saveAndFlush(any(Matricula.class));
    }
    @Test
    void naoDevePermitirAlterarMatriculasFinalizadas() {
        //Testa os dois estados definitivos da matrícula.
        StatusMatricula[] statusFinalizados = {StatusMatricula.CANCELADA, StatusMatricula.ENCERRADA};

        for (StatusMatricula status : statusFinalizados) {
            MatriculaRepository matriculaRepository = mock(MatriculaRepository.class);
            ClubeRepository clubeRepository = mock(ClubeRepository.class);
            SocioRepository socioRepository = mock(SocioRepository.class);
            PlanoRepository planoRepository = mock(PlanoRepository.class);

            MatriculaService service = new MatriculaService(matriculaRepository, clubeRepository, socioRepository, planoRepository,
                mock(PagamentoRepository.class), mock(InscricaoModalidadeRepository.class));

            //Simula uma matrícula já finalizada.
            Matricula matricula = new Matricula();
            matricula.setId(1L);
            matricula.setStatus(status);
            matricula.setValorContratado(new BigDecimal("100.00"));

            when(matriculaRepository.findById(1L)).thenReturn(Optional.of(matricula));

            //Tenta atualizar a matrícula mantendo o mesmo status.
            MatriculaService.EntradaMatricula dados = new MatriculaService.EntradaMatricula(null, LocalDate.of(2026, 11, 22), null, new BigDecimal("100.00"), status, 1L, 1L, 1L);

            //A atualização precisa ser rejeitada.
            IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> service.atualizar(1L, dados));

            assertEquals("Não é permitido alterar uma matrícula " + status, erro.getMessage());

            //Confirma que a matrícula não foi salva novamente.
            verify(matriculaRepository, never()).saveAndFlush(any(Matricula.class));
        }
    }

    @Test
    void devePermitirMatriculaAtivaFicarInadimplente() {
        //Simula os repositories.
        MatriculaRepository matriculaRepository = mock(MatriculaRepository.class);
        ClubeRepository clubeRepository = mock(ClubeRepository.class);
        SocioRepository socioRepository = mock(SocioRepository.class);
        PlanoRepository planoRepository = mock(PlanoRepository.class);

        MatriculaService service = new MatriculaService(matriculaRepository, clubeRepository, socioRepository, planoRepository,
                mock(PagamentoRepository.class), mock(InscricaoModalidadeRepository.class));

        //Cria os registros relacionados à matrícula.
        Clube clube = new Clube();
        clube.setId(1L);

        Socio socio = new Socio();
        socio.setId(2L);

        Plano plano = new Plano();
        plano.setId(3L);

        //Simula uma matrícula ativa já cadastrada.
        Matricula matricula = new Matricula();
        matricula.setId(10L);
        matricula.setStatus(StatusMatricula.ATIVA);
        matricula.setValorContratado(new BigDecimal("100.00"));
        matricula.setClube(clube);
        matricula.setSocio(socio);
        matricula.setPlano(plano);

        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(matricula));

        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clube));

        when(socioRepository.findById(2L)).thenReturn(Optional.of(socio));

        when(planoRepository.findById(3L)).thenReturn(Optional.of(plano));

        when(matriculaRepository.saveAndFlush(any(Matricula.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        //Solicita a mudança para INADIMPLENTE.
        MatriculaService.EntradaMatricula dados = new MatriculaService.EntradaMatricula(null, LocalDate.of(2026, 11, 22), null, new BigDecimal("100.00"), StatusMatricula.INADIMPLENTE, 1L, 2L, 3L);

        MatriculaService.SaidaMatricula resultado = service.atualizar(10L, dados);

        //Confirma que a transição foi realizada.
        assertEquals(StatusMatricula.INADIMPLENTE, resultado.status());

        //Confirma que o valor contratado foi preservado.
        assertEquals(0, new BigDecimal("100.00").compareTo(resultado.valorContratado()));

        //Confirma que a matrícula foi enviada para salvar.
        verify(matriculaRepository).saveAndFlush(matricula);
    }

    @Test
    void naoDevePermitirAlterarRelacionamentosDaMatricula() {
        //Simula os repositories utilizados pelo Service.
        MatriculaRepository matriculaRepository = mock(MatriculaRepository.class);
        ClubeRepository clubeRepository = mock(ClubeRepository.class);
        SocioRepository socioRepository = mock(SocioRepository.class);
        PlanoRepository planoRepository = mock(PlanoRepository.class);

        MatriculaService service = new MatriculaService(matriculaRepository, clubeRepository, socioRepository, planoRepository,
                mock(PagamentoRepository.class), mock(InscricaoModalidadeRepository.class));

        //Cria os registros relacionados à matrícula original.
        Clube clube = new Clube();
        clube.setId(1L);

        Socio socio = new Socio();
        socio.setId(2L);

        Plano plano = new Plano();
        plano.setId(3L);

        //Simula uma matrícula existente.
        Matricula matricula = new Matricula();
        matricula.setId(10L);
        matricula.setStatus(StatusMatricula.ATIVA);
        matricula.setValorContratado(new BigDecimal("100.00"));
        matricula.setClube(clube);
        matricula.setSocio(socio);
        matricula.setPlano(plano);

        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(matricula));

        //Cada linha tenta modificar um relacionamento diferente.
        Long[][] tentativas = {
                {99L, 2L, 3L}, // Trocar o clube.
                {1L, 99L, 3L}, // Trocar o sócio.
                {1L, 2L, 99L}  // Trocar o plano.
        };

        for (Long[] ids : tentativas) {
            MatriculaService.EntradaMatricula dados = new MatriculaService.EntradaMatricula(null, LocalDate.of(2026, 11, 22), null, new BigDecimal("100.00"), StatusMatricula.ATIVA, ids[0], ids[1], ids[2]);

            //Nenhuma das três alterações deve ser permitida.
            assertThrows(IllegalArgumentException.class, () -> service.atualizar(10L, dados));
        }

        //Confirma que os relacionamentos originais foram preservados.
        assertEquals(1L, matricula.getClube().getId());
        assertEquals(2L, matricula.getSocio().getId());
        assertEquals(3L, matricula.getPlano().getId());

        //Nenhuma tentativa pode chegar ao salvamento.
        verify(matriculaRepository, never()).saveAndFlush(any(Matricula.class));
    }

    @Test
    void devePermitirMesmoSocioEmClubesDiferentes() {
        MatriculaRepository matriculaRepository = mock(MatriculaRepository.class);
        ClubeRepository clubeRepository = mock(ClubeRepository.class);
        SocioRepository socioRepository = mock(SocioRepository.class);
        PlanoRepository planoRepository = mock(PlanoRepository.class);

        MatriculaService service = new MatriculaService(matriculaRepository, clubeRepository, socioRepository, planoRepository,
                mock(PagamentoRepository.class), mock(InscricaoModalidadeRepository.class));

        //Mesmo sócio para as duas matrículas.
        Socio socio = new Socio();
        socio.setId(2L);

        Clube clube1 = new Clube();
        clube1.setId(1L);

        Clube clube2 = new Clube();
        clube2.setId(5L);

        Plano plano = new Plano();
        plano.setId(3L);
        plano.setAtivo(true);

        when(socioRepository.findById(2L)).thenReturn(Optional.of(socio));

        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clube1));

        when(clubeRepository.findById(5L)).thenReturn(Optional.of(clube2));

        when(planoRepository.findById(3L)).thenReturn(Optional.of(plano));

        //Simula o salvamento e atribui IDs diferentes.
        when(matriculaRepository.saveAndFlush(any(Matricula.class))).thenAnswer(invocacao -> {
            Matricula matricula = invocacao.getArgument(0);
                    if (matricula.getClube().getId().equals(1L)) {
                        matricula.setId(10L);
                    } else {
                        matricula.setId(11L);
                    }
                    return matricula;
        });

        MatriculaService.EntradaMatricula dados1 = new MatriculaService.EntradaMatricula(null, LocalDate.of(2026, 11, 22), null, new BigDecimal("100.00"), StatusMatricula.ATIVA, 1L, 2L, 3L);

        MatriculaService.EntradaMatricula dados2 = new MatriculaService.EntradaMatricula(null, LocalDate.of(2026, 11, 22), null, new BigDecimal("150.00"), StatusMatricula.ATIVA, 5L, 2L, 3L);

        MatriculaService.SaidaMatricula resultado1 = service.cadastrar(dados1);
        MatriculaService.SaidaMatricula resultado2 = service.cadastrar(dados2);

        //Confirma que são matrículas diferentes.
        assertNotEquals(resultado1.id(), resultado2.id());

        //Confirma que pertencem ao mesmo sócio.
        assertEquals(2L, resultado1.socioId());
        assertEquals(2L, resultado2.socioId());

        //Confirma que pertencem a clubes diferentes.
        assertEquals(1L, resultado1.clubeId());
        assertEquals(5L, resultado2.clubeId());

        //Confirma os valores contratados individuais.
        assertEquals(0, new BigDecimal("100.00").compareTo(resultado1.valorContratado()));

        assertEquals(0, new BigDecimal("150.00").compareTo(resultado2.valorContratado()));

        verify(matriculaRepository, times(2)).saveAndFlush(any(Matricula.class));
    }

    @Test
    void devePreservarValorContratadoAposReajusteDoPlano() {
        //Simula os repositories.
        MatriculaRepository matriculaRepository = mock(MatriculaRepository.class);
        ClubeRepository clubeRepository = mock(ClubeRepository.class);
        SocioRepository socioRepository = mock(SocioRepository.class);
        PlanoRepository planoRepository = mock(PlanoRepository.class);

        MatriculaService service = new MatriculaService(matriculaRepository, clubeRepository, socioRepository, planoRepository,
                mock(PagamentoRepository.class), mock(InscricaoModalidadeRepository.class));

        //Cria um plano com mensalidade de R$ 100,00.
        Plano plano = new Plano();
        plano.setAtivo(true);
        plano.setId(3L);
        plano.setValorMensalidade(new BigDecimal("100.00"));

        Clube clube = new Clube();
        clube.setId(1L);

        Socio socio = new Socio();
        socio.setId(2L);

        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clube));

        when(socioRepository.findById(2L)).thenReturn(Optional.of(socio));

        when(planoRepository.findById(3L)).thenReturn(Optional.of(plano));

        when(matriculaRepository.saveAndFlush(any(Matricula.class))).thenAnswer(invocacao -> {Matricula matricula = invocacao.getArgument(0);matricula.setId(10L);return matricula;});

        //Cadastra sem informar o valor contratado.
        MatriculaService.EntradaMatricula dados = new MatriculaService.EntradaMatricula(null, LocalDate.of(2026, 11, 22), null, null, StatusMatricula.ATIVA, 1L, 2L, 3L);

        MatriculaService.SaidaMatricula resultado = service.cadastrar(dados);

        //Verifica se a matrícula recebeu o preço do plano.
        assertEquals(0, new BigDecimal("100.00").compareTo(resultado.valorContratado()));

        //Simula um reajuste do plano para R$ 150,00.
        plano.atualizarValor(new BigDecimal("150.00"));

        //O contrato antigo deve continuar com R$ 100,00.
        assertEquals(0, new BigDecimal("100.00").compareTo(resultado.valorContratado()));

        //Confirma que o plano foi reajustado.
        assertEquals(0, new BigDecimal("150.00").compareTo(plano.getValorMensalidade()));

        verify(matriculaRepository).saveAndFlush(any(Matricula.class));
    }

    @Test
    void naoDevePermitirMatriculaEmPlanoInativo() {
        //Simula os repositories.
        MatriculaRepository matriculaRepository = mock(MatriculaRepository.class);
        ClubeRepository clubeRepository = mock(ClubeRepository.class);
        SocioRepository socioRepository = mock(SocioRepository.class);
        PlanoRepository planoRepository = mock(PlanoRepository.class);

        MatriculaService service = new MatriculaService(matriculaRepository, clubeRepository, socioRepository, planoRepository,
                mock(PagamentoRepository.class), mock(InscricaoModalidadeRepository.class));

        //Cria um plano desativado.
        Plano plano = new Plano();
        plano.setId(3L);
        plano.setAtivo(false);
        plano.setValorMensalidade(new BigDecimal("100.00"));

        Clube clube = new Clube();
        clube.setId(1L);

        Socio socio = new Socio();
        socio.setId(2L);

        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clube));

        when(socioRepository.findById(2L)).thenReturn(Optional.of(socio));

        when(planoRepository.findById(3L)).thenReturn(Optional.of(plano));

        //Tenta cadastrar uma matrícula usando o plano inativo.
        MatriculaService.EntradaMatricula dados = new MatriculaService.EntradaMatricula(null, LocalDate.of(2026, 11, 22), null, null, StatusMatricula.ATIVA, 1L, 2L, 3L);

        //O Service deve rejeitar o cadastro.
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> service.cadastrar(dados));

        assertEquals("Não é permitido cadastrar matrícula em um plano inativo.", erro.getMessage());

        //Confirma que a matrícula não foi salva.
        verify(matriculaRepository, never()).saveAndFlush(any(Matricula.class));
    }

    @Test
    void naoDeveExcluirMatriculaComPagamentos() {
        MatriculaRepository matriculas = mock(MatriculaRepository.class);
        PagamentoRepository pagamentos = mock(PagamentoRepository.class);
        InscricaoModalidadeRepository inscricoes = mock(InscricaoModalidadeRepository.class);
        MatriculaService service = new MatriculaService(matriculas, mock(ClubeRepository.class),
                mock(SocioRepository.class), mock(PlanoRepository.class), pagamentos, inscricoes);
        Matricula matricula = new Matricula();
        matricula.setId(10L);
        when(matriculas.findById(10L)).thenReturn(Optional.of(matricula));
        when(pagamentos.existsByMatricula_Id(10L)).thenReturn(true);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.excluir(10L));
        assertEquals("Não é permitido excluir matrícula com pagamentos vinculados.", erro.getMessage());
        verify(matriculas, never()).delete(any(Matricula.class));
        verifyNoInteractions(inscricoes);
    }

    @Test
    void naoDeveExcluirUnicaMatriculaDeSocioComInscricaoAtiva() {
        MatriculaRepository matriculas = mock(MatriculaRepository.class);
        PagamentoRepository pagamentos = mock(PagamentoRepository.class);
        InscricaoModalidadeRepository inscricoes = mock(InscricaoModalidadeRepository.class);
        MatriculaService service = new MatriculaService(matriculas, mock(ClubeRepository.class),
                mock(SocioRepository.class), mock(PlanoRepository.class), pagamentos, inscricoes);

        Clube clube = new Clube(); clube.setId(1L);
        Socio socio = new Socio(); socio.setId(2L);
        Matricula matricula = new Matricula();
        matricula.setId(10L); matricula.setClube(clube); matricula.setSocio(socio);
        matricula.setStatus(StatusMatricula.ATIVA);
        matricula.setDataInicio(LocalDate.now().minusDays(5));

        when(matriculas.findById(10L)).thenReturn(Optional.of(matricula));
        when(inscricoes.existsBySocio_IdAndClubeModalidade_Clube_IdAndStatusIn(
                eq(2L), eq(1L), anyCollection())).thenReturn(true);
        when(matriculas.findBySocio_IdAndClube_IdAndStatus(2L, 1L, StatusMatricula.ATIVA))
                .thenReturn(List.of(matricula));

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.excluir(10L));
        assertTrue(erro.getMessage().contains("única matrícula vigente"));
        verify(matriculas, never()).delete(any(Matricula.class));
    }

    @Test
    void permiteExclusaoQuandoOutraMatriculaVigenteMantemInscricao() {
        MatriculaRepository matriculas = mock(MatriculaRepository.class);
        PagamentoRepository pagamentos = mock(PagamentoRepository.class);
        InscricaoModalidadeRepository inscricoes = mock(InscricaoModalidadeRepository.class);
        MatriculaService service = new MatriculaService(matriculas, mock(ClubeRepository.class),
                mock(SocioRepository.class), mock(PlanoRepository.class), pagamentos, inscricoes);

        Clube clube = new Clube(); clube.setId(1L);
        Socio socio = new Socio(); socio.setId(2L);
        Matricula antiga = new Matricula();
        antiga.setId(10L); antiga.setClube(clube); antiga.setSocio(socio);
        Matricula alternativa = new Matricula();
        alternativa.setId(11L); alternativa.setClube(clube); alternativa.setSocio(socio);
        alternativa.setStatus(StatusMatricula.ATIVA);
        alternativa.setDataInicio(LocalDate.now().minusDays(1));
        alternativa.setDataFim(null);

        when(matriculas.findById(10L)).thenReturn(Optional.of(antiga));
        when(inscricoes.existsBySocio_IdAndClubeModalidade_Clube_IdAndStatusIn(
                eq(2L), eq(1L), anyCollection())).thenReturn(true);
        when(matriculas.findBySocio_IdAndClube_IdAndStatus(2L, 1L, StatusMatricula.ATIVA))
                .thenReturn(List.of(alternativa));

        service.excluir(10L);
        verify(matriculas).delete(antiga);
    }

    @Test
    void permiteExclusaoQuandoNaoHaPagamentosNemInscricoes() {
        MatriculaRepository matriculas = mock(MatriculaRepository.class);
        PagamentoRepository pagamentos = mock(PagamentoRepository.class);
        InscricaoModalidadeRepository inscricoes = mock(InscricaoModalidadeRepository.class);
        MatriculaService service = new MatriculaService(matriculas, mock(ClubeRepository.class),
                mock(SocioRepository.class), mock(PlanoRepository.class), pagamentos, inscricoes);
        Clube clube = new Clube(); clube.setId(1L);
        Socio socio = new Socio(); socio.setId(2L);
        Matricula matricula = new Matricula();
        matricula.setId(10L); matricula.setClube(clube); matricula.setSocio(socio);
        when(matriculas.findById(10L)).thenReturn(Optional.of(matricula));

        service.excluir(10L);
        verify(matriculas).delete(matricula);
    }

    // Novo contrato não pode repetir o mesmo período de um contrato ativo.
    @Test
    void naoDeveCadastrarMatriculaAtivaDuplicada() {
        var repos = mock(MatriculaRepository.class);
        var clubes = mock(ClubeRepository.class);
        var socios = mock(SocioRepository.class);
        var planos = mock(PlanoRepository.class);
        var service = new MatriculaService(repos, clubes, socios, planos,
                mock(PagamentoRepository.class), mock(InscricaoModalidadeRepository.class));

        Clube clube = new Clube(); clube.setId(1L);
        Socio socio = new Socio(); socio.setId(2L);
        Plano plano = new Plano(); plano.setId(3L); plano.setAtivo(true);
        plano.setValorMensalidade(new BigDecimal("99.90"));
        Matricula existente = new Matricula();
        existente.setId(10L); existente.setClube(clube);
        existente.setSocio(socio); existente.setPlano(plano);
        existente.setStatus(StatusMatricula.ATIVA);
        existente.setDataInicio(LocalDate.of(2026, 1, 1));
        when(clubes.findById(1L)).thenReturn(Optional.of(clube));
        when(socios.findById(2L)).thenReturn(Optional.of(socio));
        when(planos.findById(3L)).thenReturn(Optional.of(plano));
        when(repos.findBySocio_IdAndClube_IdAndStatus(2L, 1L, StatusMatricula.ATIVA))
                .thenReturn(List.of(existente));

        var dados = new MatriculaService.EntradaMatricula(null, LocalDate.of(2026, 11, 22),
                null, null, StatusMatricula.ATIVA, 1L, 2L, 3L);
        var erro = assertThrows(IllegalArgumentException.class, () -> service.cadastrar(dados));
        assertTrue(erro.getMessage().contains("Já existe matrícula ativa"));
        verify(repos, never()).saveAndFlush(any(Matricula.class));
    }

    @Test
    void devePermitirNovoContratoDepoisDeMatriculaAtivaComFimAnterior() {
        var repos = mock(MatriculaRepository.class);
        var clubes = mock(ClubeRepository.class);
        var socios = mock(SocioRepository.class);
        var planos = mock(PlanoRepository.class);
        var service = new MatriculaService(repos, clubes, socios, planos,
                mock(PagamentoRepository.class), mock(InscricaoModalidadeRepository.class));
        Clube clube = new Clube(); clube.setId(1L);
        Socio socio = new Socio(); socio.setId(2L);
        Plano plano = new Plano(); plano.setId(3L); plano.setAtivo(true);
        plano.setValorMensalidade(new BigDecimal("99.90"));
        Matricula antiga = new Matricula(); antiga.setId(10L);
        antiga.setPlano(plano); antiga.setStatus(StatusMatricula.ATIVA);
        antiga.setDataInicio(LocalDate.of(2025, 1, 1));
        antiga.setDataFim(LocalDate.of(2025, 12, 31));
        when(clubes.findById(1L)).thenReturn(Optional.of(clube));
        when(socios.findById(2L)).thenReturn(Optional.of(socio));
        when(planos.findById(3L)).thenReturn(Optional.of(plano));
        when(repos.findBySocio_IdAndClube_IdAndStatus(2L, 1L, StatusMatricula.ATIVA))
                .thenReturn(List.of(antiga));
        when(repos.saveAndFlush(any(Matricula.class))).thenAnswer(i -> {
            Matricula nova = i.getArgument(0); nova.setId(11L); return nova;
        });
        var dados = new MatriculaService.EntradaMatricula(null, LocalDate.of(2026, 1, 1),
                null, null, StatusMatricula.ATIVA, 1L, 2L, 3L);
        assertEquals(11L, service.cadastrar(dados).id());
    }

    @Test
    void devePermitirOutroPlanoNoMesmoClube() {
        var repos = mock(MatriculaRepository.class);
        var clubes = mock(ClubeRepository.class);
        var socios = mock(SocioRepository.class);
        var planos = mock(PlanoRepository.class);
        var service = new MatriculaService(repos, clubes, socios, planos,
                mock(PagamentoRepository.class), mock(InscricaoModalidadeRepository.class));
        Clube clube = new Clube(); clube.setId(1L);
        Socio socio = new Socio(); socio.setId(2L);
        Plano planoNovo = new Plano(); planoNovo.setId(3L);
        planoNovo.setAtivo(true); planoNovo.setValorMensalidade(new BigDecimal("99.90"));
        Plano planoAnterior = new Plano(); planoAnterior.setId(4L);
        Matricula antiga = new Matricula(); antiga.setPlano(planoAnterior);
        antiga.setDataInicio(LocalDate.of(2026, 1, 1));
        when(clubes.findById(1L)).thenReturn(Optional.of(clube));
        when(socios.findById(2L)).thenReturn(Optional.of(socio));
        when(planos.findById(3L)).thenReturn(Optional.of(planoNovo));
        when(repos.findBySocio_IdAndClube_IdAndStatus(2L, 1L, StatusMatricula.ATIVA))
                .thenReturn(List.of(antiga));
        when(repos.saveAndFlush(any(Matricula.class))).thenAnswer(i -> {
            Matricula nova = i.getArgument(0); nova.setId(11L); return nova;
        });
        var dados = new MatriculaService.EntradaMatricula(null, LocalDate.of(2026, 11, 22),
                null, null, StatusMatricula.ATIVA, 1L, 2L, 3L);
        assertEquals(11L, service.cadastrar(dados).id());
    }
}
