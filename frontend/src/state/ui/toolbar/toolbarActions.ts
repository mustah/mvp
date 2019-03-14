import {createStandardAction} from 'typesafe-actions';
import {ToolbarView} from './toolbarModels';

export const changeToolbarView = createStandardAction('CHANGE_TOOLBAR_VIEW')<ToolbarView>();
export const changeCollectionToolbarView = createStandardAction('CHANGE_COLLECTION_TOOLBAR_VIEW')<string>();
export const changeMeterMeasurementsToolbarView =
  createStandardAction('CHANGE_METER_MEASUREMENTS_TOOLBAR_VIEW')<string>();
