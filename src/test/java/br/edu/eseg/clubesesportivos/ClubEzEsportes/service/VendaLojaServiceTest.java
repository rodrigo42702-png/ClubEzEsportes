package br.edu.eseg.clubesesportivos.ClubEzEsportes.service;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.*;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class VendaLojaServiceTest {
    private final VendaLojaRepository vendas = mock(VendaLojaRepository.class);
    private final ClubeRepository clubes = mock(ClubeRepository.class);
    private final ProdutoLojaRepository produtos = mock(ProdutoLojaRepository.class);
    private final VendaLojaService service = new VendaLojaService(vendas, clubes, produtos);
    private VendaLojaService.Entrada entrada(int qtd, BigDecimal receita, Long clubeId, Long produtoId) {
        return new VendaLojaService.Entrada(null, LocalDate.now(), qtd, receita, clubeId, produtoId);
    }
    private void referencias() {
        Clube c = new Clube(); c.setId(1L);
        ProdutoLoja p = new ProdutoLoja(); p.setId(2L); p.setPrecoUnitario(new BigDecimal("15.00"));
        when(clubes.findById(1L)).thenReturn(Optional.of(c));
        when(produtos.findById(2L)).thenReturn(Optional.of(p));
    }
    @Test void deveCalcularReceitaApenasQuandoOmitidaEmVendaNova() {
        referencias();
        when(vendas.saveAndFlush(any())).thenAnswer(a -> { VendaLoja v=a.getArgument(0);v.setId(10L);return v; });
        var saida=service.cadastrar(entrada(2,null,1L,2L));
        assertEquals(0,saida.receita().compareTo(new BigDecimal("30.00")));
    }
    @Test void devePreservarReceitaExplicitamenteInformada() {
        referencias();
        when(vendas.saveAndFlush(any())).thenAnswer(a -> { VendaLoja v=a.getArgument(0);v.setId(10L);return v; });
        var saida=service.cadastrar(entrada(2,new BigDecimal("25.50"),1L,2L));
        assertEquals(0,saida.receita().compareTo(new BigDecimal("25.50")));
    }
    @Test void naoDevePermitirQuantidadeZero() {
        assertThrows(IllegalArgumentException.class,()->service.cadastrar(entrada(0,null,1L,2L)));
        verify(vendas,never()).saveAndFlush(any());
    }
    @Test void naoDevePermitirTrocarProdutoDeVendaExistente() {
        Clube clube = new Clube();clube.setId(1L);
        ProdutoLoja produto = new ProdutoLoja();produto.setId(2L);
        VendaLoja existente=new VendaLoja();existente.setId(8L);existente.setClube(clube);existente.setProdutoLoja(produto);
        when(vendas.findById(8L)).thenReturn(Optional.of(existente));
        assertThrows(IllegalArgumentException.class,()->service.atualizar(8L,entrada(2,new BigDecimal("25"),1L,99L)));
        verify(vendas,never()).saveAndFlush(any());
    }
}
