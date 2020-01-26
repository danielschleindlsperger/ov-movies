import * as cdk from '@aws-cdk/core';
import * as lambda from '@aws-cdk/aws-lambda'
import * as sm from '@aws-cdk/aws-secretsmanager'
import { resolve } from 'path'

const uberjarDir = resolve(__dirname, '../target/uberjar')

export class Stack extends cdk.Stack {
  constructor(scope: cdk.Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const crawlerHandler = new lambda.Function(this, 'crawler', {
      runtime: lambda.Runtime.JAVA_8,
      code: lambda.Code.fromAsset(resolve(uberjarDir, 'crawler.jar')),
      handler: 'ov_movies.crawler',
      timeout: cdk.Duration.seconds(60),
      memorySize: 1024
    })

    const databaseConnection = new sm.Secret(this, 'database-connection')
    databaseConnection.grantRead(crawlerHandler)
    crawlerHandler.addEnvironment('DATABASE_URL_SECRET_ID', databaseConnection.secretArn)

    const pushoverUserKey = new sm.Secret(this, 'pushover-user-key')
    pushoverUserKey.grantRead(crawlerHandler)
    crawlerHandler.addEnvironment('PUSHOVER_USER_KEY_SECRET_ID', pushoverUserKey.secretArn)

    const pushoverApiKey = new sm.Secret(this, 'pushover-api-key')
    pushoverApiKey.grantRead(crawlerHandler)
    crawlerHandler.addEnvironment('PUSHOVER_API_KEY_SECRET_ID', pushoverApiKey.secretArn)
  }
}
