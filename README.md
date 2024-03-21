# 1. 引入依赖

把项目下载后，maven 安装到本地仓库

使用 maven 引入依赖到自己项目

```xml
    <dependency>
        <groupId>org.example</groupId>
        <artifactId>rpc-core</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
```



# 2. 配置

项目的 resources 路径下添加 application.properties 配置文件，并按需要配置

```properties
# zk 服务注册的根路径，默认 /rpcServices
zookeeper.rpcRootPath=/rpcServices
# zookeeper 连接超时时间（ms），默认 2000
zookeeper.sessionTimeOut=2000
# zk 集群连接
# ip或者域名，域名需要能解析
zookeeper.connections=zk1:2181,zk2:2181,zk3:2181
# rpc 服务端端口，默认 8080
rpc.server.port=8080
# 服务端的 netty worker 数量，默认 4
rpc.server.netty.workerNum=4
# 客户端的 netty worker 数量，默认 2
rpc.client.netty.workerNum=2
# 客户端负载均衡策略，默认 rolling，共有：rolling:轮询  random:随机  consistent:一致性哈希
rpc.client.loadBalance=rolling

# spring 是否开始服务端，默认 on，有：on off， 这个在 spring boot 里才有意义
spring.rpc.server=on
# spring 是否开启客户端，默认 on，有：on off， 这个在 spring boot 里才有意义
spring.rpc.client=off

```





# 3. 使用

## 3.1 普通使用

### 3.1.1 服务端

#### 3.1.1.1 准备接口实现类

```java
@Slf4j
@RpcServerInfo(implName = "Tony") //implName 表示名称，区分同接口的多个实现；客户端可以指定 implName 来调用指定实现类
public class HelloServiceImplTony implements HelloService {
    private String name = "Tony";

    @Override
    public String sayHello(String friendName) {
        log.info("enter Tony sayHello");
        return "hello " + friendName + ", my name is " + name + ", nice to meet you";
    }
}
```



#### 3.1.1.2 发布接口实现类

```java
  public static void main(String[] args) {
      // 创建 rpcServer
      RpcServer rpcServer = new RpcServer();
      // 添加服务
      rpcServer.addServiceWithClass(HelloServiceImplTony.class);
      rpcServer.addServiceWithClass(HelloServiceImplDefault.class);
      // 启动 rpcServer
      rpcServer.start();
  }
```



### 3.1.2 客户端

#### 3.1.2.1 调用1-基于服务发现

```java
public static void main(String[] args) {
    // 创建 rpcClient, 指定负载均衡策略
    RpcClient rpcClient = new RpcClient(LOADBALANCETYPE_POLLING);
    // 生成接口的代理,代理指定的实现
    HelloService helloServiceLily = rpcClient.getServerProxyWithServiceDiscover(HelloService.class, "Tony");
    // 生成接口的代理,代理默认实现（没有 @RpcServerInfo 注解，或者有注解但是 implName 为空）
    HelloService helloServiceTony = rpcClient.getServerProxyWithServiceDiscover(HelloService.class);
    // rpc 调用
    for (int i = 0; i < 6; i++) {
        log.info(helloServiceLily.sayHello("Liming"));
    }
    for (int i = 0; i < 6; i++) {
        log.info(helloServiceTony.sayHello("Liming"));
    }
}
```

#### 3.1.2.2 调用2-基于指定服务地址

```java
public void testCallWithSpecificConn() {
    // 创建 rpcClient
    RpcClient rpcClient = new RpcClient();
  
    // 生成接口的代理,代理指定的实现
    HelloService helloServiceLily = rpcClient.getServerProxyWithSpecificConn(HelloService.class, "192.168.1.3", 8080, "Lily");
    // 生成接口的代理,代理默认实现（没有 @RpcServerInfo 注解，或者有注解但是 implName 为空）
    HelloService helloServiceTony = rpcClient.getServerProxyWithSpecificConn(HelloService.class, "192.168.1.3", 8080);
    // rpc 调用
    for (int i = 0; i < 6; i++) {
        log.info(helloServiceLily.sayHello("Liming"));
    }
    for (int i = 0; i < 6; i++) {
        log.info(helloServiceTony.sayHello("Liming"));
    }
}
```



## 3.2 springBoot 使用

### 3.2.1 服务端

#### 3.2.1.1 准备接口实现类

```java
package com.sw.servertest.services;

import com.sw.api.HelloService;
import com.sw.rpc.assist.annotation.RpcServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service // 加上 bean 注解
@RpcServerInfo(implName = "Tony") // implName 表示名称，区分同接口的多个实现；客户端可以指定 implName 来调用指定实现类
public class HelloServiceImplTony implements HelloService {
    private String name = "Tony";

    @Override
    public String sayHello(String friendName) {
        log.info("enter Tony sayHello");
        return "hello " + friendName + ", my name is " + name + ", nice to meet you";
    }
}

```

#### 3.2.1.2 直接运行启动类

```java
@SpringBootApplication
public class ServerTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerTestApplication.class, args);
    }
}
```



### 3.2.2 客户端

运行启动类，容器获取 RpcClient，使用即可

```java
@Slf4j
@SpringBootApplication
public class ClientTestApplication {

    public static void main(String[] args) {
        // 运行启动类
        ConfigurableApplicationContext context = SpringApplication.run(ClientTestApplication.class, args);
        // 获取 rpcClient（也可以在业务 bean 里面注入属性）
        RpcClient rpcClient = context.getBean(RpcClient.class);
        // 获取代理：可以基于服务发现，也可以基于指定地址，参考普通使用
        HelloService helloServiceTony = rpcClient.getServerProxyWithServiceDiscover(HelloService.class, "Tony");
        HelloService helloServiceDefault = rpcClient.getServerProxyWithServiceDiscover(HelloService.class);
        // 执行 rpc
        for (int i = 0; i < 3; i++) {
            log.info(helloServiceTony.sayHello("Liming"));
        }
        for (int i = 0; i < 3; i++) {
            log.info(helloServiceDefault.sayHello("Liming"));
        }
    }
}
```

