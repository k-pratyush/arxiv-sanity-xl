CREATE TABLE DOCUMENT (
	id bigserial PRIMARY KEY,
	name VARCHAR(1000) UNIQUE,
	url VARCHAR(300),
	created_date date
);

CREATE TABLE DOCUMENT_EMBEDDING (
	id bigserial PRIMARY KEY,
	embedding vector(384) NOT NULL,
	document_id BIGINT,
	chunk INTEGER,
	created_date date
);
alter table DOCUMENT_EMBEDDING ADD CONSTRAINT FK_DOCUMENT_ID
	FOREIGN KEY (document_id) references document (ID);


CREATE TABLE USER (
	id bigserial PRIMARY KEY,
	user_id VARCHAR(100) UNIQUE,
	preferences vector(384),
	created_date date
);

ALTER TABLE DOCUMENT ADD COLUMN DOCUMENT VARCHAR(65535);

GRANT ALL on DOCUMENT TO PUBLIC;
GRANT ALL on DOCUMENT_EMBEDDING TO PUBLIC;

GRANT ALL on USER TO PUBLIC;