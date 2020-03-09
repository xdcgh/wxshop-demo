create table USER
(
	ID BIGINT auto_increment,
	NAME VARCHAR(100) null,
	TEL VARCHAR(20) null,
	AVATAR_URL VARCHAR(1024) null,
	CREATED_AT TIMESTAMP null,
	UPDATED_AT TIMESTAMP null,

	constraint USER_pk
		primary key (ID)
);