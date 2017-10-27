import {routerReducer as routing, RouterState} from 'react-router-redux';
import {combineReducers} from 'redux';
import {domainModels, DomainModelsState} from '../state/domain-models/domainModelsReducer';
import {searchParameters} from '../state/search/searchParameterReducer';
import {SearchParameterState} from '../state/search/selection/selectionModels';
import {selection, SelectionState} from '../state/search/selection/selectionReducer';
import {ui, UiState} from '../state/ui/uiReducer';
import {auth, AuthState} from '../usecases/auth/authReducer';
import {dashboard, DashboardState} from '../usecases/dashboard/dashboardReducer';
import {language, LanguageState} from '../usecases/main-menu/languageReducer';
import {map, MapState} from '../usecases/map/mapReducer';
import {ReportState} from '../usecases/report/models/ReportModels';
import {report} from '../usecases/report/reportReducer';
import {ValidationState} from '../usecases/validation/models/Validations';
import {validation} from '../usecases/validation/validationReducer';

export interface RootState {
  auth: AuthState;
  domainModels: DomainModelsState;
  dashboard: DashboardState;
  routing: RouterState;
  validation: ValidationState;
  report: ReportState;
  language: LanguageState;
  selection: SelectionState; // TODO: remove since it's in searchParameters?
  searchParameters: SearchParameterState;
  ui: UiState;
  map: MapState;
}

export const rootReducer = combineReducers<RootState>({
  auth,
  domainModels,
  dashboard,
  routing,
  validation,
  report,
  language,
  selection, // TODO: remove since it's in searchParameters?
  searchParameters,
  ui,
  map,
});
