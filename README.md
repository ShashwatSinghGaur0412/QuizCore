# QuizCore
 QuizCore – A Java Swing based Quiz Management System with MySQL Database. Features include student login, quiz attempt with timer, abilities (50-50, skip, hint, add time), auto result calculation, result history, admin panel for adding &amp; managing questions, and customizable settings.

## Features

Student Side:
- Login with details
- Attempt quizzes by subject
- Timer-based quiz
- Abilities: 50-50, Skip (limited), Add Time, Hint
- Automatic result calculation
- Result history with CSV export

Admin Side:
- Add / Manage / View Questions
- Settings (time, number of questions, pass percentage, abilities allowed)
- View all students' results

## Repo contents
- src/ — Java source (NetBeans layout)
- sql/quizcore_dump.sql — database schema + sample questions
- db.properties.example — DB config template (copy to db.properties)
- .gitignore — recommended ignore file

---

## Quick start (developer / another machine)

### Requirements
- Java 17 (or the Java version you used)
- MySQL 8 (or compatible)
- NetBeans (recommended) or your favorite IDE
- (Optional) MySQL Workbench or CLI for importing SQL

### Setup DB (Import dump)
1. Create database quizcore (or choose another name and update jdbc.url).
2. Import the SQL dump:
   - *MySQL Workbench*: Server > Data Import > Import from Self-Contained File → choose sql/quizcore_dump.sql → Select Import to Existing Database: quizcore → Start Import.
   - *CLI*:
     bash
     mysql -u root -p
     CREATE DATABASE quizcore;
     exit
     mysql -u root -p quizcore < sql/quizcore_dump.sql
     

### Configure application DB connection
1. Copy db.properties.example → db.properties
2. Fill values:
    -    jdbc.url=jdbc:mysql://localhost:3306/quizcore?useSSL=false&serverTimezone=UTC jdbc.username=root jdbc.password=your_password
3. Place db.properties under src/main/resources/ (so it's on classpath) or next to compiled classes.

### Run in NetBeans
- Open the project folder in NetBeans (File -> Open Project).
- Build and Run.
- OR run main in quizcore.student.QuizPanel for demo.

---

## Notes & security
- *Do not commit* db.properties containing real credentials.
- If you publish to public GitHub, remove any secrets and rotate passwords if accidentally committed.

---

## Export DB for distribution
- I included sql/quizcore_dump.sql which contains CREATE TABLE ... and sample data. Import it on target machine to recreate DB.

---

## Screenshots
<img width="1919" height="1027" alt="01" src="https://github.com/user-attachments/assets/78d57d61-6bd3-45bc-8f1e-1788d691bcf9" />
<img width="1919" height="1030" alt="02" src="https://github.com/user-attachments/assets/12c5f86d-397a-4f29-bf36-ae04eed9fd5f" />
<img width="1919" height="1028" alt="03" src="https://github.com/user-attachments/assets/653d48ae-8340-4b54-b52d-bb1def4a21e3" />
<img width="1919" height="1030" alt="04" src="https://github.com/user-attachments/assets/d74da31c-25c3-4976-9b10-cb02782a57e8" />
<img width="1919" height="1029" alt="05" src="https://github.com/user-attachments/assets/ba267315-a495-4c4f-9397-ad7f14c539d6" />
<img width="1919" height="1031" alt="06" src="https://github.com/user-attachments/assets/778eaf72-f293-4245-84d5-918dcfde77fe" />
<img width="1919" height="1031" alt="07" src="https://github.com/user-attachments/assets/b8cd4929-2e56-4ade-989d-f3abc9255d9c" />
<img width="1919" height="1028" alt="08" src="https://github.com/user-attachments/assets/4b317395-6d86-47ea-a02d-3db46d2d4165" />
<img width="1919" height="1030" alt="09" src="https://github.com/user-attachments/assets/0fdad9bc-b811-476a-b92a-a7030cdffe5d" />
<img width="1919" height="1032" alt="10" src="https://github.com/user-attachments/assets/4c508c48-7a6e-4a56-8f13-71134f02bf80" />
<img width="1919" height="1030" alt="11" src="https://github.com/user-attachments/assets/ce6baf5d-1c5e-4e4a-b531-cb3437613f33" />
<img width="1919" height="1031" alt="12" src="https://github.com/user-attachments/assets/dd3e56c1-3d04-4dd6-af99-690ac3ff6070" />
<img width="1919" height="1032" alt="13" src="https://github.com/user-attachments/assets/fbe3fdd7-3138-47d4-b919-4ebfb65bfe3b" />
<img width="1919" height="1027" alt="14" src="https://github.com/user-attachments/assets/e04e4b73-9c83-4fd2-8f05-a52a28937052" />
<img width="1919" height="1031" alt="15" src="https://github.com/user-attachments/assets/5ebbb1a8-4bf7-4890-8098-d3c16b6812f5" />
<img width="1919" height="1032" alt="16" src="https://github.com/user-attachments/assets/3b8c3442-20f5-46a1-b9f5-49795b1f7a04" />
<img width="1919" height="1031" alt="17" src="https://github.com/user-attachments/assets/93c0f748-2714-44dc-b4ff-bc68e582199f" />
<img width="1919" height="1034" alt="18" src="https://github.com/user-attachments/assets/5de37e4e-2770-4a3e-a54c-df9b4736f32a" />
<img width="1915" height="1030" alt="19" src="https://github.com/user-attachments/assets/4370aa03-f3dd-4415-945b-3709f1f9becf" />
<img width="1919" height="1030" alt="20" src="https://github.com/user-attachments/assets/6cd5e746-f6f8-4038-ba95-09373d1bb030" />
<img width="1919" height="1029" alt="21" src="https://github.com/user-attachments/assets/b950a2b4-39eb-4a7e-aa02-6c4c7918120a" />

## License
(Apache 2.0)
