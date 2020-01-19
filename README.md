# ov-movies

Simple application to crawl the website of my local theatre and send me updates about OV films.

## How it works

Once a day the application will scrape the website.
It will then persist any entries that were not previously scraped in a database and notify me via email about these new entries.
The application also has a web interface to search for upcoming ov screenings.

## Database

Postgres with hugsql and migratus.

### Development

`docker-compose up -d` for testing.

### Migrations

`lein migratus create`

`lein migratus migrate`

`lein migratus rollback`