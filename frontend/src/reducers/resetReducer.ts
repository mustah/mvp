import {getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/type-helpers';
import {search} from '../state/search/searchActions';
import {
  addParameterToSelection,
  deselectSelection,
  resetSelection,
  selectSavedSelectionAction,
  setThreshold
} from '../state/user-selection/userSelectionActions';
import {logoutUser} from '../usecases/auth/authActions';
import {setCollectionStatsTimePeriod} from '../usecases/collection/collectionActions';

export const resetReducer = <S>(
  state: S,
  {type}: EmptyAction<string>,
  initialState: S,
): S => {
  switch (type) {
    case getType(addParameterToSelection):
    case getType(deselectSelection):
    case getType(logoutUser):
    case getType(resetSelection):
    case getType(search):
    case getType(selectSavedSelectionAction):
    case getType(setCollectionStatsTimePeriod):
    case getType(setThreshold):
      return initialState;
    default:
      return state;
  }
};
