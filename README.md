AI-Agent-Station
 
多智能体协作与运维调度平台，一站式 Agent 管理与任务执行中枢
 
Tech Stack（技术栈）
 
Spring AI + Spring Boot + MyBatis + Docker
 
Core Features（核心功能）
 
- 多 Agent 协同调度：支持运维、问答等多智能体并行协作
- RAG 智能检索：基于 Knowledge 标签精准匹配业务知识库，拒绝无效推理
- 运维监控集成：Arthas 深度监控方法调用，定位性能瓶颈
- 任务异步编排：RabbitMQ 解耦任务流程，提升高并发场景稳定性
- 动态配置管理：支持 Agent 规则、超时策略、资源配额动态调整
- 日志与结果持久化：全流程日志留存，支持任务回溯与复盘
 
Performance（性能）
 
- 单 Agent 任务执行：秒级~十几秒级（适配 AI 推理特性）
- 支持并发调度：按运维场景容量规划，支持任务队列限流
- 核心接口低延迟：调度、配置查询接口响应毫秒级
 
Project Structure（项目结构）
 
controller：接口层 | domain：业务/调度逻辑 | agent：智能体核心模块
rag：检索与知识库处理 | monitor：Arthas 监控集成 | config：中间件/策略配置
common：工具类、常量、枚举 | dao/mapper：数据访问