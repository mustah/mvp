import 'es6-shim'; // adds polyfills for a host of functions that might otherwise be missing in
                   // older browsers
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import * as React from 'react';
import 'react-dates/initialize'; // Needs to be imported in beginning of application in order for
                                 // styling to work.
import 'react-dates/lib/css/_datepicker.css';
import * as ReactDOM from 'react-dom';
import {Provider} from 'react-redux';
import {ConnectedRouter} from 'react-router-redux';
import {PersistGate} from 'redux-persist/integration/react';
import {App} from './app/App';
import {history} from './app/routes';
import {mvpTheme} from './app/themes';
import {LoadingLarge} from './components/loading/Loading';
import {onTranslationInitialized} from './services/translationService';
import {persistor, store} from './store/configureStore';

onTranslationInitialized(() =>
  ReactDOM.render(
    (
      <Provider store={store}>
        <MuiThemeProvider muiTheme={mvpTheme}>
          <PersistGate loading={<LoadingLarge/>} persistor={persistor}>
            <ConnectedRouter history={history}>
              <App/>
            </ConnectedRouter>
          </PersistGate>
        </MuiThemeProvider>
      </Provider>
    ),
    document.getElementById('app'),
  ));
