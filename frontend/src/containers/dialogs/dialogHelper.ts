import {DateRange} from '../../components/dates/dateModels';
import {NormalizedPaginated} from '../../state/domain-models-paginated/paginatedDomainModels';
import {
  allQuantities,
  ExistingReadings,
  Measurement,
  Medium,
  Quantity,
  Reading,
  Readings
} from '../../state/ui/graph/measurement/measurementModels';
import {UnixTimestamp, uuid} from '../../types/Types';

const orderedQuantities = (medium: Medium): Quantity[] =>
  medium in allQuantities
    ? allQuantities[medium]
    : [];

export interface MeasurementTableData {
  readings: ExistingReadings;
  quantities: Quantity[];
}

export const groupMeasurementsByDate =
  (measurementPage: NormalizedPaginated<Measurement>, medium: Medium): MeasurementTableData => {
    const readings: ExistingReadings = {};

    const quantities: Quantity[] = orderedQuantities(medium);
    const quantitiesFoundInResponse: Set<Quantity> = new Set<Quantity>();

    if (measurementPage) {
      measurementPage.result.content.forEach((id: uuid) => {
        const measurement: Measurement = measurementPage.entities.measurements[id];

        const reading: Reading = readings[measurement.created] || {id: measurement.created, measurements: {}};

        reading.measurements[measurement.quantity] = measurement;
        readings[measurement.created] = reading;
        quantitiesFoundInResponse.add(measurement.quantity);
      });
    }

    return {
      readings,
      quantities: quantities.filter((q) => quantitiesFoundInResponse.has(q)),
    };
  };

interface ReadingArguments {
  existingReadings: ExistingReadings;
  dateRange: DateRange;
  readIntervalMinutes?: number;
}

export const fillMissingMeasurements =
  ({existingReadings, readIntervalMinutes, dateRange: {start, end}}: ReadingArguments): Readings => {
    if (!readIntervalMinutes) {
      return existingReadings;
    }

    const startInSeconds = start.valueOf() / 1000;
    const numRows = (end.valueOf() - start.valueOf()) / (1000 * readIntervalMinutes * 60);
    const withMissingReadings: Readings = {...existingReadings};

    for (let row = 0; row < numRows; row++) {
      const timestamp: UnixTimestamp = startInSeconds + (row * readIntervalMinutes * 60);
      if (!withMissingReadings[timestamp]) {
        withMissingReadings[timestamp] = {id: timestamp};
      }
    }
    return withMissingReadings;
  };
