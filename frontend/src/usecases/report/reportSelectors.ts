import {uniqBy} from 'lodash';
import {createSelector} from 'reselect';
import {identity, isDefined} from '../../helpers/commonHelpers';
import {Maybe} from '../../helpers/Maybe';
import {RootState} from '../../reducers/rootReducer';
import {MeasurementParameters} from '../../state/ui/graph/measurement/measurementActions';
import {
  allQuantities,
  defaultQuantityForMedium,
  Medium,
  Quantity
} from '../../state/ui/graph/measurement/measurementModels';
import {LegendItem, Report, ReportState, SelectedReportPayload} from './reportModels';

const orderedMedia: Medium[] = Object.keys(allQuantities) as Medium[];

const selectedReportPayloadCombiner = (legendItems: LegendItem[]): SelectedReportPayload => {
  const itemsWithKnownMedium = legendItems.filter(({medium}) => medium !== Medium.unknown);
  const items: LegendItem[] = uniqBy(itemsWithKnownMedium, 'id');

  const currentlyActiveMedia: Set<Medium> = new Set(
    items.map(({medium}: LegendItem) => medium).filter(isDefined)
  );

  const activeMedia: Medium[] = orderedMedia.filter((medium) => currentlyActiveMedia.has(medium));

  orderedMedia
    .filter((medium) => currentlyActiveMedia.has(medium))
    .forEach((activeMedium) => {
      if (activeMedia.length < 2) {
        activeMedia.push(activeMedium);
      }
    });

  const media: Medium[] = orderedMedia.filter((medium) => activeMedia.includes(medium));

  const quantities: Quantity[] = Array.from(new Set(media.map(defaultQuantityForMedium)));

  return {items, media, quantities};
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
      ui: {indicator: {selectedQuantities: quantities}},
    }) => ({
      items: getLegendItems(report),
      quantities,
      resolution: report.resolution,
      selectionParameters,
    })
  );
