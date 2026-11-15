package br.edu.eseg.clubesesportivos.ClubEzEsportes.service;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Socio;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.SocioRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class SocioService {
    private final SocioRepository socioRepository;

    //Recebe o Repository para acessar o banco de dados.
    public SocioService(SocioRepository socioRepository) {
        this.socioRepository = socioRepository;
    }

    //Valida os dados e salva o sócio no banco.
    public Socio salvar(Socio socio) {
        validarSocio(socio);
        return socioRepository.save(socio);
    }

    //Retorna todos os sócios cadastrados.
    public List<Socio> listarTodos() {
        return socioRepository.findAll();
    }

    //Busca um sócio pelo ID informado.
    public Socio buscarPorId(Long id) {
        return socioRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Sócio não encontrado: " + id));
    }

    //Verifica se os dados do sócio são válidos antes do cadastro.
    private void validarSocio(Socio socio) {
        //O nome não pode ser nulo ou vazio.
        if (socio.getNome() == null || socio.getNome().isBlank()) {throw new IllegalArgumentException("O nome do sócio é obrigatório.");
        }

        //O CPF deve conter exatamente 11 números.
        if (socio.getCpf() == null || !socio.getCpf().matches("[0-9]{11}")) {throw new IllegalArgumentException("O CPF deve conter exatamente 11 números.");
        }

        //O e-mail deve possuir um formato básico válido.
        if (socio.getEmail() == null || !socio.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {throw new IllegalArgumentException("Informe um e-mail válido.");
        }

        //A data de nascimento não pode estar no futuro.
        if (socio.getDataNascimento() != null && socio.getDataNascimento().isAfter(java.time.LocalDate.now())) {throw new IllegalArgumentException("A data de nascimento não pode ser futura.");
        }
    }

    // Exclui um sócio cadastrado.
    public void excluir(Long id) {
        //Verifica se o sócio existe.
        Socio socio = buscarPorId(id);
        //Remove o registro do banco de dados.
        socioRepository.delete(socio);
    }
}
