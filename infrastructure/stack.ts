import * as CDK from '@aws-cdk/core';
import * as Lambda from '@aws-cdk/aws-lambda'
import * as SM from '@aws-cdk/aws-secretsmanager'
import * as Events from '@aws-cdk/aws-events'
import * as Targets from '@aws-cdk/aws-events-targets'
import * as ApiGateway from '@aws-cdk/aws-apigateway'
import {resolve} from 'path'

const uberjarDir = resolve(__dirname, '../target/uberjar')

export class Stack extends CDK.Stack {
    constructor(scope: CDK.Construct, id: string, props?: CDK.StackProps) {
        super(scope, id, props);

        const databaseConnection = new SM.Secret(this, 'database-connection')

        const {url, apiHandler} = api(this)
        const {crawlerHandler} = crawler(this)

        crawlerHandler.addEnvironment('API_URL', url)

        databaseConnection.grantRead(crawlerHandler)
        databaseConnection.grantRead(apiHandler)
        crawlerHandler.addEnvironment('DATABASE_URL_SECRET_ID', databaseConnection.secretArn)
        apiHandler.addEnvironment('DATABASE_URL_SECRET_ID', databaseConnection.secretArn)
    }
}

function crawler(scope: CDK.Construct) {
    const crawlerHandler = new Lambda.Function(scope, 'crawler', {
        runtime: Lambda.Runtime.JAVA_8,
        code: Lambda.Code.fromAsset(resolve(uberjarDir, 'crawler.jar')),
        handler: 'ov_movies.crawler.handler',
        timeout: CDK.Duration.seconds(60),
        memorySize: 1024
    })

    new Events.Rule(scope, 'ov-movies-crawler-schedule', {
        // At 16:00 on Sunday and Wednesday.
        schedule: Events.Schedule.expression('cron(0 16 ? * SUN,WED *)'),
        targets: [new Targets.LambdaFunction(crawlerHandler)]
    })


    const pushoverUserKey = new SM.Secret(scope, 'pushover-user-key')
    pushoverUserKey.grantRead(crawlerHandler)
    crawlerHandler.addEnvironment('PUSHOVER_USER_KEY_SECRET_ID', pushoverUserKey.secretArn)

    const pushoverApiKey = new SM.Secret(scope, 'pushover-api-key')
    pushoverApiKey.grantRead(crawlerHandler)
    crawlerHandler.addEnvironment('PUSHOVER_API_KEY_SECRET_ID', pushoverApiKey.secretArn)

    return {crawlerHandler}
}

function api(scope: CDK.Construct) {
    const handler = new Lambda.Function(scope, 'ov-movies-api-handler', {
        runtime: Lambda.Runtime.JAVA_8,
        code: Lambda.Code.fromAsset(resolve(uberjarDir, 'api.jar')),
        handler: 'ov_movies.api.handler',
        timeout: CDK.Duration.seconds(10),
        memorySize: 512
    })

    const lambdaApi = new ApiGateway.LambdaRestApi(scope, 'ov-movies-rest-api', {
        handler,
        deploy: true,
    })

    return {
        url: lambdaApi.url,
        apiHandler: handler,
    }
}