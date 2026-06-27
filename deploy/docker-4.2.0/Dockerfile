FROM tomcat:11.0.13-jdk21-temurin-noble AS builder

WORKDIR $CATALINA_HOME

ENV POSTGRES_USER=postgres \
    POSTGRES_PASSWORD=postgres \
    POSTGRES_PORT=5432 \
    POSTGRES_HOST=cmdbuild_db \
    POSTGRES_DB=cmdbuild \
    CMDBUILD_DUMP=demo.dump

RUN apt-get update \
    && apt-get install -y --no-install-recommends zip unzip

# Use the WAR produced by the Maven build (project root is the build context)
COPY cmdbuild/target/cmdbuild-*.war /tmp/cmdbuild.war

RUN mkdir -p $CATALINA_HOME/webapps/cmdbuild \
    && unzip /tmp/cmdbuild.war -d $CATALINA_HOME/webapps/cmdbuild

RUN rm /tmp/cmdbuild.war

# Stage 2: Final image stage with only the extracted contents
FROM tomcat:11.0.13-jdk21-temurin-noble

COPY --from=builder $CATALINA_HOME/webapps/cmdbuild $CATALINA_HOME/webapps/cmdbuild

# postgresql-client-17
RUN sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt noble-pgdg main" > /etc/apt/sources.list.d/pgdg.list'
RUN curl -fsSL https://www.postgresql.org/media/keys/ACCC4CF8.asc | gpg --dearmor -o /etc/apt/trusted.gpg.d/postgresql.gpg

RUN apt-get update \
    && apt-get install -y postgresql-client-17 curl \
    && groupadd tomcat \
    && useradd -s /bin/false -g tomcat -d $CATALINA_HOME tomcat \
    && mkdir -p $CATALINA_HOME/conf/cmdbuild \
    && cp -r $CATALINA_HOME/webapps.dist/manager $CATALINA_HOME/webapps/ \
    && chmod +x $CATALINA_HOME/webapps/cmdbuild/cmdbuild.sh \
    && apt-get purge -y zip unzip \
    && apt-get autoremove -y \
    && rm -rf /tmp/* \
    && rm -rf /var/lib/apt/lists/*

# Copy supporting files (context is project root)
COPY deploy/docker-4.2.0/files/tomcat-users.xml $CATALINA_HOME/conf/
COPY deploy/docker-4.2.0/files/context.xml $CATALINA_HOME/webapps/manager/META-INF/
COPY deploy/docker-4.2.0/files/database.conf $CATALINA_HOME/conf/cmdbuild/
COPY deploy/docker-4.2.0/files/docker-entrypoint.sh /usr/local/bin/
COPY deploy/docker-4.2.0/files/postgresql-42.7.3.jar $CATALINA_HOME/lib/

# Copy uncompressed database dump into the webapp dump directory
COPY database/demo.dump $CATALINA_HOME/webapps/cmdbuild/WEB-INF/sql/dump/demo.dump
COPY database/empty.dump $CATALINA_HOME/webapps/cmdbuild/WEB-INF/sql/dump/empty.dump

RUN chmod +x /usr/local/bin/docker-entrypoint.sh \
    && chown -R tomcat:tomcat $CATALINA_HOME

USER tomcat

EXPOSE 8080

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
CMD ["catalina.sh", "run"]
