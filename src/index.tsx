import * as React from 'react';
import * as ReactDOM from 'react-dom';
import {Provider} from 'react-redux';
import {HashRouter} from 'react-router-dom';
import {configureStore} from './store/configureStore.dev';
import {App} from './usecases/app/App';

const appStore = configureStore();

ReactDOM.render(
  <Provider store={appStore}>
    <HashRouter>
      <App/>
    </HashRouter>
  </Provider>,
  document.getElementById('app'),
);
