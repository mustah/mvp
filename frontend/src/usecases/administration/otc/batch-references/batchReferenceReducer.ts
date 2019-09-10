import {ActionType, getType} from 'typesafe-actions';
import {BatchRequestState} from '../../../../state/domain-models-paginated/batch-references/batchReferenceModels';
import {logoutUser} from '../../../auth/authActions';
import * as actions from './batchReferenceActions';

export const initialState: BatchRequestState = {
  batchId: '',
  deviceEuis: [],
  organisationId: '',
  requireApproval: false,
};

type ActionTypes = ActionType<typeof actions | typeof logoutUser>;

export const batchReferenceReducer = (state: BatchRequestState, action: ActionTypes): BatchRequestState => {
  switch (action.type) {
    case getType(actions.changeBatchReference):
      return {...state, batchId: action.payload};
    case getType(actions.changeRequireApproval):
      return {...state, requireApproval: action.payload};
    case getType(actions.selectDeviceEuis):
      return {...state, deviceEuis: action.payload};
    default:
      return state;
  }
};
