import {Location} from 'history';
import {LOCATION_CHANGE} from 'react-router-redux';
import {createStandardAction} from 'typesafe-actions';

// LOCATION_CHANGE is not typed but it needs to be, so we wrap it
export const locationChange = createStandardAction(LOCATION_CHANGE)<Location>();
