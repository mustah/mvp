import 'es6-shim'; // adds polyfills for a host of functions that might otherwise be missing in older browsers
import * as React from 'react';
import 'react-dates/initialize'; // Needs to be imported in beginning of application in order for styling to work.
import 'react-dates/lib/css/_datepicker.css';
import * as ReactDOM from 'react-dom';
import {Provider} from 'react-redux';
import 'react-virtualized/styles.css';
import {AppContainer} from './app/AppContainer';
import {onTranslationInitialized} from './services/translationService';
import {store} from './store/configureStore';

onTranslationInitialized(() =>
  ReactDOM.render(
    (
      <Provider store={store}>
        <AppContainer/>
      </Provider>
    ),
    document.getElementById('app'),
  ));
