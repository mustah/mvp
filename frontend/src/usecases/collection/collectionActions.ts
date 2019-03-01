import {createStandardAction} from 'typesafe-actions';
import {SelectionInterval} from '../../state/user-selection/userSelectionModels';

export const setCollectionTimePeriod = createStandardAction('SET_COLLECTION_TIME_PERIOD')<SelectionInterval>();
