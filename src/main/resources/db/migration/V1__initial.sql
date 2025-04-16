CREATE TABLE users (
   id SERIAL PRIMARY KEY,
   username TEXT NOT NULL,
   is_admin BOOLEAN NOT NULL,
   password_hash TEXT NOT NULL
);

CREATE TABLE refresh_tokens (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    token TEXT UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE job_listings (
    id SERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    company TEXT NOT NULL,
    location TEXT NOT NULL,
    remote BOOLEAN NOT NULL,
    salary NUMERIC
);
