# Mätvärdesportalen (MVP)

## Table of Contents
* [Prerequisite](#prerequisite)
* [Installing](#installation)
* [Running](#running-the-app)
  * [Run Fake REST api](#fake-api)
* [Developing](#developing)
* [Testing](#testing)
* [Resources](#resources)

## Prerequisite
First install latest version of node [Node](https://nodejs.org/en/) and then install Yarn, the package manage from 
[Yarn](https://yarnpkg.com/en/docs/install) for your environment.

## Installation and starting
Install all dependencies.

```bash
$ yarn install
```

Run the server in development mode, featuring:
- live reload on source code change
- configuration based on the config.dev.ts file

```bash
$ yarn start
```

Or,
Run the server in production mode, featuring:
- live reload on source code change
- configuration based on the config.prod.ts file

```bash
$ yarn dist-server
```

### Run Fake REST API
We use [json-server](https://github.com/typicode/json-server) to fake our API.

**Note** the JSON server runs in the foreground, which means that you should leave it up and running in a terminal
as long as you need it, or run it in the background by suffixing the JSON server call with `&` (i.e. `... --port 4000 &`).

```bash
$ yarn json-server
```

## Tests

```bash
# single run
$ yarn test 

# TDD style
$ yarn test-watch
```

# Upgrading packages

Some tips:
- Use `yarn outdated` to find which packages are in need of updates
- Upgrade one package at a time, run `yarn tslint`, start the application, commit, upgrade the next package

# Material Design
We are using the material design react components from [material-ui](http://www.material-ui.com/#/) library.

## Icons

In the design report from BOID it's stated that we should us icons from the following sources:
* Elvacos proprietary
* [Material Icons](https://material.io/icons/)
* [Material Design Icons](https://materialdesignicons.com/)

Currently we have installed the Material Design Icons webfonts in this repo. To make the usage of these icons easy we have
created the <Icon> tag. To use any of these fonts:
* Search the Material Design Icons library for the icon you want.
* Get the name of the icon, e.g. account-circle .
* Add the icon, e.g. <Icon name="account-circle"/>.
* You're done!

So far we have not mapped Material Icons and will try to avoid this as long as possible.
This will hopefully make the icon handling easy although we at some point most likely will have to decide
how to handle the Elvaco proprietary icons.

# Fonts

We are using *PT Sans* as the font of choice for most text in the application. More specifically, we are using both the regular and the bold versions.

The font is available in [Google Fonts](https://fonts.google.com/specimen/PT+Sans?selection.family=PT+Sans:400,700).

For easier local hosting, we use [Google Webfonts Helper](https://google-webfonts-helper.herokuapp.com/fonts/pt-sans?subsets=latin) which has copy & paste instructions for when you want to host the font files together with the application.

# Resources

### Bundlers and module loaders

* [Fusebox](http://fuse-box.org/) - FuseBox is a next generation bundler and module loader.

### React
* [Presentational and Container Components](https://medium.com/@dan_abramov/smart-and-dumb-components-7ca2f9a7c7d0) - By Dan Abramov
* [Container Components](https://medium.com/@learnreact/container-components-c0e67432e005) - Container Component Pattern
* [React](https://facebook.github.io/react/) - UI library, superset of HTML together with smart rendering optimizations
* [Redux](http://redux.js.org/) - Predictable state container for JavaScript apps.
* [Redux-Thunk](https://github.com/gaearon/redux-thunk) - Thunk middleware for redux (async actions)
* [React Router](https://reacttraining.com/react-router/web/guides/philosophy) - Connects application to browser window's URL
* [Redux Auth Wrapper](https://mjrussell.github.io/redux-auth-wrapper/) - Decouple your Authentication and Authorization from your components
* [Reselect](https://github.com/reactjs/reselect) - Simple “selector” library for Redux
* [React Developer Tools](https://chrome.google.com/webstore/detail/react-developer-tools/fmkadmapgofadopljbjfkapdkoienihi) - React Developer Tools is a Chrome DevTools extension for the open-source React JavaScript library. 
* [Redux DevTools](https://chrome.google.com/webstore/detail/redux-devtools/lmhkpmbekcpmknklioeibfkpmmfibljd) - Redux DevTools for debugging application's state changes.

### Other
* [Axios](https://github.com/mzabriskie/axios) - Promise based HTTP client for the browser and node.js
* [Jest](https://github.com/kulshekhar/ts-jest) - Test framework
* [normalizer](https://tonyhb.gitbooks.io/redux-without-profanity/content/normalizer.html) - API response that has nested resources and flatten them
* [classnames](https://github.com/JedWatson/classnames) - A simple JavaScript utility for conditionally joining classNames together.
* [Flexbox](https://css-tricks.com/snippets/css/a-guide-to-flexbox/) - A Complete Guide to Flexbox.

