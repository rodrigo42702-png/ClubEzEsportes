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

**Situação da migração — 15/11/2026**

Foi executada a migração dos dados legados para um banco H2 separado, denominado `TesteMigracao`, utilizando os scripts `schema.sql` e `migracao_legado_clubez.sql`.

A execução resultou em **16.925 registros distribuídos por 13 tabelas**, com as 17 tabelas do modelo relacional disponíveis.

Foram realizadas verificações de integridade entre pagamentos e matrículas, matrículas e seus respectivos sócios, clubes e planos, além de equipes e modalidades. Todas as cinco consultas retornaram zero referências inválidas.

A aplicação Spring Boot também foi executada com o banco migrado, e quatro endpoints REST foram validados com sucesso: clubes (30 registros), sócios (1.000), matrículas (1.400) e pagamentos (4.200).

As tabelas `inscricao_modalidade`, `participacao_equipe`, `perfil` e `usuario` permaneceram vazias após a migração. A importação foi validada em ambiente de teste, sem substituir o banco principal `ClubData`. Permanecem as limitações de mapeamento descritas no relatório de migração.

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