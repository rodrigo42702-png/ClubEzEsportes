package br.edu.eseg.clubesesportivos.ClubEzEsportes.service;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Perfil;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Usuario;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.*;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UsuarioServiceTest {
    @Test
    public void deveCriptografarSenhaAoCadastrar() {
        //Simula os repositórios, sem utilizar o banco.
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        PerfilRepository perfilRepository = mock(PerfilRepository.class);
        SocioRepository socioRepository = mock(SocioRepository.class);
        //Usa o mesmo tipo de encoder configurado no sistema.
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        UsuarioService service = new UsuarioService(usuarioRepository, perfilRepository, socioRepository, encoder);
        //Simula um perfil existente.
        Perfil perfil = mock(Perfil.class);
        when(perfilRepository.findById(1L)).thenReturn(Optional.of(perfil));
        //Retorna a entidade recebida pelo repositório.
        when(usuarioRepository.saveAndFlush(any(Usuario.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
        //Dados utilizados no teste.
        String senhaOriginal = "SenhaTeste123!";
        UsuarioService.Entrada dados = new UsuarioService.Entrada("teste@exemplo.com", senhaOriginal, true, 1L, null);
        //Executa o cadastro.
        service.cadastrar(dados);
        //Recupera o usuário enviado para persistência.
        var captor = org.mockito.ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).saveAndFlush(captor.capture());
        Usuario usuarioSalvo = captor.getValue();
        //Confirma que a senha não foi armazenada em texto puro.
        assertNotEquals(senhaOriginal, usuarioSalvo.getSenhaHash());
        //Confirma que o hash corresponde à senha original.
        assertTrue(encoder.matches(senhaOriginal, usuarioSalvo.getSenhaHash()));
    }
}
