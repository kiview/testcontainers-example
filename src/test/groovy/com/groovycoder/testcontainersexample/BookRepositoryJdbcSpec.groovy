package com.groovycoder.testcontainersexample

import org.flywaydb.core.Flyway
import spock.lang.Specification

class BookRepositoryJdbcSpec extends Specification {

    static final TC_JDBC_URL = 'jdbc:tc:postgresql:9.6.8://hostname/databasename?TC_DAEMON=true'

    def setup() {

        def flyway = new Flyway()
        flyway.setDataSource(TC_JDBC_URL,
                '',
                '')
        flyway.clean()
        flyway.migrate()
    }

    private BookRepository buildRepository() {
        new BookRepository(TC_JDBC_URL,
                '',
                '')
    }

    def "repository contains one book after saving it"() {
        given: "the repo"
        def repo = buildRepository()

        and: "a book"
        def book = new Book("Moby Dick", "Herman Melville")

        when: "saving it"
        repo.save(book)

        then: "repo contains one book"
        repo.count() == 1
    }

    def "repository finds books of given author"() {
        given: "the repo"
        def repo = buildRepository()

        and: "some books"
        def mobyDick = new Book("Moby Dick", "Herman Melville")
        def terryPratchett = "Terry Pratchett"
        def magic = new Book("The Colour of Magic", terryPratchett)
        def elephant = new Book("The Fifth Elephant", terryPratchett)

        when: "saving them"
        [mobyDick, magic, elephant].each { repo.save(it) }

        and: "searching for books of author"
        def foundBooks = repo.findAllByAuthor(terryPratchett)

        then: "expected books were found"
        foundBooks.size() == 2
        foundBooks.containsAll([magic, elephant])
    }


}
