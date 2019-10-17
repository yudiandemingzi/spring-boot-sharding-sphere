## <font color=#FFD700> 一、项目概述 </font>

#### 1、技术架构

项目总体技术选型

```
SpringBoot2.0.6 + shardingsphere4.0.0-RC1 + Maven3.5.4  + MySQL + lombok(插件)
```

#### 2、项目说明

`场景` 在实际开发中，如果表的数据过大，我们可能需要把一张表拆分成多张表，这里就是通过ShardingSphere实现分表+读写分离功能，但不分库。

#### 3、数据库设计

`分表`  tab_user单表拆分为tab_user0表 和 tab_user1表。

`读写分离` 数据写入master库 ,数据读取 slave库 。

**如图**

`master库`

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191016213947793-556434384.png)

`slave库`

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191016213957809-1645338573.png)


`说明` 初始数据的时候，这边只有 **slave从库的tab_user0** 我插入了一条数据。那是因为我们这个项目中Mysql服务器并没有实现主从部署,这两个库都在同一服务器上，所以

做不到主数据库数据自动同步到从数据库。所以这里在从数据库建一条数据。等下验证的时候，我们只需验证数据是否存入`master库`，数据读取是否在`slave库`。

具体的创建表SQL也会放到GitHub项目里

<br>

## <font color=#FFD700>二、核心代码 </font>

`说明` 完整的代码会放到GitHub上，这里只放一些核心代码。

#### 1、application.properties

```properties
server.port=8084

#指定mybatis信息
mybatis.config-location=classpath:mybatis-config.xml

#数据库
spring.shardingsphere.datasource.names=master0,slave0

spring.shardingsphere.datasource.master0.type=com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.master0.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.master0.url=jdbc:mysql://localhost:3306/master?characterEncoding=utf-8
spring.shardingsphere.datasource.master0.username=root
spring.shardingsphere.datasource.master0.password=123456

spring.shardingsphere.datasource.slave0.type=com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.slave0.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.slave0.url=jdbc:mysql://localhost:3306/slave?characterEncoding=utf-8
spring.shardingsphere.datasource.slave0.username=root
spring.shardingsphere.datasource.slave0.password=root

#数据分表规则
#指定所需分的表
spring.shardingsphere.sharding.tables.tab_user.actual-data-nodes=master0.tab_user$->{0..1}
#指定主键
spring.shardingsphere.sharding.tables.tab_user.table-strategy.inline.sharding-column=id
#分表规则为主键除以2取模
spring.shardingsphere.sharding.tables.tab_user.table-strategy.inline.algorithm-expression=tab_user$->{id % 2}

# 读写分离
spring.shardingsphere.masterslave.load-balance-algorithm-type=round_robin
spring.shardingsphere.masterslave.name=ms
#这里配置读写分离的时候一定要记得添加主库的数据源名称 这里为master0
spring.shardingsphere.sharding.master-slave-rules.master0.master-data-source-name=master0
spring.shardingsphere.sharding.master-slave-rules.master0.slave-data-source-names=slave0

#打印sql
spring.shardingsphere.props.sql.show=true
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
localhost:8084/save-user
```

我们可以从商品接口代码中可以看出，它会批量插入5条数据。我们先看控制台输出SQL语句

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191016214013613-520082614.png)



我们可以从SQL语句可以看出	**master0数据源** 中 **tab_user0** 表插入了`三条数据`，而 **tab_user1** 表中插入`两条数据`。

我们再来看数据库

`master.tab_user0`

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191016214021616-2037678416.png)



`master.tab_user1`

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191016214028932-1050325570.png)





完成分表插入数据。

#### 2、获取数据

我们来获取列表接口的SQL。

```mysql
  select *  from tab_user 
```

请求接口结果

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191016214037389-922151476.png)

`结论` 从接口返回的结果可以很明显的看出，数据存储在master主库,而数据库的读取在slave从库。

`注意` ShardingSphere并不支持`CASE WHEN`、`HAVING`、`UNION (ALL)`，`有限支持子查询`。这个官网有详细说明。

<br>