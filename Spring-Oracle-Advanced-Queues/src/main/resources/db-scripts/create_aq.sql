-- Check user roles
SELECT * FROM USER_ROLE_PRIVS;

-- Check system privileges
SELECT * FROM USER_SYS_PRIVS WHERE PRIVILEGE LIKE 'AQ%';

-- Create a queue table
BEGIN
    DBMS_AQADM.CREATE_QUEUE_TABLE(
            queue_table         => 'my_queue_table',
            queue_payload_type  => 'SYS.AQ$_JMS_TEXT_MESSAGE'  -- Specify the payload type
    );
END;
/

-- Create a queue on the newly created queue table
BEGIN
    DBMS_AQADM.CREATE_QUEUE(
            queue_name  => 'my_queue',
            queue_table => 'my_queue_table'
    );
END;
/

-- Start the queue
BEGIN
    DBMS_AQADM.START_QUEUE(
            queue_name => 'my_queue'
    );
END;
/

SELECT * FROM dba_queue_tables where owner = 'EXAMPLE_SCHEMA';

SELECT owner, name, queue_table, enqueue_enabled, dequeue_enabled FROM dba_queues where owner = 'EXAMPLE_SCHEMA';

SELECT q.name, q.queue_table, q.queue_type, q.owner, s.ready, s.waiting, s.expired, s.total_wait
FROM v$aq s, dba_queues q WHERE q.QID = s.QID AND q.owner = 'EXAMPLE_SCHEMA';

SELECT name, enqueue_enabled, dequeue_enabled FROM all_queues WHERE name = 'MY_QUEUE';

SELECT * FROM MY_QUEUE_TABLE;


DECLARE
enqueue_options    DBMS_AQ.ENQUEUE_OPTIONS_T;  -- Enqueue options
    message_properties DBMS_AQ.MESSAGE_PROPERTIES_T;  -- Message properties
    message_handle     RAW(16);  -- To hold the message ID
    jms_message        SYS.AQ$_JMS_TEXT_MESSAGE;  -- The actual message
BEGIN
    -- Create the message (JMS Text Message)
    jms_message := SYS.AQ$_JMS_TEXT_MESSAGE.construct;
    jms_message.set_text('Hello, this is a message to the topic!');

    -- Enqueue the message to the topic
    DBMS_AQ.ENQUEUE(
            queue_name => 'MY_QUEUE',          -- The topic (queue) name
            enqueue_options => enqueue_options, -- Default options
            message_properties => message_properties, -- Default properties
            payload => jms_message,            -- The actual JMS text message payload
            msgid => message_handle            -- Get the message ID in this variable
    );

    -- Commit the transaction
COMMIT;

-- Output message ID (optional, for debugging)
DBMS_OUTPUT.PUT_LINE('Message enqueued with ID: ' || RAWTOHEX(message_handle));
END;
/

