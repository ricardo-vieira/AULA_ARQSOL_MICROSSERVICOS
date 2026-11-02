package com.autoleilao.leilao;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.GeneralCodingRules;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Testes de Arquitetura em Camadas do Microsserviço de Leilão (Pregão) com ArchUnit.
 * Garante o isolamento estrito: Controller -> Service -> Repository.
 */
@AnalyzeClasses(packages = "com.autoleilao.leilao", importOptions = ImportOption.DoNotIncludeTests.class)
public class LeilaoArquiteturaTest {

    // =========================================================================
    // 1. REGRAS GERAIS DE CODIFICAÇÃO (General Coding Rules)
    // =========================================================================

    @ArchTest
    static final ArchRule proibir_system_out_e_err =
        GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;

    @ArchTest
    static final ArchRule proibir_injecao_por_campo =
        GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

    @ArchTest
    static final ArchRule proibir_lancar_excecoes_genericas =
        GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;

    // =========================================================================
    // 2. ISOLAMENTO DE CAMADAS (Layered Architecture)
    // =========================================================================

    @ArchTest
    static final ArchRule controllers_nunca_devem_acessar_repositories_diretamente =
        noClasses().that().resideInAPackage("..controller..")
            .should().dependOnClassesThat().resideInAPackage("..repository..");

    @ArchTest
    static final ArchRule services_nunca_devem_depender_de_controllers =
        noClasses().that().resideInAPackage("..service..")
            .should().dependOnClassesThat().resideInAPackage("..controller..");

    @ArchTest
    static final ArchRule modelo_de_dominio_nao_deve_depender_de_camadas_externas =
        noClasses().that().resideInAPackage("..model..")
            .should().dependOnClassesThat().resideInAnyPackage("..controller..", "..service..");

    // =========================================================================
    // 3. CONVENÇÕES DE NOMENCLATURA E ANOTAÇÕES
    // =========================================================================

    @ArchTest
    static final ArchRule controllers_devem_ter_sufixo_e_anotacao =
        classes().that().resideInAPackage("..controller..")
            .should().haveSimpleNameEndingWith("Controller")
            .andShould().beAnnotatedWith(RestController.class);

    @ArchTest
    static final ArchRule services_devem_ter_sufixo_e_anotacao =
        classes().that().resideInAPackage("..service..")
            .should().haveSimpleNameEndingWith("Service")
            .andShould().beAnnotatedWith(Service.class);

    @ArchTest
    static final ArchRule repositories_devem_estender_interface_spring_data =
        classes().that().resideInAPackage("..repository..")
            .should().haveSimpleNameEndingWith("Repository")
            .andShould().beAssignableTo(Repository.class);
}
