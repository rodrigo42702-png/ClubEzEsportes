package br.edu.eseg.clubesesportivos.ClubEzEsportes.service;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.model.*;
import br.edu.eseg.clubesesportivos.ClubEzEsportes.repository.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ProdutoLojaServiceTest {
    private final ProdutoLojaRepository produtos=mock(ProdutoLojaRepository.class);
    private final VendaLojaRepository vendas=mock(VendaLojaRepository.class);
    private final ProdutoLojaService service=new ProdutoLojaService(produtos,vendas);
    @Test void naoDeveExcluirProdutoComVendas() {
        ProdutoLoja p=new ProdutoLoja();p.setId(3L);
        when(produtos.findById(3L)).thenReturn(Optional.of(p));
        when(vendas.existsByProdutoLoja_Id(3L)).thenReturn(true);
        assertThrows(IllegalArgumentException.class,()->service.excluir(3L));
        verify(produtos,never()).delete(any());
    }
    @Test void devePermitirExcluirProdutoSemVendas() {
        ProdutoLoja p=new ProdutoLoja();p.setId(3L);
        when(produtos.findById(3L)).thenReturn(Optional.of(p));
        service.excluir(3L);
        verify(produtos).delete(p);
    }
    @Test void naoDeveAlterarCodigoLegadoDeProdutoExistente() {
        ProdutoLoja p=new ProdutoLoja();p.setId(3L);p.setCodigoLegado("PROD-1");
        when(produtos.findById(3L)).thenReturn(Optional.of(p));
        var dados=new ProdutoLojaService.Entrada("PROD-2","Produto","Loja",new BigDecimal("5"));
        assertThrows(IllegalArgumentException.class,()->service.atualizar(3L,dados));
        verify(produtos,never()).saveAndFlush(any());
    }
}
