package com.groovycoder.testcontainersexample;

import org.flywaydb.core.Flyway;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class BookRepositoryTests {

    @Rule
    public PostgreSQLContainer databaseContainer = new PostgreSQLContainer();

    @Before
    public void setup() {
        Flyway flyway = new Flyway();
        flyway.setDataSource(databaseContainer.getJdbcUrl(), databaseContainer.getUsername(),
                databaseContainer.getPassword());
        flyway.migrate();
    }

    private BookRepository buildRepository() {
        return new BookRepository(databaseContainer.getJdbcUrl(), databaseContainer.getUsername(),
                databaseContainer.getPassword());
    }

    @Test
    public void emptyRepository_isEmpty() {
        BookRepository bookRepository = buildRepository();
        assertEquals(0L, bookRepository.count());
    }

    @Test
    public void repository_contains_one_book_after_saving_it() {
        BookRepository bookRepository = buildRepository();
        Book mobyDick = new Book("Moby Dick", "Herman Melville");

        bookRepository.save(mobyDick);

        assertEquals(1L, bookRepository.count());
    }

    @Test
    public void repository_finds_books_of_given_author() {
        BookRepository bookRepository = buildRepository();
        Book mobyDick = new Book("Moby Dick", "Herman Melville");
        String terryPratchett = "Terry Pratchett";
        Book magic = new Book("The Colour of Magic", terryPratchett);
        Book elephant = new Book("The Fifth Elephant", terryPratchett);

        bookRepository.save(mobyDick);
        bookRepository.save(magic);
        bookRepository.save(elephant);

        List<Book> queriedBooks = bookRepository.findAllByAuthor(terryPratchett);

        assertEquals(2, queriedBooks.size());
        assertThat(queriedBooks, hasItems(magic, elephant));
    }

}
