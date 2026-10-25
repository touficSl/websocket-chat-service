# ---------- build stage ----------
FROM maven:3.9.9-eclipse-temurin-8 AS build
WORKDIR /build

# Private dependency, not published to Maven Central
COPY CustomerIdentificator-0.0.2-SNAPSHOT.jar pom.xml ./
RUN mvn -B -q install:install-file -Dfile=CustomerIdentificator-0.0.2-SNAPSHOT.jar \
      -DgroupId=NCI -DartifactId=CustomerIdentificator -Dversion=0.0.2-SNAPSHOT -Dpackaging=jar \
 && mvn -B -q dependency:go-offline || true

COPY src ./src
RUN mvn -B -q package

# ---------- runtime stage ----------
# Tomcat 9 = Servlet 4 / javax.* (required by Spring Boot 2.7)
FROM tomcat:9.0-jdk8-temurin-jammy

# No default/example apps, non root user
RUN rm -rf /usr/local/tomcat/webapps/* /usr/local/tomcat/webapps.dist \
 && groupadd -r -g 1001 tomcat \
 && useradd -r -u 1001 -g tomcat -d /usr/local/tomcat -s /usr/sbin/nologin tomcat \
 && mkdir -p /tsl/midd_data/portal \
 && sed -i 's/<Server port="8005" shutdown="SHUTDOWN">/<Server port="-1" shutdown="SHUTDOWN">/' /usr/local/tomcat/conf/server.xml \
 && sed -i 's#</Host>#  <Valve className="org.apache.catalina.valves.ErrorReportValve" showReport="false" showServerInfo="false" />\n      </Host>#' /usr/local/tomcat/conf/server.xml \
 && chown -R tomcat:tomcat /usr/local/tomcat/webapps /usr/local/tomcat/work /usr/local/tomcat/temp /usr/local/tomcat/logs /tsl/midd_data

COPY --from=build --chown=tomcat:tomcat /build/target/messenger.war /usr/local/tomcat/webapps/messenger.war

USER tomcat
EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
  CMD bash -c 'exec 3<>/dev/tcp/127.0.0.1/8080 && printf "GET /messenger/actuator/health HTTP/1.0\r\n\r\n" >&3 && grep -q UP <&3'

CMD ["catalina.sh", "run"]
