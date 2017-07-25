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
$ yarn start
```

### Run Fake REST api 
We use [json-server](https://github.com/typicode/json-server) to fake our api and store the data in db.json file. 
This file is also included in the git repository for now. Note that the json-server must be installed globally and 
run as a background task in another terminal window.

```bash
$ yarn global add json-server 

# start JSON server
$ cd [to-project-root]
$ json-server --watch db.json
```


## Tests

```bash
# single run
$ yarn test 

# TDD style
$ yarn test:watch
```

# Resources

* [Fusebox](http://fuse-box.org/) - FuseBox is a next generation bundler and module loader.
* [Presentational and Container Components](https://medium.com/@dan_abramov/smart-and-dumb-components-7ca2f9a7c7d0) - By Dan Abramov
* [Container Components](https://medium.com/@learnreact/container-components-c0e67432e005) - Container Component Pattern
* [Redux](http://redux.js.org/) - Predictable state container for JavaScript apps.
* [Redux-Thunk](https://github.com/gaearon/redux-thunk) - Thunk middleware for redux (async actions)
* [Axios](https://github.com/mzabriskie/axios) - Promise based HTTP client for the browser and node.js
* [Jest](https://github.com/kulshekhar/ts-jest) - Test framework
* [normalizer](https://tonyhb.gitbooks.io/redux-without-profanity/content/normalizer.html) - API response that has nested resources and flatten them
* [classnames](https://github.com/JedWatson/classnames) - A simple JavaScript utility for conditionally joining classNames together.
* [Flexbox] (https://css-tricks.com/snippets/css/a-guide-to-flexbox/) - A Complete Guide to Flexbox.
* [React Developer Tools] (https://chrome.google.com/webstore/detail/react-developer-tools/fmkadmapgofadopljbjfkapdkoienihi) - React Developer Tools is a Chrome DevTools extension for the open-source React JavaScript library. 
* [Redux DevTools] (https://chrome.google.com/webstore/detail/redux-devtools/lmhkpmbekcpmknklioeibfkpmmfibljd) - Redux DevTools for debugging application's state changes.
