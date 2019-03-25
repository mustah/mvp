import {SelectionInterval} from '../../state/user-selection/userSelectionModels';

export interface CollectionState {
  isExportingToExcel: boolean;
  isTimePeriodDefault: boolean;
  timePeriod: SelectionInterval;
}
