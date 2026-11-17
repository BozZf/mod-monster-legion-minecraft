# Monster Legion

Mod para **Minecraft Java 1.21.1**, **Fabric** e **Java 21**: monstros que se organizam sob uma liderança. Inclui o Rei Lich com modelo, cajado animado, sons e efeitos.

## Rei Lich

- Modelo GeckoLib e staff anexada ao osso `cajado`, com joia animada.
- Animações de repouso, voo, invocação, comando, ataque, dano e morte.
- Sons e partículas sincronizados com os eventos das animações.
- Glowmasks dos dois modelos, ativadas por padrão e configuráveis no cliente.
- Ovo de invocação e comando de prévia `/monsterlegion animate`.

## O que já existe

- Projeto Gradle com wrapper, Fabric Loader e Fabric API.
- Recrutamento no servidor: um líder reúne até 12 membros num raio de 24 blocos.
- Membros ociosos seguem o líder; o alvo válido do líder é compartilhado a cada segundo com membros próximos.
- Cada membro pertence a apenas um líder. O primeiro líder processado tem preferência.
- Vínculo desfeito quando o líder morre, perde a tag, descarrega ou fica a mais de 48 blocos.
- Lista de monstros recrutáveis configurável por tag de entidades.
- Testes de capacidade, exclusividade e liberação do grupo; build automático no GitHub Actions.

## Preparar e executar

Instale um JDK 21, clone este repositório e abra a pasta como projeto Gradle na IDE.

```sh
# Linux/macOS
./gradlew build
./gradlew runClient

# Windows
.\gradlew.bat build
.\gradlew.bat runClient
```

O JAR instalável fica em `build/libs/monster-legion-0.2.0-dev.jar`. Para jogar fora da IDE, instale Fabric Loader, Fabric API e GeckoLib 4.7.6 para 1.21.1 e coloque o JAR em `mods/`. O mod precisa estar no cliente e no servidor. O workflow também disponibiliza os JARs na seção Actions.

## Testar a liderança provisória

Em um mundo de teste com comandos habilitados:

```mcfunction
/difficulty normal
/time set midnight
/summon minecraft:zombie ~ ~ ~ {Tags:["monster_legion.leader"],PersistenceRequired:1b,CustomName:'{"text":"Líder de teste"}'}
/summon minecraft:zombie ~3 ~ ~ {PersistenceRequired:1b}
/summon minecraft:skeleton ~5 ~ ~ {PersistenceRequired:1b}
```

Afaste-se em criativo e observe os seguidores se aproximarem do líder. Passe para sobrevivência perto do grupo para testar combate. Para remover a liderança:

```mcfunction
/tag @e[tag=monster_legion.leader] remove monster_legion.leader
```

Não existe comando `/legion` nem geração natural de líderes nesta etapa. A tag permite experimentar sem depender de modelo, textura ou animações.

## Onde mexer

| Arquivo/pasta | Função |
| --- | --- |
| `src/main/java/io/github/bozzf/monsterlegion/MonsterLegion.java` | Inicialização e eventos |
| `legion/LegionManager.java` no pacote acima | Recrutamento, limites, raio e alvos |
| `legion/LegionRoster.java` | Regras de associação de membros |
| `legion/FollowLeaderGoal.java` | Navegação dos seguidores |
| `src/main/resources/data/monster_legion/tags/entity_type/recruitable.json` | Tipos recrutáveis, também alteráveis por datapack |
| `art/leader/` | Arquivos originais do futuro líder |
| `docs/leader-integration.md` | Guia para integrar o monstro |

## Limites desta primeira versão

A associação dos seguidores fica em memória. Após reiniciar ou recarregar chunks, o grupo se forma novamente; a tag de liderança fica salva na entidade. Seguidores liberados voltam à IA comum, que pode continuar um combate já iniciado. Não há formações, cerco, invasões, proteção especial ou prevenção completa de fogo amigo. O líder compartilha seu alvo, mas a IA vanilla ainda pode escolher outro alvo entre as atualizações. A busca roda uma vez por segundo sobre entidades carregadas; ainda precisa de avaliação em servidores grandes.

O Rei Lich já pode ser invocado com `/summon monster_legion:lich_king`; a staff é obtida com `/give @s monster_legion:lich_staff`. Veja [o guia de integração](docs/leader-integration.md) para configurações de glowmasks, sons, partículas e prévias de animação. Geração natural e balanceamento definitivo ficam para a próxima etapa. Não foi escolhida uma licença de distribuição para o projeto.
