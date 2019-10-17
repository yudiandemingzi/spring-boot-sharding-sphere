## <font color=#FFD700> 一、项目概述 </font>

#### 1、技术架构

项目总体技术选型

```
SpringBoot2.0.6 + shardingsphere4.0.0-RC1 + Maven3.5.4  + MySQL + lombok(插件)
```

#### 2、项目说明

`场景` 在实际开发中，如果表的数据过大我们需要把一张表拆分成多张表，也可以垂直切分把一个库拆分成多个库，这里就是通过ShardingSphere实现`分库分表`功能。

#### 3、数据库设计

`分库` ds一个库分为 **ds0库** 和 **ds1库**。

`分表`  tab_user一张表分为**tab_user0表** 和 **tab_user1表**。

**如图**

`ds0库`

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191014193639269-230175212.jpg)

`ds1库`	

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191014193649751-736195635.jpg)

具体的创建表SQL也会放到GitHub项目里

<br>

## <font color=#FFD700>二、核心代码 </font>

`说明` 完整的代码会放到GitHub上，这里只放一些核心代码。

#### 1、application.properties

```properties
server.port=8084

#指定mybatis信息
mybatis.config-location=classpath:mybatis-config.xml
#打印sql
spring.shardingsphere.props.sql.show=true

spring.shardingsphere.datasource.names=ds0,ds1

spring.shardingsphere.datasource.ds0.type=com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.ds0.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds0.url=jdbc:mysql://localhost:3306/ds0?characterEncoding=utf-8
spring.shardingsphere.datasource.ds0.username=root
spring.shardingsphere.datasource.ds0.password=root

spring.shardingsphere.datasource.ds1.type=com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.ds1.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds1.url=jdbc:mysql://localhost:3306/ds1?characterEncoding=utf-8
spring.shardingsphere.datasource.ds1.username=root
spring.shardingsphere.datasource.ds1.password=root

#根据年龄分库
spring.shardingsphere.sharding.default-database-strategy.inline.sharding-column=age
spring.shardingsphere.sharding.default-database-strategy.inline.algorithm-expression=ds$->{age % 2}
#根据id分表
spring.shardingsphere.sharding.tables.tab_user.actual-data-nodes=ds$->{0..1}.tab_user$->{0..1}
spring.shardingsphere.sharding.tables.tab_user.table-strategy.inline.sharding-column=id
spring.shardingsphere.sharding.tables.tab_user.table-strategy.inline.algorithm-expression=tab_user$->{id % 2}
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

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191014193958255-984204288.jpg)


我们可以从SQL语句可以看出 **ds0** 和 **ds1** 库中都插入了数据。

我们再来看数据库

`ds0.tab_user0`

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191014194112856-1815322205.jpg)

`ds0.tab_user1`

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191014194536294-534144947.jpg)

`ds1.tab_user0`

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191014194544830-1729440572.jpg)

`ds1.tab_user1`

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191014194553598-572433744.jpg)

完成分库分表插入数据。

#### 2、获取数据

这里获取列表接口的SQL，这里对SQL做了order排序操作，具体ShardingSphere分表实现order操作的原理可以看上面一篇博客。

```mysql
  select *  from tab_user order by age  <!--根据年龄排序-->
```

请求接口结果

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191014194611502-1843756299.png)

我们可以看出虽然已经分库分表，但依然可以将多表数据聚合在一起并可以支持按**age排序**。

`注意` ShardingSphere并不支持`CASE WHEN`、`HAVING`、`UNION (ALL)`，`有限支持子查询`。这个官网有详细说明。


<br>