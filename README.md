# Monster Legion

Mod para **Minecraft Java 1.21.1**, **Fabric** e **Java 21**: monstros que se organizam sob uma liderança. Inclui o Rei Lich com modelo, cajado animado, sons e efeitos.

## Patrulhas noturnas

- Grupos aleatórios de 5 a 8 monstros variados na superfície escura do Overworld à noite, inclusive perto de jogadores em criativo; tentativa padrão de 50% a cada 20 segundos.
- Um comandante adulto com armadura completa e espada de netherita; equipamento não dropa, mesmo com Saque.
- Comandante concede 3 vezes o XP base da espécie. Seguidores obedecem seu alvo e o acompanham.
- Identidade do grupo salva, limites de geração e configuração no servidor.
- Veja [regras, configuração e testes](docs/night-patrols.md). Para testar à noite: `/monsterlegion patrol`.

## Rei Lich

- Modelo GeckoLib e staff anexada ao osso `staff_anchor`, com joia animada.
- Animações de repouso, voo, invocação, comando, ataque, dano e morte.
- Sons e partículas sincronizados com os eventos das animações.
- Glowmasks dos dois modelos, ativadas por padrão e configuráveis no cliente.
- 200 pontos de vida, defesa adaptada do dragão, dano de golems/criaturas amigas e queimadura ao sol.
- Voo com flutuação e busca de linha de visão: reposiciona-se por cima/lados quando o jogador se esconde.
- Invocação de 2 a 4 mobs por conjuração (cooldown de 60 s), caveira sem explosão (cooldown de 10 s) e comando de foco em jogador.
- Invocados do Lich sem prazo de cinco minutos; morrem com seu dono, mantendo o vínculo após descarregar chunks.
- Caveira aplica Wither/Fome por 10 s e alimenta cura gradual; morte concede 10 ossos, 12.000 XP e 25% de chance de staff.
- Invocações seguem o sol vanilla: capacetes protegem quando aplicável e espécies imunes continuam imunes. A do jogador retorna por teleporte ao se afastar 12 blocos, quando há chão seguro na mesma dimensão.
- Staff nas abas Ferramentas e utilidades e Combate do criativo. Botão direito invoca sem som um aliado por jogador durante cinco minutos; botão esquerdo cancela/mata sua invocação sem pancada normal, com reserva persistida; ao expirar ele morre sem drops nem XP.
- Veja [o combate e as regras da staff](docs/lich-combat.md).
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

O JAR instalável fica em `build/libs/monster-legion-0.4.11-dev.jar`. Para jogar fora da IDE, instale Fabric Loader, Fabric API e GeckoLib 4.7.6 para 1.21.1 e coloque o JAR em `mods/`. O mod precisa estar no cliente e no servidor. O workflow também disponibiliza os JARs na seção Actions.

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

A tag permite experimentar com liderança manual. As patrulhas noturnas agora geram comandantes automaticamente; o Rei Lich continua sem geração natural.

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

O roster de navegação dos seguidores é refeito após reiniciar ou recarregar chunks; tags de patrulha, alianças do Lich e reservas da staff são persistidas para manter sua identidade. Seguidores liberados voltam à IA comum, que pode continuar um combate já iniciado. Não há formações, cerco, invasões, proteção especial ou prevenção completa de fogo amigo. Líderes marcados manualmente compartilham seu alvo periodicamente. Aliados do Rei Lich, invocações da staff e seguidores de comandantes de patrulha usam metas dedicadas para obedecer ao alvo de seu dono/líder. A busca roda uma vez por segundo sobre entidades carregadas; ainda precisa de avaliação em servidores grandes.

O Rei Lich já pode ser invocado com `/summon monster_legion:lich_king`; a staff é obtida com `/give @s monster_legion:lich_staff`. Veja [o guia de integração](docs/leader-integration.md) para configurações de glowmasks, sons, partículas e prévias de animação. Geração natural do Rei Lich e balanceamento definitivo ficam para a próxima etapa. Não foi escolhida uma licença de distribuição para o projeto.
