# Integração do monstro líder

## O que enviar

- Projeto original do Blockbench (`.bbmodel`), além da textura `.png`.
- Modelo exportado e animações, se já existirem; informe o formato de exportação.
- Tamanho desejado, nome e ideia dos ataques/habilidades.
- Se tiver sons, os arquivos originais e indicação de quando devem tocar.

Não é necessário refazer seu modelo agora. Ainda não há dependência de GeckoLib: decidiremos a renderização depois de inspecionar seu arquivo. Modelos Java e modelos animados em `.geo.json` exigem integrações diferentes.

## Etapa seguinte no código

1. Registrar `monster_legion:leader` e seus atributos.
2. Criar a entidade como `PathAwareEntity` (ou subclasse, como `HostileEntity`) e dar a ela a tag `monster_legion.leader` no construtor. Isso conecta o monstro ao sistema atual.
3. Configurar os objetivos de combate do líder para que ele escolha um alvo.
4. Registrar renderer e modelo em uma entrada exclusiva de cliente, sem classes gráficas na inicialização comum do servidor.
5. Copiar os recursos exportados para os caminhos apropriados em `src/main/resources/assets/monster_legion/`.
6. Criar tradução, ovo de invocação, loot e regras de spawn depois de testar a entidade por invocação.

## Verificação dentro do jogo

- O mod inicia tanto no cliente quanto em servidor dedicado.
- Um líder recruta até 12 monstros permitidos e nunca recruta outro líder.
- Dois líderes não conseguem possuir o mesmo seguidor.
- Seguidores ociosos acompanham o líder e tentam atacar seu alvo válido.
- Morte, remoção da tag, distância e descarregamento desfazem o vínculo.
- O grupo volta a se formar após salvar/reabrir o mundo.
- Modelo, textura, animações e sons funcionam sem erros no log.

Os testes unitários validam as regras de associação. Navegação, prioridade de IA, combate e renderização exigem testes dentro do Minecraft.
