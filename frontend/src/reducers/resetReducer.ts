import {getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/type-helpers';
import {
  addParameterToSelection,
  deselectSelection,
  resetSelection,
  selectSavedSelectionAction,
  setThreshold
} from '../state/user-selection/userSelectionActions';
import {Sectors} from '../types/Types';
import {logoutUser} from '../usecases/auth/authActions';
import {setCollectionTimePeriod} from '../usecases/collection/collectionActions';

export const resetReducer = <S>(
  state: S,
  {type}: EmptyAction<string>,
  initialState: S,
): S => {
  switch (type) {
    case getType(setThreshold):
    case getType(setCollectionTimePeriod(Sectors.collection)):
    case getType(setCollectionTimePeriod(Sectors.meterCollection)):
    case getType(addParameterToSelection):
    case getType(deselectSelection):
    case getType(selectSavedSelectionAction):
    case getType(logoutUser):
    case getType(resetSelection):
      return initialState;
    default:
      return state;
  }
};
