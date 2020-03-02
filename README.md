# ov-movies

Simple application to crawl the website of my local theatre and send me updates about OV films.

## How it works

The application will scrape the Cineplex website in an interval (a few days).
It will then persist any entries that were not previously scraped in a database and notify me via email about these new entries.

## Helpful Commands

```bash
lein repl :headless # start leiningen repl in headless mode to connect to (e.g. from Cursive)

(dev) # switch to dev namespace
(restart) # restart web server

npm run build # build uberjars

npm run deploy # deploy stack to aws
```

## Architecture

The application is split up into two "processes". The crawler and the web app.

### Crawler

- Crawls the cineplex website
- Stores newly found entries in a database
- Sends a notification
- Triggered by URL invokation

### Web App

- Blacklist a movie (hide from user)
- List all upcoming screenings

#### Enpoints

##### `/blacklist/:movie-id`

Hides a movie from future notifications.

## Database

Postgres.

### Development

`docker-compose up -d` for testing.

### Production

I'm currently using a free database on [elephantsql.com/](https://www.elephantsql.com/).

### Migrations

`lein migratus create`

`lein migratus migrate`

`lein migratus rollback`

## Infrastructure and Deployment

Build with `npm run build`

### Deploy Infrastructure 

```bash
npm install

npx cdk synth # emits the synthesized CloudFormation template
npx cdk diff # compare deployed stack with current state

# actual deploy
npx cdk deploy # deploy this stack to your default AWS account/region
```

## Roadmap (TODOs)

- Deployment
    - Remove CDK deployment to AWS
    - Deploy to Heroku
    - HTTP Webhook to trigger crawler with IFTTT
- Include original German movies as well
- HTML page with filterable results (Optional)
    - Pushover Notifications have a maximum length so we could just render it out as html and shorten the notification