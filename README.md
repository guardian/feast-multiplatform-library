# Feast Multiplatform Library

## What is it?

This library contains a set of shared logic that can be used on our iOS and Android applications.

It is built using Kotlin Multiplatform, which can targets our core platforms.

## Scope of this library

As of Sept. 2025, the aim of this library is to contain small, re-usable chunks of business logic in relation to the Feast application. A good way to think about it is to only implement [pure functions](https://en.wikipedia.org/wiki/Pure_function) in this repo.

We do not want to go into the realm of reusable UI components, nor shared API calls logic at this stage.

## How to publish?

Go to the [github action tab, release.yaml](https://github.com/guardian/feast-multiplatform-library/actions/workflows/release.yml), then trigger the workflow.

## Re-generating the models

The backend repository contains [the schemas for our recipe models](https://github.com/guardian/recipes-backend/tree/main/schema). 
To avoid any error and make evolving these models easier, we generate the kotlin code from these schemas.

see `./generate-models.sh` for the script that does this.

The generated file is commit to github, making the build deterministic.
