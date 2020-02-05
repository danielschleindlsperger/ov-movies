import * as CDK from '@aws-cdk/core';
import * as Lambda from '@aws-cdk/aws-lambda'
import * as SM from '@aws-cdk/aws-secretsmanager'
import * as Events from '@aws-cdk/aws-events'
import * as Targets from '@aws-cdk/aws-events-targets'
import { resolve } from 'path'

const uberjarDir = resolve(__dirname, '../target/uberjar')

export class Stack extends CDK.Stack {
    constructor(scope: CDK.Construct, id: string, props?: CDK.StackProps) {
        super(scope, id, props);

        const crawlerHandler = new Lambda.Function(this, 'crawler', {
            runtime: Lambda.Runtime.JAVA_8,
            code: Lambda.Code.fromAsset(resolve(uberjarDir, 'crawler.jar')),
            handler: 'ov_movies.crawler.handler',
            timeout: CDK.Duration.seconds(60),
            memorySize: 1024
        })

        new Events.Rule(this, 'ov-movies-crawler-schedule', {
            // At 16:00 on Sunday and Wednesday.
            schedule: Events.Schedule.expression('cron(0 16 ? * SUN,WED *)'),
            targets: [new Targets.LambdaFunction(crawlerHandler)]
        })

        const databaseConnection = new SM.Secret(this, 'database-connection')
        databaseConnection.grantRead(crawlerHandler)
        crawlerHandler.addEnvironment('DATABASE_URL_SECRET_ID', databaseConnection.secretArn)

        const pushoverUserKey = new SM.Secret(this, 'pushover-user-key')
        pushoverUserKey.grantRead(crawlerHandler)
        crawlerHandler.addEnvironment('PUSHOVER_USER_KEY_SECRET_ID', pushoverUserKey.secretArn)

        const pushoverApiKey = new SM.Secret(this, 'pushover-api-key')
        pushoverApiKey.grantRead(crawlerHandler)
        crawlerHandler.addEnvironment('PUSHOVER_API_KEY_SECRET_ID', pushoverApiKey.secretArn)
    }
}
