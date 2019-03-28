import {createStandardAction} from 'typesafe-actions';
import {ReportSector} from '../../report/reportActions';
import {ToolbarView} from './toolbarModels';

export const changeToolbarView = (sector: ReportSector) =>
  createStandardAction(`CHANGE_TOOLBAR_VIEW_${sector}`)<ToolbarView>();
export const changeCollectionToolbarView = createStandardAction('CHANGE_COLLECTION_TOOLBAR_VIEW')<string>();
export const changeMeterMeasurementsToolbarView =
  createStandardAction('CHANGE_METER_MEASUREMENTS_TOOLBAR_VIEW')<string>();
