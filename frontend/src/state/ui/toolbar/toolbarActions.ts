import {createStandardAction} from 'typesafe-actions';
import {Sectors} from '../../../types/Types';
import {ReportSector} from '../../report/reportModels';
import {ToolbarView} from './toolbarModels';

export const changeToolbarView = (sector: ReportSector) =>
  createStandardAction(`CHANGE_TOOLBAR_VIEW_${sector}`)<ToolbarView>();

export const changeCollectionToolbarView = (sector: Sectors) =>
  createStandardAction(`CHANGE_COLLECTION_TOOLBAR_VIEW_${sector}`)<string>();

export const changeMeterMeasurementsToolbarView =
  createStandardAction('CHANGE_METER_MEASUREMENTS_TOOLBAR_VIEW')<string>();
