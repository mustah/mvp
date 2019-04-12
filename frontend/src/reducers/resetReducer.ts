import {getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/type-helpers';
import {
  addParameterToSelection,
  deselectSelection,
  RESET_SELECTION,
  selectSavedSelectionAction,
  setThreshold
} from '../state/user-selection/userSelectionActions';
import {ModelSectors} from '../types/Types';
import {logoutUser} from '../usecases/auth/authActions';
import {setCollectionTimePeriod} from '../usecases/collection/collectionActions';

export const resetReducer = <S>(
  state: S,
  {type}: EmptyAction<string>,
  initialState: S,
): S => {
  switch (type) {
    case getType(setThreshold):
    case getType(setCollectionTimePeriod(ModelSectors.collection)):
    case getType(addParameterToSelection):
    case getType(deselectSelection):
    case getType(selectSavedSelectionAction):
    case getType(logoutUser):
    case RESET_SELECTION:
      return initialState;
    default:
      return state;
  }
};
