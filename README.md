# MediConnect (OnTime Systems for Philadelphia Medical Center)

JavaFX 25 + JDK 25 + SQL Server 2025 desktop application.

## Set up
1. In SQL Server, run `sql/schema.sql` (creates the `MediConnect` database and its 5 tables).
2. Edit `src/main/resources/db.properties` with your server URL, user and password.
3. Run: `mvn javafx:run`
4. First start creates the default login **S001 / Admin@123**. Change it before real use.

If Maven cannot find `mssql-jdbc 12.10.0.jre11`, use the newest `.jre11` version on Maven Central.

## Project layout
| Folder | Purpose |
|---|---|
| `model` | Plain data records: Staff, Doctor, Patient, Visit, Bill |
| `dao` | All SQL (PreparedStatements): one class per table plus counts |
| `util` | `Validator` (input rules), `PasswordHasher` (PBKDF2) |
| `ui` | JavaFX screens: Login, Main, Doctor, Patient, Visit, Bill, Query |

## Tests that need no database
```
javac -d out $(find src/main/java -name "*.java" | grep -v /ui/) src/test/java/CoreChecks.java
java -cp out CoreChecks
```
