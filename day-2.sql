ALTER TABLE STUDENT MODIFY COLUMN EMAIL VARCHAR(100)
ALTER TABLE STUDENT MODIFY COLUMN CGPA FLOAT
INSERT INTO STUDENT VALUES(1201,"SRINU",22,"KUTLURISRINIVASAREDDY@GMAIL.COM",7.7)
INSERT INTO STUDENT (EMAIL,STU_ID,STU_AGE,CGPA,STU_NAME) VALUES("RAVIVARMA@GMAIL.COM",1204,24,7.0,"RAVI")
INSERT INTO STUDENT VALUES(1208,"BALARAM",23,"BALABALA@GMAIL.COM",7.2),(1232,"VARUN",24,"GARLAPATIVARUN@GMAIL.COM",7.8),(1205,"GOPI",22,"GPOI@GMAIL.COM",8.2)
SELECT * FROM STUDENT
CREATE TABLE DEPT(ID INT,NAME VARCHAR(30))
INSERT INTO DEPT(ID,NAME) SELECT STU_ID,STU_NAME FROM STUDENT
INSERT INTO DEPT(ID,NAME) SELECT STU_ID,STU_NAME FROM STUDENT WHERE STU_NAME = "SRINU"
SELECT * FROM DEPT
SET SQL_SAFE_UPDATES = 0
UPDATE STUDENT SET CGPA = 7.7 WHERE STU_NAME = "SRINU"
SELECT * FROM STUDENT
DELETE FROM STUDENT WHERE STU_ID=1205
CREATE TABLE EMPLOYEE(ID INT,EMP_NAME VARCHAR(30) NOT NULL, AGE INT NOT NULL DEFAULT 20)
INSERT INTO EMPLOYEE VALUES(1,"SRINU",22)
INSERT INTO EMPLOYEE (ID,EMP_NAME) VALUES(2,"VENU")
SELECT * FROM EMPLOYEE
DELETE FROM EMPLOYEE WHERE AGE=20