FROM tomcat:8.5.47-jdk8-openjdk
COPY messenger.war /usr/local/tomcat/webapps
RUN apt-get update && apt-get install nano
RUN ln -s /usr/local/openjdk-8 /usr/local/tomcat/jdk
RUN groupadd tomcat && useradd tomcat -g tomcat 
RUN groupmod -g 1001 tomcat && usermod -u 1001 -g 1001 tomcat
RUN chown -R tomcat:tomcat /usr/local/tomcat

USER tomcat
EXPOSE 8080

CMD [ "catalina.sh", "run"]
