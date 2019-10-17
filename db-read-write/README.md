
## <font color=#FFD700> 一、项目概述 </font>

#### 1、技术架构

项目总体技术选型

```
SpringBoot2.0.6 + shardingsphere4.0.0-RC1 + Maven3.5.4  + MySQL + lombok(插件)
```

#### 2、项目说明

`场景` 如果实际项目中Mysql是 **Master-Slave** (主从)部署的，那么数据保存到Master库，Master库数据同步数据到Slave库，数据读取到Slave库，

这样可以减缓数据库的压力。

#### 3、数据库设计

我们这个项目中Mysql服务器并没有实现主从部署,而是同一个服务器建立两个库，一个当做Master库，一个当做Slave库。所以这里是不能实现的功能就是Master库

新增数据主动同步到Slave库。这样也更有利于我们测试看效果。

`Master库`

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191009185338624-1852756872.jpg)

`Slave库`

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191009185349432-1461519408.jpg)

从两幅图中可以看出，我这里在同一个服务器建两个数据库来模拟主从数据库。为了方便看测试效果，这里`主从数据库中的数据是不一样的`。

<br>

## <font color=#FFD700>二、核心代码 </font>

`说明` 完整的代码会放到GitHub上，这里只放一些核心代码。

#### 1、pom.xml

```xml
    <properties>
        <java.version>1.8</java.version>
        <mybatis-spring-boot>2.0.1</mybatis-spring-boot>
        <druid>1.1.16</druid>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>${mybatis-spring-boot}</version>
        </dependency>
        <!--mybatis驱动-->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
        <!--druid数据源-->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-starter</artifactId>
            <version>${druid}</version>
        </dependency>
        <!--shardingsphere最新版本-->
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>sharding-jdbc-spring-boot-starter</artifactId>
            <version>4.0.0-RC1</version>
        </dependency>
        <!--lombok实体工具-->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
    </dependencies>
```

#### 2、application.properties

```properties
server.port=8088
#指定mybatis信息
mybatis.config-location=classpath:mybatis-config.xml

spring.shardingsphere.datasource.names=master,slave0
# 数据源 主库
spring.shardingsphere.datasource.master.type=com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.master.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.master.url=jdbc:mysql://localhost:3306/master?characterEncoding=utf-8
spring.shardingsphere.datasource.master.username=root
spring.shardingsphere.datasource.master.password=123456
# 数据源 从库
spring.shardingsphere.datasource.slave0.type=com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.slave0.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.slave0.url=jdbc:mysql://localhost:3306/slave?characterEncoding=utf-8
spring.shardingsphere.datasource.slave0.username=root
spring.shardingsphere.datasource.slave0.password=123456

# 读写分离
spring.shardingsphere.masterslave.load-balance-algorithm-type=round_robin
spring.shardingsphere.masterslave.name=ms
spring.shardingsphere.masterslave.master-data-source-name=master
spring.shardingsphere.masterslave.slave-data-source-names=slave0
#打印sql
spring.shardingsphere.props.sql.show=true

```

Sharding-JDBC可以通过`Java`，`YAML`，`Spring命名空间`和`Spring Boot Starter`四种方式配置，开发者可根据场景选择适合的配置方式。具体可以看官网。

#### 3、UserController

```java
@RestController
public class UserController {

    @Autowired
    private UserService userService;
    /**
     * @Description: 保存用户
     */
    @PostMapping("save-user")
    public Object saveUser() {
        return userService.saveOne(new User("小小", "女", 3));
    }
    /**
     * @Description: 获取用户列表
     */
    @GetMapping("list-user")
    public Object listUser() {
        return userService.list();
    }
}
```

<br>

## <font color=#FFD700>三、测试验证  </font>

#### 1、读数据

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191009185402278-1091601392.png)

我们可以发现读取的数据是Slave库的数据。我们再来看控制台打印的SQL。可以看到读操作是Slave库。

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191009185411133-1376483500.png)

#### 2、写数据

`请求`

```
localhost:8088/save-user?name=小小&sex=女&age=3
```

**查看Mater数据库**

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191009185420244-1105231293.png)

发现Master数据库已经多了一条数据了，再看控制台打印的SQL。

![](https://img2018.cnblogs.com/blog/1090617/201910/1090617-20191009185429389-1675126607.png)

这个时候如果去看Slave库的话这条新增的数据是没有的，因为没有同步过去。


<br>