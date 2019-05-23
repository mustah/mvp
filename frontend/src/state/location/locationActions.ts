import {LOCATION_CHANGE, LocationChangePayload} from 'connected-react-router';
import {createStandardAction} from 'typesafe-actions';

// LOCATION_CHANGE is not typed but it needs to be, so we wrap it
export const locationChange = createStandardAction(LOCATION_CHANGE)<LocationChangePayload>();
