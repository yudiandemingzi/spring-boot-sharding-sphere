# ShardingSphere实现分库 + 读写分离

有关ShardingSphere实现分库分表前面写了7篇博客作为该项目做详细说明。

  1、[分库分表(1) --- 理论](https://www.cnblogs.com/qdhxhz/p/11608222.html)

  2、[分库分表(2) --- ShardingSphere(理论)](https://www.cnblogs.com/qdhxhz/p/11629883.html)

  3、[分库分表(3) ---SpringBoot + ShardingSphere实现读写分离](https://www.cnblogs.com/qdhxhz/p/11656205.html)

  4、[分库分表(4) ---SpringBoot + ShardingSphere 实现分表](https://www.cnblogs.com/qdhxhz/p/11651163.html)

  5、[分库分表(5) ---SpringBoot + ShardingSphere 实现分库分表](https://www.cnblogs.com/qdhxhz/p/11673493.html)

  6、[分库分表(6)--- SpringBoot+ShardingSphere实现分表+ 读写分离](https://www.cnblogs.com/qdhxhz/p/11688651.html)
  
  7、[分库分表(7)--- SpringBoot+ShardingSphere实现分库分表 + 读写分离](https://www.cnblogs.com/qdhxhz/p/11688371.html)

该项目拉下后配置数据库地址，和创建相应的库和表后就可以运行成功。


## 一、项目概述


#### 1、技术架构

项目总体技术选型

```
SpringBoot2.0.6 + shardingsphere4.0.0-RC1 + Maven3.5.4  + MySQL + lombok(插件)
```

#### 2、项目说明

`场景` 在实际开发中，如果数据库压力大我们可以通过  **分库分表**  的基础上进行 **读写分离**，来减缓数据库压力。

#### 3、项目整体结构

```makefile
spring-boot-sharding-ssphere # 父工程
 
  | #实现读写分离功能
  ---db-read-write 
 
  | #实现分表功能
  ---sub-table
    
  | #实现分库分表功能
  ---sub-db-table                      
     
  | #实现分表 + 读写分离
  ---sub-table-read-write                                        
  
   | #实现分库分表 + 读写分离
  ---sub-db-table-read-write                  
                        
```

<br>
备注: 如果对项目还不是很理解或者想对分库分表更深入理解，后期会录制视频教程。可以加QQ(450760938) 备注: 分库分表。


