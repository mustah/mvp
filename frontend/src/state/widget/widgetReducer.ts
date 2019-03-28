import {getType} from 'typesafe-actions';
import {EndPoints} from '../../services/endPoints';
import {Identifiable} from '../../types/Types';
import {ObjectsById, RequestsHttp} from '../domain-models/domainModels';
import {domainModelsPutSuccess} from '../domain-models/domainModelsActions';
import {widgetActions} from './widgetActions';

export interface WidgetData extends Identifiable {
  data: any; // TODO please type this!!!
}

export type WidgetState = ObjectsById<WidgetData & RequestsHttp>;

const initialState: WidgetState = {};

export const widget = (state: WidgetState = initialState, action): WidgetState => {
  switch (action.type) {
    // TODO remove data when widget is deleted
    case domainModelsPutSuccess(EndPoints.widgets):
      if (!state[action.payload.id]) {
        return state;
      }
      const newState = {...state};
      delete newState[action.payload.id];
      return newState;
    case getType(widgetActions.request):
      return {
        ...state,
        [action.payload]: {
          isFetching: true,
        },
      };
    case getType(widgetActions.success):
      return {
        ...state,
        [action.payload.id]: {
          isFetching: false,
          isSuccessfullyFetched: true,
          data: action.payload.data,
        },
      };
    case getType(widgetActions.failure):
      // TODO show error somehow
      return {
        ...state,
        [action.payload.id]: {
          isFetching: false,
          isSuccessfullyFetched: false,
          error: action.payload,
        },
      };
    default:
      return state;
  }
};
