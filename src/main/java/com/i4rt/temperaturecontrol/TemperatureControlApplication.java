package com.i4rt.temperaturecontrol;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@SpringBootApplication
@Configuration
@ComponentScan(basePackages = {"com.i4rt"})
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class TemperatureControlApplication {

    public static void main(String[] args) {
        SpringApplication.run(TemperatureControlApplication.class, args);
    }

}
