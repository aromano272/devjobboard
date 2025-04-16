CREATE TABLE job_listings (
    id SERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    company TEXT NOT NULL,
    location TEXT NOT NULL,
    remote BOOLEAN NOT NULL,
    salary NUMERIC
);