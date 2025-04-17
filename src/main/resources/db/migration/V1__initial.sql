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
    experience TEXT NOT NULL,
    company TEXT NOT NULL,
    remote TEXT NOT NULL,
    type TEXT NOT NULL,
    location TEXT NOT NULL,
    min_salary NUMERIC,
    max_salary NUMERIC,
    created_at TIMESTAMP DEFAULT now(),
    created_by_user_id INT REFERENCES users(id)
);
