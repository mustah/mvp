import {routerReducer as routing, RouterState} from 'react-router-redux';
import {combineReducers} from 'redux';
import {searchParameters} from '../state/search/searchParameterReducer';
import {SearchParameterState} from '../state/search/selection/selectionModels';
import {selection, SelectionState} from '../state/search/selection/selectionReducer';
import {ui, UiState} from '../state/ui/uiReducer';
import {auth, AuthState} from '../usecases/auth/authReducer';
import {collection} from '../usecases/collection/collectionReducer';
import {CollectionState} from '../usecases/collection/models/Collections';
import {map, MapState} from '../usecases/dashboard/components/map/MapReducer';
import {dashboard, DashboardState} from '../usecases/dashboard/dashboardReducer';
import {language, LanguageState} from '../usecases/main-menu/languageReducer';
import {ReportState} from '../usecases/report/models/ReportModels';
import {report} from '../usecases/report/reportReducer';
import {ValidationState} from '../usecases/validation/models/Validations';
import {validation} from '../usecases/validation/validationReducer';

export interface RootState {
  auth: AuthState;
  dashboard: DashboardState;
  collection: CollectionState;
  routing: RouterState;
  validation: ValidationState;
  report: ReportState;
  language: LanguageState;
  selection: SelectionState;
  searchParameters: SearchParameterState;
  ui: UiState;
  map: MapState;
}

export const rootReducer = combineReducers<RootState>({
  auth,
  dashboard,
  collection,
  routing,
  validation,
  report,
  language,
  selection,
  searchParameters,
  ui,
  map,
});
