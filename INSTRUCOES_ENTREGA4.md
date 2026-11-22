# Instruções de aplicação — Entrega 4 (ClubEzEsportes)

## Importante
Este ZIP contém **apenas 13 arquivos novos ou alterados** (e este guia), baseados no projeto `ClubEzEsportes(6).zip` enviado em 22/11/2026. Não é um repositório Git nem contém banco H2. **Não substitua o diretório inteiro do projeto**, apenas os arquivos correspondentes nas subpastas do módulo que contém `pom.xml`.

1. Feche a aplicação em execução e faça cópia de segurança do projeto e do arquivo `ClubData.mv.db` com o H2 parado.
2. Abra `C:\Users\rodri\Downloads\ClubEzEsportes\ClubEzEsportes` no Explorador (a pasta que contém `pom.xml`).
3. Extraia o ZIP **dentro desta pasta**, mantendo a estrutura `src/main/...` e `src/test/...`, autorizando substituir os arquivos indicados.
4. Abra no IntelliJ e aguarde a atualização Maven. Não altere o `Working directory` existente; continue usando a pasta externa se quiser acessar o H2 com seus dados antigos.
5. Execute `Maven > Lifecycle > test`. Não execute comandos de `git add` até saber se os testes passaram.
6. Execute a aplicação com `--clubez.bootstrap.enabled=false --server.port=8085` e confirme login, páginas `/clubes`, `/socios`, `/pagamentos`, `/matriculas` e operações ADMIN/FUNCIONARIO.
7. Se os testes passarem, use `git status --short`, `git diff --check` e revise diferenças. Depois faça commit e push.

## Atenção
- O arquivo `README.md` incluído parte da versão atualizada da Entrega 3 fornecida nesta conversa; verifique o `git diff` para conservar edições locais feitas posteriormente.
- O ZIP não modifica diretamente seu GitHub, IntelliJ nem banco local.
- O professor exige fluxos integrados, testes e documentação; estes arquivos só se tornam evidência conclusiva após compilação, execução e commit no seu repositório.
