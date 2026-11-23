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
    private final PagamentoRepository pagamentoRepository;
    private final InscricaoModalidadeRepository inscricaoModalidadeRepository;

    //Recebe os Repositories utilizados pela Matrícula.
    public MatriculaService(MatriculaRepository matriculaRepository, ClubeRepository clubeRepository, SocioRepository socioRepository, PlanoRepository planoRepository, PagamentoRepository pagamentoRepository, InscricaoModalidadeRepository inscricaoModalidadeRepository) {
        this.matriculaRepository = matriculaRepository;
        this.clubeRepository = clubeRepository;
        this.socioRepository = socioRepository;
        this.planoRepository = planoRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.inscricaoModalidadeRepository = inscricaoModalidadeRepository;
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

    // Datas finais vazias indicam matrículas sem data de término.
    private boolean periodosSeSobrepoem(Matricula nova, Matricula existente) {
        if (existente.getDataInicio() == null) {
            // Dado legado incompleto: bloquear novo vínculo até revisão manual.
            return true;
        }
        boolean novaComecaAntesDoFimExistente = existente.getDataFim() == null
                || !nova.getDataInicio().isAfter(existente.getDataFim());
        boolean existenteComecaAntesDoFimNovo = nova.getDataFim() == null
                || !existente.getDataInicio().isAfter(nova.getDataFim());
        return novaComecaAntesDoFimExistente && existenteComecaAntesDoFimNovo;
    }

    // Cadastra uma nova matrícula.
    @Transactional
    public SaidaMatricula cadastrar(EntradaMatricula dados) {
        Matricula matricula = new Matricula();
        preencherMatricula(matricula, dados);
        //Impede novas matrículas em planos desativados.
        if (!matricula.getPlano().isAtivo()) {
            throw new IllegalArgumentException("Não é permitido cadastrar matrícula em um plano inativo.");
        }
        // Impede contratos ativos sobrepostos para o mesmo sócio, clube e plano.
        // Registros históricos já existentes não são alterados.
        if (matricula.getStatus() == StatusMatricula.ATIVA) {
            boolean duplicada = matriculaRepository
                    .findBySocio_IdAndClube_IdAndStatus(
                            matricula.getSocio().getId(), matricula.getClube().getId(), StatusMatricula.ATIVA)
                    .stream()
                    .anyMatch(existente ->
                            matricula.getPlano().getId().equals(existente.getPlano().getId())
                            && periodosSeSobrepoem(matricula, existente));
            if (duplicada) {
                throw new IllegalArgumentException(
                        "Já existe matrícula ativa para este sócio, clube e plano no período informado.");
            }
        }
        //Quando não há valor informado, utiliza o preço atual do plano. O preço fica registrado na matrícula e não acompanha reajustes futuros.
        if (matricula.getValorContratado() == null) {
            BigDecimal valorPlano = matricula.getPlano().getValorMensalidade();
            if (valorPlano == null || valorPlano.signum() < 0) {
                throw new IllegalArgumentException("O plano não possui um valor de mensalidade válido.");
            }
            matricula.setValorContratado(valorPlano);
        }
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
        //Impede alterações em matrículas finalizadas.
        if (atual == StatusMatricula.CANCELADA || atual == StatusMatricula.ENCERRADA) {
            throw new IllegalArgumentException("Não é permitido alterar uma matrícula " + atual);
        }

        //Manter o mesmo status é permitido para matrículas não finalizadas.
        if (atual == novo) {
            return;
        }

        boolean transicaoPermitida = false;

        if (atual == StatusMatricula.ATIVA) {
            transicaoPermitida = novo == StatusMatricula.INADIMPLENTE || novo == StatusMatricula.CANCELADA || novo == StatusMatricula.ENCERRADA;
        } else if (atual == StatusMatricula.INADIMPLENTE) {
            transicaoPermitida = novo == StatusMatricula.ATIVA || novo == StatusMatricula.CANCELADA || novo == StatusMatricula.ENCERRADA;
        }

        if (!transicaoPermitida) {
            throw new IllegalArgumentException("Transição de matrícula não permitida: " + atual + " para " + novo);
        }
    }

    //Atualiza uma matrícula existente.
    @Transactional
    public SaidaMatricula atualizar(Long id, EntradaMatricula dados) {
        Matricula matricula = matriculaRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Matrícula não encontrada: " + id));
        //Valida os dados antes de consultar o novo status.
        validarMatricula(dados);
        // Matrículas canceladas ou encerradas não podem ser alteradas.
        if (matricula.getStatus() == StatusMatricula.CANCELADA || matricula.getStatus() == StatusMatricula.ENCERRADA) {
            throw new IllegalArgumentException("Não é permitido alterar uma matrícula " + matricula.getStatus());
        }
        //O valor contratado é um registro histórico da matrícula.
        //Depois do cadastro, não pode ser alterado.
        BigDecimal valorAtual = matricula.getValorContratado();
        BigDecimal valorNovo = dados.valorContratado();
        boolean valorAlterado;
        if (valorAtual == null || valorNovo == null) {
            valorAlterado = valorAtual != valorNovo;
        } else {
            valorAlterado = valorAtual.compareTo(valorNovo) != 0;
        }
        if (valorAlterado) {
            throw new IllegalArgumentException("O valor contratado não pode ser alterado após o cadastro.");
        }
        //Impede mudanças de status proibidas.
        validarTransicaoStatus(matricula.getStatus(), dados.status());
        // Impede a troca dos relacionamentos de uma matrícula existente.
        if (!matricula.getClube().getId().equals(dados.clubeId())) {
            throw new IllegalArgumentException("Não é permitido alterar o clube de uma matrícula.");
        }
        if (!matricula.getSocio().getId().equals(dados.socioId())) {
            throw new IllegalArgumentException("Não é permitido alterar o sócio de uma matrícula.");
        }
        if (!matricula.getPlano().getId().equals(dados.planoId())) {
            throw new IllegalArgumentException("Não é permitido alterar o plano de uma matrícula.");
        }
        //Atualiza os dados e os relacionamentos.
        preencherMatricula(matricula, dados);
        return converterSaida(matriculaRepository.saveAndFlush(matricula));
    }

    //Exclui uma matrícula existente.
    @Transactional
    public void excluir(Long id) {
        Matricula matricula = matriculaRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Matrícula não encontrada: " + id));

        //Pagamentos são histórico financeiro e nunca devem ser apagados por exclusão da matrícula.
        if (pagamentoRepository.existsByMatricula_Id(id)) {
            throw new IllegalArgumentException("Não é permitido excluir matrícula com pagamentos vinculados.");
        }

        //A inscrição está ligada ao sócio e à oferta do clube, não à matrícula. Se existe inscrição não cancelada, precisamos preservar outra matrícula vigente no clube.
        boolean possuiInscricoes = inscricaoModalidadeRepository.existsBySocio_IdAndClubeModalidade_Clube_IdAndStatusIn(matricula.getSocio().getId(), matricula.getClube().getId(), List.of(StatusInscricao.PENDENTE, StatusInscricao.ATIVA));

        if (possuiInscricoes) {
            boolean existeOutraMatriculaValida = matriculaRepository.findBySocio_IdAndClube_IdAndStatus(matricula.getSocio().getId(), matricula.getClube().getId(), StatusMatricula.ATIVA).stream().anyMatch(outra -> !id.equals(outra.getId()) && outra.verificarVigencia());

            if (!existeOutraMatriculaValida) {
                throw new IllegalArgumentException("Não é permitido excluir a única matrícula vigente que sustenta inscrições neste clube.");
            }
        }
        matriculaRepository.delete(matricula);
    }
}
