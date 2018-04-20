import {routerReducer as routing, RouterState} from 'react-router-redux';
import {AnyAction, combineReducers} from 'redux';
import {PaginatedDomainModelsState} from '../state/domain-models-paginated/paginatedDomainModels';
import {paginatedDomainModels} from '../state/domain-models-paginated/paginatedDomainModelsReducer';
import {DomainModelsState} from '../state/domain-models/domainModels';
import {domainModels} from '../state/domain-models/domainModelsReducer';
import {LanguageState} from '../state/language/languageModels';
import {language} from '../state/language/languageReducer';
import {UserSelectionState} from '../state/user-selection/userSelectionModels';
import {userSelection} from '../state/user-selection/userSelectionReducer';
import {SelectionTreeState} from '../state/selection-tree/selectionTreeModels';
import {selectionTree} from '../state/selection-tree/selectionTreeReducer';
import {SummaryState} from '../state/summary/summaryModels';
import {summary} from '../state/summary/summaryReducer';
import {ui, UiState} from '../state/ui/uiReducer';
import {LOGOUT_USER} from '../usecases/auth/authActions';
import {AuthState} from '../usecases/auth/authModels';
import {auth} from '../usecases/auth/authReducer';
import {dashboard, DashboardState} from '../usecases/dashboard/dashboardReducer';
import {map, MapState} from '../usecases/map/mapReducer';
import {ReportState} from '../usecases/report/reportModels';
import {report} from '../usecases/report/reportReducer';

export interface RootState {
  auth: AuthState;
  domainModels: DomainModelsState;
  paginatedDomainModels: PaginatedDomainModelsState;
  summary: SummaryState;
  selectionTree: SelectionTreeState;
  dashboard: DashboardState;
  routing: RouterState;
  report: ReportState;
  language: LanguageState;
  userSelection: UserSelectionState;
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
  userSelection,
  summary,
  selectionTree,
  ui,
  map,
});

export const rootReducer = (state: AppState, action: AnyAction) => {
  if (action.type === LOGOUT_USER) {
    return appReducer(undefined, action);
  }
  return appReducer(state, action);
};
