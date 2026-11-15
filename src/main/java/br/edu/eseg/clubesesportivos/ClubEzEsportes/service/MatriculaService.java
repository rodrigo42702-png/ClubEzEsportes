package br.edu.eseg.clubesesportivos.ClubEzEsportes.service;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.*;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class MatriculaService {
    private final MatriculaRepository matriculaRepository;
    private final ClubeRepository clubeRepository;
    private final SocioRepository socioRepository;
    private final PlanoRepository planoRepository;

    //Recebe os Repositories utilizados pela Matrícula.
    public MatriculaService(MatriculaRepository matriculaRepository, ClubeRepository clubeRepository, SocioRepository socioRepository, PlanoRepository planoRepository) {
        this.matriculaRepository = matriculaRepository;
        this.clubeRepository = clubeRepository;
        this.socioRepository = socioRepository;
        this.planoRepository = planoRepository;
    }

    //Representa os dados recebidos pela API.
    public record EntradaMatricula(String codigoLegado, LocalDate dataInicio, LocalDate dataFim, BigDecimal valorContratado, StatusMatricula status, Long clubeId, Long socioId, Long planoId) {}

    //Representa os dados devolvidos pela API.
    public record SaidaMatricula(Long id, String codigoLegado, LocalDate dataInicio, LocalDate dataFim, BigDecimal valorContratado, StatusMatricula status, Long clubeId, Long socioId, Long planoId) {}

    //Valida os dados obrigatórios e as datas.
    private void validarMatricula(EntradaMatricula dados) {
        if (dados == null) {
            throw new IllegalArgumentException("Os dados da matrícula são obrigatórios.");
        }
        if (dados.dataInicio() == null) {
            throw new IllegalArgumentException("A data de início é obrigatória.");
        }
        if (dados.dataFim() != null && dados.dataFim().isBefore(dados.dataInicio())) {
            throw new IllegalArgumentException("A data de fim não pode ser anterior ao início.");
        }
        if (dados.valorContratado() != null && dados.valorContratado().signum() < 0) {
            throw new IllegalArgumentException("O valor contratado não pode ser negativo.");
        }
        if (dados.status() == null) {
            throw new IllegalArgumentException("O status da matrícula é obrigatório.");
        }
        if (dados.clubeId() == null || dados.socioId() == null || dados.planoId() == null) {
            throw new IllegalArgumentException("Clube, sócio e plano são obrigatórios.");
        }
    }

    //Converte uma entidade em uma resposta com IDs.
    private SaidaMatricula converterSaida(Matricula matricula) {
        return new SaidaMatricula(matricula.getId(), matricula.getCodigoLegado(), matricula.getDataInicio(), matricula.getDataFim(), matricula.getValorContratado(), matricula.getStatus(), matricula.getClube().getId(), matricula.getSocio().getId(), matricula.getPlano().getId());
    }

    //Preenche a matrícula usando registros existentes.
    private void preencherMatricula(Matricula matricula, EntradaMatricula dados) {
        validarMatricula(dados);
        //Procura as entidades relacionadas no banco.
        Clube clube = clubeRepository.findById(dados.clubeId()).orElseThrow(() -> new NoSuchElementException("Clube não encontrado: " + dados.clubeId()));
        Socio socio = socioRepository.findById(dados.socioId()).orElseThrow(() -> new NoSuchElementException("Sócio não encontrado: " + dados.socioId()));
        Plano plano = planoRepository.findById(dados.planoId()).orElseThrow(() -> new NoSuchElementException("Plano não encontrado: " + dados.planoId()));
        //Define os atributos da matrícula.
        matricula.setCodigoLegado(dados.codigoLegado());
        matricula.setDataInicio(dados.dataInicio());
        matricula.setDataFim(dados.dataFim());
        matricula.setValorContratado(dados.valorContratado());
        matricula.setStatus(dados.status());
        //Estabelece os relacionamentos JPA.
        matricula.setClube(clube);
        matricula.setSocio(socio);
        matricula.setPlano(plano);
    }

    //Cadastra uma nova matrícula.
    @Transactional
    public SaidaMatricula cadastrar(EntradaMatricula dados) {
        Matricula matricula = new Matricula();
        preencherMatricula(matricula, dados);
        return converterSaida(matriculaRepository.saveAndFlush(matricula));
    }

    //Lista todas as matrículas.
    @Transactional(readOnly = true)
    public List<SaidaMatricula> listarTodos() {
        return matriculaRepository.findAll().stream().map(this::converterSaida).toList();
    }

    //Busca uma matrícula pelo ID.
    @Transactional(readOnly = true)
    public SaidaMatricula buscarPorId(Long id) {
        Matricula matricula = matriculaRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Matrícula não encontrada: " + id));
        return converterSaida(matricula);
    }

    private void validarTransicaoStatus(StatusMatricula atual, StatusMatricula novo) {
        //Manter o mesmo status não é uma transição inválida.
        if (atual == novo) {
            return;
        }
        //Matrículas canceladas ou encerradas são definitivas.
        if (atual == StatusMatricula.CANCELADA || atual == StatusMatricula.ENCERRADA) {
            throw new IllegalArgumentException("Não é permitido alterar uma matrícula " + atual);
        }
        //Matrículas ativas ou inadimplentes podem mudar para os outros estados definidos no enum.
    }

    //Atualiza uma matrícula existente.
    @Transactional
    public SaidaMatricula atualizar(Long id, EntradaMatricula dados) {
        Matricula matricula = matriculaRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Matrícula não encontrada: " + id));
        //Valida os dados antes de consultar o novo status.
        validarMatricula(dados);
        //Impede mudanças de status proibidas.
        validarTransicaoStatus(matricula.getStatus(), dados.status());
        //Atualiza os dados e os relacionamentos.
        preencherMatricula(matricula, dados);
        return converterSaida(matriculaRepository.saveAndFlush(matricula));
    }

    //Exclui uma matrícula existente.
    @Transactional
    public void excluir(Long id) {
        Matricula matricula = matriculaRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Matrícula não encontrada: " + id));
        matriculaRepository.delete(matricula);
    }
}
