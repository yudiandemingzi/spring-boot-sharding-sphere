package com.oujiong;

import org.apache.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Description: 启动类
 *
 * TODO 这里一定要排除这里的SpringBootConfiguration，因为我们已经自定义了DataSource，所以需要排序shardingjdbc设置的DataSource
 * @author xub
 * @date 2019/10/08 下午6:33
 */
@SpringBootApplication(exclude = SpringBootConfiguration.class)
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
