<details open>
<summary>中文</summary>

# Sable Ragdolls Patch

## 描述

这是一个用于修复 `Sable_Ragdolls`、`Ragdoll Reactions` 和 `Sable:Ragdoll_Corpse` 的补丁模组。

## 功能（截至现在1.9）

### 修复功能汇总
- 修复了 `FA+Player-v1.1` 材质包导致手臂粗大。
- 修复了 `FA+Player_Expressions-v1.2` 材质包导致模型显示出现异常。
- 修复了 `Curios` 模组的显示问题。
- 修复了死亡前若第二层皮肤或披风隐藏了，那么死后布娃娃也会正常隐藏这些内容。
- 修复了 `RuOK` 开启方块实体剔除时，布娃娃方块实体不可见问题。
- 修复了布娃娃光照问题，让布娃娃可以接收到附近的光照。
- 修复了手臂嵌入地板时变黑。
- 修复了与 `Leawind Third Person`模组一起使用导致崩溃的问题。
- 修复了在安装了 `iris` 模组下导致皮肤半透明层失效的问题。
- 修复了布娃娃模式时仍显示 `punchy` 的第一人称手臂（punchy兼容）。
- 修复了布娃娃模式下仍然可以投掷末影珍珠和风弹的bug。
- 修复了布娃娃模式下若执行 '/sable remove @e' 导致玩家掉出世界和后续无法进入世界的bug。

### 兼容模组汇总
- 兼容模组 `Dynamic Lantern` 和 `Beltborne Lanterns`，布娃娃可以正确显示灯笼的位置与外观，并且与动态光源兼容。
- 兼容模组 `lambdynamiclights` 和 `sodiumdynamiclights`，现在布娃娃手上和身上有光源则会发光。
- 兼容模组 `Cosmetic Armor Reworked` 和 `Accessories`的时装栏，现在布娃娃可以隐藏盔甲了。

### 新功能汇总
- 当你在抓取布娃娃时，你将不会打开布娃娃的物品栏，但你依旧可以右键单击中间部位打开布娃娃物品栏。
- 现在你不会与布娃娃发生碰撞，添加了 `/nocollide` 指令可以控制玩家是否与布娃娃发生碰撞。
- 当玩家处于方块边缘的布娃娃下方时，不会再做游泳姿势。
- 当你在抓取布娃娃时，你将不会打开箱子，熔炉等容器。
- 抓取布娃娃时不会与门交互。
- 添加了模组菜单配置。

### 模组依赖

- [sable-player-ragdoll](https://modrinth.com/mod/sable-ragdolls) 必须的版本`0.7.5`
- [ragdoll_corpse](https://modrinth.com/mod/sable-ragdoll-corpse) 必须的版本`0.3.0`
- [Ragdoll Reactions](https://modrinth.com/mod/ragdoll-reactions) 可选的版本`0.7.0`

</details>

<details>
<summary>English</summary>

# Sable Ragdolls Patch

## Description

This is a patch mod to fix `Sable_Ragdolls`, `Ragdoll Reactions`, and `Sable:Ragdoll_Corpse`.

## Features (as of version 1.9)

### Fixes Summary

- Fixed an issue where the `FA+Player-v1.1` texture pack caused the arms to appear too large.
- Fixed an issue where the `FA+Player_Expressions-v1.2` texture pack caused abnormal model display.
- Fixed a display issue with the `Curios` mod.
- Fixed an issue where if a second layer of skin or cape was hidden before death, the ragdoll would also hide these elements correctly after death.
- Fixed an issue where ragdoll block entities were invisible when `RuOK` enabled block entity culling.
- Fixed ragdoll lighting issues, allowing the ragdoll to receive nearby light.
- Fixed the issue where the arms would turn black when embedded in the floor.
- Fixed a crash issue caused by using the `Leawind Third Person` mod.
- Fixed an issue where the skin's translucency layer was disabled when the `iris` mod was installed.
- Fixed the issue where the `punchy` first-person arms were still displayed in ragdoll mode (punchy compatible).
- Fixed a bug where ragdoll could still throw ender pearls and wind bombs in ragdoll mode.
- Fixed a bug where executing `/sable remove @e` in ragdoll mode would cause the player to fall out of the world and be unable to re-enter.

### Compatible Mods Summary

- Compatible with `Dynamic Lantern` and `Beltborne Lanterns` mods; the ragdoll can now correctly display the position and appearance of lanterns and is compatible with dynamic light sources.
- Compatible with `lambdynamiclights` and `sodiumdynamiclights` mods; the ragdoll will now glow when there is a light source on its hands and body.
- Compatible with the `Cosmetic Armor Reworked` and `Accessories` mods' costume menus; ragdolls can now hide armor.

### New Features Summary

- When you grab a ragdoll, its inventory will not open, but you can still right-click on the center to open it.
- You will no longer collide with ragdolls; the `/nocollide` command has been added to control whether the player collides with the ragdoll.
- The player will no longer perform a swimming pose when below a ragdoll at the edge of a block.
- When you grab a ragdoll, you will no longer open chests, furnaces, or other containers.
- You will no longer interact with doors when grabbing a ragdoll.
- Added mod menu configuration.

### Module Dependencies

- [Sable_Player_Ragdoll](https://modrinth.com/mod/sable-ragdolls) Required version `0.7.5`
- [Ragdoll_Corpse](https://modrinth.com/mod/sable-ragdoll-corpse) Required version `0.3.0`
- [Ragdoll_Reactions](https://modrinth.com/mod/ragdoll-reactions) Optional version `0.7.0`

</details>

## ⚠️ 声明
* 本模组只是出于热爱而制作，所有权利归属于 [Leonardoinc22](https://modrinth.com/user/leonardoinc22) 所有。
* 原模组链接在此[sable-player-ragdoll](https://modrinth.com/mod/sable-ragdolls)、[ragdoll_corpse](https://modrinth.com/mod/sable-ragdoll-corpse)。

## ⚠️ Disclaimer
* This mod was created purely out of passion, and all rights belong to [Leonardoinc22](https://modrinth.com/user/leonardoinc22).
* The original mod links are here:[sable-player-ragdoll](https://modrinth.com/mod/sable-ragdolls)、[ragdoll_corpse](https://modrinth.com/mod/sable-ragdoll-corpse).
