import {combineReducers} from 'redux';
import {dashboard, DashboardProps} from '../usecases/dashboard/dashboardReducer';

export interface RootState {
  dashboard: DashboardProps;
}

export const rootReducer = combineReducers<RootState>({
  dashboard,
});
