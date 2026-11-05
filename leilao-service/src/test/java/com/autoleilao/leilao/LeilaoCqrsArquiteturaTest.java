package com.autoleilao.leilao;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.GeneralCodingRules;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

/**
 * Testes de Arquitetura CQRS e Database per Service do Microsserviço de Leilão com ArchUnit.
 * Valida o isolamento de comandos (CosmosDB + @Version Locking) e consultas (Azure SQL),
 * bem como a independência dos eventos de integração do Azure Service Bus.
 */
@AnalyzeClasses(packages = "com.autoleilao.leilao", importOptions = ImportOption.DoNotIncludeTests.class)
public class LeilaoCqrsArquiteturaTest {

    // =========================================================================
    // 1. REGRAS GERAIS DE CODIFICAÇÃO (General Coding Rules)
    // =========================================================================

    @ArchTest
    static final ArchRule proibir_system_out_e_err =
        GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;

    @ArchTest
    static final ArchRule proibir_autowired_em_campos =
        noFields().should().beAnnotatedWith(Autowired.class)
            .because("injeção de dependências do Spring deve ser feita obrigatoriamente via construtor");

    @ArchTest
    static final ArchRule proibir_lancar_excecoes_genericas =
        GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;

    // =========================================================================
    // 2. SEGREGAÇÃO CQRS (Command Service vs Query Service)
    // =========================================================================

    @ArchTest
    static final ArchRule command_service_nao_deve_depender_de_query_service =
        noClasses().that().resideInAPackage("..command.service..")
            .should().dependOnClassesThat().resideInAPackage("..query.service..")
            .because("a camada de escrita (Command) não deve depender da camada de leitura (Query)");

    @ArchTest
    static final ArchRule query_service_nao_deve_acessar_repositorio_de_escrita =
        noClasses().that().resideInAPackage("..query.service..")
            .should().dependOnClassesThat().resideInAPackage("..command.repository..")
            .because("a camada de serviço de leitura deve ler exclusivamente do Read Model relacional");

    // =========================================================================
    // 3. CONVENÇÕES DE NOMENCLATURA E ANOTAÇÕES
    // =========================================================================

    @ArchTest
    static final ArchRule controllers_devem_ter_sufixo_e_anotacao =
        classes().that().resideInAnyPackage("..controller..", "..command.controller..", "..query.controller..")
            .should().haveSimpleNameEndingWith("Controller")
            .andShould().beAnnotatedWith(RestController.class);

    @ArchTest
    static final ArchRule services_devem_ter_sufixo_e_anotacao =
        classes().that().resideInAnyPackage("..service..", "..command.service..", "..query.service..")
            .should().haveSimpleNameEndingWith("Service")
            .andShould().beAnnotatedWith(Service.class);

    // =========================================================================
    // 4. MENSAGERIA ASSÍNCRONA (Events)
    // =========================================================================

    @ArchTest
    static final ArchRule eventos_de_integracao_nao_devem_depender_de_controllers =
        noClasses().that().resideInAPackage("..messaging.event..")
            .should().dependOnClassesThat().resideInAnyPackage("..controller..", "..command.controller..", "..query.controller..")
            .because("eventos de integração são contratos de dados agnósticos de controladores");
}
