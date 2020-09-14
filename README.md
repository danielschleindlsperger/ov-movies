# ov-movies

Simple application to crawl the website of my local theatres and send me updates about OV films.

## How it works

The application will scrape the cinemas' websites in an interval (a few days).
It will then persist any entries that were not previously scraped in a database and notify me via email about these new entries.

## Helpful Commands

```bash
$ lein repl :headless # start leiningen repl in headless mode to connect to (e.g. from Cursive)

(dev) # switch to dev namespace
(dev/restart) # restart web server

$ lein kibit --replace --interactive # run kibit with interactive autofixer

$lein clj-kondo --lint src # run clj-kondo linter
```

## Architecture

The application is split up into two "processes". The crawler and the web app.

### Crawler

- Crawls the cineplex website
- Stores newly found entries in a database
- Sends a notification (via [Pushover](https://pushover.net/))
- Triggered by URL invokation (via [IFTTT](https://ifttt.com/my_applets) Webhook)

### Web App

- Blacklist a movie (hide from user)
- List all upcoming screenings

### Endpoints

#### `/`

Overview page with future screenings of OV movies.

#### `/crawl`

Trigger a crawl of the cineplex website.

##### Parameters

- `?passphrase=$PASSPHRASE`

#### `/blacklist/:movie-id`

Hides a movie from future notifications.

### SaaS

The external services used:

- [Pushover](https://pushover.net/)
- [IFTTT](https://ifttt.com/my_applets)

## Database

Postgres

### Development

`docker-compose up -d` for testing.

### Migrations

`lein migratus create`

`lein migratus migrate`

`lein migratus rollback`

## Environment Variables

| Name              | Usage                                           |
| ----------------- | ----------------------------------------------- |
| BASE_URL          | HTTPS URL of the application                    |
| PASSPHRASE        | User entered passphrase to protected endpoints  |
| DATABASE_URL      | JDBC URL to Postgres database                   |
| PORT              | Port the application will run on.               |
| PUSHOVER_USER_KEY | User key for [Pushover](https://pushover.net/)  |
| PUSHOVER_API_KEY  |                                                 |
| MOVIE_DB_API_KEY  | API key for [TMDB](https://www.themoviedb.org/) |

## Deployment

The application is deployed continuously on every successful `master` build.

## Roadmap (TODOs)

- Add more cinemas (Gilching, Seefeld, Gauting)
- Monitoring to detect when a scraper breaks
