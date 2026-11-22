package br.edu.eseg.clubesesportivos.ClubEzEsportes.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ClubeSegurancaTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "funcionario@teste.com", roles = "FUNCIONARIO")
    public void funcionarioNaoPodeCadastrarClube() throws Exception {
        mockMvc.perform(post("/clubes").param("nome", "Clube Sem Permissao").param("cidade", "Campinas").param("estado", "SP").with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@teste.com", roles = "ADMIN")
    public void adminPodeCadastrarClube() throws Exception {
        mockMvc.perform(post("/clubes").param("nome", "Clube Teste Admin").param("cidade", "Campinas").param("estado", "SP").with(csrf())).andExpect(status().is3xxRedirection());
    }
}
