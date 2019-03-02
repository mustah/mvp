import {getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/types';
import {Period, TemporalResolution} from '../../components/dates/dateModels';
import {SelectionInterval} from '../../state/user-selection/userSelectionModels';
import {Action} from '../../types/Types';
import {logoutUser} from '../auth/authActions';
import {setCollectionTimePeriod} from './collectionActions';
import {CollectionState} from './collectionModels';

const initialState: CollectionState = {
  timePeriod: {period: Period.latest},
};

type ActionTypes = Action<TemporalResolution | SelectionInterval> | EmptyAction<string>;

export const collection = (state: CollectionState = initialState, action: ActionTypes): CollectionState => {
  switch (action.type) {
    case getType(setCollectionTimePeriod):
      return {...state, timePeriod: {...(action as Action<SelectionInterval>).payload}};
    case getType(logoutUser):
      return initialState;
    default:
      return state;
  }
};
