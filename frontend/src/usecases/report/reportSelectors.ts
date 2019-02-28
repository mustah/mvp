import {createSelector} from 'reselect';
import {unique} from '../../helpers/collections';
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
import {
  LegendItem,
  MediumViewOptions,
  Report,
  SavedReportsState,
  SelectedQuantityColumns,
  ViewOptions
} from './reportModels';

export const getMeterPage = (state: SavedReportsState): Report => state.meterPage;

export const getMediumViewOptions = (state: SavedReportsState): MediumViewOptions =>
  getMeterPage(state).mediumViewOptions;

export const getViewOptions = (state: SavedReportsState, medium: Medium): ViewOptions =>
  getMediumViewOptions(state)[medium];

export const getLegendItems = (state: SavedReportsState): LegendItem[] =>
  getMeterPage(state).meters;

export const hasLegendItems = (state: SavedReportsState): boolean =>
  getLegendItems(state).length > 0;

export const makeMediumQuantitiesMap = (): SelectedQuantityColumns =>
  Object.keys(Medium)
    .map(k => Medium[k])
    .reduce((acc, medium) => ({...acc, [medium]: []}), {});

export const getHiddenLines =
  createSelector<SavedReportsState, LegendItem[], uuid[]>(
    getLegendItems,
    (items) => items.filter(it => it.isHidden).map(({id}) => id)
  );

export const getSelectedQuantityColumns =
  createSelector<SavedReportsState, LegendItem[], SelectedQuantityColumns>(
    getLegendItems,
    items => {
      const columns: SelectedQuantityColumns = makeMediumQuantitiesMap();
      items.forEach(it => columns[it.medium] = unique([...columns[it.medium], ...it.quantities]));
      return columns;
    }
  );

export const getMeasurementParameters =
  createSelector<RootState, RootState, MeasurementParameters>(
    identity,
    ({
      report: {savedReports, temporal},
      userSelection: {userSelection: {selectionParameters}},
    }) => ({
      resolution: temporal.resolution,
      selectedReportItems: {meters: getMeterPage(savedReports).meters},
      selectionParameters: {
        ...selectionParameters,
        dateRange: temporal.timePeriod,
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
