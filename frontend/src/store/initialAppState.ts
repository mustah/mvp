import {RouterState} from 'react-router-redux';
import {RootState} from '../reducers/index';
import {loadAuthState} from '../services/authService';
import {AuthState} from '../usecases/auth/authReducer';
import {CollectionState} from '../usecases/collection/models/Collections';
import {DashboardState} from '../usecases/dashboard/dashboardReducer';
import {DataAnalysisState} from '../usecases/dataAnalysis/models/DataAnalysis';
import {ValidationState} from '../usecases/validation/models/Validations';

export const auth: AuthState = loadAuthState();

export const dashboard: DashboardState = {
  isFetching: false,
  records: [],
};

export const collection: CollectionState = {
  title: 'CollectionState',
  records: [],
  isFetching: false,
};

export const validation: ValidationState = {
  title: 'ValidationState',
  records: [],
  isFetching: false,
};

export const dataAnalysis: DataAnalysisState = {
  title: 'DataAnalysisState',
  records: [],
  isFetching: false,
};

const routing: RouterState = {location: null};

export const initialAppState: RootState = {
  auth,
  dashboard,
  collection,
  routing,
  validation,
  dataAnalysis,
};
