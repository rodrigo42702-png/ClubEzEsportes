# Auditoria automatizada da migração — ClubEzEsportes

**Data:** 15/11/2026  
**Fonte:** `29_esportes_clubes.xlsx`  
**Destino auditado:** conteúdo de `migracao_legado_clubez.sql`, previamente executado no H2 `TesteMigracao` e conferido por contagens e quatro endpoints REST.

## Resultado

- **12 abas de dados** comparadas à exportação SQL (as abas de documentação/dicionário não são dados migrados).
- **16.925 linhas de origem** e **16.925 comandos INSERT** de destino.
- **Zero registros faltantes, extras ou chaves duplicadas** nos conjuntos comparados.
- **51 mapeamentos/verificações de campos**, incluindo IDs, nomes, datas, valores, status e referências, sem divergências.
- **146 associações clube–modalidade** derivadas das equipes: nenhuma faltante ou extra; a capacidade técnica calculada como o máximo de atletas por par coincide integralmente com o script.

| Origem (aba) | Registros na origem | INSERTs gerados | Diferença |
|---|---:|---:|---:|
| clubes | 30 | 30 | 0 |
| modalidades | 12 | 12 | 0 |
| planos | 7 | 7 | 0 |
| socios | 1.000 | 1.000 | 0 |
| matriculas | 1.400 | 1.400 | 0 |
| equipes | 180 | 180 | 0 |
| partidas_eventos | 720 | 720 | 0 |
| treinos | 6.000 | 6.000 | 0 |
| pagamentos | 4.200 | 4.200 | 0 |
| produtos_loja | 10 | 10 | 0 |
| vendas_loja | 2.500 | 2.500 | 0 |
| indicadores_mensais | 720 | 720 | 0 |
| **Total** | **16.779** | **16.779** | **0** |

**Observação:** os 146 registros adicionais de `clube_modalidade` são **derivados das equipes**, e não de uma aba própria. Assim, 16.779 + 146 = **16.925 INSERTs**.

## Transformações documentadas (não são diferenças indevidas)

- **Sócios:** sem nome, CPF, e-mail ou nascimento na planilha, esses atributos foram importados como `NULL`. O status foi convertido para `ativo` booleano.
- **Equipes:** capacidade máxima foi preenchida com `qtde_atletas`; é uma aproximação operacional, não a capacidade real.
- **ClubeModalidade:** capacidade máxima foi inferida pelo maior `qtde_atletas` das equipes do mesmo par clube–modalidade.
- **Indicadores mensais:** `churn_pct` foi multiplicado por 100 e arredondado a duas casas; os 720 valores corresponderam à transformação esperada.
- **Matrículas:** previsão de fim não foi tratada como fim efetivo; `data_fim` e `valor_contratado` permanecem `NULL` quando não há equivalente.
- **Pagamentos:** `vencimento` permanece `NULL` porque não existe equivalente na planilha.
- **Treinos:** equipe, horário inicial e final ausentes na origem permanecem `NULL`; o código original do clube e da modalidade foi mantido.
- **Tabelas sem origem correspondente:** `perfil`, `usuario`, `inscricao_modalidade` e `participacao_equipe` ficam vazias.
- Outros atributos que não possuem campos correspondentes no modelo de destino não foram preservados. Consultar `RELATORIO_MIGRACAO_CLUBEZ.md` para detalhes.

## Evidência do ambiente executado (informada no procedimento de validação)

- `RUNSCRIPT` executado sem erros no H2 `TesteMigracao`.
- 17 tabelas criadas; 13 tabelas com registros.
- Cinco testes de integridade referencial com zero registros órfãos.
- Quatro endpoints REST consultados com sucesso: clubes (30), sócios (1.000), matrículas (1.400) e pagamentos (4.200).

## Escopo e limitações

Esta auditoria compara **a planilha original com os valores e referências escritos no script SQL**. Ela não faz nova extração linha a linha do arquivo `.mv.db` local, que não foi fornecido aqui. A correspondência entre script e banco executado é sustentada pelas contagens e testes de API já realizados, mas não substitui uma comparação de todos os valores diretamente no banco. Também não mede a preservação dos campos legados sem destino no modelo atual.

**Conclusão:** não foram encontradas divergências nos atributos mapeados e nas transformações verificadas. O resultado dá alta confiança na fidelidade **dos campos efetivamente migrados**, com as limitações de modelagem documentadas.
