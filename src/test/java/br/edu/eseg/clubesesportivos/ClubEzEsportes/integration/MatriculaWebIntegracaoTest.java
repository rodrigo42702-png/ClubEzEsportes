package br.edu.eseg.clubesesportivos.ClubEzEsportes.integration;

import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Clube;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Matricula;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Plano;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.Socio;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.ClubeRepository;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.MatriculaRepository;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.PlanoRepository;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.SocioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // Os registros criados neste teste são desfeitos após a execução.
class MatriculaWebIntegracaoTest {
    @Autowired MockMvc mockMvc;
    @Autowired ClubeRepository clubeRepository;
    @Autowired SocioRepository socioRepository;
    @Autowired PlanoRepository planoRepository;
    @Autowired MatriculaRepository matriculaRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminVisualizaFormulario() throws Exception {
        mockMvc.perform(get("/matriculas"))
                .andExpect(status().isOk())
                .andExpect(view().name("matriculas/lista"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Cadastrar matrícula")));
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    void funcionarioPodeConsultarMasNaoVeFormulario() throws Exception {
        mockMvc.perform(get("/matriculas"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("Cadastrar matrícula"))));
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    void funcionarioNaoPodeCadastrar() throws Exception {
        mockMvc.perform(post("/matriculas").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cadastroSemCsrfEhBloqueado() throws Exception {
        mockMvc.perform(post("/matriculas"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCadastraMatriculaComValorDoPlano() throws Exception {
        Clube clube = new Clube();
        clube.setNome("Clube de Teste Web");
        clube.setCidade("São Paulo");
        clube.setEstado("SP");
        clube.setAtivo(true);
        Clube clubeSalvo = clubeRepository.saveAndFlush(clube);

        Socio socio = new Socio();
        socio.setNome("Sócio de Teste Web");
        socio.setAtivo(true);
        Socio socioSalvo = socioRepository.saveAndFlush(socio);

        Plano plano = new Plano();
        plano.setNome("Plano de Teste Web");
        plano.setValorMensalidade(new BigDecimal("145.50"));
        plano.setAtivo(true);
        Plano planoSalvo = planoRepository.saveAndFlush(plano);

        long antes = matriculaRepository.count();
        mockMvc.perform(post("/matriculas").with(csrf())
                        .param("clubeId", clubeSalvo.getId().toString())
                        .param("socioId", socioSalvo.getId().toString())
                        .param("planoId", planoSalvo.getId().toString())
                        .param("dataInicio", LocalDate.now().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/matriculas"))
                .andExpect(flash().attribute("sucesso", "Matrícula cadastrada com sucesso!"));
        assertEquals(antes + 1, matriculaRepository.count());
        List<Matricula> novas = matriculaRepository.findAll();
        Matricula criada = novas.stream()
                .filter(m -> m.getClube().getId().equals(clubeSalvo.getId()))
                .filter(m -> m.getSocio().getId().equals(socioSalvo.getId()))
                .findFirst().orElseThrow();
        assertEquals(0, new BigDecimal("145.50").compareTo(criada.getValorContratado()));
    }
}
