import {routerReducer as routing, RouterState} from 'react-router-redux';
import {Reducer} from 'redux';
import {createMigrate, MigrationDispatch, persistCombineReducers} from 'redux-persist';
import storage from 'redux-persist/lib/storage';
import {PersistConfig, PersistedState} from 'redux-persist/lib/types';
import {PaginatedDomainModelsState} from '../state/domain-models-paginated/paginatedDomainModels';
import {paginatedDomainModels} from '../state/domain-models-paginated/paginatedDomainModelsReducer';
import {DomainModelsState} from '../state/domain-models/domainModels';
import {domainModels} from '../state/domain-models/domainModelsReducer';
import {LanguageState} from '../state/language/languageModels';
import {language} from '../state/language/languageReducer';
import {previousSession, PreviousSessionState} from '../state/previous-session/previousSessionReducer';
import {SelectionTreeState} from '../state/selection-tree/selectionTreeModels';
import {selectionTree} from '../state/selection-tree/selectionTreeReducer';
import {SummaryState} from '../state/summary/summaryModels';
import {summary} from '../state/summary/summaryReducer';
import {MeasurementState} from '../state/ui/graph/measurement/measurementModels';
import {measurement} from '../state/ui/graph/measurement/measurementReducer';
import {ui, UiState} from '../state/ui/uiReducer';
import {UserSelectionState} from '../state/user-selection/userSelectionModels';
import {userSelection} from '../state/user-selection/userSelectionReducer';
import {AuthState} from '../usecases/auth/authModels';
import {auth} from '../usecases/auth/authReducer';
import {CollectionState} from '../usecases/collection/collectionModels';
import {collection} from '../usecases/collection/collectionReducer';
import {dashboard, DashboardState} from '../usecases/dashboard/dashboardReducer';
import {map, MapState} from '../usecases/map/mapReducer';
import {ReportState} from '../usecases/report/reportModels';
import {report} from '../usecases/report/reportReducer';
import {search, SearchState} from '../usecases/search/searchReducer';
import {currentVersion, migrations} from './stateMigrations';

export interface RootState {
  auth: AuthState;
  domainModels: DomainModelsState;
  paginatedDomainModels: PaginatedDomainModelsState;
  summary: SummaryState;
  selectionTree: SelectionTreeState;
  dashboard: DashboardState;
  routing: RouterState;
  report: ReportState;
  measurement: MeasurementState;
  language: LanguageState;
  userSelection: UserSelectionState;
  ui: UiState;
  map: MapState;
  search: SearchState;
  previousSession: PreviousSessionState;
  collection: CollectionState;
}

export type AppState = RootState | undefined;

export type GetState = () => RootState;

const whitelist: Array<keyof RootState> = ['auth', 'language', 'ui', 'userSelection', 'report', 'previousSession'];

const migrate: MigrationDispatch = createMigrate(migrations, {debug: false});

const persistConfig: PersistConfig = {
  key: 'primary',
  storage,
  whitelist,
  migrate,
  version: currentVersion,
};

export const rootReducer: Reducer<undefined | ((AppState | undefined) & PersistedState)> =
  persistCombineReducers<AppState>(persistConfig, {
    auth,
    domainModels,
    paginatedDomainModels,
    dashboard,
    routing,
    report,
    measurement,
    language,
    userSelection,
    summary,
    selectionTree,
    ui,
    map,
    search,
    previousSession,
    collection,
  });
