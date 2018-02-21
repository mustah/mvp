import {routerReducer as routing, RouterState} from 'react-router-redux';
import {AnyAction, combineReducers} from 'redux';
import {PaginatedDomainModelsState} from '../state/domain-models-paginated/paginatedDomainModels';
import {paginatedDomainModels} from '../state/domain-models-paginated/paginatedDomainModelsReducer';
import {DomainModelsState} from '../state/domain-models/domainModels';
import {domainModels} from '../state/domain-models/domainModelsReducer';
import {searchParameters, SearchParameterState} from '../state/search/searchParameterReducer';
import {ui, UiState} from '../state/ui/uiReducer';
import {LOGOUT_USER} from '../usecases/auth/authActions';
import {AuthState} from '../usecases/auth/authModels';
import {auth} from '../usecases/auth/authReducer';
import {dashboard, DashboardState} from '../usecases/dashboard/dashboardReducer';
import {LanguageState} from '../usecases/main-menu/languageModels';
import {language} from '../usecases/main-menu/languageReducer';
import {map, MapState} from '../usecases/map/mapReducer';
import {ReportState} from '../usecases/report/reportModels';
import {report} from '../usecases/report/reportReducer';

export interface RootState {
  auth: AuthState;
  domainModels: DomainModelsState;
  paginatedDomainModels: PaginatedDomainModelsState;
  dashboard: DashboardState;
  routing: RouterState;
  report: ReportState;
  language: LanguageState;
  searchParameters: SearchParameterState;
  ui: UiState;
  map: MapState;
}

export type AppState = RootState | undefined;

export type GetState = () => RootState;

const appReducer = combineReducers<AppState>({
  auth,
  domainModels,
  paginatedDomainModels,
  dashboard,
  routing,
  report,
  language,
  searchParameters,
  ui,
  map,
});

export const rootReducer = (state: AppState, action: AnyAction) => {
  if (action.type === LOGOUT_USER) {
    return appReducer(undefined, action);
  }
  return appReducer(state, action);
};
