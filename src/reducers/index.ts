import {combineReducers} from 'redux';
import {dashboard, DashboardProps} from './dashboard';

export interface RootState {
  dashboard: DashboardProps;
}

export const rootReducer = combineReducers<RootState>({
  dashboard,
});
