Copyright PAT Srl 2005-2026


CMDBuild SUITE
==============

| | |
|---|---|
| **Project** | CMDBuild - The platform for Asset Management |
| **Products** | CMDBuild, CMDBuild READY2USE, openMAINT |
| **Maintainer** | PAT - https://www.tecnoteca.com |
| **License** | GNU Affero General Public License v3 |

* **CMDBuild** - The platform for Asset Management - https://www.cmdbuild.org
* **CMDBuild READY2USE** - IT Assets & Services Management - https://www.cmdbuildready2use.org
* **openMAINT** - Property & Facility Management - https://www.openmaint.org

CMDBuild is an open-source Web Enterprise Asset Management (EAM), Configuration Management Database (CMDB) and Facility Management platform. It is designed to model any kind of asset, service or process and to provide a flexible web interface, workflow engine, reporting, GIS/BIM integration and REST APIs.

This repository contains the source code of the CMDBuild Suite.

---

## Table of Contents

- [Quick Start](#quick-start)
- [System Requirements](#system-requirements)
- [Project Structure](#project-structure)
- [Installation](#installation)
- [Documentation](#documentation)
- [Available Languages](#available-languages)
- [External Contributors](#external-contributors)
- [License and Legal Notices](#license-and-legal-notices)

---

## Quick Start

The fastest way to get CMDBuild running is to use the graphical setup wizard bundled in the WAR file:

```bash
# 1. Make sure you have Java 17 and PostgreSQL 17 installed
# 2. Download or build cmdbuild.war
# 3. Run the setup wizard
java -jar cmdbuild.war
```

For detailed instructions see [INSTALL.md](INSTALL.md).

---

## System Requirements

### Hardware minimum requirements (simple use cases)

* server-class computer (modern architecture)
* 16 GB of RAM
* 150 GB of available hard disk space for each CMDBuild instance (SSD recommended)
* 4-core CPU

### Hardware recommended requirements (separated instances)

* **CMDBuild instances**: 16 GB RAM, 50 GB SSD, 8-core
* **PostgreSQL services**: 16 GB RAM, 500 GB SSD, 8-core
* for all other services follow their recommended requirements

### Software requirements

* any OS able to handle the following applications (Linux recommended)
* PostgreSQL 17.x (17.4 recommended)
* PostGIS 3.5.x (optional)
* Apache Tomcat 10 (latest version recommended)
* Java 17 (OpenJDK or Amazon Corretto 17 recommended)
* Alfresco Community (optional)
* Geoserver 2.25.3 (optional)
* Docker (optional)

### Included libraries

* JDBC library for DB connection
* JasperReports libraries for report generation
* Ext JS libraries for user interface
* Server and client components for map making feature
* Server and client components for BIM viewer (xeokit and BIMSurfer)

### Additional useful software (not included)

* JasperSoft Studio for custom report design
* Together Workflow Editor for custom workflow design
* OCS Inventory as automatic inventory software

---

## Project Structure

This is a multi-module Maven project. The main modules are:

| Module | Description |
|---|---|
| `parent` | Parent POM with shared dependencies and plugins |
| `cmdbuild` | Main web application (WAR packaging) |
| `core` | Core business logic and services |
| `auth` | Authentication and authorization |
| `dao` | Data access layer and ORM |
| `services/rest` | REST API implementations (v2, v3, v4) |
| `ui` | Web user interface (Ext JS / Sencha) |
| `workflow` | Workflow engine |
| `etl` | ETL framework |
| `dms` | Document management connectors |
| `gis` | GIS integration |
| `bim` | BIM integration |
| `report` | Reporting engine |
| `jobs` | Background jobs |
| `cli` | Command-line utilities |
| `utils` | Shared utility libraries |

---

## Installation

See the dedicated [INSTALL.md](INSTALL.md) guide for:

* Installing prerequisites (Java, PostgreSQL, Tomcat)
* Building from source with Maven
* Loading an empty or demo database
* Deploying on Tomcat or Docker
* Configuring `database.conf`
* Running the initial setup wizard
* First login and post-installation steps

---

## Documentation

For further information please refer to the Technical Manual:
https://www.cmdbuild.org/en/documentation/manuals/technical-manual

Official websites:
* CMDBuild: https://www.cmdbuild.org
* CMDBuild READY2USE: https://www.cmdbuildready2use.org
* openMAINT: https://www.openmaint.org

---

## Available Languages

Available languages for CMDBuild project (first level localization):

* English
* Arabic
* Brazilian Portuguese
* Bulgarian
* Chinese
* Croatian
* Czech
* Danish
* Dutch
* European Portuguese
* French
* German
* Greek
* Hungarian
* Indonesian
* Italian
* Japanese
* Korean
* Malay
* Mongolian
* Norwegian
* Persian
* Polish
* Romanian
* Russian
* Serbian (Latin)
* Serbian (Cyrillic)
* Slovak
* Slovenian
* Spanish
* Thai
* Turkish
* Ukrainian
* Vietnamese

For updated information please refer to https://www.cmdbuild.org/en/download/available-languages

Available languages for CMDBuild READY2USE (second level localization):
Please refer to https://www.cmdbuildready2use.org/en/download/available-languages

Available languages for openMAINT (second level localization):
Please refer to https://www.openmaint.org/en/download/available-languages

---

## External Contributors

* Arabic translation by Mohammed Nabigh and Fahad Senan
* Brazilian Portuguese translation by T4HD
* Bulgarian translation by Yasen Arsov
* Chinese translation by Liansheng Yang
* Croatian translation by Tomislav Perić
* Czech translation by Igor Kurty
* Danish translation by Ali Araghi
* Dutch translation by Eric van Rheenen and Jeroen Baten
* European Portuguese translation by João Cruz and T4HD
* French translation by Damien Boustany-Kasper and Pierre Danel
* German translation by Susanne Tober
* Greek translation by Vasilis Papadakis and Dimitris Maniadakis
* Hungarian translation by Márton Natkó
* Indonesian translation by Inovamap
* Japanese translation by Satoru Funai
* Korean translation by Kyungik An
* Malay translation by Openstack Sdn Bhd
* Mongolian translation by Tsolmonbaatar Adiyasuren
* Norwegian translation by Lars Tangen
* Persian translation by Mohsen Salami
* Polish translation by Mike Gleczman and Sebastian Hajdus
* Romanian translation by Adrian Popescu
* Russian translation by Yasen Arsov
* Serbian (Latin and Cyrillic script) translation by Miroslav Zaric
* Slovak translation by Igor Kurty
* Slovenian translation by Tine Cus
* Thai translation by Wangkanai
* Turkish translation by Onur Guleryuz
* Ukrainian translation by Artem Yanchuk
* Vietnamese translation by Avenue JSC

---

## License and Legal Notices

### LICENSE

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License version 3
as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.
If not, see <http://www.gnu.org/licenses/agpl.html>.

### COPYRIGHT AND OWNERSHIP

Title, copyrights, intellectual property rights, international treaty,
other rights as applicable and all other legal rights in the Software
and Documentation are and shall remain the sole and exclusive property
of PAT Srl or its suppliers.
As well all modifications, enhancements, derivatives and other
alterations of the Software and Documentation regardless of who made
any modifications, if any, are and shall remain the sole and exclusive
property of PAT Srl or its suppliers.
The license for use granted herein is limited to the Software and
Documentation and does not transfer any ownership rights from PAT
to End Users or any other intellectual property rights.

### LEGAL NOTICES

The interactive user interfaces in modified source and object code
versions of this program must display Appropriate Legal Notices, as
required under Section 5 of the GNU General Public License version 3.

### TRADEMARKS

CMDBuild, CMDBuild READY2USE, openMAINT trademarks can't be altered
(colors, font, shape, etc) and can't be included in other trademarks.
CMDBuild, CMDBuild READY2USE, openMAINT trademarks can't be used as
a company logo, moreover no company can act as CMDBuild or
CMDBuild READY2USE or openMAINT author/owner/maintainer.
CMDBuild, CMDBuild READY2USE, openMAINT trademarks can't be removed
from the parts of the application in which are reported, and in
particular from the header at the top of each page.

In accordance with Section 7(b) of the GNU General Public License
version 3, these Appropriate Legal Notices must retain the display
of the "CMDBuild", "CMDBuild READY2USE" or "openMAINT" logo.

The Logo "CMDBuild" must be a clickable link that leads directly
to the Internet URL https://www.cmdbuild.org

The Logo "CMDBuild READY2USE" must be a clickable link that leads directly
to the Internet URL https://www.cmdbuildready2use.org

The Logo "openMAINT" must be a clickable link that leads directly
to the Internet URL https://www.openmaint.org

---

Below is a list of the publicly available software and resources used
by and distributed with CMDBuild, along with the licensing terms.

* Ace (Ajax.org Cloud9 Editor) (https://ace.c9.io) is
 released under the BSD License:
   https://raw.githubusercontent.com/ajaxorg/ace/master/LICENSE

* FullCalendar (https://fullcalendar.io) is released under the MIT License:
   https://github.com/fullcalendar/fullcalendar/blob/master/LICENSE.txt
