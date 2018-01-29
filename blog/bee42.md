
## The dark age of testing
Es war einmal vor langer Zeit, in einer weit entfernten Software-Entwicklungs-Galaxie, da war Computing-Zeit teuer und menschliche Arbeit billig. Einen Computer verwenden um Software zu testen? Undenkbar! Dafür können wir doch Tester einstellen und ausführliche Testhandbücher schreiben. 

[TODO: Bild von Testhandbuch, Robert Martin]

Doch auch später, als es üblich wurde Unit-Tests zu schreiben (interessanterweise gibt es verschiedene historische Definitionen von _Unit_ in diesem Kontext, das Wort meint entweder die zu testestende Einheit der Software, oder besagt, dass der Tests selber eine Einheit darstellt), wurde Testen von Entwicklern gerne stiefmütterlich behandelt oder, dem over-the-shoulder Prinzip folgend, der Testabteilung überlassen (und ob diese Abteilung automatisiert oder manuell testet, was kümmert mich als Entwickler das?). Aus dieser Zeit, und lehrreichen Softwaretechnikvorlesungen, ist dem einen oder anderen sicherlich die klassische Testpyramide bekannt.

[TODO: Bild Testpyramide]

## Die Agile-Revolution
Diese Einstellung in Bezug aufs Testen änderte sich in den 90ern im Zuge der Agile-Revolution mit dem Aufkommen von Techniken wie Xtreme Programming (XP)[TODO: link]  und Test Driven Development (TDD)[TODO: link], sowie einer gehoben Wertschätzung für den Aspekt der Software-Qualität zumindest in Teilen der Entwickler-Community. Das Schreiben von Unit-Tests viel im Zuge von TDD nun eindeutig in den Verantwortungsbereich des Entwicklers und auch Integration-Tests wurden, wo nötig, von Entwicklern geschrieben. Wann immer möglich wurde allerdings versucht auf Integration-Tests zu verzichten, waren diese doch eine zeitaufwändige und fragile Angelegenheit, schließlich musste sich wohlmöglich die gesamte Entwicklungsabteilung die selbe, mühsam von Hand gepflegte, Testumgebung für die Integration-Tests teilen.

## Testen im Microservice-Zeitalter
Vor kurzem hat Spotify einen interessanten [Blogartikel](https://labs.spotify.com/2018/01/11/testing-of-microservices/) zum Thema _Testen von Microservices_ veröffentlicht, in dem die Nützlichkeit der klassischen Testpyramide in Frage gestellt wird und die Testhonigwabe als alternatives Paradigma vorgestellt wird. 

[TODO: Testing Honeycomb]

Unabhängig davon, ob man nun die Ansichten der Autoren zum Verhältnis Unit- vs Integration-Tests teilt, sollte man sich zumindest ihre Definition von Integrated-Test (**NICHT** Integration-Test) durch den Kopf gehen lassen:

> A test that will pass or fail based on the correctness of another system.
> Some signs of having Integrated Tests are:
> 
>   * We spin up other services in a local testing environment
>   * We test against other services in a shared testing environment
>   * Changes to your system breaks tests for other systems
> This is a quite fragile way of testing and we recommend learning more directly from the source above.

Aber genau können wir uns von der gemeinsam verwendeten Testumgebung emanzipieren? 

## Docker + Testcontainers to the rescue
Docker als Container-Technologie hat die Entwicklung und den Betrieb von Software in den letzten Jahren in vielerlei Hinsicht revolutioniert. Und auch für das Erstellen von portablen ad-hoc Testumgebungen ist Docker ein ideales Werkzeug. Gerade für Java-Entwickler existieren bereits einige, auf Docker basierende Tools, die eine einfache Integration in die gewohnten Entwicklungsprozesse ermöglichen, zum Beispiel das exzellente [docker-maven-plugin](https://github.com/fabric8io/docker-maven-plugin). Ein weiteres Beispiel ist die Java Library [Testcontainers](https://github.com/testcontainers/testcontainers-java), die eine native JUnit Integration anbietet (und für die Spock-Fans gibt es außerdem eine [Spock-Erweiterung](https://github.com/testcontainers/testcontainers-spock)).

[TODO: Testcontainers image]

Im folgenden soll anhand einer einfachen Beispielanwendung der Einsatz von Testcontainers demonstriert werden.

## Der obligatorische Bücherkatalog




PS: Testcontainers unterstützt Linux, Docker Machine, Docker for Mac und Docker for Window out-of-the-box :slight_smile:

## Links
  * [http://blog.thecodewhisperer.com/permalink/integrated-tests-are-a-scam](Integrated Tests are a Scam)