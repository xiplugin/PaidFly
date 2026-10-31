[**English**](https://github.com/coderxi1/PaidFly/blob/master/README.md)

# PaidFly

**PaidFly** 是一款为 Minecraft 服务器设计的插件，让玩家可以通过消耗（游戏币/经验值）进行飞行！

![banner](https://github.com/user-attachments/assets/a36e817f-58ae-4890-9f6c-373ac6a22fca)

## 功能

- 支持玩家使用游戏币/经验值进行付费飞行
- 支持Title、Action、音效提示
- 支持倒计时提示
- 付费飞行在空中时显示侧边计分板，展示当前支付余额和预计剩余飞行时间

## 安装

1. 在[插件发布页](https://github.com/coderxi1/PaidFly/releases)下载最新版本的 **`PaidFly.jar`** (若服务器具有`kotlin-stdlib`可使用`PaidFly-kotlin.jar`)
2. 将插件放入服务器 `plugins/` 文件夹  
3. 若要允许玩家使用，给默认用户组设置`paidfly.fly`权限
4. 如果使用经验飞行，请确保服务器已安装 **`Vault`** 经济插件。

## 配置

```yaml
Language: zh-TW.yml     #繁體中文；默认为 zh-CN.yml
Main:
  PayType: Money        #扣费模式
  PayInterval: 3s       #扣费间隔
  PayCost: 0.5          #每次扣多少
  AutoOffThreshold: 0.5 #小于此值关闭飞行 推荐设置为和PayCost一致
Scoreboard:
  Enable: true          #显示付费飞行计分板
```

插件启动时会自动生成缺失的 `plugins/PaidFly/lang/zh-TW.yml`，不会覆盖已有文件。将 `Language` 设为 `zh-TW.yml` 后重载配置即可使用繁體中文。

玩家在空中飞行时显示计分板，落地后隐藏。计分板每秒更新，余额显示当前支付类型（游戏币或经验值）。剩余时间按当前余额和费率估算；实际扣费按间隔进行，自动关闭阈值可能使飞行提前结束。计分板隐藏时会恢复之前的计分板；如果期间其他插件替换了计分板，则保留其他插件的计分板。可在语言文件的 `Scoreboard` 部分修改文字。

## 指令

所有指令均以 `/paidfly` 开头，可使用别名 `/fly`：

| 指令 | 权限 | 描述 |
|------|------|------|
| `/fly` | `paidfly.fly` | 等于`/fly toggle`命令 |
| `/fly on/off/toggle` | `paidfly.fly` | 切换飞行状态 |
| `/fly on/off/toggle [player]` | `paidfly.others` | 切换其他玩家飞行状态，需要 `paidfly.others` 权限 |
| `/fly help` | `paidfly.use` | 显示插件帮助信息（根据权限显示不同内容） |
| `/fly reload` | `paidfly.reload` | 重载插件配置文件 |
---

## 权限

| 权限节点 | 作用 |
|-----------|------|
| `paidfly.use` | 基础使用权限，查看帮助信息 |
| `paidfly.fly` | 启用、关闭或切换飞行 |
| `paidfly.others` | 对其他玩家操作飞行 |
| `paidfly.reload` | 重载插件配置 |
| `paidfly.admin` | 访问管理员子命令（如 `/paidfly reload`） |
---

## PlaceholderAPI

| 占位符 | 描述 | 示例 |
|------|------|------|
| `%paidfly_status%` | 玩家当前是否处于付费飞行状态 | `true` / `false` |
| `%paidfly_remaining_seconds%` | 玩家剩余飞行时间（秒） | `120` |
| `%paidfly_pay_type%` | 当前使用的支付类型名称（本地化名称） | `金币` |
| `%paidfly_pay_type_value%` | 当前支付类型对玩家计算出的原始数值 | `12.3333333334` |
| `%paidfly_pay_type_value_fixed%` | 当前支付数值，默认保留 **1 位小数** | `12.3` |
| `%paidfly_pay_type_value_fixed_<digits>%` | 当前支付数值，保留指定小数位数 | `%paidfly_pay_type_value_fixed_2%` → `12.34` |
