import {ActionType, getType} from 'typesafe-actions';
import {routes} from '../../app/routes';
import {locationChange} from '../../state/location/locationActions';
import {failureTheme, requestTheme, successTheme} from './themeActions';
import {ThemeState} from './themeModels';

export const initialState: ThemeState = {
  isFetching: false,
  isSuccessfullyFetched: false,
  color: {
    primary: '#0091ea',
    secondary: '#b6e2cc',
  }
};

type ActionTypes = ActionType<typeof locationChange
  | typeof requestTheme
  | typeof successTheme
  | typeof failureTheme>;

export const theme = (state: ThemeState = initialState, action: ActionTypes): ThemeState => {
  switch (action.type) {
    case getType(requestTheme):
      return {
        ...state,
        isFetching: true,
        isSuccessfullyFetched: false,
      };
    case getType(successTheme):
      return {
        ...state,
        color: {...state.color, ...action.payload},
        isFetching: false,
        isSuccessfullyFetched: true
      };
    case getType(failureTheme):
      return {
        ...state,
        error: action.payload,
        isFetching: false,
        isSuccessfullyFetched: false,
      };
    case getType(locationChange):
      return action.payload.pathname.startsWith(routes.adminOrganisationsModify)
        ? {...state, isFetching: false, isSuccessfullyFetched: false}
        : state;
    default:
      return state;
  }
};
