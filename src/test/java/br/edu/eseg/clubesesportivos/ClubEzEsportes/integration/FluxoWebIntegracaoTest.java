package br.edu.eseg.clubesesportivos.ClubEzEsportes.integration;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.ClubeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FluxoWebIntegracaoTest {
    @Autowired MockMvc mockMvc;
    @Autowired ClubeRepository clubeRepository;

    @Test
    void paginaPrivadaSemLoginRedireciona() throws Exception {
        mockMvc.perform(get("/painel").accept(MediaType.TEXT_HTML)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cadastroSemCsrfEhBloqueado() throws Exception {
        mockMvc.perform(post("/clubes").param("nome", "Tentativa").param("cidade", "Campinas").param("estado", "SP")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void formularioSalvaEListaClubeNoBancoDeTeste() throws Exception {
        long antes = clubeRepository.count();
        mockMvc.perform(post("/clubes").with(csrf()).param("nome", "Clube Integracao Quarta Entrega").param("cidade", "Campinas").param("estado", "SP")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/clubes"));
        assertEquals(antes + 1, clubeRepository.count());
        mockMvc.perform(get("/clubes")).andExpect(status().isOk()).andExpect(view().name("clubes/lista")).andExpect(content().string(org.hamcrest.Matchers.containsString("Clube Integracao Quarta Entrega")));
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    void funcionarioNaoPodeCadastrarSocioOuPagamento() throws Exception {
        mockMvc.perform(post("/socios").with(csrf())).andExpect(status().isForbidden());
        mockMvc.perform(post("/pagamentos").with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    void funcionarioPodeConsultarPaginas() throws Exception {
        mockMvc.perform(get("/socios")).andExpect(status().isOk()).andExpect(view().name("socios/lista"));
        mockMvc.perform(get("/pagamentos")).andExpect(status().isOk()).andExpect(view().name("pagamentos/lista"));
    }
}
