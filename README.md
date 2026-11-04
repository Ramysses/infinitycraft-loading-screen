# InfinityCraft Loading Screen (Minecraft 1.7.10 Forge)

Fork técnico e código-fonte completo sob licença **GNU General Public License v2.0 (GPL-2.0)** utilizado para a tela de carregamento customizada do modpack **A Era do Futuro 3 (Minecraft 1.7.10)** na rede brasileira **InfinityCraft**.

---

## Sobre o Projeto e Créditos Upstream (GPL-2.0)

Este repositório preserva integralmente o código-fonte e as modificações aplicadas sobre o port `1.7.10` do mod **BetterLoadingScreen**, em estrita conformidade com a licença **GPL-2.0**:

- **Projeto Original:** *BetterLoadingScreen* por **AlexIIL** e colaboradores do port 1.7.10.
- **Customização InfinityCraft (`InfinityCraftLoadingScreen-1.3.10-r1.jar`):** Renderização da barra de progresso e identidade visual para o cliente `1.7.10` (Crucible / Forge) sem necessidade da biblioteca externa `AlexIILLib`.
- **Licença:** Distribuído sob os termos da **GPL-2.0** (consulte o cabeçalho dos arquivos em `src/main/java` para detalhes de copyright e liberdade de redistribuição).

---

## Ambientes Compatíveis na Rede InfinityCraft

A **InfinityCraft** mantém três gerações de servidores brasileiros de Minecraft com mods integrados ao **InfinityCraft Launcher** oficial para Windows:

1. **A Era do Futuro 3 (`AEDF3` — Minecraft `1.7.10` Forge):** Automação industrial e mágica com *Applied Energistics 2*, *Ender IO*, *Mekanism*, *Big Reactors*, *Thaumcraft 4*, *Draconic Evolution* e *Avaritia* (utiliza este módulo de carregamento).
2. **A Era do Futuro 1 (`AEDF1` — Minecraft `1.5.2` Forge):** Modpack clássico com *IndustrialCraft 2*, *BuildCraft*, *Logistics Pipes*, *Thaumcraft 3*, *Galacticraft* e *Twilight Forest* (otimizado para rodar a partir de `1,5 GB` de RAM).
3. **All The Mods 10: To The Sky (`ATM10 SKY` — Minecraft `1.21.1` NeoForge):** Skyblock técnico moderno com mais de `3.800` missões, *Ex Deorum*, *Create*, *Mekanism* e *FTB Chunks*.

---

## Links Oficiais e Documentação

- **Guia Oficial Passo a Passo (Como Jogar):** [https://infinitycrafts.com.br/como-jogar](https://infinitycrafts.com.br/como-jogar?utm_source=github_readme&utm_medium=organic&utm_campaign=ic_org_nov26&utm_content=aedf3)
- **Status Ao Vivo dos Servidores:** [https://infinitycrafts.com.br/servidores](https://infinitycrafts.com.br/servidores?utm_source=github_readme&utm_medium=organic&utm_campaign=ic_org_nov26&utm_content=rede)
- **Download do Launcher Oficial (Windows x64):** [https://infinitycrafts.com.br/launcher](https://infinitycrafts.com.br/launcher?utm_source=github_readme&utm_medium=organic&utm_campaign=ic_org_nov26&utm_content=launcher)
- **Modpack no Technic Platform (`A Era Do Futuro 3 - IC`):** [https://www.technicpack.net/modpack/a-era-do-futuro-3-ic.1241781](https://www.technicpack.net/modpack/a-era-do-futuro-3-ic.1241781)
- **Comunidade no Discord:** [https://discord.gg/infinitycraft](https://discord.gg/infinitycraft)

---

## Compilação Local

Requisitos: **JDK 8** e **ForgeGradle 1.2** (`1.7.10-10.13.4.1614-1.7.10`).

```bash
./gradlew build
```

O artefato compilado será gerado no diretório `build/libs/`.
