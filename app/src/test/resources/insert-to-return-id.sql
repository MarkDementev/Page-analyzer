INSERT INTO url (name, created_at) VALUES ('a', '2022-02-21 12:00:40');

INSERT INTO url_check (
status_code, title, h1, description, url_id, created_at
)
VALUES (
200, 'addedTestCheckWithId', 'a', 'a', (SELECT id FROM url WHERE name = 'a'), '2022-02-21 12:00:40'
);