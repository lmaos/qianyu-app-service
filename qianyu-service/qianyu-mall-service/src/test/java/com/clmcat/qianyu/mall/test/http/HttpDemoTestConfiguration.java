package com.clmcat.qianyu.mall.test.http;

import com.clmcat.framework.webmvc.WebMvcConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * 仅供 {@code @WebMvcTest} 切片引导使用的最小配置入口。
 *
 * <p>mall-service 是库模块，没有 {@code @SpringBootApplication}；而 {@code @WebMvcTest} 的引导器
 * 会从测试类所在包向上查找 {@code @SpringBootConfiguration}，本类即作为该入口。
 *
 * <p>{@code @ComponentScan} 扫描测试包内的 @Controller（{@code @WebMvcTest} 的 TypeExcludeFilter
 * 只放行 Web 组件），否则 {@code @WebMvcTest(controllers=...)} 无法发现控制器、请求会落到静态资源处理器返回 404。
 *
 * <p>{@code @WebMvcTest} 默认只装载 Web 切片自动配置（不拉起 DataSource/Dubbo/MyBatis/Redis）；
 * 显式 {@code @Import(WebMvcConfiguration.class)} 装回 clmcat-webmvc 的拦截器/参数解析器/统一信封包装。
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.clmcat.qianyu.mall.test.http")
@Import(WebMvcConfiguration.class)
public class HttpDemoTestConfiguration {
}
