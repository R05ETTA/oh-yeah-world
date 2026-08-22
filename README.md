# Oh Yeah! World

Oh Yeah! World（欧耶世界）是一个适用于 Minecraft 1.21.1 / NeoForge 21.1.223 的生物玩法模组，加入三种自定义生物：天素罗亚种·围巾罗、天素罗亚种·战斗脸和素虾。

## 下载与安装

### 正式版本

正式版本会在发布后出现在 GitHub 的 **Releases** 页面：

1. 打开本项目 GitHub 页面的 **Releases**。
2. 选择需要的版本。
3. 下载其中的 `ohyeah-*.jar`。
4. 将 JAR 文件放入 Minecraft 实例的 `mods` 文件夹。
5. 使用以下版本启动：
   - Minecraft `1.21.1`
   - NeoForge `21.1.223`
   - Java `21`

### 开发测试版本

维护者也可以从 GitHub **Actions → Build → Artifacts** 下载 `ohyeah-mod`。该文件用于开发测试，不代表正式发布版本。

## 生物玩法

### 围巾罗（天素罗亚种）

- 可驯服、喂食成长和治疗。
- 喜欢食物：小麦、胡萝卜、甜菜根、马铃薯。
- 最爱食物：蛋糕、`ohyeah:chips`。
- 成年后可以繁殖，生成围巾罗栾栾块。
- 使用剪刀可以获得红羊毛，并使其进入禁声状态。
- 受到攻击后会进行最多 6 发远程连射反击。
- 已驯服时，主人攻击其他生物，围巾罗会加入战斗。
- 主人误伤已驯服围巾罗时，围巾罗仍会反击主人。

### 战斗脸（天素罗亚种，Battle Face）

- 可驯服、喂食成长和治疗。
- 喜欢食物：小麦、胡萝卜、甜菜根、马铃薯。
- 最爱食物：`ohyeah:chips`。
- 成年后可以繁殖，生成战斗脸栾栾块。
- 使用剪刀可以获得红羊毛，并使其进入禁声状态。
- 受到攻击后会执行“宣言 → 靠近 → 近身扑击 → 冷却/结束”的战斗链。
- 已驯服时，主人攻击其他生物，战斗脸会加入战斗。
- 主人误伤已驯服战斗脸时，战斗脸仍会反击主人。

### 素虾

- 轻量水生生物，在海洋和河流相关水域生成。
- 手持 `ohyeah:chips` 时会靠近玩家。
- 薯片只会诱食，不会驯服素虾，也不会使其繁殖。
- 水中受伤或死亡时会喷墨。
- 死亡时掉落 `ohyeah:xiami_huhu`。
- 支持原版拴绳。

## 快速体验

建议创建创造模式新世界，并使用以下命令生成实体：

```mcfunction
/summon ohyeah:tiansuluo_pink_scarf ~ ~ ~ {PersistenceRequired:1b}
/summon ohyeah:tiansuluo_battle_face ~ ~ ~ {PersistenceRequired:1b}
/summon ohyeah:suxia ~ ~ ~ {PersistenceRequired:1b}
```

常用物品和方块 ID：

```text
ohyeah:chips
ohyeah:xiami_huhu
ohyeah:tiansuluo_pink_scarf_egg
ohyeah:tiansuluo_battle_face_egg
ohyeah:suxia_egg
ohyeah:tiansuluo_pink_scarf_luanluan_block
ohyeah:tiansuluo_battle_face_luanluan_block
```

## 声音说明

Oh Yeah! World 只管理围巾罗、战斗脸和素虾的自定义物种声音，包括环境、受伤、死亡、进食、成长、繁殖、战斗、剪刀和喷墨反馈音。

剪刀工具音、栾栾块放置音、方块音、脚步声和世界环境音仍由 Minecraft 原生系统处理。

## 常见问题

### Mod 没有加载

确认以下内容完全匹配：

- Minecraft `1.21.1`
- NeoForge `21.1.223`
- Java `21`
- JAR 文件位于当前实例的 `mods` 文件夹中

### 找不到生物

第一次体验建议使用上面的 `/summon` 命令，不要先等待自然生成。自然生成会受到群系、水体和随机生成条件影响。

### GitHub Actions 没有 JAR

只有成功完成的 **Build** 工作流才会上传 `ohyeah-mod` Artifact。若工作流失败，请先查看构建日志，再使用成功的工作流运行记录下载 JAR。

## 项目状态

当前版本已经完成客户端人工回归，核心的喂食、声音、主人协同攻击、误伤反击和战斗脸扑击没有发现明显异常。

后续版本仍可能继续调整生物行为、资源和数值。使用问题请记录：

- Minecraft、NeoForge 和 Java 版本
- Mod JAR 来源对应的 GitHub Actions 工作流
- 复现步骤
- 游戏日志或截图
