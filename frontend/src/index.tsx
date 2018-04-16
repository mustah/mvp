import 'es6-shim'; // adds polyfills for a host of functions that might otherwise be missing in older browsers
import {History} from 'history';
import createHashHistory from 'history/createHashHistory';
import {InitOptions} from 'i18next';
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import * as React from 'react';
import * as ReactDOM from 'react-dom';
import {Provider} from 'react-redux';
import {ConnectedRouter} from 'react-router-redux';
import {Store} from 'redux';
import {persistStore} from 'redux-persist';
import {App} from './app/App';
import {mvpTheme} from './app/themes';
import {initLanguage} from './i18n/i18n';
import {AppState, RootState} from './reducers/rootReducer';
import {restClientWith} from './services/restClient';
import {onTranslationInitialized} from './services/translationService';
import {storeFactory} from './store/configureStore';

export const history: History = createHashHistory();

const appStore: Store<AppState> = storeFactory(history);

persistStore<AppState>(appStore, {whitelist: ['auth', 'language', 'ui', 'searchParameters']}, (error?: any) => {
  if (!error) {
    const {auth: {token}, language: {language}}: RootState = appStore.getState()!;
    restClientWith(token);
    initLanguage(language.code);
  }
});

onTranslationInitialized((options: InitOptions) => {
  ReactDOM.render(
    <Provider store={appStore}>
      <ConnectedRouter history={history}>
        <MuiThemeProvider muiTheme={mvpTheme}>
          <App/>
        </MuiThemeProvider>
      </ConnectedRouter>
    </Provider>,
    document.getElementById('app'),
  );
});
