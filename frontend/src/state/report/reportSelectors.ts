import {flatMap, flatten, values} from 'lodash';
import {createSelector} from 'reselect';
import {getId, unique} from '../../helpers/collections';
import {identity} from '../../helpers/commonHelpers';
import {RootState} from '../../reducers/rootReducer';
import {uuid} from '../../types/Types';
import {VisibilitySummaryProps} from '../../usecases/report/components/VisibilitySummary';
import {groupLegendItemsByQuantity} from '../ui/graph/measurement/measurementActions';
import {MeasurementParameters, Medium, Quantity} from '../ui/graph/measurement/measurementModels';
import {
  isMedium,
  LegendItem,
  LegendType,
  LegendViewOptions,
  SavedReportsState,
  SelectedQuantities,
  ViewOptions
} from './reportModels';

export const getLegendViewOptions = ({meterPage: {legendViewOptions}}: SavedReportsState): LegendViewOptions =>
  legendViewOptions;

export const getViewOptions = (state: SavedReportsState, type: LegendType): ViewOptions =>
  getLegendViewOptions(state)[type];

export const getLegendItems = ({meterPage: {legendItems}}: SavedReportsState): LegendItem[] =>
  legendItems;

export const hasLegendItems = ({meterPage: {legendItems}}: SavedReportsState): boolean =>
  legendItems.length > 0;

export const makeLegendTypeQuantitiesMap = (): SelectedQuantities =>
  Object.keys(Medium).map(k => Medium[k])
    .reduce((acc, medium) => ({...acc, [medium]: []}), {aggregate: []});

export const getMeterLegendItems = createSelector<SavedReportsState, LegendItem[], LegendItem[]>(
  getLegendItems,
  items => items.filter(it => isMedium(it.type)),
);

export const getHiddenLines =
  createSelector<SavedReportsState, LegendItem[], uuid[]>(
    getLegendItems,
    items => items.filter(it => !!it.isHidden).map(it => it.id)
  );

export const getVisibilitySummary =
  createSelector<SavedReportsState, LegendItem[], VisibilitySummaryProps>(
    getLegendItems,
    items => ({
      allMeters: items.map(getId),
      checkedMeters: unique(flatten(values(groupLegendItemsByQuantity(items)))),
    })
  );

export const getSelectedQuantitiesMap =
  createSelector<SavedReportsState, LegendItem[], SelectedQuantities>(
    getLegendItems,
    items => {
      const columns: SelectedQuantities = makeLegendTypeQuantitiesMap();
      items.forEach(({type, quantities}) => columns[type] = unique([...columns[type], ...quantities]));
      return columns;
    }
  );

export const getSelectedQuantities =
  createSelector<SavedReportsState, SelectedQuantities, Quantity[]>(
    getSelectedQuantitiesMap,
    selectedQuantities => unique(flatMap(values(selectedQuantities)))
  );

export const hasSelectedQuantities =
  createSelector<SavedReportsState, Quantity[], boolean>(
    getSelectedQuantities,
    selectedQuantities => selectedQuantities.length > 0
  );

export const getMeasurementParameters =
  createSelector<RootState, RootState, MeasurementParameters>(
    identity,
    ({
      report: {savedReports, temporal: {resolution, timePeriod, shouldComparePeriod}},
    }) => ({
      reportDateRange: timePeriod,
      resolution,
      legendItems: getLegendItems(savedReports),
      shouldComparePeriod,
      shouldShowAverage: savedReports.meterPage.shouldShowAverage,
    })
  );

export const getSelectionMeasurementParameters =
  createSelector<RootState, RootState, MeasurementParameters>(
    identity,
    ({
      selectionReport: {savedReports, temporal: {resolution, timePeriod, shouldComparePeriod}},
    }) => ({
      reportDateRange: timePeriod,
      resolution,
      legendItems: getLegendItems(savedReports),
      shouldComparePeriod,
      shouldShowAverage: savedReports.meterPage.shouldShowAverage,
    })
  );
