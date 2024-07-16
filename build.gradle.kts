import com.kobylynskyi.graphql.codegen.model.GeneratedLanguage
import io.github.kobylynskyi.graphql.codegen.gradle.GraphQLCodegenGradleTask
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
    java
    kotlin("jvm") version "2.0.0"
    id("io.github.kobylynskyi.graphql.codegen") version "5.10.0"
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.graphql:spring-graphql:1.3.0")
}

val generatedCodePath = "${layout.projectDirectory}/src/generated"

tasks.named<GraphQLCodegenGradleTask>("graphqlCodegen") {
    graphqlSchemaPaths = listOf("src/main/resources/schema.graphqls")
    outputDir = File(generatedCodePath)
    packageName = "dev.besi.inventory.graphql"
    apiPackageName = "dev.besi.inventory.graphql.api"
    modelPackageName = "dev.besi.inventory.graphql.model"
    generateImmutableModels = true
    generatedLanguage = GeneratedLanguage.KOTLIN
    resolverArgumentAnnotations = setOf("org.springframework.graphql.data.method.annotation.Argument")
    parametrizedResolverAnnotations = setOf("org.springframework.graphql.data.method.annotation.SchemaMapping(typeName=\"{{TYPE_NAME}}\")")
    customAnnotationsMapping = mapOf("Query\\..*" to listOf("org.springframework.graphql.data.method.annotation.QueryMapping"))
}

sourceSets.getByName("main").kotlin.srcDirs(generatedCodePath)

java {
    withSourcesJar()
}

tasks.named<KotlinCompile>("compileKotlin") {
    dependsOn("graphqlCodegen")
}

tasks.named<Jar>("sourcesJar") {
    dependsOn("graphqlCodegen")
}

publishing {
    publications {
        create<MavenPublication>("inventory-api-models-jvm") {
            groupId = "dev.besi.inventory.graphql"
            artifactId = "inventory-api-models-jvm"
            version = "0.0.2"
            from(components["java"])
        }
    }
    repositories {
        maven {
            url = URI("https://${System.getenv("GITLAB_DOMAIN")}/api/v4/projects/4/packages/maven")
            credentials {
                username = System.getenv("GITLAB_PUBLISH_USERNAME")
                password = System.getenv("GITLAB_PUBLISH_PASSWORD")
            }
        }
    }
}
