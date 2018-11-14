import {startOfLatestInterval} from '../../helpers/dateHelpers';
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
    const readings: ExistingReadings = new Map<number, Reading>();

    const quantities: Quantity[] = orderedQuantities(medium);
    const quantitiesFoundInResponse: Set<Quantity> = new Set<Quantity>();

    if (measurementPage) {
      measurementPage.result.content.forEach((id: uuid) => {
        const measurement: Measurement = measurementPage.entities.measurements[id];

        const reading: Reading =
          readings.get(measurement.created) || {id: measurement.created, measurements: {}};

        reading.measurements[measurement.quantity] = measurement;
        quantitiesFoundInResponse.add(measurement.quantity);
        readings.set(measurement.created, reading);
      });
    }

    return {
      readings,
      quantities: quantities.filter((q) => quantitiesFoundInResponse.has(q)),
    };
  };

interface ReadingArguments {
  numberOfRows: number;
  receivedData: ExistingReadings;
  readIntervalMinutes?: number;
  lastDate: Date;
}

export const fillMissingMeasurements =
  ({numberOfRows, receivedData, readIntervalMinutes, lastDate}: ReadingArguments): Readings => {
    const readings: Readings = new Map(receivedData);

    if (!readIntervalMinutes) {
      return readings;
    }

    const end: UnixTimestamp = startOfLatestInterval(lastDate, readIntervalMinutes).valueOf() / 1000;

    const firstMeasurementInData: UnixTimestamp = Math.min(...Array.from(receivedData.keys()));

    for (let row = 0; row < numberOfRows; row++) {
      const currentTimestamp: UnixTimestamp = end - (row * readIntervalMinutes * 60);

      if (receivedData.size && currentTimestamp < firstMeasurementInData) {
        break;
      }

      if (!readings.get(currentTimestamp)) {
        readings.set(currentTimestamp, {id: currentTimestamp});
      }
    }

    return readings;
  };
