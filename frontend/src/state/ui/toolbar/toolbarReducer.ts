import {getType} from 'typesafe-actions';
import {Action} from '../../../types/Types';
import {
  changeCollectionToolbarView,
  changeMeterMeasurementsToolbarView,
  changeToolbarView
} from './toolbarActions';
import {ToolbarState, ToolbarView} from './toolbarModels';

export const initialState: ToolbarState = {
  measurement: {view: ToolbarView.graph},
  collection: {view: ToolbarView.graph},
  meterMeasurement: {view: ToolbarView.table},
};

type ActionTypes = Action<ToolbarView>;

export const toolbar = (state: ToolbarState = initialState, action: ActionTypes): ToolbarState => {
  switch (action.type) {
    case getType(changeToolbarView):
      return {...state, measurement: {...state.measurement, view: action.payload}};
    case getType(changeCollectionToolbarView):
      return {...state, collection: {...state.collection, view: action.payload}};
    case getType(changeMeterMeasurementsToolbarView):
      return {...state, meterMeasurement: {...state.meterMeasurement, view: action.payload}};
    default:
      return state;
  }
};
