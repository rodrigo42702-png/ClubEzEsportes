# Migração do legado — ClubEzEsportes

**Origem:** `29_esportes_clubes.xlsx` (base sintética acadêmica, período 2024–2025). **Destino:** H2, schema APP do projeto Spring Boot.

## Quantidades esperadas no banco vazio

| Tabela nova | Linhas |
|---|---:|
| `clube` | 30 |
| `modalidade` | 12 |
| `plano` | 7 |
| `socio` | 1,000 |
| `clube_modalidade` | 146 |
| `equipe` | 180 |
| `matricula` | 1,400 |
| `pagamento` | 4,200 |
| `partida_evento` | 720 |
| `produto_loja` | 10 |
| `venda_loja` | 2,500 |
| `treino` | 6,000 |
| `indicador_mensal` | 720 |

**Total de linhas INSERT geradas:** 16,925. As tabelas `perfil`, `usuario`, `inscricao_modalidade` e `participacao_equipe` não têm registros correspondentes na base original e permanecem vazias.

## Mapeamento e limitações explícitas

- **Clube:** nome, cidade, estado, `codigoLegado` e ativo (`status=ativo`). Tipo de clube, país, geolocalização e fundação não existem no destino.
- **Modalidade:** nome e `codigoLegado` preservados; categoria usada como descrição. `ativa=TRUE` é valor operacional assumido, pois não há status de modalidade na fonte.
- **Plano:** nome e mensalidade preservados; `ativo=TRUE` assumido por ausência de status na origem. Duração, perfil e multa não têm colunas no destino.
- **Sócio:** preservados os 1.000 IDs legados; `ativo=TRUE` somente para `status_socio=ativo`, demais FALSE. **Nome, CPF, e-mail e nascimento são NULL**, pois não constam na planilha. Não se inventam dados pessoais.
- **ClubeModalidade:** associação inferida dos pares clube–modalidade existentes em equipes. `capacidade_maxima` toma o **maior número de atletas observado em uma equipe** daquele par como aproximação técnica, **não capacidade real**. `ativa=TRUE` presumido.
- **Equipe:** associações preservadas via ClubeModalidade; `capacidade_maxima` recebe `qtde_atletas` como **aproximação, não equivalência semântica**; `ativa=TRUE` presumido. Nome e ID legado preservados.
- **Matrícula:** IDs, início, status e FKs preservados. `data_fim` e `valor_contratado` são NULL, pois `data_fim_prevista` é previsão, não encerramento efetivo, e a fonte não registra valor contratado.
- **Pagamento:** IDs, data, `valor_pago`, status e FK da matrícula preservados. O campo `vencimento` não existe na fonte e é NULL. Valor bruto/desconto e forma de pagamento não têm colunas equivalentes.
- **PartidaEvento:** data, resultado, clube mandante, modalidade e código legado preservados; demais estatísticas não têm campos equivalentes.
- **ProdutoLoja:** nome, categoria, preço e código legado preservados; custo, fornecedor e status não têm campos no destino.
- **VendaLoja:** data, quantidade, receita, FKs e ID legado preservados; preço unitário e canal não têm campos equivalentes.
- **Treino:** data, ID legado e códigos legados de clube/modalidade preservados; a origem não traz equipe ou horas exatas, então `equipe_id`, `hora_inicio`, `hora_fim` ficam NULL; `cancelado=FALSE` é um valor operacional assumido, não comprovado pelo legado.
- **IndicadorMensal:** competência, clube, sócios ativos e receita preservados; `churn_pct` (fração, ex.: 0.0256) foi multiplicado por 100 e arredondado a duas casas para `churn_percentual` (2.56).

## Pré-validação da planilha

- Chaves estrangeiras referenciadas nas planilhas foram checadas e **não foram encontrados IDs órfãos** nos relacionamentos examinados.
- Nos pagamentos, os códigos de sócio e plano foram comparados com os das matrículas correspondentes: **0 divergências**.
- Essas verificações são prévias à execução SQL; **não substituem a validação no H2**.

## Como demonstrar no IntelliJ / H2 Console

1. Pare o perfil `schema`, se estiver usando outro banco. **Nunca execute sobre `ClubData` ou `TesteSchema` já populado.**
2. Crie um banco H2 **novo e vazio**, por exemplo `jdbc:h2:file:./database/TesteMigracao` via uma configuração Spring separada.
3. No H2 Console conectado **explicitamente a `TesteMigracao`**, execute primeiro `src/main/resources/db/schema.sql`.
4. Execute `migracao_legado_clubez.sql` (após copiar para `src/main/resources/db/`), preferencialmente com `RUNSCRIPT FROM` usando um caminho absoluto e pasta sem restrições de acesso.
5. Confira as consultas de contagem no fim do script e os registros `codigo_legado`.
6. Não reexecute a migração no mesmo banco: não é idempotente. Para repetir, descarte apenas o banco **de teste**, recrie-o e rode os scripts na ordem.

## Situação de validação

**Gerado a partir das planilhas e verificado estaticamente.** Não foi executado contra o H2 neste ambiente. Só após `RUNSCRIPT`/H2 Console completar sem erros e as contagens baterem será correto registrar a importação como validada.
