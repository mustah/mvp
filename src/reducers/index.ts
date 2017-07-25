import {routerReducer as routing, RouterState} from 'react-router-redux';
import {combineReducers} from 'redux';
import {collection} from '../usecases/collection/collectionReducer';
import {CollectionState} from '../usecases/collection/models/Collections';
import {dashboard, DashboardState} from '../usecases/dashboard/dashboardReducer';

export interface RootState {
  dashboard: DashboardState;
  collection: CollectionState;
  routing: RouterState;
}

export const rootReducer = combineReducers<RootState>({
  dashboard,
  collection,
  routing,
});
