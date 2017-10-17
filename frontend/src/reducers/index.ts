import {routerReducer as routing, RouterState} from 'react-router-redux';
import {combineReducers} from 'redux';
import {auth, AuthState} from '../usecases/auth/authReducer';
import {collection} from '../usecases/collection/collectionReducer';
import {CollectionState} from '../usecases/collection/models/Collections';
import {dashboard, DashboardState} from '../usecases/dashboard/dashboardReducer';
import {ReportState} from '../usecases/report/models/ReportModels';
import {report} from '../usecases/report/reportReducer';
import {search, SearchState} from '../usecases/search/searchReducer';
import {language, LanguageState} from '../usecases/main-menu/languageReducer';
import {ui, UiState} from '../usecases/ui/uiReducer';
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
  search: SearchState;
  ui: UiState;
}

export const rootReducer = combineReducers<RootState>({
  auth,
  dashboard,
  collection,
  routing,
  validation,
  report,
  language,
  search,
  ui,
});
