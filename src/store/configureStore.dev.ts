import {applyMiddleware, compose, createStore, Store} from 'redux';
import thunk from 'redux-thunk';
import {rootReducer, RootState} from '../reducers';

const composeEnhancers = (
                           process.env.NODE_ENV === 'development' &&
                           window && window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__
                         ) || compose;

export const configureStore = (initialState?: RootState): Store<RootState> => {
  return createStore<RootState>(
    rootReducer,
    initialState!,
    composeEnhancers(applyMiddleware(...[thunk])),
  );
};
