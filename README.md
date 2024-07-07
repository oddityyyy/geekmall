# geekmall

#### 介绍
极客商城

#### 技术栈
SpringBoot+MySQL+MyBatis Plus+Redis(SpringCache+Redisson+SpringSession)+Nacos+SpringCloud-Gateway+Open Feign+OAuth2.0+OSS+Elasticsearch+RabbitMQ+Docker+Nginx+Vue+Sentinel+Sleuth+Zipkin+Ngrok

#### 软件架构
![img](https://github.com/oddityyyy/geekmall/blob/master/img/%E6%9E%81%E5%AE%A2%E5%95%86%E5%9F%8E-%E5%BE%AE%E6%9C%8D%E5%8A%A1%E6%9E%B6%E6%9E%84%E5%9B%BE.jpg)


#### 安装教程

1.  xxxx
2.  xxxx
3.  xxxx

#### 使用说明

1.  商城的后端分为商城后台管理系统和商城本体, 由13个微服务组成：auth-server认证服务, cart购物车服务, common工具包, coupon优惠券服务, gateway网关, member会员服务, order订单服务, product商品服务, search全文检索服务, seckill秒杀服务, third-party第三方服务, ware库存服务, fast后台管理系统模块。后台管理系统可以实现商品三级分类、品牌管理、商品属性管理、商品发布与上架、商品库存管理、秒杀活动上架等功能。
2.  商城本体借助es实现商品信息全局查询。借助线程池+异步编排实现商品详情页的业务处理。借助阿里云短信验证码实现注册、借助Gitee实现社交登录。借助SpringSession实现分布式session，个人信息的全模块共享。借助redis实现购物车项存储功能。实现了订单功能：创建订单、提交订单，使用token机制解决提交订单的幂等性问题。借助支付宝沙箱及内网穿透实现支付。实现商城主页展示秒杀商品、商品详情页获取秒杀信息、借助RabbitMQ实现秒杀业务的流量削峰。 
3.  使用Nacos作为注册中心与配置中心，借助Nginx负载均衡到Gateway网关，搭建域名访问环境。微服务之间通信使用Fegin远程过程调用，Redis作为中间件提升系统性能，借助SpringCache完成商品分类在首页的缓存，借助Redisson信号量实现秒杀并防止超卖问题。服务和服务之间使用消息队列RabbitMQ完成异步解耦，保证分布式事务的最终一致性。整合Sentinel实现熔断降级限流，SpringCloud-Sleuth、Zipkin，实现服务链路追踪及可视化，保障业务正常运行。

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


