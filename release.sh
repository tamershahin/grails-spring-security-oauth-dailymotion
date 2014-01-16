#!/bin/bash

rm -rf target/release
mkdir target/release
cd target/release
git clone https://github.com/tamershahin/grails-spring-security-oauth-dailymotion.git
cd grails-spring-security-oauth-dailymotion
grails clean
grails compile
grails publish-plugin --stacktrace
