import {createStandardAction} from 'typesafe-actions';
import {SelectionInterval} from '../../../state/user-selection/userSelectionModels';

export const setMeterDetailsTimePeriod = createStandardAction('SET_METER_DETAILS_TIME_PERIOD')<SelectionInterval>();
