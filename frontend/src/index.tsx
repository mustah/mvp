import {History} from 'history';
import createHashHistory from 'history/createHashHistory';
import * as React from 'react';
import * as ReactDOM from 'react-dom';
import {Provider} from 'react-redux';
import {HashRouter} from 'react-router-dom';
import {ConnectedRouter} from 'react-router-redux';
import {Store} from 'redux';
import {RootState} from './reducers/index';
import {configureStore} from './store/configureStore.dev';
import {App} from './usecases/app/App';

const history: History = createHashHistory();

const appStore: Store<RootState> = configureStore(history);

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
