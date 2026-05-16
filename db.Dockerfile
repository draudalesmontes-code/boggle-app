FROM mysql:8.0

COPY BoggleSpringBoot/src/main/resources/db/BoggleDB.sql /docker-entrypoint-initdb.d/001_init.sql
COPY BoggleSpringBoot/src/main/resources/db/DictionaryData.sql /docker-entrypoint-initdb.d/002_DictionaryData.sql
