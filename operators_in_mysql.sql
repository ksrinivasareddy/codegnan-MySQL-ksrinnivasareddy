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


-- comparision operatora
select  * from employee where salary > 50000
select  * from employee where salary < 50000
select  * from employee where salary >= 45000
select  * from employee where salary <= 45000
select  * from employee where salary != 50000

-- logical operators
select * from employee where age = 24 and salary > 45000
select * from employee where age = 24 or salary > 45000


-- null operators

set sql_safe_updates = 0

alter table employee add column mobileno bigint
update employee set mobileno = 656565656 where salary > 50000
select * from employee where mobileno is null
select * from employee where mobileno is not null


-- between operator
select * from employee where age between 22 and 25 


-- in operator
select * from employee where salary in(30000,50000)


-- case operator

select *,
case
	when salary <= 10000 then 0.01
    when salary <= 20000 then 0.02
    when salary >= 30000 then 0.04
    else 0
end as bonus
from employee;
    
-- string operators
-- concat

select name,age, concat(name," (",age," in age )") as data from employee

-- concat_ws(with separator)

alter table employee add column surname varchar(10)
set sql_safe_updates=0;

update employee set surname="kutluri" where name ="srinu"
update employee set surname="a" where name ="ravio"
update employee set surname="c" where name ="venkat"
update employee set surname="p" where name ="varun"
update employee set surname="a" where name ="gopi"

select * from employee

select id,concat(name,"",surname) as full_name from employee 
-- concat_ws

select id,age,concat_ws(" ",name,surname,salary) as full_name from employee

-- lower and upper

select upper(surname) from employee
select lower(name) from employee

-- character_length
-- length and char_length

select length(surname) as len from employee
select char_length(surname) as clen from employee

-- substring

select substring(name,3,5) from employee

-- reverse

select id,name,reverse(name) as rev from employee
 
 
 -- replace
 
 select replace(surname,"a","ampabathina") as re from employee where name="ravio"
 
 -- like
 
 select * from employee where name like "s%"
 
 select * from employee where name like"____o"
  select * from employee where name like"sr__u"
  