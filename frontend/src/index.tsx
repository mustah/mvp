import {History} from 'history';
import createHashHistory from 'history/createHashHistory';
import * as React from 'react';
import * as ReactDOM from 'react-dom';
import {Provider} from 'react-redux';
import {HashRouter} from 'react-router-dom';
import {ConnectedRouter} from 'react-router-redux';
import {Store} from 'redux';
import {persistStore} from 'redux-persist';
import {RootState} from './reducers/index';
import {initRestClient} from './services/restClient';
import {configureStore} from './store/configureStore';
import App from './usecases/app/App';

const history: History = createHashHistory();

const appStore: Store<RootState> = configureStore(history);

persistStore<RootState>(appStore, {whitelist: ['auth']}, (error?: any) => {
  if (!error) {
    const {token} = appStore.getState().auth;
    initRestClient(token);
  }
});

ReactDOM.render(
  <Provider store={appStore}>
    <ConnectedRouter history={history}>
      <HashRouter>
        <App/>
      </HashRouter>
    </ConnectedRouter>
  </Provider>,
  document.getElementById('app'),
);
