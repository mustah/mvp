import {replace} from 'lodash';
import {ActionType, getType} from 'typesafe-actions';
import {
  BatchReferencePayload,
  BatchRequestState
} from '../../../../state/domain-models-paginated/batch-references/batchReferenceModels';
import {logoutUser} from '../../../auth/authActions';
import * as actions from './batchReferenceActions';

export const initialState: BatchRequestState = {
  batchId: '',
  deviceEuis: [],
  deviceEuisText: '',
  organisationId: '',
  requireApproval: false,
};

type ActionTypes = ActionType<typeof actions | typeof logoutUser>;

const makeBatchId = ({shortPrefix, value}: BatchReferencePayload): string =>
  shortPrefix.map(prefix => value.length === 1
    ? `${prefix}_${value}` : value.length > 1 && value !== prefix
      ? `${prefix}${replace(value, prefix, '')}` : '')
    .orElse(value);

export const batchReferenceReducer = (state: BatchRequestState, action: ActionTypes): BatchRequestState => {
  switch (action.type) {
    case getType(actions.changeBatchReference):
      return {...state, batchId: makeBatchId(action.payload)};
    case getType(actions.changeRequireApproval):
      return {...state, requireApproval: action.payload};
    case getType(actions.changeDeviceEuis):
      return {...state, deviceEuisText: action.payload};
    default:
      return state;
  }
};
