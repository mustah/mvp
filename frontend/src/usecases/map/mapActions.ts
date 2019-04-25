import {createStandardAction} from 'typesafe-actions';
import {MapZoomSettingsPayload} from './mapModels';

export const onCenterMap = createStandardAction('CENTER_MAP')<MapZoomSettingsPayload>();
