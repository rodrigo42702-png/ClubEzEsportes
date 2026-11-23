package br.edu.eseg.clubesesportivos.ClubEzEsportes.service;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.*;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.time.LocalDate;

@Service
public class InscricaoModalidadeService {
    private final InscricaoModalidadeRepository inscricaoModalidadeRepository;
    private final SocioRepository socioRepository;
    private final ClubeModalidadeRepository clubeModalidadeRepository;
    private final MatriculaRepository matriculaRepository;

    public InscricaoModalidadeService(InscricaoModalidadeRepository inscricaoModalidadeRepository, SocioRepository socioRepository, ClubeModalidadeRepository clubeModalidadeRepository, MatriculaRepository matriculaRepository) {
        this.inscricaoModalidadeRepository = inscricaoModalidadeRepository;
        this.socioRepository = socioRepository;
        this.clubeModalidadeRepository = clubeModalidadeRepository;
        this.matriculaRepository = matriculaRepository;
    }

    //Contratos JSON: relacionamentos são enviados por IDs, não objetos JPA.
    public record Entrada(LocalDate dataInscricao, StatusInscricao status, Long socioId, Long clubeModalidadeId) {}

    public record Saida(Long id, LocalDate dataInscricao, StatusInscricao status, Long socioId, Long clubeModalidadeId) {}

    private void validar(Entrada dados) {
        if (dados == null) throw new IllegalArgumentException("Dados obrigatórios.");
        if (dados.dataInscricao() == null) throw new IllegalArgumentException("dataInscricao inválido ou obrigatório.");
        if (dados.status() == null) throw new IllegalArgumentException("status inválido ou obrigatório.");
        if (dados.socioId() == null) throw new IllegalArgumentException("socioId inválido ou obrigatório.");
        if (dados.clubeModalidadeId() == null) throw new IllegalArgumentException("clubeModalidadeId inválido ou obrigatório.");
    }

    private void preencher(InscricaoModalidade entidade, Entrada dados) {
        validar(dados);
        entidade.setDataInscricao(dados.dataInscricao());
        entidade.setStatus(dados.status());
        Socio socio = null;
        if (dados.socioId() != null) {
            socio = socioRepository.findById(dados.socioId()).orElseThrow(() -> new NoSuchElementException("Socio não encontrado: " + dados.socioId()));
        }
        entidade.setSocio(socio);
        ClubeModalidade clubeModalidade = null;
        if (dados.clubeModalidadeId() != null) {
            clubeModalidade = clubeModalidadeRepository.findById(dados.clubeModalidadeId()).orElseThrow(() -> new NoSuchElementException("ClubeModalidade não encontrado: " + dados.clubeModalidadeId()));
        }
        entidade.setClubeModalidade(clubeModalidade);
    }

    private Saida converter(InscricaoModalidade entidade) {
        return new Saida(entidade.getId(), entidade.getDataInscricao(), entidade.getStatus(), entidade.getSocio() == null ? null : entidade.getSocio().getId(), entidade.getClubeModalidade() == null ? null : entidade.getClubeModalidade().getId());
    }

    //Verifica se o sócio possui matrícula ativa e vigente no clube.
    private void validarMatriculaDoSocio(InscricaoModalidade inscricao) {
        Long socioId = inscricao.getSocio().getId();
        Long clubeId = inscricao.getClubeModalidade().getClube().getId();

        //Busca matrículas ativas do sócio no clube da modalidade.
        List<Matricula> matriculas = matriculaRepository.findBySocio_IdAndClube_IdAndStatus(socioId, clubeId, StatusMatricula.ATIVA);

        //Verifica se pelo menos uma matrícula está vigente.
        boolean possuiMatriculaValida = matriculas.stream().anyMatch(Matricula::verificarVigencia);

        if (!possuiMatriculaValida) {
            throw new IllegalArgumentException("O sócio precisa possuir matrícula ativa e vigente neste clube.");
        }
    }

    // Não representa a quantidade de vagas restantes: apenas valida a disponibilidade básica.
    private void validarOfertaAtiva(InscricaoModalidade inscricao) {
        ClubeModalidade oferta = inscricao.getClubeModalidade();
        if (!oferta.isAtiva() || oferta.getCapacidadeMaxima() <= 0) {
            throw new IllegalArgumentException("A modalidade não está disponível para inscrições.");
        }
    }

    @Transactional
    public Saida cadastrar(Entrada dados) {
        InscricaoModalidade entidade = new InscricaoModalidade();
        //Preenche e valida os dados obrigatórios.
        preencher(entidade, dados);
        //Inscrições novas começam pendentes. Outras situações dependem de transição.
        if (entidade.getStatus() != StatusInscricao.PENDENTE) {
            throw new IllegalArgumentException("Uma nova inscrição deve iniciar como PENDENTE.");
        }
        validarOfertaAtiva(entidade);
        //Confirma que o sócio está matriculado no clube.
        validarMatriculaDoSocio(entidade);
        //Salva somente quando as validações forem aprovadas.
        return converter(inscricaoModalidadeRepository.saveAndFlush(entidade));
    }

    @Transactional(readOnly = true)
    public List<Saida> listarTodos() {
        return inscricaoModalidadeRepository.findAll().stream().map(this::converter).toList();
    }

    @Transactional(readOnly = true)
    public Saida buscarPorId(Long id) {
        InscricaoModalidade entidade = inscricaoModalidadeRepository.findById(id).orElseThrow(() -> new NoSuchElementException("InscricaoModalidade não encontrado: " + id));
        return converter(entidade);
    }

    private void validarTransicaoStatus(StatusInscricao atual, StatusInscricao novo) {
        if (novo == null) {
            throw new IllegalArgumentException("O status da inscrição é obrigatório");
        }
        if (atual == novo) {
            return;
        }
        boolean permitido = switch (atual) {
            case PENDENTE -> novo == StatusInscricao.ATIVA || novo == StatusInscricao.CANCELADA;
            case ATIVA -> novo == StatusInscricao.CANCELADA;
            case CANCELADA -> false;
        };
        if (!permitido) {
            throw new IllegalArgumentException("Transição de inscrição não permitida: " + atual + " para " + novo);
        }
    }

    @Transactional
    public Saida atualizar(Long id, Entrada dados) {
        InscricaoModalidade entidade = inscricaoModalidadeRepository.findById(id).orElseThrow(() -> new NoSuchElementException("InscricaoModalidade não encontrado: " + id));
        validar(dados);

        //Registro cancelado é definitivo, mesmo que seja enviado CANCELADA novamente.
        if (entidade.getStatus() == StatusInscricao.CANCELADA) {
            throw new IllegalArgumentException("Não é permitido alterar uma inscrição CANCELADA.");
        }
        if (!entidade.getSocio().getId().equals(dados.socioId())) {
            throw new IllegalArgumentException("Não é permitido alterar o sócio de uma inscrição.");
        }
        if (!entidade.getClubeModalidade().getId().equals(dados.clubeModalidadeId())) {
            throw new IllegalArgumentException("Não é permitido alterar a modalidade de uma inscrição.");
        }
        if (!entidade.getDataInscricao().equals(dados.dataInscricao())) {
            throw new IllegalArgumentException("Não é permitido alterar a data original da inscrição.");
        }

        validarTransicaoStatus(entidade.getStatus(), dados.status());

        //Para ativar uma inscrição, exige que oferta e matrícula continuem válidas.
        if (dados.status() == StatusInscricao.ATIVA) {
            validarOfertaAtiva(entidade);
            validarMatriculaDoSocio(entidade);
        }

        //Os vínculos permanecem intactos; atualizamos somente a situação.
        entidade.setStatus(dados.status());
        return converter(inscricaoModalidadeRepository.saveAndFlush(entidade));
    }

    @Transactional
    public void excluir(Long id) {
        InscricaoModalidade entidade = inscricaoModalidadeRepository.findById(id).orElseThrow(() -> new NoSuchElementException("InscricaoModalidade não encontrado: " + id));
        inscricaoModalidadeRepository.delete(entidade);
        inscricaoModalidadeRepository.flush();
    }
}
