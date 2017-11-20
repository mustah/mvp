import {routerReducer as routing, RouterState} from 'react-router-redux';
import {combineReducers} from 'redux';
import {domainModels} from '../state/domain-models/domainModelsReducer';
import {searchParameters, SearchParameterState} from '../state/search/searchParameterReducer';
import {ui, UiState} from '../state/ui/uiReducer';
import {auth, AuthState} from '../usecases/auth/authReducer';
import {dashboard, DashboardState} from '../usecases/dashboard/dashboardReducer';
import {language, LanguageState} from '../usecases/main-menu/languageReducer';
import {map, MapState} from '../usecases/map/mapReducer';
import {ReportState} from '../usecases/report/models/reportModels';
import {report} from '../usecases/report/reportReducer';
import {DomainModelsState} from '../state/domain-models/domainModels';

export interface RootState {
  auth: AuthState;
  domainModels: DomainModelsState;
  dashboard: DashboardState;
  routing: RouterState;
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
  report,
  language,
  searchParameters,
  ui,
  map,
});
