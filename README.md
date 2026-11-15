# ClubEzEsportes

Sistema web para gerenciamento de clubes esportivos, desenvolvido como projeto individual da disciplina de Programação Web da ESEG, no segundo semestre de 2026.

## 1. Objetivo

O ClubEzEsportes tem como objetivo modernizar um sistema legado de administração de clubes esportivos, melhorando a organização dos dados, os relacionamentos entre entidades, as regras de negócio e a persistência das informações.

O sistema contempla funcionalidades de gerenciamento de clubes, sócios, planos, matrículas, pagamentos, modalidades esportivas, equipes, treinos, eventos e operações comerciais.

## 2. Tecnologias utilizadas

- Java 21
- Spring Boot 4.1.2
- Spring MVC
- Spring Data JPA
- Hibernate
- H2 Database
- Maven

O projeto também possui dependências de Thymeleaf, destinadas à implementação da interface web.

## 3. Estrutura do projeto

O código-fonte está organizado em quatro pacotes principais:

- `model`: entidades JPA e enumerações do sistema.
- `repository`: interfaces responsáveis pelo acesso ao banco de dados.
- `service`: regras de negócio, validações e operações de persistência.
- `controller`: endpoints REST e tratamento de requisições HTTP.

## 4. Funcionalidades implementadas

O backend contém 17 entidades:

1. Clube
2. Socio
3. Plano
4. Matricula
5. Pagamento
6. Modalidade
7. ClubeModalidade
8. Equipe
9. InscricaoModalidade
10. ParticipacaoEquipe
11. Treino
12. PartidaEvento
13. ProdutoLoja
14. VendaLoja
15. IndicadorMensal
16. Perfil
17. Usuario

Foram implementadas operações de cadastro, consulta, atualização e exclusão por meio de Controllers REST, Services e Repositories.

O sistema utiliza associações JPA para relacionar os registros, incluindo Matrícula com Clube, Sócio e Plano, e Pagamento com Matrícula.

## 5. Banco de dados

O sistema utiliza o banco relacional H2, configurado com persistência em arquivo.

Configuração principal:

- Banco: H2
- Schema: `APP`
- Arquivo: `./database/ClubData`
- Usuário: `sa`
- Senha: vazia, para desenvolvimento local

As configurações estão no arquivo:

`src/main/resources/application.properties`

O Hibernate está configurado com `spring.jpa.hibernate.ddl-auto=update` para atualizar a estrutura do banco durante o desenvolvimento.

### Script SQL

O arquivo `src/main/resources/db/schema.sql` contém os comandos de criação das 17 tabelas e seus relacionamentos.

O script foi executado em um banco H2 separado, chamado `TesteSchema`, e a consulta ao catálogo do banco confirmou a existência das 17 tabelas.

O script deve ser executado apenas em um banco vazio. Ele não deve ser aplicado diretamente sobre o banco principal já inicializado.

O script representa a estrutura atual do banco. A migração dos dados legados constitui uma atividade distinta.

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