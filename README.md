# ov-movies

Simple application to crawl the website of my local theatre and send me updates about OV films.

## How it works

The application will scrape the Cineplex website in an interval (a few days).
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

The external services I used.

- [Heroku](https://heroku.com)
- [ElephantSQL](https://www.elephantsql.com/)
- [Pushover](https://pushover.net/)
- [IFTTT](https://ifttt.com/my_applets)

## Database

Postgres

### Development

`docker-compose up -d` for testing.

### Production

I'm currently using a free database on [elephantsql.com/](https://www.elephantsql.com/).

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

```bash
git push heroku master
```

## Roadmap (TODOs)

- Find IMDB id for all movies and use that to save the movie.
  - Add the :cinema identifier to the screening
  - This will remove redundancies and enable a nicer looking UI
  - In case we can't determine a movie: Drop it?
- Add filter capabilities:
  - Filter by cinema: Multi-select
  - Only show Originals
  - Only show Non-dubbed movies (german and OV)
- Add more cinemas (Gilching, Seefeld, Gauting)
