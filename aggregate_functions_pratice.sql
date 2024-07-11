create database aggregate_fun
use aggregate_fun
create table student_details(id int primary key,name varchar(100),english int,maths int,social int,telugu int,hindi int,pyshics int)
insert into student_details values(1,"srinu",95,90,85,95,96,97)
insert into student_details values(2,"venkat",96,89,89,87,90,98),(3,"ravi",99,80,85,88,87,88),(4,"gopi",99,88,79,97,70,88),(5,"sai",99,80,88,97,60,98)
select * from student_details
select id,name,(english+maths+social+telugu+hindi+pyshics) as total from student_details order by total asc
create table final_marks(id int,name varchar(100),total int)
insert into final_marks select id,name,(english+maths+social+telugu+hindi+pyshics) as total from student_details
select * from final_marks
alter table final_marks add column grade char
set sql_safe_updates=0;
update final_marks set grade="b" where total < 550