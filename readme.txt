COS 221 Practical Assignment 4
Student Number: u25136608
Application: Chinook Music Store GUI

================================================
REQUIREMENTS
================================================
- Java JDK 11 or higher
- Maven 3.6 or higher
- MariaDB or MySQL server running locally

================================================
HOW TO BUILD THE PROJECT
================================================
1. Open a terminal and navigate to the project folder:
   cd ChinookApp

2. Run the following Maven command to build:
   mvn clean package -DskipTests

This will generate the file:
   target/ChinookApp-1.0-SNAPSHOT-jar-with-dependencies.jar

================================================
HOW TO CONNECT TO THE DATABASE
================================================
The application reads database credentials from 
environment variables. Set the following before running:

   export CHINOOK_DB_PROTO=mariadb
   export CHINOOK_DB_HOST=localhost
   export CHINOOK_DB_PORT=3306
   export CHINOOK_DB_NAME=u25136608_chinook
   export CHINOOK_DB_USERNAME=root
   export CHINOOK_DB_PASSWORD=your_password_here

If environment variables are not set, the application
will use the following defaults:
   Host:     localhost
   Port:     3306
   Database: u25136608_chinook
   Username: root

================================================
HOW TO RUN THE APPLICATION
================================================
After building, run the following command:

   java -jar target/ChinookApp-1.0-SNAPSHOT-jar-with-dependencies.jar

================================================
DATABASE SETUP
================================================
1. Download Chinook_MySql.sql from ClickUp / https://github.com/lerocha/chinook-database/releases

2. Edit the SQL file to replace 'Chinook' with 
   'u25136608_chinook', then import:

   sudo mariadb < Chinook_MySql.sql

3. Verify import:
   sudo mariadb -e "SHOW TABLES IN u25136608_chinook;"