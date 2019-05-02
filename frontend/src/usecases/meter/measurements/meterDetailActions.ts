import {createStandardAction} from 'typesafe-actions';
import {TemporalResolution} from '../../../components/dates/dateModels';
import {SelectionInterval} from '../../../state/user-selection/userSelectionModels';

export const setTimePeriod = createStandardAction('SET_TIME_PERIOD_METER_DETAILS')<SelectionInterval>();

export const selectResolution = createStandardAction(`SELECT_RESOLUTION_METER_DETAILS`)<TemporalResolution>();
