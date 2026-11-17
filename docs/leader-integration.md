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

O Rei Lich é hostil e usa o sistema de [combate, invocação e recompensas](lich-combat.md). Ele tem 200 pontos de vida, queima ao sol e sua caveira aplica Wither/Fome sem explosão. A staff invoca um aliado temporário (uma vaga por jogador, cinco minutos). Os valores e regras completos estão no guia de combate. Ainda não há geração natural do Rei Lich.

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

Todas as opções começam ativadas. Edite o JSON e reinicie o cliente para aplicar. O arquivo é local a cada jogador e não altera o comportamento do combate no servidor. Ainda não há tela de configuração. A ativação da staff pelo jogador é silenciosa; o som de summon continua na animação do Rei Lich.

As glowmasks são renderizadas com `AutoGlowingGeoLayer` no Rei Lich e na staff. O nome `_glowmask.png` é mantido ao lado da textura correspondente. Brilho emissivo deixa os pixels visíveis no escuro, sem iluminar blocos próximos.

## Arquivos e validação

- Fontes fornecidas: `art/leader/original/`.
- Recursos usados no jogo: `src/main/resources/assets/monster_legion/`.
- Classes gráficas e registros de renderers: pacote `client`, carregado exclusivamente pelo entrypoint de cliente.
- Comportamento e entidades: pacote `entity`.
- A staff é renderizada no osso `staff_anchor`, acompanhando as transformações do braço; a animação `rotação` da joia é preservada.

Os testes verificam regras de associação, nomes dos ossos, recursos de áudio, sequência dos efeitos da morte e dimensões das glowmasks. Compilação não substitui a verificação dentro do jogo: conferir alinhamento da staff, orientação do modelo, UV, animações em movimento, prioridade de combate, caminhos de voo e efeitos com dois clientes. Sem chunks carregados o grupo é refeito ao retornar, como na base anterior.

## Revisão do encaixe e orientação

O modelo `lich.geo(2).json` substitui a geometria de execução; o original também fica em `art/leader/original/`. O ponto de encaixe agora é `staff_anchor`, filho de `braco_esquerdo`, com pivot `[0, 28, -6]`. A geometria independente da staff é alinhada pelo ponto de empunhadura e recebe uma correção local de orientação.

O renderer aplica **180° adicionais** em Y sobre a correção anterior de 90° (total de 270°). A transformação é aplicada ao Lich e às suas camadas, incluindo glowmask e staff. É necessário conferir o resultado visual no jogo, pois testes de compilação não validam a aparência do encaixe.

### Empunhadura inclinada (0.4.5)

A pose local da staff segue a sequência fornecida pelo autor: **67° em Z**, depois **90° em Y**, depois **translação (0, 1, 0)** em blocos. A ordem é relevante: a translação é aplicada no espaço local já rotacionado. O renderer mantém seu push/pop e o encaixe no osso `staff_anchor`; a orientação geral do Lich permanece igual.

A pose anterior de 60° e seu alinhamento pelo pixel 20 foram substituídos. Os testes geométricos que fixavam aquela empunhadura foram retirados porque descreviam a pose anterior. Os testes de modelo, anchor, animações e recursos continuam válidos. Conferir a nova posição dentro do jogo.

### Voo e controles (0.4.4)

A orientação e o encaixe do modelo foram mantidos. O Lich agora procura um ponto de visão pelo ar ao perder o jogador; use uma parede baixa, uma coluna e uma cobertura para conferir a perseguição. Para a staff, botão direito invoca e botão esquerdo encerra a invocação; testar mirando ar, bloco e criatura. A proteção solar dos capacetes voltou ao vanilla.
