import {routerReducer as routing, RouterState} from 'react-router-redux';
import {combineReducers} from 'redux';
import {domainModels, DomainModelsState} from '../state/domain-models/domainModelsReducer';
import {searchParameters} from '../state/search/searchParameterReducer';
import {SearchParameterState} from '../state/search/selection/selectionModels';
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
  searchParameters,
  ui,
  map,
});
