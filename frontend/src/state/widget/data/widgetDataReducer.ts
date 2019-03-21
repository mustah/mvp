import {getType} from 'typesafe-actions';
import {EndPoints} from '../../../services/endPoints';
import {Identifiable} from '../../../types/Types';
import {ObjectsById, RequestsHttp} from '../../domain-models/domainModels';
import {domainModelsPutSuccess} from '../../domain-models/domainModelsActions';
import {widgetDataActions} from './widgetDataActions';

export interface WidgetData extends Identifiable {
  data: any;
}

export type WidgetDataState = ObjectsById<WidgetData & RequestsHttp>;

const initialState: WidgetDataState = {};

export const widgetData = (state: WidgetDataState = initialState, action): WidgetDataState => {
  switch (action.type) {
    // TODO remove data when widget is deleted
    case domainModelsPutSuccess(EndPoints.widgets):
      if (!state[action.payload.id]) {
        return state;
      }
      const newState = {...state};
      delete newState[action.payload.id];
      return newState;
    case getType(widgetDataActions.request):
      return {
        ...state,
        [action.payload]: {
          isFetching: true,
        },
      };
    case getType(widgetDataActions.success):
      return {
        ...state,
        [action.payload.id]: {
          isFetching: false,
          isSuccessfullyFetched: true,
          data: action.payload.data,
        },
      };
    case getType(widgetDataActions.failure):
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
