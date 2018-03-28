import {History} from 'history';
import {routerMiddleware} from 'react-router-redux';
import {applyMiddleware, compose, createStore, Store} from 'redux';
import {autoRehydrate} from 'redux-persist';
import thunk from 'redux-thunk';
import {rootReducer, AppState} from '../reducers/rootReducer';

const composeEnhancers = (
                           process.env.NODE_ENV === 'development' &&
                           window && window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__
                         ) || compose;

export const storeFactory = (history: History, initialState?: AppState): Store<AppState> => {
  return createStore<AppState>(
    rootReducer,
    initialState!,
    composeEnhancers(applyMiddleware(...[thunk, routerMiddleware(history)]), autoRehydrate()),
  );
};
