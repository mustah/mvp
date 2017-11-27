import 'es6-shim'; // adds polyfills for a host of functions that might otherwise be missing in older browsers
import {History} from 'history';
import createHashHistory from 'history/createHashHistory';
import {InitOptions} from 'i18next';
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import * as React from 'react';
import * as ReactDOM from 'react-dom';
import {Provider} from 'react-redux';
import {HashRouter} from 'react-router-dom';
import {ConnectedRouter} from 'react-router-redux';
import {Store} from 'redux';
import {persistStore} from 'redux-persist';
import {initLanguage} from './i18n/i18n';
import {RootState} from './reducers/rootReducer';
import {initRestClient} from './services/restClient';
import {onTranslationInitialized} from './services/translationService';
import {configureStore} from './store/configureStore';
import {App} from './app/App';
import {mvpTheme} from './app/themes';

export const history: History = createHashHistory();

const appStore: Store<RootState> = configureStore(history);

persistStore<RootState>(appStore, {whitelist: ['auth', 'language', 'ui', 'searchParameters']}, (error?: any) => {
  if (!error) {
    const {auth: {token}, language: {language}} = appStore.getState();
    initRestClient(token);
    initLanguage(language);
  }
});

onTranslationInitialized((options: InitOptions) => {
  ReactDOM.render(
    <Provider store={appStore}>
      <ConnectedRouter history={history}>
        <HashRouter>
          <MuiThemeProvider muiTheme={mvpTheme}>
            <App/>
          </MuiThemeProvider>
        </HashRouter>
      </ConnectedRouter>
    </Provider>,
    document.getElementById('app'),
  );
});
