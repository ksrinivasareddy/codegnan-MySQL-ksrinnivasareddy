use training
-- ARITHEMATIC OPERATONS
CREATE TABLE EMPLOYEE(ID INT PRIMARY KEY AUTO_INCREMENT,NAME VARCHAR(50),AGE INT,SALARY FLOAT)

INSERT INTO EMPLOYEE(NAME,AGE,SALARY) VALUES("SRINU",24,50000)
INSERT INTO EMPLOYEE(NAME,AGE,SALARY) VALUES("RAVIO",22,59000),("VENKAT",25,30000),("VARUN",34,56000)
SELECT * FROM EMPLOYEE
SELECT NAME,AGE,(SALARY+1000) AS BONAS FROM EMPLOYEE
SELECT NAME,AGE,(SALARY-1000) AS CUTTINGS FROM EMPLOYEE
SELECT NAME,AGE,(ID*AGE) AS NEWDATA FROM EMPLOYEE
SELECT NAME,AGE,(SALARY/2) AS NEWDATA FROM EMPLOYEE
SELECT NAME,AGE,(AGE%3) AS NEWDATA FROM EMPLOYEE


-- 