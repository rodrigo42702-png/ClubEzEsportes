package br.edu.eseg.clubesesportivos.ClubEzEsportes.service;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Clube;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

//Inicia o contexto do Spring Boot para executar os testes, utilizando os componentes reais da aplicação.
@SpringBootTest
@ActiveProfiles("test")
class ClubeServiceTest {
    //Injeta o Service que contém as regras de negócio dos clubes.
    @Autowired
    private ClubeService clubeService;

    //Testa se o sistema impede o cadastro de um clube sem nome.
    @Test
    void deveRejeitarClubeSemNome() {
        //Cria um clube com nome vazio e estado válido.
        Clube clube = new Clube();
        clube.setNome("");
        clube.setEstado("SP");

        //Verifica se o Service lança uma exceção ao tentar salvar o clube com nome inválido.
        assertThrows(IllegalArgumentException.class, () -> clubeService.salvar(clube));
    }

    //Testa se o sistema rejeita um estado com formato inválido.
    @Test
    void deveRejeitarClubeComEstadoInvalido() {
        //Cria um clube com nome válido e estado inválido.
        Clube clube = new Clube();
        clube.setNome("Clube Olimpico");
        clube.setEstado("123");

        //Verifica se o Service lança uma exceção, pois o estado deve conter exatamente duas letras.
        assertThrows(IllegalArgumentException.class, () -> clubeService.salvar(clube));
    }

    @Test
    void deveCadastrarClubeValido() {
        //Cria um clube com dados válidos.
        Clube clube = new Clube();
        clube.setNome("Clube Olimpico");
        clube.setCidade("Sao Paulo");
        clube.setEstado("SP");
        clube.setAtivo(true);

        //Salva o clube utilizando o Service.
        Clube clubeSalvo = clubeService.salvar(clube);

        //Verifica se o banco gerou um ID para o clube.
        assertNotNull(clubeSalvo.getId());

        //Verifica se os dados foram salvos corretamente.
        assertEquals("Clube Olimpico", clubeSalvo.getNome());
        assertEquals("SP", clubeSalvo.getEstado());
        assertTrue(clubeSalvo.isAtivo());
    }


    @Test
    void deveBuscarClubePorId() {
        //Cria um clube com dados válidos.
        Clube clube = new Clube();
        clube.setNome("Clube Esportivo");
        clube.setCidade("Campinas");
        clube.setEstado("SP");
        clube.setAtivo(true);

        //Salva o clube no banco de testes.
        Clube clubeSalvo = clubeService.salvar(clube);

        //Busca o clube utilizando o ID gerado.
        Clube clubeEncontrado = clubeService.buscarPorId(clubeSalvo.getId());

        //Verifica se encontrou o clube correto.
        assertNotNull(clubeEncontrado);
        assertEquals(clubeSalvo.getId(), clubeEncontrado.getId());
        assertEquals("Clube Esportivo", clubeEncontrado.getNome());
        assertEquals("Campinas", clubeEncontrado.getCidade());
        assertEquals("SP", clubeEncontrado.getEstado());
    }
}
