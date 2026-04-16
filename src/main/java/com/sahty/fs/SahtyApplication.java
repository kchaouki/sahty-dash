package com.sahty.fs;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@Theme("sahty")
@PWA(name = "Sahty EMR", shortName = "Sahty",
        description = "Système de Gestion des Dossiers Médicaux Électroniques")
public class SahtyApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(SahtyApplication.class, args);
    }
}
