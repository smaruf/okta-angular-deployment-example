package com.okta.developer.notes

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.HandleBeforeCreate
import org.springframework.data.rest.core.annotation.RepositoryEventHandler
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@SpringBootApplication
class DemoApplication {

    @Value("#{ @environment['allowed.origins'] ?: {} }")
    private lateinit var allowedOrigins: List<String>

    @Bean
    fun simpleCorsFilter(): FilterRegistrationBean<CorsFilter> {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()
        config.allowCredentials = true
        config.allowedOrigins = allowedOrigins
        config.allowedMethods = listOf("*");
        config.allowedHeaders = listOf("*")
        source.registerCorsConfiguration("/**", config)
        val bean = FilterRegistrationBean(CorsFilter(source))
        bean.order = Ordered.HIGHEST_PRECEDENCE
        return bean
    }
}

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}

@Entity
data class Note(@Id @GeneratedValue var id: Long? = null,
                var title: String? = null,
                var text: String? = null,
                @JsonIgnore var username: String? = null)

@RepositoryRestResource
interface NotesRepository : JpaRepository<Note, Long> {
    fun findAllByUsername(name: String, pageable: Pageable): Page<Note>
    fun findAllByUsernameAndTitleContainingIgnoreCase(name: String, title: String, pageable: Pageable): Page<Note>
}

@Component
@RepositoryEventHandler(Note::class)
class AddUserToNote {

    @HandleBeforeCreate
    fun handleCreate(note: Note) {
        val username: String = SecurityContextHolder.getContext().getAuthentication().name
        println("Creating note: $note with user: $username")
        note.username = username
    }
}
