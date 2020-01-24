import * as cdk from '@aws-cdk/core';
import * as lambda from '@aws-cdk/aws-lambda'
import { resolve } from 'path'

const uberjarDir = resolve(__dirname, '../target/uberjar')

export class Stack extends cdk.Stack {
  constructor(scope: cdk.Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    // The code that defines your stack goes here
    const crawlerHandler = new lambda.Function(this, 'crawler', {
      runtime: lambda.Runtime.JAVA_8,
      code: lambda.Code.fromAsset(resolve(uberjarDir, 'crawler.jar')),
      handler: 'ov_movies.crawler',
      timeout: cdk.Duration.seconds(60),
      memorySize: 1024
    })
  }
}
