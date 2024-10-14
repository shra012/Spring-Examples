CREATE USER example_schema IDENTIFIED BY pwd;

GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CREATE VIEW TO example_schema;

-- Grant execute privileges on the AQ packages
GRANT EXECUTE ON DBMS_AQ TO example_schema;
GRANT EXECUTE ON DBMS_AQADM TO example_schema;
GRANT EXECUTE ON DBMS_AQIN TO example_schema;
GRANT AQ_USER_ROLE TO example_schema;
GRANT AQ_ADMINISTRATOR_ROLE TO example_schema;
-- Create a new tablespace for user data
ALTER USER example_schema QUOTA 1024M ON SYSTEM;
GRANT SELECT_CATALOG_ROLE TO example_schema;
GRANT SELECT ON ALL_SCHEDULER_JOBS TO example_schema;
GRANT SELECT ON USER_SCHEDULER_JOBS TO example_schema;


ALTER USER example_schema ACCOUNT UNLOCK;