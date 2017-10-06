import {routerReducer as routing, RouterState} from 'react-router-redux';
import {combineReducers} from 'redux';
import {auth, AuthState} from '../usecases/auth/authReducer';
import {collection} from '../usecases/collection/collectionReducer';
import {CollectionState} from '../usecases/collection/models/Collections';
import {dashboard, DashboardState} from '../usecases/dashboard/dashboardReducer';
import {dataAnalysis} from '../usecases/data-analysis/dataAnalysisReducer';
import {DataAnalysisState} from '../usecases/data-analysis/models/DataAnalysis';
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
  dataAnalysis: DataAnalysisState;
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
  dataAnalysis,
  language,
  ui,
  tabs,
});
