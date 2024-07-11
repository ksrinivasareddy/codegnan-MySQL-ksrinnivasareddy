use training
show tables
-- drop table student
-- alter table student drop constraint foreign key
create table student_data(id int, name varchar(30),sub_name varchar(50),marks int default 0)
insert into student_data values(1,"srinu","maths",80)
insert into student_data values(1,"srinu","english",90),(1,"srinu","social",95),(2,"venkat","maths",85),(2,"venkat","english",80),(2,"venkat","social",90),(3,"ravi","maths",90),(3,"ravi","english",90),(3,"ravi","social",80),(4,"gopi","maths",98),(4,"gopi","english",85),(4,"gopi","social",95)
select * from student_data
select name, avg(marks) from student_data group by name 
select name, avg(marks) from student_data group by name order by avg(marks) asc
select name, sum(marks) as total from student_data group by name order by avg(marks) desc
select id,name,sum(marks) as total from student_data group by name,id
select id,name,marks from student_data where sub_name="english" order by marks desc


create table final_marks(id int primary key,name varchar(100),final_marks int)
insert into final_marks select id,name,sum(marks) as total from student_data group by name,id
select * from final_marks
