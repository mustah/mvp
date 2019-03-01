import {createSelector} from 'reselect';
import {unique} from '../../helpers/collections';
import {identity} from '../../helpers/commonHelpers';
import {RootState} from '../../reducers/rootReducer';
import {MeasurementParameters, Medium} from '../../state/ui/graph/measurement/measurementModels';
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

export const makeLegendTypeQuantitiesMap = (): SelectedQuantityColumns =>
  Object.keys(Medium).map(k => Medium[k])
    .reduce((acc, medium) => ({...acc, [medium]: []}), {aggregate: []});

export const getHiddenLines =
  createSelector<SavedReportsState, LegendItem[], uuid[]>(
    getLegendItems,
    items => items.filter(it => it.isHidden).map(it => it.id)
  );

export const getSelectedQuantityColumns =
  createSelector<SavedReportsState, LegendItem[], SelectedQuantityColumns>(
    getLegendItems,
    items => {
      const columns: SelectedQuantityColumns = makeLegendTypeQuantitiesMap();
      items.forEach(({type, quantities}) => columns[type] = unique([...columns[type], ...quantities]));
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
      legendItems: getLegendItems(savedReports),
      selectionParameters: {
        ...selectionParameters,
        dateRange: temporal.timePeriod,
      },
    })
  );
