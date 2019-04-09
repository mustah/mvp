import {getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/type-helpers';
import {
  addParameterToSelection,
  deselectSelection,
  RESET_SELECTION,
  selectSavedSelectionAction,
  setThresholdAction
} from '../state/user-selection/userSelectionActions';
import {logoutUser} from '../usecases/auth/authActions';
import {setCollectionTimePeriod} from '../usecases/collection/collectionActions';

export const resetReducer = <S>(
  state: S,
  {type}: EmptyAction<string>,
  initialState: S,
): S => {
  switch (type) {
    case getType(setThresholdAction):
    case getType(setCollectionTimePeriod):
    case getType(addParameterToSelection):
    case getType(deselectSelection):
    case getType(selectSavedSelectionAction):
    case RESET_SELECTION:
    case getType(logoutUser):
      return initialState;
    default:
      return state;
  }
};
