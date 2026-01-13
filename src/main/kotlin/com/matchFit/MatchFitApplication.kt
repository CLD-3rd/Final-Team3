package com.matchFit

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableJpaAuditing
@EnableScheduling
class MatchFitApplication

fun main(args: Array<String>) {
    SpringApplication.run(MatchFitApplication::class.java, *args)
}
