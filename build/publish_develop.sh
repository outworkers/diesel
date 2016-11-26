#!/usr/bin/env bash
if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "develop" ];
then

    if [ "${TRAVIS_SCALA_VERSION}" == "2.12.0" ] && [ "${TRAVIS_JDK_VERSION}" == "oraclejdk8" ];
    then

        echo "Setting git user email to ci@outworkers.com"
        git config user.email "ci@outworkers.com"

        echo "Setting git user name to Travis CI"
        git config user.name "Travis CI"

        echo "The current JDK version is ${TRAVIS_JDK_VERSION}"
        echo "The current Scala version is ${TRAVIS_SCALA_VERSION}"

        echo "Creating credentials file"
        if [ -e "$HOME/.bintray/.credentials" ]; then
            echo "Bintray credentials file already exists"
        else
            mkdir -p "$HOME/.bintray/"
            touch "$HOME/.bintray/.credentials"
            echo "realm = Bintray API Realm" >> "$HOME/.bintray/.credentials"
            echo "host = api.bintray.com" >> "$HOME/.bintray/.credentials"
            echo "user = $bintray_user" >> "$HOME/.bintray/.credentials"
            echo "password = $bintray_password" >> "$HOME/.bintray/.credentials"
        fi

        if [ -e "$HOME/.bintray/.credentials" ]; then
            echo "Bintray credentials file successfully created"
        else
            echo "Bintray credentials still not found"
        fi

        if [ -e "$HOME/.ivy2/.credentials" ]; then
            echo "Maven credentials file already exists"
        else
        mkdir -p "$HOME/.ivy2/"
            touch "$HOME/.ivy2/.credentials"
            echo "realm = Sonatype Nexus Repository Manager" >> "$HOME/.ivy2/.credentials"
            echo "host = oss.sonatype.org" >> "$HOME/.ivy2/.credentials"
            echo "user = $maven_user" >> "$HOME/.ivy2/.credentials"
            echo "password = $maven_password" >> "$HOME/.ivy2/.credentials"
        fi

        if [ -e "$HOME/.ivy2/.credentials" ]; then
            echo "Maven credentials file successfully created"
        else
            echo "Maven credentials still not found"
        fi

        if [ -e "$HOME/.bintray/.credentials" ]; then
            echo "Bintray credentials file successfully created"
        else
            echo "Bintray credentials still not found"
        fi

        sbt version-bump-patch git-tag

        echo "Pushing tag to GitHub."
        git push --tags "https://${github_token}@${GH_REF}"

        echo "Publishing version bump information to GitHub"
        git add .
        git commit -m "TravisCI: Bumping version to match CI definition [ci skip]"
        git checkout -b version_branch
        git checkout -B develop version_branch

        git push "https://${github_token}@${GH_REF}" develop

        echo "Publishing new version to bintray"
        sbt +bintray:publish

        exit $?

    else
        echo "Only publishing version for Scala 2.12.0 and Oracle JDK 8 to prevent multiple artifacts"
        exit 0
    fi
else
    echo "This is either a pull request or the branch is not develop, deployment not necessary"
    exit 0
fi