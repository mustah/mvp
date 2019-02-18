import {uniqBy} from 'lodash';
import {createSelector} from 'reselect';
import {identity, isDefined} from '../../helpers/commonHelpers';
import {Maybe} from '../../helpers/Maybe';
import {RootState} from '../../reducers/rootReducer';
import {MeasurementParameters} from '../../state/ui/graph/measurement/measurementActions';
import {allQuantities, Medium, Quantity} from '../../state/ui/graph/measurement/measurementModels';
import {ThresholdQuery} from '../../state/user-selection/userSelectionModels';
import {LegendItem, Report, ReportState, SelectedReportPayload} from './reportModels';

const orderedMedia: Medium[] = Object.keys(allQuantities) as Medium[];

const selectedReportPayloadCombiner = (legendItems: LegendItem[]): SelectedReportPayload => {
  const itemsWithKnownMedium = legendItems.filter(({medium}) => medium !== Medium.unknown);
  const items: LegendItem[] = uniqBy(itemsWithKnownMedium, 'id');

  const legendMedia: Set<Medium> = new Set(items.map(({medium}: LegendItem) => medium).filter(isDefined));

  const activeMedia: Medium[] = orderedMedia.filter((medium) => legendMedia.has(medium));

  orderedMedia
    .filter((medium) => legendMedia.has(medium))
    .forEach((medium) => {
      if (activeMedia.length < 2) {
        activeMedia.push(medium);
      }
    });

  const media: Medium[] = orderedMedia.filter((medium) => activeMedia.includes(medium));

  const quantities: Set<Quantity> = new Set();

  media.forEach((m) => allQuantities[m].forEach((q) => quantities.add(q)));

  return {items, media, quantities: Array.from(quantities)};
};

export const getSelectedReportPayload =
  createSelector<LegendItem[], LegendItem[], SelectedReportPayload>(
    identity,
    selectedReportPayloadCombiner
  );

export const getLegendItems =
  createSelector<ReportState, Maybe<Report>, LegendItem[]>(
    ({savedReports}: ReportState) => Maybe.maybe(savedReports.meterPage),
    (meterPage: Maybe<Report>) => meterPage.map(({meters}: Report) => meters).orElse([]),
  );

export const getMeasurementParameters =
  createSelector<RootState, RootState, MeasurementParameters>(
    identity,
    ({
      report,
      userSelection: {userSelection: {selectionParameters}},
    }) => ({
      items: getLegendItems(report),
      resolution: report.resolution,
      selectionParameters,
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
