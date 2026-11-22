package br.edu.eseg.clubesesportivos.ClubEzEsportes.service;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Socio;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.SocioRepository;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.Optional;

class SocioServiceValidacaoTest {
    private final SocioRepository repository = mock(SocioRepository.class);
    private final SocioService service = new SocioService(repository);

    //Cria um sócio válido para reutilizar nos testes.
    private Socio socioValido() {
        Socio socio = new Socio();
        socio.setNome("Sócio Teste");
        socio.setCpf("52998224725");
        socio.setEmail("socio@example.test");
        return socio;
    }

    @Test
    void rejeitaCpfComTamanhoErrado() {
        Socio socio = socioValido();
        socio.setCpf("123");
        assertThrows(IllegalArgumentException.class, () -> service.salvar(socio));
        verifyNoInteractions(repository);
    }

    @Test
    void rejeitaEmailInvalido() {
        Socio socio = socioValido();
        socio.setEmail("invalido");
        assertThrows(IllegalArgumentException.class, () -> service.salvar(socio));
        verifyNoInteractions(repository);
    }

    @Test
    void rejeitaNascimentoFuturo() {
        Socio socio = socioValido();
        socio.setDataNascimento(LocalDate.now().plusDays(1));
        assertThrows(IllegalArgumentException.class, () -> service.salvar(socio));
        verifyNoInteractions(repository);
    }

    @Test
    void deveAceitarCpfValido() {
        Socio socio = socioValido();
        when(repository.save(any(Socio.class))).thenReturn(socio);
        Socio resultado = service.salvar(socio);
        assertEquals("52998224725", resultado.getCpf());
        verify(repository).save(socio);
    }

    @Test
    void deveRejeitarCpfComDigitosVerificadoresInvalidos() {
        Socio socio = socioValido();
        socio.setCpf("12345678900");
        assertThrows(IllegalArgumentException.class, () -> service.salvar(socio));
        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarCpfComTodosOsDigitosIguais() {
        Socio socio = socioValido();
        socio.setCpf("11111111111");
        assertThrows(IllegalArgumentException.class, () -> service.salvar(socio));
        verifyNoInteractions(repository);
    }

    @Test
    void deveAtualizarDadosDoSocio() {
        Socio socio = socioValido();
        socio.setId(1L);
        socio.setAtivo(true);
        when(repository.findById(1L)).thenReturn(Optional.of(socio));
        when(repository.save(any(Socio.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Socio atualizado = service.atualizar(1L, "Novo Nome", "52998224725", "novo@example.com", LocalDate.of(2000, 5, 15));
        assertEquals("Novo Nome", atualizado.getNome());
        assertEquals("novo@example.com", atualizado.getEmail());
        assertTrue(Boolean.TRUE.equals(atualizado.isAtivo()));
        verify(repository).save(socio);
    }

    @Test
    void naoDeveModificarSocioQuandoEdicaoForInvalida() {
        Socio socio = socioValido();
        socio.setId(1L);
        assertThrows(IllegalArgumentException.class, () -> service.atualizar(1L, "Novo Nome", "12345678900", "novo@example.com", LocalDate.of(2000, 5, 15)));
        //Uma edição inválida não deve consultar nem alterar o banco.
        verifyNoInteractions(repository);
    }

    @Test
    void deveDesativarEReativarSocio() {
        Socio socio = socioValido();
        socio.setId(1L);
        socio.setAtivo(true);
        when(repository.findById(1L)).thenReturn(Optional.of(socio));
        when(repository.save(any(Socio.class))).thenAnswer(invocation -> invocation.getArgument(0));
        //Primeira alteração: ativo para inativo.
        Socio desativado = service.alterarSituacao(1L);
        assertFalse(Boolean.TRUE.equals(desativado.isAtivo()));
        //Segunda alteração: inativo para ativo.
        Socio reativado = service.alterarSituacao(1L);
        assertTrue(Boolean.TRUE.equals(reativado.isAtivo()));
        verify(repository, times(2)).save(socio);
    }
}
