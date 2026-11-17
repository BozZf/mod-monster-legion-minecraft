# Patrulhas noturnas

## Funcionamento

Grupos de monstros aparecem aleatoriamente na superfície escura do Overworld, durante a noite (horário 13000 a 22999). Não aparecem no Pacífico nem quando `doMobSpawning` está desativado. A tentativa natural precisa de um jogador vivo em sobrevivência ou aventura; criativo e espectador não geram tentativas.

Cada grupo tem, por padrão, 5 a 8 monstros, incluindo um comandante. Os participantes são zumbis, esqueletos, husks, strays e aranhas; há pelo menos dois tipos no grupo. O comandante é escolhido entre os quatro mortos-vivos adultos e usa espada, capacete, peitoral, calças e botas de netherita. Esqueletos comandantes usam a IA de combate corpo a corpo ao receber a espada.

O comandante conserva os atributos normais da espécie, acrescidos dos efeitos reais do equipamento. Não é um novo tipo de entidade nem um Rei Lich. Não há encantamentos extras ou aumento artificial de vida.

## Liderança

Os seguidores ficam associados ao UUID do grupo através de tags salvas na entidade. Depois de recarregar chunks ou o mundo, o sistema refaz a associação de navegação sem misturar patrulhas próximas. O Rei Lich também não rouba membros dessas patrulhas.

Seguidores ociosos acompanham o comandante. Enquanto vinculados a ele, uma meta de alvo com prioridade máxima faz com que usem o alvo atual do comandante, à frente das metas vanilla de caça e retaliação. Se o comandante não tem alvo válido, aguardam sua ordem. Após a morte dele ou perda do vínculo por distância/descarregamento, voltam à IA comum.

O alcance inicial de recrutamento é 24 blocos; o vínculo ativo se mantém até 48 blocos. Tags do grupo permanecem salvas mesmo fora desse alcance e impedem que outra patrulha capture os membros. Transformações vanilla, como zumbi para afogado, transferem a identidade de grupo e o papel de comandante.

Não há formações militares nem imunidade a dano acidental: as tags evitam escolher companheiros como alvos, mas flechas ainda podem acertar outros monstros no caminho.

## Equipamento e XP

- A armadura e a espada do comandante não são dropadas na morte. Além das chances zeradas, a rotina de drop de equipamento é bloqueada especificamente para comandantes, inclusive com Saque.
- O comandante não recolhe equipamentos do chão.
- O XP é 3 vezes o XP **base da espécie**, sem o bônus aleatório vanilla por peça equipada: um comandante de uma espécie com base de 5 XP vale 15 XP.
- As condições vanilla para conceder XP continuam valendo (morte atribuída ao jogador, gamerules etc.). Não são geradas esferas extras para mortes que normalmente não dariam XP.
- Loot normal da espécie, como ossos e carne podre, continua normal. Seguidores mantêm recompensas normais.
- Transformações vanilla preservam a regra de equipamento e XP do comandante.

Os grupos só são criados à noite; os existentes não somem instantaneamente ao amanhecer. Despawn, exposição ao sol e comportamento vanilla continuam valendo. Não se usa nome customizado nem `PersistenceRequired` para tornar os grupos permanentes.

## Limites e configurações

O arquivo `config/monster_legion-patrols.json` é criado na inicialização. Edite e reinicie o servidor/jogo:

```json
{
  "enabled": true,
  "attemptIntervalTicks": 600,
  "spawnChance": 0.25,
  "minGroupSize": 5,
  "maxGroupSize": 8,
  "minDistance": 48,
  "maxDistance": 96,
  "maxLoadedGroups": 8,
  "maxNearbyGroups": 2
}
```

- Uma tentativa a cada 30 segundos por mundo, com 25% de chance. Um jogador elegível é escolhido; a chance não é multiplicada pela quantidade de jogadores.
- A tentativa procura local de 48 a 96 blocos do jogador escolhido; todo monstro aparece a pelo menos 32 blocos de qualquer jogador não espectador.
- No máximo 8 comandantes vivos carregados no mundo e 2 a menos de 192 blocos do jogador escolhido. Também se bloqueia a tentativa quando já há 64 monstros hostis a menos de 128 blocos desse jogador.
- Busca limitada a 24 centros e 64 posições de seguidores por centro. Não carrega nem gera chunks para procurar local.
- Respeita fronteira do mundo, terreno sólido, ausência de fluido, escuridão vanilla e colisões. Se não cabe o tamanho mínimo, descarta o grupo parcial sem loot/XP.
- Não funciona no Nether ou End. Patrulhas fora dos chunks carregados não entram no limite de grupos carregados.
- Valores inválidos são limitados a intervalos seguros. Tamanho máximo de grupo é 13 (1 comandante + 12 membros).

## Teste no jogo

Em um mundo de teste, com comandos e dificuldade normal:

```mcfunction
/time set midnight
/monsterlegion patrol
```

O comando retorna as coordenadas do grupo se encontrar terreno válido. Pode ser usado em criativo para teste, mas preserva noite, escuridão, gamerules e limites; ignora apenas o sorteio e o intervalo da tentativa natural. Procure uma área ampla, escura e carregada, fora da base iluminada.

Para localizar comandantes por seletor, use `@e[tag=monster_legion.commander]`. A armadura torna o comandante reconhecível sem nome permanente.

### Verificação manual pendente

1. Conferir grupos com tipos variados e equipamento completo do comandante.
2. Atrair o comandante e observar os seguidores atacarem seu alvo e acompanharem a navegação.
3. Matar um comandante com Saque III: armadura e espada não caem; loot normal da espécie permanece.
4. Conferir 15 XP para um comandante adulto dessas espécies com as condições normais de XP. Comparar com 5 XP do mesmo mob sem equipamento.
5. Salvar/reabrir o mundo perto de dois grupos: eles não trocam seguidores.
6. Conferir transformação de um comandante em água, manutenção de identidade e ausência de drops de netherita.
7. Verificar ausência de novas patrulhas de dia, no Pacífico, com `doMobSpawning false`, em área iluminada ou em chunks não carregados.

Testes automáticos cobrem horário, multiplicação de XP, reconhecimento das tags salvas e limites da configuração; navegação, drops e interação com outros mods ainda precisam ser observados no jogo.
