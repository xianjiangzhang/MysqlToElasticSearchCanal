# MysqlToElasticSearchCanal
利用Canal同步mysql数据到ElasticSearch，通过修改配置文件支持动态同步Mysql数据。主要介绍一下Canal的主要配置信息。
# Canal
## Canal介绍
  canal是阿里巴巴旗下的一款开源项目，纯Java开发。基于数据库增量日志解析，提供增量数据订阅&消费，目前主要支持了MySQL（也支持mariaDB）。Canal 会将自己伪装成 MySQL 从节点（Slave），并从主节点（Master）获取 Binlog，解析和贮存后供下游消费端使用。Canal 包含两个组成部分：服务端和客户端。服务端负责连接至不同的 MySQL 实例，并为每个实例维护一个事件消息队列；客户端则可以订阅这些队列中的数据变更事件，处理并存储到数据仓库中。下面我们来看如何快速搭建起一个 Canal 服务。
## Canal原理
1. canal模拟mysql slave的交互协议，伪装自己为mysql slave，向mysql master发送dump协议
2. mysql master收到dump请求，开始推送binary log给slave(也就是canal)
3. canal解析binary log对象(原始为byte流)
## Canal配置
   /canal.deployer-1.0.24/canal/conf/canal.properties配置修改
```
canal.id= 1
# canal的ip地址和端口
canal.ip= 172.168.10.177
canal.port= 12345

#################################################
######### 		destinations		############# 
#################################################
# 设置canal读取的配置文件
canal.destinations= iyan
# conf root dir
canal.conf.dir = ../conf
# auto scan instance dir add/remove and start/stop instance
canal.auto.scan = true
canal.auto.scan.interval = 5

```
  /canal.deployer-1.0.24/canal/conf/iyan/instance.properties 配置修改
```
#################################################
## mysql serverId
canal.instance.mysql.slaveId = 1212

# position info 配置数据库的ip与端口，以及同步位置，默认未0开始
canal.instance.master.address = 172.168.10.177:3306
canal.instance.master.journal.name = 
canal.instance.master.position =0
canal.instance.master.timestamp = 

#canal.instance.standby.address = 
#canal.instance.standby.journal.name =
#canal.instance.standby.position = 
#canal.instance.standby.timestamp = 

# username/password
canal.instance.dbUsername = db_user
canal.instance.dbPassword = db_pwd
canal.instance.defaultDatabaseName = 
canal.instance.connectionCharset = UTF-8
# table regex
canal.instance.filter.regex = .*\\..*
# table black regex
canal.instance.filter.black.regex =  
#################################################
```
# 联系方式
  若有问题欢迎大家及时沟通
* QQ：848050490
* 邮箱: 848050490@qq.com


