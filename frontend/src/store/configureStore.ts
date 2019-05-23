import {routerMiddleware} from 'connected-react-router';
import {applyMiddleware, compose, createStore} from 'redux';
import {persistStore} from 'redux-persist';
import {Persistor} from 'redux-persist/lib/types';
import thunk from 'redux-thunk';
import {history} from '../app/routes';
import {changeLocale} from '../helpers/dateHelpers';
import {initLanguage} from '../i18n/i18n';
import {rootReducer, RootState} from '../reducers/rootReducer';
import {restClientWith} from '../services/restClient';

const composeEnhancers = (
                           process.env.NODE_ENV === 'development' &&
                           window && window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__
                         ) || compose;

export const store = createStore(
  rootReducer(history),
  composeEnhancers(applyMiddleware(...[thunk, routerMiddleware(history)])),
);

export const persistor: Persistor = persistStore(store, {}, () => {
  const {auth: {token, error}, language: {language: {code}}}: RootState = store.getState()!;
  if (!error) {
    restClientWith(token);
  }
  initLanguage(code);
  changeLocale(code);
});
