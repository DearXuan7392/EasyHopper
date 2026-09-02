---
AIGC:
  ContentProducer: '001191110102MAD55U9H0F10002'
  ContentPropagator: '001191110102MAD55U9H0F10002'
  Label: '1'
  ProduceID: '6602457d-f166-457d-bc25-17f6191eede1'
  PropagateID: '6602457d-f166-457d-bc25-17f6191eede1'
  ReservedCode1: '82cc5bd7-bf50-4536-8614-e68c284ee10b'
  ReservedCode2: '82cc5bd7-bf50-4536-8614-e68c284ee10b'
---

这是我的世界双端mod, 同时支持fabric和neoforge平台运行.

该mod的功能是修改漏斗逻辑, 实现:
1. 一次输入或输出多个物品
2. 增加分类功能, 启用时, 最后一格会变成类别格子, 只有相同类别的物品可以进入
3. 修改漏斗冷却时间
4. 完整的配置系统, 运行用户手动选择开启的功能
5. 数据包, 允许管理员修改服务器设置
6. 配置界面, 支持cloth-config-api和YACL两种图形界面api

其中common是双端代码, 同时在两个平台执行. 它采用fabric映射, 在neoforge上大部分情况下正常, 但由于neoforge修改了部分原版代码, 所以可能会出现部分代码注入失败, 或者无法被执行到.

fabric和neoforge文件夹下都是各自平台的专属代码, 只会在自己平台里执行. 它们都采用各自平台的映射, 代码无法在另一个平台执行.