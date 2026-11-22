package br.edu.eseg.clubesesportivos.ClubEzEsportes.service;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Socio;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.SocioRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import java.time.LocalDate;

class SocioServiceValidacaoTest {
    private final SocioRepository repository = mock(SocioRepository.class);
    private final SocioService service = new SocioService(repository);

    private Socio socioValido() {
        Socio socio = new Socio();
        socio.setNome("Sócio Teste");
        socio.setCpf("12345678901"); // Apenas formato de teste; não representa CPF real.
        socio.setEmail("socio@example.test");
        return socio;
    }

    @Test void rejeitaCpfComTamanhoErrado() {
        Socio socio = socioValido(); socio.setCpf("123");
        assertThrows(IllegalArgumentException.class, () -> service.salvar(socio));
        verifyNoInteractions(repository);
    }

    @Test void rejeitaEmailInvalido() {
        Socio socio = socioValido(); socio.setEmail("invalido");
        assertThrows(IllegalArgumentException.class, () -> service.salvar(socio));
        verifyNoInteractions(repository);
    }

    @Test void rejeitaNascimentoFuturo() {
        Socio socio = socioValido(); socio.setDataNascimento(LocalDate.now().plusDays(1));
        assertThrows(IllegalArgumentException.class, () -> service.salvar(socio));
        verifyNoInteractions(repository);
    }
}
