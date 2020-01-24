# ov-movies

Simple application to crawl the website of my local theatre and send me updates about OV films.

## How it works

Once a day the application will scrape the website.
It will then persist any entries that were not previously scraped in a database and notify me via email about these new entries.
The application also has a web interface to search for upcoming ov screenings.

## Architecture

The application is split up into two "processes". The crawler and the web app.

### Crawler

- Crawls the cineplex website
- Stores newly found entries in a database
- Sends a notification

Invokable with `lein with-profiles crawler run`

### Web App

**TODO**

## Database

Postgres with hugsql and migratus.

### Development

`docker-compose up -d` for testing.

### Migrations

`lein migratus create`

`lein migratus migrate`

`lein migratus rollback`

## Infrastructure and Deployment

Build with `lein with-profile crawler uberjar`

### Deploy Infrastructure 

```bash
npm install

npx cdk synth # emits the synthesized CloudFormation template
npx cdk diff # compare deployed stack with current state

# actual deploy
npx cdk deploy # deploy this stack to your default AWS account/region
```