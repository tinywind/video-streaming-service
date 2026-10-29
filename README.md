# spring-boot-streaming
spring-boot, video streaming, jpa, lombok


![capture](https://raw.githubusercontent.com/tinywind/spring-boot-streaming/master/capture.png "comment")


# How to Play: Method 1
* Install Maven 3.9 or newer and JDK 17.
* Build and test: `mvn verify`.
* Run: `mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dvideo.root=/absolute/path/to/videos"`.

# How to Play: Method 2
* Install JDK 17 and build the executable WAR with `mvn verify`.
* Run: `java -Dvideo.root="/absolute/path/to/videos" -jar target/spring-boot-streaming-0.1.war`.

The historical prebuilt WAR was removed because it contained obsolete dependencies. Build the replacement from source. The application uses Spring Boot 4.1.2, Jakarta JSP/JSTL, Bootstrap 5.3.8 and jQuery 3.7.1.

The configured video directory must exist. Browsing, downloads and range requests resolve real paths and reject files or directory symlinks outside that directory. Downloads retain the original filename, including Unicode. Saved annotations and their CSV export keep the existing schema and keyboard controls. CSV exports prefix formula-leading or control-leading keys with an apostrophe so spreadsheets read them as text; stored annotations and ordinary custom keys stay unchanged. The H2 web console is disabled by default.

The default database is `spring-boot-streaming` in the process working directory. H2 2.x cannot directly open old H2 1.4 database files. Before running this version against existing data, back up the original database, export it with the old H2 version and import it into a separate H2 2.x database using `RUNSCRIPT ... FROM_1X` as described in the [H2 migration guide](https://www.h2database.com/html/migration-to-v2.html). Verify the imported annotations before selecting that database. `NON_KEYWORDS=KEY` in the JDBC URL preserves the existing annotation `key` column; retain it when supplying a custom URL. The JPA mapping keeps the original identity columns so new annotations do not collide with imported IDs.

`mvn verify` uses temporary fixture videos, a separate in-memory H2 database and loopback HTTP requests. It checks range boundaries, path and symlink confinement, JSP escaping, annotation persistence, CSV output and disabled consoles. It does not open an existing database or deploy the application. The application still has no user authentication; keep access within its intended trusted environment.

# Using Framework/Tools
* spring-boot
* jpa
* h2 database
* lombok

**If fail compile on any IDE, you check the IDE support _lombok_.** 

References
* http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle
* http://millky.com/@origoni/post/1100
* http://march3samwuli.tistory.com/entry/Step-1-%EC%9D%8C%EC%95%85-%EC%8A%A4%ED%8A%B8%EB%A6%AC%EB%B0%8D-%EC%84%9C%EB%B2%84Spring-boot-%EC%95%B1Android%EC%9D%84-%EB%A7%8C%EB%93%A4%EC%96%B4%EB%B3%B4%EC%9E%90
* http://aodis.egloos.com/5962812
* http://www.w3schools.com/html/html5_video.asp
* https://msdn.microsoft.com/ko-kr/library/hh924823(v=vs.85).aspx
* http://gingertech.net/2009/08/19/jumping-to-time-offsets-in-videos/ 
* https://ko.wikipedia.org/wiki/HTML5_%EB%B9%84%EB%94%94%EC%98%A4


# Why I work this project?
* `심심해서...`
* `요즘 할 일이 없어서......`
* ~~`여친의 존재 유무에 따라.........`~~
