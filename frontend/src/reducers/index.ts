import {routerReducer as routing, RouterState} from 'react-router-redux';
import {combineReducers} from 'redux';
import {auth, AuthState} from '../usecases/auth/authReducer';
import {collection} from '../usecases/collection/collectionReducer';
import {CollectionState} from '../usecases/collection/models/Collections';
import {dashboard, DashboardState} from '../usecases/dashboard/dashboardReducer';
import {ReportState} from '../usecases/report/models/ReportModels';
import {report} from '../usecases/report/reportReducer';
import {TabsState} from '../usecases/tabs/models/TabsModel';
import {tabs} from '../usecases/tabs/tabsReducer';
import {language, LanguageState} from '../usecases/topmenu/languageReducer';
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
  ui: UiState;
  tabs: TabsState;
}

export const rootReducer = combineReducers<RootState>({
  auth,
  dashboard,
  collection,
  routing,
  validation,
  report,
  language,
  ui,
  tabs,
});
