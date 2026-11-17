# Rei Lich integrado

## Plataforma

Minecraft Java 1.21.1, Fabric Loader 0.16.14+, Fabric API e **GeckoLib 4.7.6**. GeckoLib é dependência obrigatória nos dois lados; instale o JAR Fabric para 1.21.1 em `mods/`. O Gradle baixa a dependência para desenvolvimento automaticamente.

Não é necessário ativar uma opção global do GeckoLib. O mod registra os controladores de animação, os callbacks de sons/partículas e as camadas emissivas.

## Teste rápido

```mcfunction
/summon monster_legion:lich_king ~ ~ ~
/give @s monster_legion:lich_staff
/give @s monster_legion:lich_king_spawn_egg
```

O Rei Lich é hostil, voa para se aproximar de jogadores, invoca até dois zumbis por conjuração respeitando o limite da legião, compartilha seu alvo e lança projéteis mágicos. A staff também lança projéteis com botão direito (intervalo de 1,5 segundo). O projétil usa provisoriamente a aparência da pérola do End e causa 8 pontos de dano mágico, sem explosão. O líder tem 120 pontos de vida e não possui geração natural nesta versão.

Use um mundo de teste. Para verificar cada animação sem esperar pela IA, afaste o líder de jogadores em sobrevivência e use:

```mcfunction
/monsterlegion animate @e[type=monster_legion:lich_king,limit=1,sort=nearest] summon
```

As opções são `idle_float`, `fly_move`, `summon`, `command`, `attack_magic`, `hurt` e `animationdeath`. São prévias visuais de até três segundos: não invocam monstros nem causam dano. A morte real pode ser testada com `/kill @e[type=monster_legion:lich_king,limit=1,sort=nearest]`.

## Partículas e sons

| Animação | Partícula | Momento | Som |
| --- | --- | --- | --- |
| `idle_float` | `minecraft:sculk_soul` | Início de cada ciclo | — |
| `fly_move` | `minecraft:sculk_soul` | Início de cada ciclo | — |
| `sumon` (nome original mantido) | `minecraft:enchant` | Início | `lich.summon` |
| `command` | `minecraft:end_rod` | Início | `lich.command` |
| `attack_magic` | `minecraft:end_rod` | Início | `lich.magic_bolt` |
| `hurt` | — | Início | `lich.hurt` |
| `animationdeath` | `minecraft:dust_plume` | 1,39585 s, metade | `lich.death`, no início |
| `animationdeath` | `minecraft:ash` | 2,7083 s, final | — |

A animação de morte dura 2,7917 s; a entidade permanece por três segundos para os efeitos finais aparecerem. A rotação vanilla de morte foi desativada para não interferir na animação fornecida.

As referências antigas a `.mp3` foram substituídas pelos eventos registrados em `sounds.json`; o som de morte duplicado foi removido. As cópias de execução de hurt/death foram convertidas de estéreo para mono para posicionamento no mundo. Os arquivos originais permanecem intactos em `art/leader/original/`.

Os cinco JSONs de partículas enviados referenciam texturas vanilla. O mod usa os tipos vanilla, sem substituir o namespace `minecraft`. Não há locators nos eventos fornecidos: os efeitos são emitidos ao redor do corpo. Sons e partículas do Rei Lich são callbacks de animação no cliente e dependem de a entidade estar sendo renderizada; não são eventos globais de áudio do servidor.

## Configurações visuais

No primeiro início do cliente, o mod cria `config/monster_legion-client.json`:

```json
{
  "enableAnimationParticles": true,
  "enableAnimationSounds": true,
  "enableLichGlow": true,
  "enableStaffGlow": true
}
```

Todas as opções começam ativadas. Edite o JSON e reinicie o cliente para aplicar. O arquivo é local a cada jogador e não altera o comportamento do combate no servidor. Ainda não há tela de configuração. O som de ativação da staff é um som de jogabilidade e não é controlado por `enableAnimationSounds`.

As glowmasks são renderizadas com `AutoGlowingGeoLayer` no Rei Lich e na staff. O nome `_glowmask.png` é mantido ao lado da textura correspondente. Brilho emissivo deixa os pixels visíveis no escuro, sem iluminar blocos próximos.

## Arquivos e validação

- Fontes fornecidas: `art/leader/original/`.
- Recursos usados no jogo: `src/main/resources/assets/monster_legion/`.
- Classes gráficas e registros de renderers: pacote `client`, carregado exclusivamente pelo entrypoint de cliente.
- Comportamento e entidades: pacote `entity`.
- A staff é renderizada no osso `cajado`, acompanhando as transformações do braço; a animação `rotação` da joia é preservada.

Os testes verificam regras de associação, nomes dos ossos, recursos de áudio, sequência dos efeitos da morte e dimensões das glowmasks. Compilação não substitui a verificação dentro do jogo: conferir alinhamento da staff, orientação do modelo, UV, animações em movimento, prioridade de combate, caminhos de voo e efeitos com dois clientes. Sem chunks carregados o grupo é refeito ao retornar, como na base anterior.
