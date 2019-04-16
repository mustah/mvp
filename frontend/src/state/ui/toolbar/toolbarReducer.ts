import {getType} from 'typesafe-actions';
import {Action, ModelSectors} from '../../../types/Types';
import {ReportSector} from '../../report/reportActions';
import {changeCollectionToolbarView, changeMeterMeasurementsToolbarView, changeToolbarView} from './toolbarActions';
import {ToolbarState, ToolbarView} from './toolbarModels';

export const initialState: ToolbarState = {
  measurement: {view: ToolbarView.graph},
  collection: {view: ToolbarView.graph},
  meterCollection: {view: ToolbarView.graph},
  meterMeasurement: {view: ToolbarView.table},
  selectionReport: {view: ToolbarView.graph},
};

type ActionTypes = Action<ToolbarView>;

export const toolbar = (state: ToolbarState = initialState, action: ActionTypes): ToolbarState => {
  switch (action.type) {
    case getType(changeToolbarView(ReportSector.report)):
      return {...state, measurement: {...state.measurement, view: action.payload}};
    case getType(changeToolbarView(ReportSector.selectionReport)):
      return {...state, selectionReport: {...state.measurement, view: action.payload}};
    case getType(changeCollectionToolbarView(ModelSectors.collection)):
      return {...state, collection: {...state.collection, view: action.payload}};
    case getType(changeCollectionToolbarView(ModelSectors.meterCollection)):
      return {...state, meterCollection: {...state.meterCollection, view: action.payload}};
    case getType(changeMeterMeasurementsToolbarView):
      return {...state, meterMeasurement: {...state.meterMeasurement, view: action.payload}};
    default:
      return state;
  }
};
