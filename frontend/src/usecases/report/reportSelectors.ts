import {createSelector} from 'reselect';
import {identity} from '../../helpers/commonHelpers';
import {RootState} from '../../reducers/rootReducer';
import {
  allQuantities,
  MeasurementParameters,
  Medium,
  Quantity
} from '../../state/ui/graph/measurement/measurementModels';
import {ThresholdQuery} from '../../state/user-selection/userSelectionModels';
import {uuid} from '../../types/Types';
import {LegendItem, MediumViewOptions, Report, ReportState, ViewOptions} from './reportModels';

export const getMeterPage = (state: ReportState): Report => state.savedReports.meterPage;
export const getMediumViewOptions = (state: ReportState): MediumViewOptions => getMeterPage(state).mediumViewOptions;
export const getViewOptions = (state: ReportState, medium: Medium): ViewOptions => getMediumViewOptions(state)[medium];
export const getLegendItems = (state: ReportState): LegendItem[] => getMeterPage(state).meters;
export const hasLegendItems = (state: ReportState): boolean => getLegendItems(state).length > 0;

export const getHiddenLines =
  createSelector<ReportState, LegendItem[], uuid[]>(
    getLegendItems,
    (items) => items.filter(it => it.isHidden).map(({id}) => id)
  );

export const getMeasurementParameters =
  createSelector<RootState, RootState, MeasurementParameters>(
    identity,
    ({
      report,
      userSelection: {userSelection: {selectionParameters}},
    }) => ({
      resolution: report.resolution,
      selectedReportItems: {meters: getMeterPage(report).meters},
      selectionParameters: {
        ...selectionParameters,
        dateRange: report.timePeriod,
      },
    })
  );

export const getThresholdMedia = createSelector<ThresholdQuery | undefined, Quantity, Medium[]>(
  (threshold: ThresholdQuery) => threshold && threshold.quantity,
  (quantity) => {
    if (quantity) {
      return Array.from(new Set<Medium>(Object.keys(allQuantities)
        .map((medium) => (medium as Medium))
        .filter((medium) => Array.from(allQuantities[medium]).includes(quantity))
      ));
    } else {
      return [];
    }
  },
);
