package br.edu.eseg.clubesesportivos.ClubEzEsportes.service;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Socio;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.SocioRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;
import java.time.LocalDate;

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
        if (socio.getNome() == null || socio.getNome().isBlank()) throw new IllegalArgumentException("O nome do sócio é obrigatório.");


        // Verifica o formato e os dois dígitos verificadores do CPF.
        if (!cpfValido(socio.getCpf())) throw new IllegalArgumentException("Informe um CPF válido com 11 números.");

        //O e-mail deve possuir um formato básico válido.
        if (socio.getEmail() == null || !socio.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw new IllegalArgumentException("Informe um e-mail válido.");

        //A data de nascimento não pode estar no futuro.
        if (socio.getDataNascimento() != null && socio.getDataNascimento().isAfter(java.time.LocalDate.now())) throw new IllegalArgumentException("A data de nascimento não pode ser futura.");
    }

    //Exclui um sócio cadastrado.
    public void excluir(Long id) {
        //Verifica se o sócio existe.
        Socio socio = buscarPorId(id);
        //Remove o registro do banco de dados.
        socioRepository.delete(socio);
    }

    //Verifica se o CPF possui 11 números e dígitos verificadores corretos.
    private boolean cpfValido(String cpf) {
        if (cpf == null || !cpf.matches("[0-9]{11}")) {
            return false;
        }
        //Impede CPFs com todos os números iguais.
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }
        //Calcula o primeiro dígito verificador.
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            int numero = Character.getNumericValue(cpf.charAt(i));
            soma += numero * (10 - i);
        }
        int resto = soma % 11;
        int primeiroDigito = (resto < 2) ? 0 : 11 - resto;
        if (primeiroDigito != Character.getNumericValue(cpf.charAt(9))) {
            return false;
        }
        //Calcula o segundo dígito verificador.
        soma = 0;
        for (int i = 0; i < 10; i++) {
            int numero = Character.getNumericValue(cpf.charAt(i));
            soma += numero * (11 - i);
        }
        resto = soma % 11;
        int segundoDigito = (resto < 2) ? 0 : 11 - resto;
        return segundoDigito == Character.getNumericValue(cpf.charAt(10));
    }

    //Atualiza os dados de um sócio após validar as informações.
    public Socio atualizar(Long id, String nome, String cpf, String email, LocalDate dataNascimento) {
        //Cria um objeto temporário para validar os novos dados.
        Socio dadosNovos = new Socio();
        dadosNovos.setNome(nome == null ? null : nome.trim());
        dadosNovos.setCpf(cpf == null ? null : cpf.trim());
        dadosNovos.setEmail(email == null ? null : email.trim());
        dadosNovos.setDataNascimento(dataNascimento);
        //Valida antes de modificar o registro existente.
        validarSocio(dadosNovos);
        //Busca o registro original no banco.
        Socio socio = buscarPorId(id);
        //Atualiza somente os campos editáveis.
        socio.setNome(dadosNovos.getNome());
        socio.setCpf(dadosNovos.getCpf());
        socio.setEmail(dadosNovos.getEmail());
        socio.setDataNascimento(dadosNovos.getDataNascimento());
        //Mantém o ID e a situação atual do sócio.
        return socioRepository.save(socio);
    }

    //Altera a situação sem exigir CPF ou e-mail dos dados legados.
    public Socio alterarSituacao(Long id) {
        Socio socio = buscarPorId(id);
        //Inverte a situação atual.
        socio.setAtivo(!Boolean.TRUE.equals(socio.isAtivo()));
        return socioRepository.save(socio);
    }
}
