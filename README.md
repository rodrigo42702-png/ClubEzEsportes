### Estratégia de migração dos dados legados

A modernização do ClubEzEsportes prevê a transferência dos dados do sistema legado para o novo modelo relacional implementado com Spring Data JPA e Hibernate.

A migração deverá ocorrer em etapas controladas, garantindo a integridade dos registros e preservando a identificação de sua origem.

**1. Identificação e mapeamento**

Os campos das tabelas legadas deverão ser analisados e associados às entidades do novo sistema.

As entidades que possuem o atributo `codigoLegado` poderão utilizar esse campo para preservar a identificação dos registros originais e facilitar a rastreabilidade durante a migração.

**2. Preparação dos dados**

Antes da importação, deverão ser verificadas possíveis inconsistências, incluindo:

- Registros duplicados.
- Campos obrigatórios não preenchidos.
- Referências a registros inexistentes.
- Valores incompatíveis com os tipos definidos no novo banco.
- Status antigos que não correspondam aos estados aceitos pelo sistema.

Registros inconsistentes deverão ser tratados ou separados para revisão antes da importação.

**3. Importação por dependências**

A ordem de importação deverá respeitar os relacionamentos e as chaves estrangeiras.

Por exemplo, clubes, sócios e planos precisam estar disponíveis antes da importação das matrículas que os referenciam. Da mesma forma, pagamentos dependem das respectivas matrículas.

Os identificadores do banco antigo deverão ser mapeados para os identificadores utilizados no novo banco.

**4. Validação após a migração**

Após cada etapa de importação, deverão ser realizadas verificações de:

- Quantidade de registros importados.
- Unicidade dos campos que possuem restrições.
- Integridade das chaves estrangeiras.
- Correspondência entre registros antigos e novos.
- Consistência dos estados e valores armazenados.

**5. Segurança e recuperação**

A migração deverá ser testada inicialmente em um banco separado, preservando uma cópia dos dados originais.

Em caso de falhas, será necessário corrigir as inconsistências e repetir a importação no ambiente de testes antes de aplicar o processo ao banco de destino.

**Situação atual:** o arquivo `src/main/resources/db/schema.sql` foi executado com sucesso em um banco H2 vazio, criando as 17 tabelas e seus relacionamentos. A importação efetiva dos dados legados ainda não foi implementada ou executada. Esta seção documenta a estratégia prevista para essa atividade.
## 6. Como executar

### Pré-requisitos

- JDK 21
- IntelliJ IDEA ou outra IDE compatível com projetos Maven
- Maven configurado, caso a execução seja realizada pelo terminal

### Execução pelo IntelliJ

1. Abrir a pasta que contém o arquivo `pom.xml`.
2. Aguardar o carregamento das dependências Maven.
3. Localizar a classe `ClubEzEsportesApplication`.
4. Executar a classe principal.
5. Verificar no console se a aplicação iniciou corretamente.

Por padrão, a aplicação é executada em:

`http://localhost:8080`

### Console H2

Com a aplicação em execução, acessar:

`http://localhost:8080/h2-console`

Para o banco principal, utilizar a URL JDBC configurada em `application.properties`, incluindo os parâmetros necessários para selecionar o schema `APP`.

## 7. Endpoints REST

Exemplos de endpoints disponíveis:

| Recurso | Endpoint |
|---|---|
| Clubes | `/api/clubes` |
| Sócios | `/api/socios` |
| Planos | `/api/planos` |
| Matrículas | `/api/matriculas` |
| Pagamentos | `/api/pagamentos` |
| Modalidades | `/api/modalidades` |
| Equipes | `/api/equipes` |
| Treinos | `/api/treinos` |
| Produtos | `/api/produtos-loja` |
| Vendas | `/api/vendas-loja` |
| Indicadores | `/api/indicadores-mensais` |
| Perfis | `/api/perfis` |
| Usuários | `/api/usuarios` |

Os Controllers implementam operações REST usando os métodos HTTP GET, POST, PUT e DELETE.

## 8. Validações e tratamento de erros

Foram implementadas validações de regras de negócio em diferentes Services, incluindo:

- Obrigatoriedade de nomes e identificadores.
- Rejeição de valores monetários negativos.
- Verificação de registros relacionados.
- Validação de intervalos de datas e horários.
- Tratamento de consultas a registros inexistentes.

As APIs utilizam respostas HTTP como:

- `200 OK`: operação de consulta realizada.
- `400 Bad Request`: dados inválidos.
- `404 Not Found`: recurso não encontrado.
- `204 No Content`: exclusão realizada.
- `409 Conflict`: conflitos de integridade tratados pelos Controllers correspondentes.

## 9. Verificações realizadas

Durante o desenvolvimento, foram executados testes manuais utilizando PowerShell e os endpoints REST.

Foram verificados cadastros, consultas, atualizações, exclusões e diferentes relacionamentos JPA.

Também foram executados quatro testes automatizados de `ClubeService` utilizando JUnit, com resultados aprovados.

O script SQL de criação do banco foi executado separadamente, e a consulta ao `INFORMATION_SCHEMA` do H2 confirmou 17 tabelas criadas.