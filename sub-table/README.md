
## <font color=#FFD700> 一、项目概述 </font>

#### 1、技术架构

项目总体技术选型

```
SpringBoot2.0.6 + shardingsphere4.0.0-RC1 + Maven3.5.4  + MySQL + lombok(插件)
```

#### 2、项目说明

`场景` 在实际开发中，如果表的数据过大，我们可能需要把一张表拆分成多张表，这里就是通过ShardingSphere实现分表功能，但不分库。

#### 3、数据库设计

这里有个member库，里面的`tab_user`表由一张拆分成3张，分别是`tab_user0`、`tab_user1`、`tab_user2`。

**如图**

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191011190937605-1357123570.png)

具体的创建表SQL也会放到GitHub项目里

<br>

## <font color=#FFD700>二、核心代码 </font>

`说明` 完整的代码会放到GitHub上，这里只放一些核心代码。

#### 1、application.properties

```properties
server.port=8086

#指定mybatis信息
mybatis.config-location=classpath:mybatis-config.xml

spring.shardingsphere.datasource.names=master

# 数据源 主库
spring.shardingsphere.datasource.master.type=com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.master.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.master.url=jdbc:mysql://localhost:3306/member?characterEncoding=utf-8
spring.shardingsphere.datasource.master.username=root
spring.shardingsphere.datasource.master.password=123456

#数据分表规则
#指定所需分的表
spring.shardingsphere.sharding.tables.tab_user.actual-data-nodes=master.tab_user$->{0..2}
#指定主键
spring.shardingsphere.sharding.tables.tab_user.table-strategy.inline.sharding-column=id
#分表规则为主键除以3取模
spring.shardingsphere.sharding.tables.tab_user.table-strategy.inline.algorithm-expression=tab_user$->{id % 3}

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
localhost:8086/save-user
```

我们可以从商品接口代码中可以看出，它会批量插入5条数据。我们先看控制台输出SQL语句

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191011190953489-147927942.jpg)



我们可以从SQL语句可以看出 **tab_user1** 和 **tab_user2** 表插入了`两条数据`，而 **tab_user0** 表中插入`一条数据`。

我们再来看数据库

`tab_user0`

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191011191003991-863659850.jpg)

`tab_user1`

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191011191015624-277025558.jpg)

`tab_user2`

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191011191024993-963489468.jpg)

完成分表插入数据。

#### 2、获取数据

我们来获取列表的SQL，这里对SQL做了order排序操作，具体ShardingSphere分表实现order操作的原理可以看上面一篇博客。

```mysql
  select *  from tab_user order by id
```

请求接口结果

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191011191043297-1315264145.jpg)

我们可以看出虽然已经分表，但依然可以将多表数据聚合在一起并可以排序。

`注意` ShardingSphere并不支持`CASE WHEN`、`HAVING`、`UNION (ALL)`，`有限支持子查询`。这个官网有详细说明。


<br>