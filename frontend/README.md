# Mätvärdesportalen (MVP)

## Table of Contents
* [Prerequisite](#prerequisite)
* [Installing](#installation)
* [Running](#running-the-app)
  * [Run Fake REST api](#fake-api)
* [Developing](#developing)
* [Testing](#testing)
* [Upgrading packages](#upgradingPackages)
* [Css](#css)
  * [Material Design](#materialDesign)
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
- see package.json for more ways to run and build the front end application

```bash
$ yarn start
```

## Tests

```bash
# single run
$ yarn test 

# run type checker and then the tests
$ yarn verify

# TDD style
$ yarn test-watch
```

# Upgrading packages

Some tips:
- Use `yarn outdated` to find which packages are in need of updates
- Upgrade one package at a time, run `yarn tslint`, start the application, commit, upgrade the next package

# Css
We use sass-files to write our _old_ main css but from **now on (2019-05-13) we will/must** use _css-in-js_ 
library called [typestyle](https://typestyle.github.io/#/). So no more changes in sass-files! 

## Material Design
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

# Resources

### Bundlers and module loaders

* [Parcel](https://parceljs.org/) - "Blazing fast, zero configuration web application bundler"
* [Fusebox](http://fuse-box.org/) - FuseBox "A bundler that does it right"

### React
* [Presentational and Container Components](https://medium.com/@dan_abramov/smart-and-dumb-components-7ca2f9a7c7d0) - By Dan Abramov
* [Container Components](https://medium.com/@learnreact/container-components-c0e67432e005) - Container Component Pattern
* [React](https://facebook.github.io/react/) - UI library, superset of HTML together with smart rendering optimizations
* [Redux](http://redux.js.org/) - Predictable state container for JavaScript apps.
* [Redux-Thunk](https://github.com/gaearon/redux-thunk) - Thunk middleware for redux (async actions)
* [React Router](https://reacttraining.com/react-router/web/guides/philosophy) - Connects application to browser window's URL
* [Redux Auth Wrapper](https://mjrussell.github.io/redux-auth-wrapper/) - Decouple your Authentication and Authorization from your components
* [Reselect](https://github.com/reactjs/reselect) - Simple “selector” library for Redux
* [Recompose](https://github.com/acdlite/recompose) - Recompose is a React utility belt for function components and higher-order components. Think of it like lodash for React.
* [React Developer Tools](https://chrome.google.com/webstore/detail/react-developer-tools/fmkadmapgofadopljbjfkapdkoienihi) - React Developer Tools is a Chrome DevTools extension for the open-source React JavaScript library. 
* [Redux DevTools](https://chrome.google.com/webstore/detail/redux-devtools/lmhkpmbekcpmknklioeibfkpmmfibljd) - Redux DevTools for debugging application's state changes.

### Other
* [typestyle](https://typestyle.github.io/#/) - Making CSS TypeSafe
* [Axios](https://github.com/mzabriskie/axios) - Promise based HTTP client for the browser and node.js
* [Jest](https://github.com/kulshekhar/ts-jest) - Test framework
* [normalizer](https://tonyhb.gitbooks.io/redux-without-profanity/content/normalizer.html) - API response that has nested resources and flatten them
* [classnames](https://github.com/JedWatson/classnames) - A simple JavaScript utility for conditionally joining classNames together.
* [Flexbox](https://css-tricks.com/snippets/css/a-guide-to-flexbox/) - A Complete Guide to Flexbox.
* [Lodash](https://lodash.com/docs/4.17.10) - A modern JavaScript utility library delivering modularity, performance & extras. 

