
## The dark age of testing
Es war einmal vor langer Zeit, in einer weit entfernten Software-Entwicklungs-Galaxie, da war Computing-Zeit teuer und menschliche Arbeit billig. Einen Computer verwenden um Software zu testen? Undenkbar! Dafür können wir doch Tester einstellen und ausführliche Testhandbücher schreiben. 

![Ehrwürdiger Testplan](http://www.ancient-origins.net/sites/default/files/field/image/Ancient-Book.jpg)

(Quelle: http://www.ancient-origins.net/sites/default/files/field/image/Ancient-Book.jpg)

Doch auch später, als es üblich wurde Unit-Tests zu schreiben (interessanterweise gibt es verschiedene historische Definitionen von _Unit_ in diesem Kontext, das Wort meint entweder die zu testestende Einheit der Software, oder besagt, dass der Tests selber eine Einheit darstellt), wurde Testen von Entwicklern gerne stiefmütterlich behandelt oder, dem over-the-shoulder Prinzip folgend, der Testabteilung überlassen (und ob diese Abteilung automatisiert oder manuell testet, was kümmert mich als Entwickler das?). Aus dieser Zeit, und lehrreichen Softwaretechnikvorlesungen, ist dem einen oder anderen sicherlich die klassische Testpyramide bekannt.

![Testpyramide](https://blog.namics.com/files/2012/06/idealautomatedtestingpyramid.png)
(Quelle: https://blog.namics.com/2012/06/tour-durch-die-testpyramide.html)

## Die Agile-Revolution
Diese Einstellung in Bezug aufs Testen änderte sich in den 90ern im Zuge der Agile-Revolution mit dem Aufkommen von Techniken wie Xtreme Programming ([XP](https://de.wikipedia.org/wiki/Extreme_Programming)) und Test Driven Development ([TDD](https://de.wikipedia.org/wiki/Testgetriebene_Entwicklung)), sowie einer gehoben Wertschätzung für den Aspekt der Software-Qualität zumindest in Teilen der Entwickler-Community. Das Schreiben von Unit-Tests fiel im Zuge von TDD nun eindeutig in den Verantwortungsbereich des Entwicklers und auch Integration-Tests wurden, wo nötig, von Entwicklern geschrieben. Wann immer möglich wurde allerdings versucht auf Integration-Tests zu verzichten, waren diese doch eine zeitaufwändige und fragile Angelegenheit, schließlich musste sich wohlmöglich die gesamte Entwicklungsabteilung die selbe, mühsam von Hand gepflegte, Testumgebung für die Integration-Tests teilen.

## Testen im Microservice-Zeitalter
Vor kurzem hat Spotify einen interessanten [Blogartikel](https://labs.spotify.com/2018/01/11/testing-of-microservices/) zum Thema _Testen von Microservices_ veröffentlicht, in dem die Nützlichkeit der klassischen Testpyramide in Frage gestellt wird und die Testhonigwabe als alternatives Paradigma vorgestellt wird. 

![Honeycomb](https://spotifylabscom.files.wordpress.com/2018/02/microservices-testing-honeycomb-2.png?w=310&h=308&zoom=2)

(Quelle: https://labs.spotify.com/2018/01/11/testing-of-microservices/)

Unabhängig davon, ob man nun die Ansichten der Autoren zum Verhältnis Unit- vs Integration-Tests teilt, sollte man sich zumindest ihre Definition von Integrated-Test (**NICHT** Integration-Test) durch den Kopf gehen lassen:

> A test that will pass or fail based on the correctness of another system.
> Some signs of having Integrated Tests are:
> 
>   * We spin up other services in a local testing environment
>   * We test against other services in a shared testing environment
>   * Changes to your system breaks tests for other systems
> This is a quite fragile way of testing and we recommend learning more directly from the source above.

Aber wie genau können wir uns von der gemeinsam verwendeten Testumgebung emanzipieren? 

## Docker + Testcontainers to the rescue
Docker als Container-Technologie hat die Entwicklung und den Betrieb von Software in den letzten Jahren in vielerlei Hinsicht revolutioniert. Und auch für das Erstellen von portablen ad-hoc Testumgebungen ist Docker ein ideales Werkzeug. Gerade für Java-Entwickler existieren bereits einige, auf Docker basierende Tools, die eine einfache Integration in die gewohnten Entwicklungsprozesse ermöglichen, zum Beispiel das exzellente [docker-maven-plugin](https://github.com/fabric8io/docker-maven-plugin). Ein weiteres Beispiel ist die Java Library [Testcontainers](https://github.com/testcontainers/testcontainers-java), die eine native JUnit Integration anbietet (und für die Spock-Fans gibt es außerdem eine [Spock-Erweiterung](https://github.com/testcontainers/testcontainers-spock)).

![Testcontainers](https://rnorth.org/public/testcontainers/logo.png)

Im folgenden soll anhand einer einfachen Beispielanwendung der Einsatz von Testcontainers demonstriert werden.

## Der obligatorische Bücherkatalog
Wie kann man Konzepte besser vermitteln, als am Beispiel des klassischen Bücherkatalogs? Vermutlich ist jedes andere beliebige Beispiel mittlerweile besser geeignet und weniger abgedroschen, doch dies soll uns nicht davon abhalten, uns am Beispiel einer Bücherkatalog-Applikation die Konzepte von Testcontainers ein wenig näher anzuschauen.

### Integration-Tests gegen RDMBS
Angenommen wir folgen den Begrifflichkeiten von [Domain Driven Design](https://de.wikipedia.org/wiki/Domain-driven_Design) und nutzen ein Repository zum Persistiern unserer Buch-Domänen-Objekte in eine relationale Datenbank, wir könnten die Integration-Test aussehen? Manch ein Team würde nun vielleicht Tests innerhalb einer geteilten, in einer Testumgebung, aufgesetzten Datenbank (womöglich sogar in einem geteilten Schema) ausführen. Wer durch solcherlei Praktiken schon genug Schmerzen erfahren hat, wird vielleicht eher zu einer isoliert testbaren H2 In-Memory Datenbank tendieren. Doch auch diese Variante ist nicht frei von Problemen, die Schmerzen treten hier lediglich in anderer Form auf (wer kennt es nicht, die Integration-Tests laufen wunderbar mit der H2, aber wenn die Software dann in Produktion mit dem Oracle-RAC interagieren muss, dann macht sie die Grätsche...). Testcontainer bietet sich hier als Alternative an, die es erlaubt, Integration-Tests in einer eigenen, Ad-Hoch erstellten, Testumgebung mit den echten zu testenden Softwarekompenenten durchzuführen.

Hier die grobe Struktur des Repositories:

```java
public class BookRepository {

    private final String jdbcUrl;
    private final String username;
    private final String password;

    public BookRepository(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    public void save(Book book) {
        // ...
    }

    public long count() {
        // ...
    }

    public List<Book> findAllByAuthor(String author) {
        // ...
    }

}
```

Der dazugehörige Integrationn-Tests, als JUnit 4 Test implementiert, sieht wie folgt aus:

```java
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
```

Der Testcontainers-Code Anteil in diesem Beispiel ist hierbei denkbar gering:
```java
@Rule
public PostgreSQLContainer databaseContainer = new PostgreSQLContainer();
```

Die `PostgreSQLContainer`-Klasse ist hierbei eine spezielle Testcontainers-Klasse, die verschiedene Komfort-Methoden, wie z.B. `getJdbcUrl()` anbietet. Alternativ kann auch jedes andere beliege Docker-Image in Form einers `GenericContainer` verwendet werden. Wenn diese Klasse nun innerhalb einer JUnit-Testklasse mit `@Rule` annotiert wird, so sorgt dies dafür, dass der Postgres-Datenbankcontainer für jede einzelne Testmethode neu hochgefahren wird. Alternativ kann auch `@ClassRule` verwendet werden, dies sorgt dafür, dass sich alle Testmethoden einer Testklasse den gleichen Datenbankcontainer teilen.

Im `@Before` Block sehen wir außerdem, dass [Flyway](https://flywaydb.org/) verwendet wird. Dies hat mit Testcontainers an sich erstmal nichts zu tun, ist allerdings eine sehr praktische Java-Library für Datenbankmigrationen.

### Integration-Tests gegen $insert-your-favourite-software-here
Grundsätzlich lässt sich Testcontainers mit jeder Software nutzen, die sich innerhalb eines Docker-Containers ausführen lässt. Abseits von `PostgreSQLContainer` und `GenericContainer` gibt es noch viele weitere spezielle Klassen, die übliche Software in komfortable Art und Weise bereitstellen (Beispiele sind Kafka, Cassandra, oder Selenium). Auch Integration-Tests gegen selbstgeschriebene Software-Komponenten sind dank `GenericContainer` kein Problem.

PS: Testcontainers unterstützt Linux, Docker Machine, Docker for Mac und Docker for Window out-of-the-box (Windows so gut out-of-the-box wie man es von Windows ewarten kann, siehe: [Windows Support](https://www.testcontainers.org/usage/windows_support.html)) :relaxed:.

## Links
  * [Testcontainers](https://github.com/testcontainers/testcontainers-java)
  * [Testing of Microservices](https://labs.spotify.com/2018/01/11/testing-of-microservices/)
  * [Integrated Tests are a Scam](http://blog.thecodewhisperer.com/permalink/integrated-tests-are-a-scam)