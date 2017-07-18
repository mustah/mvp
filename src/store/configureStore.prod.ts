import {applyMiddleware, createStore, Store} from 'redux';
import thunk from 'redux-thunk';
import {rootReducer, RootState} from '../reducers';

export const configureStore = (initialState?: RootState): Store<RootState> => {
  return createStore<RootState>(
    rootReducer,
    initialState!,
    applyMiddleware(...[thunk]),
  );
};
