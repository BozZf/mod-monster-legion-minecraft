# Combate do Rei Lich

## Vida, defesa e fogo solar

O Rei Lich tem **200 pontos de vida (100 corações)**, sem pontos extras de armadura/toughness. A defesa adapta a lógica do corpo do Ender Dragon: dano no corpo é reduzido para `dano / 4 + min(dano, 1)`; acertos na cabeça têm dano normal. A região superior começa a 2,7 blocos da base. Para projéteis, verifica-se a altura do impacto; para ataques corpo a corpo, a interseção do olhar do atacante com a hitbox. Não é uma entidade multipartes como o dragão.

Como referência de defesa, ataques de jogadores e explosões podem feri-lo, mas ataques comuns de outros mobs e efeitos de status não. Dano que ignora invulnerabilidade continua válido. Há uma exceção explícita para fogo: **o Rei Lich queima ao sol**, usando a verificação vanilla de luz, céu e exposição, e também pode receber dano de fogo/lava. A coroa é parte do modelo, não um capacete de equipamento.

Entidades antigas da versão anterior são migradas para os novos atributos ao carregar, mantendo a proporção da vida restante.

## Magias

### summon

O nome interno da animação fornecida continua `sumon`. Com cooldown de **60 segundos (1.200 ticks)** entre inícios de conjuração, nascem **2 a 4** mortos-vivos adultos, cada um escolhido entre zumbi, husk, esqueleto, stray e esqueleto Wither.

Ele usa um conjunto completo sorteado entre couro, cota de malha, ferro, ouro, diamante e netherita. Os invocados obedecem ao Lich. São procuradas posições com chão sólido e espaço livre perto dele, sem criar chunks; não se exige escuridão para magia. A conjuração não produz um mob se não há posição segura. Há limite de 12 invocados vivos e carregados do mesmo Lich, somando todas as dimensões; pouco espaço ou o limite podem reduzir o lote. Eles não têm o prazo de cinco minutos da staff do jogador: são persistentes e mantêm o vínculo mesmo longe ou com o dono descarregado. Ainda podem ser mortos em combate. Quando o Lich morre, suas invocações carregadas morrem normalmente, sem loot nem XP; as descarregadas recebem essa morte ao carregar, usando um registro de morte do dono salvo no mundo.

### attack_magic

- Cooldown próprio de **10 segundos (200 ticks)** entre inícios de ataque; não é reiniciado por command ou hurt. Os cooldowns de summon e attack_magic são independentes e persistidos no NBT. Uma animação já em execução pode atrasar a próxima magia.
- Caveira com o renderer e a textura vanilla do Wither.
- Dano direto base: **8 pontos**, usando o tipo de dano da caveira do Wither.
- **Wither II por 10 segundos** e **Fome I por 10 segundos**, aplicados somente quando o dano foi aceito; imunidades vanilla dos alvos continuam válidas.
- Não explode, não destrói blocos, não incendeia o terreno. O código não chama os métodos vanilla que geram explosão.
- Não colide com o dono ou os aliados reconhecidos.
- Uma barreira/escudo ou outra situação que rejeite o dano não gera cura.

Cada acerto cura o Lich por um total igual ao dano direto **efetivamente causado**, incluindo absorção consumida, distribuído ao longo de **10 segundos**. Por exemplo, um acerto que causa 8 pontos libera 8 pontos de cura ao todo, não 8 por tick. O dano posterior do Wither não aumenta essa cura. Cada acerto tem uma janela independente; janelas podem coexistir. Não há cura acima da vida máxima e a vida não é armazenada para além da janela. O saldo das janelas é salvo no NBT.

### command e aliança

A magia sorteia um jogador vivo em sobrevivência/aventura a até **32 blocos** e coloca todos os monstros elegíveis próximos no mesmo alvo. O Lich também passa a focá-lo.

Uma aura, atualizada a cada segundo, transforma os monstros comuns próximos em aliados do Lich, inclusive membros/comandantes de patrulhas. Eles acompanham e obedecem ao alvo dele por uma meta prioritária, preservando as regras de armadura/XP dos comandantes.

Outros Reis Lich, Withers e invocações pertencentes a jogadores ficam de fora. Mobs vinculados a outro Lich próximo não são roubados continuamente. O vínculo de navegação se mantém a até 48 blocos; quando o dono morre, sai do alcance ou deixa de estar carregado, os aliados naturais voltam à IA comum. Isso não desfaz a identidade dos monstros conjurados, que permanece vinculada até sua morte ou a morte do Lich. Podem ser recrutados novamente quando se aproximarem.

As regras são de facção/seleção de alvo, não imunidade geral a todo dano em área vanilla.

## Morte

Com `doMobLoot` ativado:

- **10 ossos**, sem variação por Saque.
- **25% de chance de uma staff**, sem aumento por Saque.
- **12.000 XP**, escolhidos como referência da primeira derrota do Ender Dragon (não o valor reduzido de dragões ressuscitados).
- A janela da animação de morte e suas partículas continua preservada.

A staff não é equipamento segurado pela entidade, de modo que o renderer do cajado não cria um segundo drop de equipamento.

## Staff: magia summon

Botão direito invoca **um** monstro e um conjunto de armadura dos mesmos sorteios da magia do Lich, **sem tocar o áudio summon**. O áudio da conjuração do próprio Lich permanece.

- É **aliado do jogador**, acompanha-o e ataca quem o jogador atacou ou quem o feriu recentemente, sem escolher o dono como alvo.
- Duração de **6.000 ticks = cinco minutos de jogo ativo**. Descarregar chunks ou trocar de dimensão não reinicia o prazo. Ao expirar, o mob recebe dano letal pela rotina normal de morte, com som/animação vanilla e sem drops/XP. Se estiver descarregado, morre assim que carregar. O corpo permanece pelo tempo normal da morte antes da remoção; não se usa desaparecimento direto para expiração.
- **Uma invocação ativa por jogador**, compartilhada entre todos os cajados e dimensões. A reserva fica em PersistentState e o vínculo/prazo ficam salvos no NBT do mob.
- Morte do invocado libera a vaga. Mudar de item, desconectar, recarregar chunks ou reiniciar o servidor não cria uma segunda vaga.
- Transformações vanilla transferem a identidade e mantêm o prazo original.
- Se não há posição segura, não se ocupa uma vaga nem se cria um mob preso em blocos.
- Invocações conjuradas pelo Lich ou pela staff não produzem loot, equipamento ou XP, evitando farm de armaduras aleatórias.

A staff deixa de atirar o projétil provisório da versão anterior; sua magia de uso agora é summon. A animação original de rotação da joia permanece.

## Teste

```mcfunction
/give @s monster_legion:lich_king_spawn_egg
/give @s monster_legion:lich_staff
/summon monster_legion:lich_king ~ ~ ~
```

O ovo tem entrada própria na aba de ovos do criativo. A staff aparece nas abas **Ferramentas e utilidades** e **Combate** da edição Java, além da busca do criativo. Use um mundo de teste: a defesa e os aliados tornam o combate bem diferente da versão anterior.

Ao testar a expiração, observar o som/animação de morte, a permanência breve do corpo e ausência de qualquer item ou XP. A vaga é liberada quando o invocado morre, sem esperar a remoção do corpo. A proteção de drops vale tanto para invocados do Lich quanto para os do jogador, inclusive mortes em combate e com Saque.

Conferir no jogo: novo encaixe `staff_anchor`, rotação adicional de 180°, cooldowns de 60/10 segundos, morte dos invocados junto ao Lich (incluindo recarga de chunks), sorteio de mobs/armaduras, colisão da caveira sem explosão, efeitos por 10 s, cura gradual no Lich, foco aleatório com dois jogadores, queimadura ao sol, loot em múltiplas mortes e vida/XP. Para a staff, conferir morte, cinco minutos, dois cajados, dois jogadores, troca de dimensão, transformação do mob e save/reload com o invocado descarregado.

Os testes automatizados cobrem os números de combate, orçamento/duração/persistência da cura e exclusividade/transferência da reserva de invocação. O workflow compila o mod e verifica carregamento em servidor dedicado; a observação da luta dentro do jogo permanece necessária.
