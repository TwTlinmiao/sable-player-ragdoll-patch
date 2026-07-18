# Sable Ragdolls Patch

## 说明

这是一个补丁模组，修复了 Sable-player-ragdoll 和 Sable-Ragdoll-corpse 的一些问题。

## 具体改动内容

### 1.对 FA+Player-v1.1 和 FA+Player_Expressions-v1.2 材质包导致的手臂粗大问题和布娃娃模型问题进行了修复。
### 2.对 Curios 模组显示问题进行了修复。

## 添加了那些功能？

### 1.当你在抓取尸体时，你将不会打开布娃娃的物品栏，但你依旧可以右键单击中间部位打开布娃娃物品栏。
### 2.当你在抓取尸体时，你将不会打开箱子，熔炉等容器。
### 3.现在你不会与布娃娃发生碰撞，添加了 /nocollide 指令可以控制玩家是否与布娃娃发生碰撞。
### 4.如果你隐藏了第二层皮肤，布娃娃也会同步隐藏

## 需要的环境

| 模组 |
|------|
| sable-player-ragdoll (≥0.7.5) |
| Sable-Ragdoll-corpse (≥0.3.0) |
| NeoForge (≥21.1.228) |
| Minecraft 1.21.1 |

## 使用方法

1. 构建：`./gradlew build`
2. 将 `build/libs/sable_player_ragdoll_patch-1.21.1-1.0.0.jar` 放入 `mods` 文件夹
3. 确保 `sable-player-ragdoll` 和 `ragdoll_corpse` 也在 `mods` 文件夹
4. 启动游戏
