import {getType} from 'typesafe-actions';
import {Action} from '../../../types/Types';
import {CHANGE_TOOLBAR_VIEW, changeCollectionToolbarView} from './toolbarActions';
import {ToolbarState, ToolbarView} from './toolbarModels';

export const initialState: ToolbarState = {
  measurement: {view: ToolbarView.graph},
  collection: {view: ToolbarView.graph}
};

type ActionTypes = Action<ToolbarView>;

export const toolbar = (state: ToolbarState = initialState, action: ActionTypes): ToolbarState => {
  switch (action.type) {
    case CHANGE_TOOLBAR_VIEW:
      return {...state, measurement: {...state.measurement, view: action.payload}};
    case getType(changeCollectionToolbarView):
      return {...state, collection: {...state.collection, view: action.payload}};
    default:
      return state;
  }
};
