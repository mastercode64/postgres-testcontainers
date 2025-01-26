package com.mastercode.postgrestestcontainers.database.config

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.context.annotation.ComponentScan
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.util.ResourceUtils
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.TimescaleDBContainerProvider
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Testcontainers
import java.nio.file.Files

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ComponentScan(basePackages = ["com.mastercode.postgrestestcontainers"])
@ExtendWith(SpringExtension::class)

abstract class PostgresTest {
    @Autowired
    lateinit var jdbc: JdbcTemplate

    companion object {
        private const val PORT = 5432
        private const val DATABASE = "test-database"
        private const val USER = "test-user"
        private const val PASSWORD = "test-user"
        private const val MIGRATION_FILES = "../db/sql"
        private const val TIMESCALE_DB_TAG = "2.15.2-pg14"

        private val container: JdbcDatabaseContainer<*> =
            TimescaleDBContainerProvider()
                .newInstance(TIMESCALE_DB_TAG)
                .apply {
                    withDatabaseName(DATABASE)
                    withUsername(USER)
                    withPassword(PASSWORD)
                    withExposedPorts(PORT)
                    withReuse(true)
                    start()
                    waitingFor(Wait.defaultWaitStrategy())
                    runFlywayMigrations(this)
                }

        private fun runFlywayMigrations(container: JdbcDatabaseContainer<*>) {
            val flyway =
                Flyway
                    .configure()
                    .dataSource(container.jdbcUrl, container.username, container.password)
                    .locations("filesystem:$MIGRATION_FILES")
                    .load()

            flyway.migrate()
        }

        @DynamicPropertySource
        @JvmStatic
        fun registerDBContainer(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", container::getJdbcUrl)
            registry.add("spring.datasource.username", container::getUsername)
            registry.add("spring.datasource.password", container::getPassword)
        }
    }

    fun clearTable(tableName: String) {
        jdbc.execute("DELETE FROM $tableName WHERE 1=1")
    }

    fun executeScript(filePath: String) {
        val file = ResourceUtils.getFile("classpath:sql-samples/$filePath")
        val query = String(Files.readAllBytes(file.toPath()))
        jdbc.execute(query)
    }

}