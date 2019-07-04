import {flatMap, flatten, values} from 'lodash';
import {createSelector} from 'reselect';
import {getId, unique} from '../../helpers/collections';
import {RootState} from '../../reducers/rootReducer';
import {uuid} from '../../types/Types';
import {VisibilitySummaryProps} from '../../usecases/report/components/VisibilitySummary';
import {mapQuantityToIds} from '../ui/graph/measurement/measurementActions';
import {MeasurementParameters, Medium, Quantity} from '../ui/graph/measurement/measurementModels';
import {ToolbarView} from '../ui/toolbar/toolbarModels';
import {
  isMedium,
  LegendItem,
  LegendType,
  LegendViewOptions,
  ReportState,
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
  items => items.filter(isMedium),
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
      checkedMeters: unique(flatten(values(mapQuantityToIds(items)))),
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

const measurementParametersSelectorFrom = (key: 'report' | 'selectionReport') =>
  createSelector<RootState, ReportState, ToolbarView, MeasurementParameters>(
    state => state[key],
    state => key === 'report' ? state.ui.toolbar.measurement.view : state.ui.toolbar.selectionReport.view,
    (
      {savedReports, temporal: {resolution, shouldComparePeriod, timePeriod}}: ReportState,
      view: ToolbarView,
    ) => ({
      legendItems: getLegendItems(savedReports),
      reportDateRange: timePeriod,
      resolution,
      shouldComparePeriod,
      shouldShowAverage: savedReports.meterPage.shouldShowAverage,
      view
    })
  );

export const getMeasurementParameters = measurementParametersSelectorFrom('report');

export const getSelectionMeasurementParameters = measurementParametersSelectorFrom('selectionReport');
