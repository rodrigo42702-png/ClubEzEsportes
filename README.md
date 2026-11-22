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

- JDK 21 ou superior (o projeto compila com Java 21; a execução local também foi observada com Java 25).
- IntelliJ IDEA ou outra IDE que suporte Maven; alternativamente, Maven instalado no terminal.
- Acesso à Internet para baixar as dependências Maven e o CSS Bootstrap via CDN no navegador.

### Estrutura e diretório de execução

O arquivo `pom.xml` fica na pasta interna `ClubEzEsportes/ClubEzEsportes/` do pacote de desenvolvimento utilizado. O banco H2 de desenvolvimento `ClubData` usa um caminho **relativo ao diretório de execução**, não ao local do `pom.xml`:

```properties
spring.datasource.url=jdbc:h2:file:./database/ClubData;DB_CLOSE_ON_EXIT=FALSE;INIT=CREATE SCHEMA IF NOT EXISTS APP\\;SET SCHEMA APP
```

**Atenção:** são duas barras invertidas (`\\`) no arquivo `.properties`, necessárias para preservar o escape do `;` da instrução `INIT`. O schema utilizado é `APP`. A configuração atual não utiliza `AUTO_SERVER=TRUE`.

Na execução de desenvolvimento verificada em 22/11/2026, a configuração do IntelliJ apontava o **Working directory** para a pasta externa `ClubEzEsportes/`, de modo a utilizar `ClubEzEsportes/database/ClubData.mv.db`, e os argumentos:

```text
--clubez.bootstrap.enabled=false --server.port=8085
```

Esse é um exemplo específico da máquina de desenvolvimento, **não um caminho obrigatório para outras instalações**. Se o projeto for executado a partir de outra pasta, o H2 pode criar um banco diferente. Confira a localização antes de executar migrações ou cadastrar usuários. Os arquivos `*.mv.db` e `*.trace.db` são ignorados pelo Git e não fazem parte do código publicado.

### Iniciar a aplicação

1. Abra no IntelliJ a pasta que contém o `pom.xml` e aguarde a importação do Maven.
2. Configure o diretório de execução de acordo com o local onde deseja armazenar/abrir o banco H2.
3. Execute a classe `ClubEzEsportesApplication`.
4. No padrão `server.port=8080`, acesse `http://localhost:8080/login`; com `--server.port=8085`, acesse `http://localhost:8085/login`.
5. Caso o banco esteja vazio, crie primeiro os perfis e usuários iniciais conforme a próxima subseção.

### Primeiro acesso: criação de usuários iniciais

O projeto **não possui credenciais padrão publicadas**. A classe `InicializadorUsuarios` só é executada com `clubez.bootstrap.enabled=true` e exige estas **quatro variáveis de ambiente**, definidas na configuração de execução do IntelliJ ou no ambiente do processo:

```text
CLUBEZ_ADMIN_EMAIL
CLUBEZ_ADMIN_SENHA
CLUBEZ_FUNCIONARIO_EMAIL
CLUBEZ_FUNCIONARIO_SENHA
```

Os dois e-mails devem ser diferentes. Na primeira inicialização de um banco sem usuários, defina as quatro variáveis e use o argumento `--clubez.bootstrap.enabled=true`. O inicializador cria, quando necessário, os perfis `ADMIN` e `FUNCIONARIO` e as contas correspondentes com hash BCrypt. Usuários já existentes **não têm suas senhas atualizadas** pelo inicializador. Após confirmar o primeiro login, desligue o inicializador usando `--clubez.bootstrap.enabled=false` e remova as senhas da configuração de execução caso não sejam mais necessárias. **Não coloque senhas no README ou no Git.**

### Console H2 e testes

O console de desenvolvimento está configurado em `/h2-console` (por exemplo, `http://localhost:8085/h2-console` na execução verificada). Seu acesso efetivo depende também da configuração de segurança do servidor; a existência do caminho não garante acesso liberado pelo Spring Security. Não é recomendável expor o console em produção.

Para executar os testes com Maven:

```powershell
mvn test
```

Ou utilize o ciclo `test` do painel Maven do IntelliJ, caso `mvn` não esteja disponível no PATH. O perfil de testes utiliza o H2 **em memória** (`jdbc:h2:mem:clubez_test`), separado do banco `ClubData`.

## 7. Endpoints REST

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

O sistema possui controllers REST para os 17 recursos do domínio, com operações de consulta e manutenção conforme cada controller. Com a configuração de segurança da terceira entrega, a API exige autenticação: as consultas GET em `/api/**` permitem `ADMIN` e `FUNCIONARIO`, **exceto** `/api/usuarios/**` e `/api/perfis/**`, reservadas a `ADMIN`. As demais operações `/api/**` exigem `ADMIN`. A autenticação HTTP Basic foi habilitada para testes REST. Requisições que modificam dados precisam respeitar a proteção CSRF, que permanece habilitada na configuração inspecionada.

## 8. Validações e tratamento de erros

Foram implementadas verificações em Services e Controllers relacionadas a campos obrigatórios, valores monetários, relacionamentos existentes, intervalos de datas e transições de estados. Nos testes manuais da segunda entrega foram observadas respostas como `200 OK`, `400 Bad Request`, `404 Not Found` e `204 No Content`, conforme o recurso e a operação. Também foram observados comportamentos de autorização distintos na terceira entrega, incluindo `403 Forbidden` ao tentar cadastrar clube como FUNCIONARIO.

Esses resultados descrevem cenários exercitados; não significam cobertura automatizada de todos os endpoints.

## 9. Verificações realizadas nas entregas 1 e 2

A modelagem evoluiu a base legada de 12 tabelas de negócio para 17 entidades/tabelas JPA. Em 15/11/2026, `schema.sql` e `migracao_legado_clubez.sql` foram executados **em banco H2 isolado**, `TesteMigracao`, sem substituir `ClubData`. O processo registrou 16.925 inserções em 13 tabelas (16.779 registros da fonte e 146 associações derivadas). As cinco consultas de integridade registradas não encontraram referências órfãs. Foram consultados via Spring Boot os totais migrados de 30 clubes, 1.000 sócios, 1.400 matrículas e 4.200 pagamentos.

A auditoria documentada comparou 51 mapeamentos da planilha com o script SQL; essa checagem não equivale a uma auditoria integral do arquivo físico H2. Consulte `RELATORIO_MIGRACAO_CLUBEZ.md` e `AUDITORIA_MIGRACAO_CLUBEZ_2026-11-15.md` para limitações e detalhes.

A segunda entrega registrou quatro testes automatizados em `ClubeServiceTest` e testes manuais de CRUD e de transições de estado.

## 10. Terceira entrega — Fases 09 e 10 (22/11/2026)

### Fase 09 — Autenticação e autorização

- Spring Security integrado à aplicação e serviço `UsuarioDetailsService` para localizar contas por e-mail no H2.
- Senhas armazenadas como hash BCrypt; `UsuarioService` recebe senha de entrada e não expõe `senhaHash` em seu DTO de saída.
- Perfis `ADMIN` e `FUNCIONARIO` com regras de acesso no servidor; conta inativa não consegue autenticar.
- Login personalizado em `/login`, redirecionamento para `/painel` após autenticação e logout com invalidação de sessão.
- Inicializador opcional de perfis/contas via variáveis de ambiente, sem senha fixa no código.

### Fase 10 — Interface dinâmica

| Página | Rota | Funcionalidade implementada |
|---|---|---|
| Login | `/login` | Formulário HTML de autenticação com Thymeleaf |
| Painel | `/painel` | E-mail e perfil autenticado; contadores de clubes, sócios e matrículas consultados no banco |
| Clubes | `/clubes` | Listagem para usuários autenticados e formulário de cadastro disponível somente para ADMIN |
| Matrículas | `/matriculas` | Listagem das matrículas por meio de `MatriculaService` |

A interface usa Thymeleaf, Bootstrap 5.3.3 via CDN, CSS próprio e fragmento de menu lateral reutilizado nas telas. Os itens **Sócios** e **Pagamentos** aparecem como *em breve* no menu: suas páginas Web não foram concluídas nesta fase. As respectivas APIs REST já pertencem ao backend anterior.

### Testes e evidências

A execução de `mvn test` pelo Maven do IntelliJ, registrada durante o desenvolvimento da terceira entrega, terminou em `BUILD SUCCESS`, com **7 testes automatizados** aprovados: 4 em `ClubeServiceTest`, 2 em `ClubeSegurancaTest` (autorização do cadastro Web de clubes) e 1 em `UsuarioServiceTest` (hash BCrypt). Trata-se da última execução registrada na conversa; **não foi executada nova suíte dentro desta revisão documental**.

Em 22/11/2026, os logs da execução local mostram H2 2.4.240 conectado ao schema `APP` e Tomcat iniciado na porta 8085. A interface foi acessada com usuário ADMIN e exibiu, no banco de desenvolvimento `ClubData`, **2 clubes, 1 sócio e 2 matrículas**. Esses números são dados de desenvolvimento, **não** o resultado da migração isolada.

O commit `75ea78e` (*Entrega 3 - Autenticacao, autorizacao e interface web*) foi enviado à branch `main` do repositório `rodrigo42702-png/ClubEzEsportes` em 22/11/2026.

### Limitações e melhorias futuras

- A interface Web atualmente implementa somente login, painel, clubes e consulta de matrículas; faltam telas Web de sócios, pagamentos e outras rotinas.
- A cobertura automatizada é parcial: sete testes executados, não uma validação de todos os 17 módulos.
- O cadastro/atualização de usuário exige senha no `UsuarioService`; o fluxo de atualização de perfil sem troca de senha não está separado.
- O caminho do banco H2 é relativo ao *Working directory*, o que exige cuidado ao mover ou abrir o projeto em outra IDE.
- As opções de desenvolvimento (`ddl-auto=update`, console H2 e logs SQL detalhados) devem ser revisadas antes de qualquer implantação de produção.

## 11. Referências internas de documentação

- `RELATORIO_MIGRACAO_CLUBEZ.md`: execução e limitações da importação.
- `AUDITORIA_MIGRACAO_CLUBEZ_2026-11-15.md`: confronto do legado com os mapeamentos.
- `src/main/resources/db/schema.sql`: definição relacional de referência.
- `src/main/resources/db/migracao_legado_clubez.sql`: script de importação para ambiente separado.

**Observação:** a terceira entrega amplia o projeto da segunda sem reescrever as evidências históricas das fases 05–08. A avaliação acadêmica depende da análise do professor.
