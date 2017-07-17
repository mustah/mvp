import {createStore, Store} from 'redux';
import {rootReducer, RootState} from '../reducers';

export const appStore: Store<RootState> = createStore(rootReducer);
