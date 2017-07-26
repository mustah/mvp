import {routerReducer as routing, RouterState} from 'react-router-redux';
import {combineReducers} from 'redux';
import {collection} from '../usecases/collection/collectionReducer';
import {CollectionState} from '../usecases/collection/models/Collections';
import {dashboard, DashboardState} from '../usecases/dashboard/dashboardReducer';
import {dataAnalysis} from '../usecases/dataAnalysis/dataAnalysisReducer';
import {DataAnalysisState} from '../usecases/dataAnalysis/models/DataAnalysis';
import {ValidationState} from '../usecases/validation/models/Validation';
import {validation} from '../usecases/validation/validationReducer';

export interface RootState {
  dashboard: DashboardState;
  collection: CollectionState;
  routing: RouterState;
  validation: ValidationState;
  dataAnalysis: DataAnalysisState;
}

export const rootReducer = combineReducers<RootState>({
  dashboard,
  collection,
  routing,
  validation,
  dataAnalysis,
});
