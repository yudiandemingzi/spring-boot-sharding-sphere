## <font color=#FFD700> 一、项目概述 </font>

#### 1、技术架构

项目总体技术选型

```
SpringBoot2.0.6 + shardingsphere4.0.0-RC1 + Maven3.5.4  + MySQL + lombok(插件)
```

#### 2、项目说明

`场景` 在实际开发中，如果数据库压力大我们可以通过  **分库分表**  的基础上进行 **读写分离**，来减缓数据库压力。

#### 3、数据库设计

`分库` ms单库分库分为 ms0库 和 ms1库。

`分表`  tab_user单表分为tab_user0表 和 tab_user1表。

`读写分离` 数据写入ms0库 和 ms1库，数据读取 sl0库 和 sl1库。

**如图**

`ms0 ---主库`

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191016204615168-856573739.png)

`ms1 ---主库`	

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191016204625922-1762015926.png)

`sl0 ---从库`

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191016204638625-407970469.png)

`sl1 ---从库`

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191016204648399-387199295.png)


`说明` 初始数据的时候，这边只有 **sl0从库** 我插入了一条数据。那是因为我们这个项目中Mysql服务器并没有实现主从部署,这四个库都在同一服务器上，所以

做不到主数据库数据自动同步到从数据库。所以这里在从数据库建一条数据。等下验证的时候，我们只需验证数据是否存入`ms0`和`ms1`，数据读取是否在`sl0`和`sl1`。

具体的创建表SQL也会放到GitHub项目里

<br>

## <font color=#FFD700>二、核心代码 </font>

`说明` 完整的代码会放到GitHub上，这里只放一些核心代码。

#### 1、application.properties

```properties
server.port=8082

#指定mybatis信息
mybatis.config-location=classpath:mybatis-config.xml
#打印sql
spring.shardingsphere.props.sql.show=true
#数据源 
spring.shardingsphere.datasource.names=master0,slave0,master1,slave1

spring.shardingsphere.datasource.master0.type=com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.master0.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.master0.url=jdbc:mysql://localhost:3306/ms0?characterEncoding=utf-8
spring.shardingsphere.datasource.master0.username=root
spring.shardingsphere.datasource.master0.password=root

spring.shardingsphere.datasource.slave0.type=com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.slave0.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.slave0.url=jdbc:mysql://localhost:3306/sl0?characterEncoding=utf-8
spring.shardingsphere.datasource.slave0.username=root
spring.shardingsphere.datasource.slave0.password=root

spring.shardingsphere.datasource.master1.type=com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.master1.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.master1.url=jdbc:mysql://localhost:3306/ms1?characterEncoding=utf-8
spring.shardingsphere.datasource.master1.username=root
spring.shardingsphere.datasource.master1.password=root

spring.shardingsphere.datasource.slave1.type=com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.slave1.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.slave1.url=jdbc:mysql://localhost:3306/slave1?characterEncoding=utf-8
spring.shardingsphere.datasource.slave1.username=root
spring.shardingsphere.datasource.slave1.password=root

#根据年龄分库
spring.shardingsphere.sharding.default-database-strategy.inline.sharding-column=age
spring.shardingsphere.sharding.default-database-strategy.inline.algorithm-expression=master$->{age % 2}
#根据id分表
spring.shardingsphere.sharding.tables.tab_user.actual-data-nodes=master$->{0..1}.tab_user$->{0..1}
spring.shardingsphere.sharding.tables.tab_user.table-strategy.inline.sharding-column=id
spring.shardingsphere.sharding.tables.tab_user.table-strategy.inline.algorithm-expression=tab_user$->{id % 2}

#指定master0为主库，slave0为它的从库
spring.shardingsphere.sharding.master-slave-rules.master0.master-data-source-name=master0
spring.shardingsphere.sharding.master-slave-rules.master0.slave-data-source-names=slave0
#指定master1为主库，slave1为它的从库
spring.shardingsphere.sharding.master-slave-rules.master1.master-data-source-name=master1
spring.shardingsphere.sharding.master-slave-rules.master1.slave-data-source-names=slave1
```

Sharding-JDBC可以通过`Java`，`YAML`，`Spring命名空间`和`Spring Boot Starter`四种方式配置，开发者可根据场景选择适合的配置方式。具体可以看官网。

#### 2、UserController

```java
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 模拟插入数据
     */
    List<User> userList = Lists.newArrayList();
    /**
     * 初始化插入数据
     */
    @PostConstruct
    private void getData() {
        userList.add(new User(1L,"小小", "女", 3));
        userList.add(new User(2L,"爸爸", "男", 30));
        userList.add(new User(3L,"妈妈", "女", 28));
        userList.add(new User(4L,"爷爷", "男", 64));
        userList.add(new User(5L,"奶奶", "女", 62));
    }
    /**
     * @Description: 批量保存用户
     */
    @PostMapping("save-user")
    public Object saveUser() {
        return userService.insertForeach(userList);
    }
    /**
     * @Description: 获取用户列表
     */
    @GetMapping("list-user")
    public Object listUser() {
        return userService.list();
    }
```

<br>

## <font color=#FFD700>三、测试验证  </font>

#### 1、批量插入数据

请求接口

```
localhost:8082/save-user
```

我们可以从商品接口代码中可以看出，它会批量插入5条数据。我们先看控制台输出SQL语句

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191016204701100-420602843.png)



我们可以从SQL语句可以看出 **master0** 和 **master1** 库中都插入了数据。

我们再来看数据库

`ms0.tab_user0`

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191016204709927-109585642.png)


`ms0.tab_user1`

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191016204720312-155045671.png)


`ms1.tab_user0`

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191016204731838-247434033.png)

`ms1.tab_user1`

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191016204740482-27142599.png)

完成分库分表插入数据。

#### 2、获取数据

这里获取列表接口的SQL。

```mysql
  select *  from tab_user 
```

请求接口结果

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191016204752338-1011637895.png)



`结论` 从接口返回的结果可以很明显的看出，数据存储在主库,而数据库的读取在从库。

`注意` ShardingSphere并不支持`CASE WHEN`、`HAVING`、`UNION (ALL)`，`有限支持子查询`。这个官网有详细说明。


<br>