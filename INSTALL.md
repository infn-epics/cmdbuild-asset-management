# CMDBuild Installation Manual

This guide describes how to install and run CMDBuild from source or from a pre-built WAR file.

For additional details please refer to the official Technical Manual:
https://www.cmdbuild.org/en/documentation/manuals/technical-manual

---

## Table of Contents

- [Prerequisites](#prerequisites)
- [Database Setup](#database-setup)
- [Build from Source](#build-from-source)
- [Deploy on Apache Tomcat](#deploy-on-apache-tomcat)
- [Run with the Setup Wizard](#run-with-the-setup-wizard)
- [Docker Deployment](#docker-deployment)
- [First Login](#first-login)
- [Optional Components](#optional-components)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required software

* **Java 17** (OpenJDK or Amazon Corretto 17 recommended)
* **PostgreSQL 17.x** (17.4 recommended)
* **Apache Tomcat 10** (latest version recommended)
* **Maven 3.9+** (only if building from source)

### Optional software

* **PostGIS 3.5.x** - required for GIS features
* **Alfresco Community** - required for document management (DMS)
* **GeoServer 2.25.3** - required for advanced GIS services
* **Docker** - alternative deployment method

### Hardware minimum requirements

* 16 GB of RAM
* 150 GB of available disk space per CMDBuild instance (SSD recommended)
* 4-core CPU

For production deployments it is recommended to separate the application server and the database server.

---

## Database Setup

CMDBuild stores all its data in PostgreSQL. The following steps create a dedicated database and user.

### 1. Create the database user

```bash
sudo -u postgres psql -c "CREATE USER cmdbuild WITH PASSWORD 'change_me' LOGIN;"
```

### 2. Create the database

```bash
sudo -u postgres psql -c "CREATE DATABASE cmdbuild OWNER cmdbuild ENCODING 'UTF8';"
```

### 3. Enable required extensions (optional but recommended)

```bash
sudo -u postgres psql -d cmdbuild -c "CREATE EXTENSION IF NOT EXISTS pg_trgm;"
```

For GIS support also enable PostGIS:

```bash
sudo -u postgres psql -d cmdbuild -c "CREATE EXTENSION IF NOT EXISTS postgis;"
```

### 4. Load an initial dump (optional)

CMDBuild provides two database dumps:

* `database/empty.dump` - minimal structure with a default admin account
* `database/demo.dump` - structure with sample data

To load a dump:

```bash
# For the empty dump
pg_restore -U cmdbuild -d cmdbuild database/empty.dump

# For the demo dump
pg_restore -U cmdbuild -d cmdbuild database/demo.dump
```

If you prefer, you can skip this step and use the graphical setup wizard to initialize the database automatically.

---

## Build from Source

The source code is a multi-module Maven project.

### 1. Clone the repository

```bash
git clone <repository-url>
cd cmdbuild
```

### 2. Build the project

```bash
mvn clean install -DskipTests
```

The build produces several artifacts, including:

* `cmdbuild/target/cmdbuild-*.war` - main CMDBuild web application
* `cmdbuild/target/cmdbuild-*.sh` - standalone setup launcher
* `ui/target/*.war` - UI-only variants for READY2USE and openMAINT

> **Note:** The full build may take several minutes depending on the hardware. If you only need the WAR, build the `cmdbuild` module after compiling its dependencies.

---

## Deploy on Apache Tomcat

### 1. Configure the database connection

CMDBuild reads the database connection from a `database.conf` file. The recommended location is:

```
<TOMCAT_HOME>/conf/cmdbuild/database.conf
```

The directory name (`cmdbuild`) must match the web application deployment context.

Create the file with the following content:

```properties
db.url=jdbc:postgresql://localhost:5432/cmdbuild
db.username=cmdbuild
db.password=change_me
db.driver=org.postgresql.Driver
db.pool.maxActive=20
db.pool.maxIdle=5
db.pool.minIdle=2
db.pool.initialSize=2
```

Adjust the host, database name, username and password to match your PostgreSQL installation.

An example file is also available inside the WAR at:

```
WEB-INF/conf/database.conf_example
```

### 2. Deploy the WAR

Copy the WAR file to the Tomcat webapps directory:

```bash
cp cmdbuild/target/cmdbuild-*.war /var/lib/tomcat10/webapps/cmdbuild.war
```

Tomcat will automatically deploy the application. Wait until the deployment completes (this may take a few minutes on first startup).

### 3. Access the application

Open a browser and navigate to:

```
http://<server>:8080/cmdbuild/
```

---

## Run with the Setup Wizard

CMDBuild includes a graphical setup wizard that can initialize the database and apply the basic configuration automatically.

### 1. Run the launcher

```bash
java -jar cmdbuild/target/cmdbuild-*.war
```

or, on Linux:

```bash
./cmdbuild/target/cmdbuild-*.sh
```

### 2. Follow the wizard steps

1. Select the language.
2. Choose the database dump to load (`empty` or `demo`).
3. Enter the PostgreSQL connection parameters.
4. Wait for the database initialization to complete.
5. Configure the initial admin password.

### 3. Start the application

After the wizard finishes, deploy the configured WAR on Tomcat or start the embedded server if available.

For CMDBuild READY2USE use `ready2use.sh` / `ready2use.war`, and for openMAINT use `openmaint.sh` / `openmaint.war`.

---

## Docker Deployment

A ready-to-use Docker Compose stack is provided under `deploy/docker-4.2.0/`. It builds the CMDBuild WAR from source, starts a PostGIS database and deploys the application on Tomcat.

### 1. Build the WAR

The Docker image is built from the project root, so the WAR must exist before starting the stack:

```bash
mvn clean install -DskipTests
```

### 2. Start the stack

```bash
cd deploy/docker-4.2.0
docker compose up -d
```

This will:
* Build a Docker image named `cmdbuild:4.2.0` using the WAR from `cmdbuild/target/`.
* Start a PostGIS 17 container named `cmdbuild_db`.
* Initialize the `cmdbuild` database with the uncompressed `database/demo.dump`.
* Start the CMDBuild application on Tomcat.

### 3. Access the application

```
http://localhost:8090/cmdbuild/
```

> **Note:** The compose file exposes PostgreSQL on port `5432` and CMDBuild on port `8090`. Change the port mappings if those ports are already in use.

### 4. Select a different dump

To start with an empty database instead of the demo data, change the `CMDBUILD_DUMP` environment variable in `docker-compose.yml`:

```yaml
environment:
  - CMDBUILD_DUMP=empty.dump
```

Both `demo.dump` and `empty.dump` are copied into the image from the `database/` folder.

### 5. Stop and clean up

```bash
docker compose down
```

To remove the volumes (database and Tomcat data):

```bash
docker compose down -v
```

---

## GitHub Actions Release Pipeline

The repository includes a GitHub Actions workflow (`.github/workflows/release.yml`) that is triggered every time a tag matching `v*` is pushed.

The workflow:
1. Checks out the repository.
2. Sets up Java 17.
3. Builds the project with Maven.
4. Uploads the generated WAR as a workflow artifact.
5. Creates a GitHub Release and attaches the WAR file.

To create a new release:

```bash
git tag v4.2.1
git push origin v4.2.1
```

After the workflow completes, the WAR will be available on the GitHub Releases page.

---

## First Login

After a successful installation, open the application URL and log in with the default admin credentials:

* **Username:** `admin`
* **Password:** `admin`

> **Important:** change the default admin password immediately after the first login via *Administration → Users*.

If you used the setup wizard, the password configured during the wizard applies instead.

---

## Optional Components

### PostGIS

Enable PostGIS on the CMDBuild database to use map layers and GIS functions.

```bash
sudo -u postgres psql -d cmdbuild -c "CREATE EXTENSION IF NOT EXISTS postgis;"
```

### Alfresco (Document Management)

Install Alfresco Community and configure the DMS connection in CMDBuild under *Administration → DMS*.

### GeoServer

Install GeoServer 2.25.3 and register it in CMDBuild under *Administration → GIS* to publish advanced map services.

---

## Troubleshooting

### Application does not start

* Check the Tomcat logs (`<TOMCAT_HOME>/logs/catalina.out` or `localhost.<date>.log`).
* Verify that `database.conf` exists and contains valid connection parameters.
* Ensure PostgreSQL is running and reachable from the Tomcat server.
* Confirm the database user has sufficient privileges on the CMDBuild database.

### Login fails with the default admin account

* If the password was changed and lost, it can be reset by updating the `Password` column in the `"User"` table of the CMDBuild database. The password must be stored using the CMDBuild password hashing format.
* For a quick recovery in a development environment, restoring the `empty.dump` or `demo.dump` will recreate the default admin account.

### Build fails

* Verify that Java 17 is active (`java -version`).
* Verify Maven version (`mvn -version`).
* Make sure you have a working internet connection for Maven dependencies.
* Run `mvn clean install -DskipTests` and inspect the first error carefully.

### Out of memory during build

Increase Maven memory:

```bash
export MAVEN_OPTS="-Xmx4g -Xms1g"
mvn clean install -DskipTests
```

---

## Next Steps

* Read the Technical Manual: https://www.cmdbuild.org/en/documentation/manuals/technical-manual
* Configure users, roles and privileges via *Administration → Users*.
* Customize the data model via *Administration → Classes*.
* Design reports with JasperSoft Studio and import them via the report module.
