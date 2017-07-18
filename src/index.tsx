import * as React from 'react';
import * as ReactDOM from 'react-dom';
import {Provider} from 'react-redux';
import {HashRouter} from 'react-router-dom';
import {App} from './app/App';
import {configureStore} from './store/configureStore.dev';

const appStore = configureStore();

ReactDOM.render(
  <Provider store={appStore}>
    <HashRouter>
      <App/>
    </HashRouter>
  </Provider>,
  document.getElementById('app'),
);
